package com.chumakov123.casedocket.data.repository

import com.chumakov123.casedocket.domain.model.imaging.DocumentLayout
import com.chumakov123.casedocket.domain.model.imaging.ImageRegion
import com.chumakov123.casedocket.domain.repository.ImageLayoutAnalyzer
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import kotlin.math.max
import kotlin.math.min

class OpenCvLayoutAnalyzerImpl : ImageLayoutAnalyzer {

    override suspend fun analyze(imageBytes: ByteArray): DocumentLayout {
        // decode
        val buf = MatOfByte(*imageBytes)
        val original = Imgcodecs.imdecode(buf, Imgcodecs.IMREAD_COLOR)
        buf.release()

        if (original.empty()) {
            throw IllegalArgumentException("Cannot decode image bytes")
        }

        try {
            // подготовим grayscale (нужно для морфологии и threshold)
            val gray = Mat()
            if (original.channels() == 3) {
                Imgproc.cvtColor(original, gray, Imgproc.COLOR_BGR2GRAY)
            } else {
                original.copyTo(gray)
            }

            // Бинаризация (локальная) — параметры можно подправить под предобработку
            val binary = Mat()
            Imgproc.adaptiveThreshold(
                gray, binary, 255.0,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, 19, 5.0
            )

            // Для детекции линий удобно иметь инверт (линии белые)
            val inverted = Mat()
            Core.bitwise_not(binary, inverted)

            // Морфология для маски таблицы (close, чтобы соединить линии)
            val kernelClose = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(9.0, 9.0))
            val tableMask = Mat()
            Imgproc.morphologyEx(inverted, tableMask, Imgproc.MORPH_CLOSE, kernelClose)

            // Найдём контуры и выберем самый большой как таблицу
            val contours = ArrayList<MatOfPoint>()
            val hierarchy = Mat()
            Imgproc.findContours(tableMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
            hierarchy.release()

            val hImg = original.rows()
            val wImg = original.cols()

            var tableRect = Rect(0, 0, wImg, hImg) // дефолт — вся картинка

            if (contours.isNotEmpty()) {
                val largest = contours.maxByOrNull { Imgproc.contourArea(it) }!!
                val area = Imgproc.contourArea(largest)
                if (area > 0.01 * (wImg.toDouble() * hImg.toDouble())) {
                    val br = Imgproc.boundingRect(largest)
                    // немного запаса
                    val margin = 8
                    val x = max(0, br.x - margin)
                    val y = max(0, br.y - margin)
                    val x2 = min(wImg, br.x + br.width + margin)
                    val y2 = min(hImg, br.y + br.height + margin)
                    tableRect = Rect(x, y, x2 - x, y2 - y)
                }
            }

            // headerRegion: от верхнего края изображения до верхней границы таблицы (exclusive)
            val headerBottom = tableRect.y.coerceAtLeast(0)
            val headerRegion = if (headerBottom >= 4) {
                ImageRegion(0, 0, wImg, headerBottom)
            } else {
                // пустой регион (высота 0) если заголовка нет
                ImageRegion(0, 0, wImg, 0)
            }

            // Вырежем область таблицы для детекции линий внутри неё
            val tableMat = Mat(original, tableRect).clone()

            // Переконвертим в grayscale + бинарный для детекции линий
            val tableGray = Mat()
            if (tableMat.channels() == 3) {
                Imgproc.cvtColor(tableMat, tableGray, Imgproc.COLOR_BGR2GRAY)
            } else {
                tableMat.copyTo(tableGray)
            }

            // Адаптативная бинаризация (локально)
            val tableBinary = Mat()
            Imgproc.adaptiveThreshold(
                tableGray, tableBinary, 255.0,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, 21, 8.0
            )

            // invert для того, чтобы линии стали белыми на чёрном фоне
            val tableInv = Mat()
            Core.bitwise_not(tableBinary, tableInv)

            // детекция вертикалей и горизонталей методом морфологического открытия
            fun detectPositions(isVertical: Boolean, kernelSize: Size, minLineRatio: Double = 0.5): MutableList<Int> {
                val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize)
                val opened = Mat()
                Imgproc.morphologyEx(tableInv, opened, Imgproc.MORPH_OPEN, kernel,
                    Point(-1.0, -1.0), 1)

                val contoursLocal = ArrayList<MatOfPoint>()
                Imgproc.findContours(opened, contoursLocal, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

                val positions = mutableListOf<Int>()
                for (c in contoursLocal) {
                    val r = Imgproc.boundingRect(c)
                    if (isVertical) {
                        // высокая компонента по высоте относительно высоты таблицы
                        if (r.height >= minLineRatio * tableMat.rows()) {
                            positions.add(r.x + r.width / 2)
                        }
                    } else {
                        // широкая компонента по ширине
                        if (r.width >= minLineRatio * tableMat.cols()) {
                            positions.add(r.y + r.height / 2)
                        }
                    }
                }
                opened.release()
                return positions
            }

            val vKernelWidth = 1
            val vKernelHeight = (max(10, (0.02 * tableMat.rows()).toInt())).coerceAtLeast(15)
            val hKernelWidth = (max(10, (0.02 * tableMat.cols()).toInt())).coerceAtLeast(15)
            val hKernelHeight = 1

            val rawV = detectPositions(true, Size(vKernelWidth.toDouble(), vKernelHeight.toDouble()))
            val rawH = detectPositions(false, Size(hKernelWidth.toDouble(), hKernelHeight.toDouble()))

            // helper: merge close positions
            fun mergePositions(positions: List<Int>, tol: Int): List<Int> {
                if (positions.isEmpty()) return listOf()
                val sorted = positions.sorted()
                val merged = ArrayList<Int>()
                var cur = sorted[0]
                for (i in 1 until sorted.size) {
                    val p = sorted[i]
                    if (p - cur <= tol) {
                        // среднее между cur и p
                        cur = (cur + p) / 2
                    } else {
                        merged.add(cur)
                        cur = p
                    }
                }
                merged.add(cur)
                return merged
            }

            val tolMerge = max(4, (0.01 * tableMat.cols()).toInt())
            var verts = mergePositions(rawV, tolMerge).toMutableList()
            var horz = mergePositions(rawH, tolMerge).toMutableList()

            // если не нашли линий — добавим края (в координатах tableMat)
            val tw = tableMat.cols()
            val th = tableMat.rows()

            if (verts.isEmpty()) {
                verts.add(0); verts.add(tw - 1)
            }
            if (horz.isEmpty()) {
                horz.add(0); horz.add(th - 1)
            }

            // snap to edges: если край близок — установим в 0 или tw-1
            val edgeSnapTol = max(8, (0.01 * tw).toInt())
            if (verts.first() <= edgeSnapTol) verts[0] = 0 else verts.add(0, 0)
            if ((tw - 1 - verts.last()) <= edgeSnapTol) verts[verts.lastIndex] = tw - 1 else verts.add(tw - 1)

            val edgeSnapTolH = max(8, (0.01 * th).toInt())
            if (horz.first() <= edgeSnapTolH) horz[0] = 0 else horz.add(0, 0)
            if ((th - 1 - horz.last()) <= edgeSnapTolH) horz[horz.lastIndex] = th - 1 else horz.add(th - 1)

            // ещё раз объединяем и сортируем
            verts = mergePositions(verts, tolMerge).toMutableList()
            horz = mergePositions(horz, tolMerge).toMutableList()
            verts.sort()
            horz.sort()

            // фильтр позиций по минимальному размеру ячейки
            fun filterPositions(posList: List<Int>, minSize: Int): List<Int> {
                if (posList.size < 2) return posList
                val filtered = ArrayList<Int>()
                filtered.add(posList[0])
                for (i in 0 until posList.size - 1) {
                    val size = posList[i + 1] - filtered.last()
                    if (size >= minSize) {
                        filtered.add(posList[i + 1])
                    } else {
                        // пропускаем слишком близкую позицию
                        continue
                    }
                }
                if (filtered.last() != posList.last()) filtered.add(posList.last())
                return filtered.distinct().sorted()
            }

            val minCellSize = max(8, (0.01 * tw).toInt())
            verts = filterPositions(verts, minCellSize).toMutableList()
            horz = filterPositions(horz, max(8, (0.01 * th).toInt())).toMutableList()

            // Ensure edges present
            if (verts.first() != 0) verts.add(0, 0)
            if (verts.last() != tw - 1) verts.add(tw - 1)
            if (horz.first() != 0) horz.add(0, 0)
            if (horz.last() != th - 1) horz.add(th - 1)

            verts.sort()
            horz.sort()

            // Построим матрицу ImageRegion — в координатах оригинального processed image
            val tableCells = ArrayList<List<ImageRegion>>()
            for (r in 0 until horz.size - 1) {
                val row = ArrayList<ImageRegion>()
                val y1 = horz[r]
                val y2 = horz[r + 1]
                val height = y2 - y1
                if (height < 1) continue
                for (c in 0 until verts.size - 1) {
                    val x1 = verts[c]
                    val x2 = verts[c + 1]
                    val width = x2 - x1
                    if (width < 1) continue
                    // coords relative to original image
                    val absX = tableRect.x + x1
                    val absY = tableRect.y + y1
                    row.add(ImageRegion(absX, absY, width, height))
                }
                if (row.isNotEmpty()) tableCells.add(row)
            }

            // Если tableCells пустой — вернём хотя бы одну ячейку-таблицу (вся таблица)
            if (tableCells.isEmpty()) {
                val single = listOf(
                    ImageRegion(
                        tableRect.x,
                        tableRect.y,
                        tableRect.width,
                        tableRect.height
                    )
                )
                tableCells.add(single)
            }

            // освобождение mats
            gray.release()
            binary.release()
            inverted.release()
            tableMask.release()
            tableMat.release()
            tableGray.release()
            tableBinary.release()
            tableInv.release()
            kernelClose.release()

            return DocumentLayout(headerRegion = headerRegion, tableCells = tableCells)
        } finally {
            original.release()
        }
    }
}