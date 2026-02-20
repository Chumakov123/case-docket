package com.chumakov123.casedocket.data.repository

import com.chumakov123.casedocket.domain.model.imaging.DocumentLayout
import com.chumakov123.casedocket.domain.model.imaging.ImageRegion
import com.chumakov123.casedocket.domain.repository.ImageLayoutAnalyzer
import com.chumakov123.casedocket.domain.repository.ImageSaver
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class OpenCvLayoutAnalyzerImpl(
    private val imageSaver: ImageSaver?
) : ImageLayoutAnalyzer {

    override suspend fun analyze(imageBytes: ByteArray): DocumentLayout {

        val src = Imgcodecs.imdecode(MatOfByte(*imageBytes), Imgcodecs.IMREAD_GRAYSCALE)
            ?: throw IllegalArgumentException("Cannot decode image")

        val h = src.rows()
        val w = src.cols()

        /* ================================
           Поиск области таблицы
           ================================ */

        val mask = Mat()
        Core.bitwise_not(src, mask)

        val kernelClose = Imgproc.getStructuringElement(
            Imgproc.MORPH_RECT,
            Size(9.0, 9.0)
        )

        val tableMask = Mat()
        Imgproc.morphologyEx(mask, tableMask, Imgproc.MORPH_CLOSE, kernelClose)

        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(
            tableMask,
            contours,
            Mat(),
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        val tableBin: Mat = if (contours.isEmpty()) {
            src.clone()
        } else {
            val largest = contours.maxByOrNull { Imgproc.contourArea(it) }!!
            val area = Imgproc.contourArea(largest)

            if (area < 0.01 * (w * h)) {
                src.clone()
            } else {
                val peri = Imgproc.arcLength(MatOfPoint2f(*largest.toArray()), true)
                val approx = MatOfPoint2f()
                Imgproc.approxPolyDP(
                    MatOfPoint2f(*largest.toArray()),
                    approx,
                    0.02 * peri,
                    true
                )

                if (approx.total() == 4L) {
                    fourPointTransform(src, approx.toArray())
                } else {
                    cropByBoundingRect(src, largest, 8)
                }
            }
        }

        /* ================================
           Делим header / table
           ================================ */

        val tableTop = findTableTop(src, tableBin)

        val headerMat = if (tableTop > 0)
            src.submat(0, tableTop, 0, w)
        else
            Mat.zeros(1, 1, CvType.CV_8U)

        /* ================================
           Построение сетки
           ================================ */

        val gridResult = buildGrid(tableBin)

        val debugGridImage = gridResult.debugImage
        val tableWithoutGrid = tableBin.clone()
        val headerBytes = matToBytes(headerMat)

        imageSaver?.save(matToBytes(debugGridImage), "table_grid")
        imageSaver?.save(matToBytes(tableWithoutGrid), "table")
        imageSaver?.save(headerBytes, "header")


        return DocumentLayout(
            headerImage = matToBytes(headerMat),
            tableImage = matToBytes(tableWithoutGrid),
            tableCells = gridResult.cells
        )
    }

    /* ========================================================= */

    private fun buildGrid(tableBin: Mat): GridResult {

        val h = tableBin.rows()
        val w = tableBin.cols()

        val verts = detectVerticalLines(tableBin)
        val horz = detectHorizontalLines(tableBin)

        val finalVerts = finalizePositions(verts, w)
        val finalHorz = finalizePositions(horz, h)

        val cells = mutableListOf<List<ImageRegion>>()

        for (r in 0 until finalHorz.size - 1) {
            val row = mutableListOf<ImageRegion>()
            for (c in 0 until finalVerts.size - 1) {

                val x0 = finalVerts[c]
                val x1 = finalVerts[c + 1]
                val y0 = finalHorz[r]
                val y1 = finalHorz[r + 1]

                row.add(
                    ImageRegion(
                        x = x0,
                        y = y0,
                        width = x1 - x0,
                        height = y1 - y0
                    )
                )
            }
            cells.add(row)
        }

        val debug = tableBin.clone()
        Imgproc.cvtColor(debug, debug, Imgproc.COLOR_GRAY2BGR)

        finalVerts.forEach {
            Imgproc.line(debug, Point(it.toDouble(), 0.0), Point(it.toDouble(), h.toDouble()),
                Scalar(0.0, 255.0, 0.0), 1)
        }

        finalHorz.forEach {
            Imgproc.line(debug, Point(0.0, it.toDouble()), Point(w.toDouble(), it.toDouble()),
                Scalar(0.0, 255.0, 0.0), 1)
        }

        return GridResult(debug, cells)
    }

    /* ========================================================= */

    private fun detectVerticalLines(image: Mat): List<Int> {
        val inverted = Mat()
        Core.bitwise_not(image, inverted)

        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(1.0, 15.0))
        val opened = Mat()
        Imgproc.morphologyEx(inverted, opened, Imgproc.MORPH_OPEN, kernel)

        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(opened, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        val h = image.rows()
        return contours
            .map { Imgproc.boundingRect(it) }
            .filter { it.height >= 0.5 * h }
            .map { it.x + it.width / 2 }
            .let { mergePositions(it, max(4, (image.cols() * 0.01).toInt())) }
    }

    private fun detectHorizontalLines(image: Mat): List<Int> {
        val inverted = Mat()
        Core.bitwise_not(image, inverted)

        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(15.0, 1.0))
        val opened = Mat()
        Imgproc.morphologyEx(inverted, opened, Imgproc.MORPH_OPEN, kernel)

        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(opened, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        val w = image.cols()
        return contours
            .map { Imgproc.boundingRect(it) }
            .filter { it.width >= 0.5 * w }
            .map { it.y + it.height / 2 }
            .let { mergePositions(it, max(4, (image.rows() * 0.01).toInt())) }
    }

    private fun mergePositions(positions: List<Int>, tol: Int): List<Int> {
        if (positions.isEmpty()) return emptyList()

        val sorted = positions.sorted()
        val merged = mutableListOf<Int>()

        var group = mutableListOf(sorted.first())

        for (i in 1 until sorted.size) {
            if (abs(sorted[i] - group.last()) <= tol) {
                group.add(sorted[i])
            } else {
                merged.add(group.average().roundToInt())
                group = mutableListOf(sorted[i])
            }
        }

        merged.add(group.average().roundToInt())
        return merged
    }

    private fun finalizePositions(input: List<Int>, size: Int): List<Int> {
        val result = input.toMutableList()

        if (result.isEmpty()) {
            return listOf(0, size - 1)
        }

        if (result.first() != 0) result.add(0)
        if (result.last() != size - 1) result.add(size - 1)

        return result.sorted()
    }

    private fun cropByBoundingRect(src: Mat, contour: MatOfPoint, margin: Int): Mat {
        val rect = Imgproc.boundingRect(contour)

        val x0 = max(rect.x - margin, 0)
        val y0 = max(rect.y - margin, 0)
        val x1 = min(rect.x + rect.width + margin, src.cols())
        val y1 = min(rect.y + rect.height + margin, src.rows())

        return src.submat(y0, y1, x0, x1)
    }

    private fun fourPointTransform(src: Mat, pts: Array<Point>): Mat {
        val sorted = sortPoints(pts)

        val widthA = distance(sorted[2], sorted[3])
        val widthB = distance(sorted[1], sorted[0])
        val maxWidth = max(widthA, widthB).roundToInt()

        val heightA = distance(sorted[1], sorted[2])
        val heightB = distance(sorted[0], sorted[3])
        val maxHeight = max(heightA, heightB).roundToInt()

        val dst = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(maxWidth - 1.0, 0.0),
            Point(maxWidth - 1.0, maxHeight - 1.0),
            Point(0.0, maxHeight - 1.0)
        )

        val transform = Imgproc.getPerspectiveTransform(
            MatOfPoint2f(*sorted),
            dst
        )

        val warped = Mat()
        Imgproc.warpPerspective(src, warped, transform, Size(maxWidth.toDouble(), maxHeight.toDouble()))

        return warped
    }

    private fun sortPoints(pts: Array<Point>): Array<Point> {
        val sumSorted = pts.sortedBy { it.x + it.y }
        val diffSorted = pts.sortedBy { it.y - it.x }

        return arrayOf(
            sumSorted.first(),
            diffSorted.first(),
            sumSorted.last(),
            diffSorted.last()
        )
    }

    private fun distance(a: Point, b: Point): Double =
        Math.hypot(a.x - b.x, a.y - b.y)

    private fun findTableTop(original: Mat, table: Mat): Int {
        return max(0, original.rows() - table.rows())
    }

    private fun matToBytes(mat: Mat): ByteArray {
        val buf = MatOfByte()
        Imgcodecs.imencode(".png", mat, buf)
        return buf.toArray()
    }

    private data class GridResult(
        val debugImage: Mat,
        val cells: List<List<ImageRegion>>
    )
}