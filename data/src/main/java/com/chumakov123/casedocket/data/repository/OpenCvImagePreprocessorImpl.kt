package com.chumakov123.casedocket.data.repository

import com.chumakov123.casedocket.domain.repository.ImagePreprocessor
import com.chumakov123.casedocket.domain.repository.ImageSaver
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class OpenCvImagePreprocessorImpl(
    private val imageSaver: ImageSaver?
) : ImagePreprocessor {
    private val openCVInitialized by lazy {
        if (!OpenCVLoader.initLocal()) {
            throw IllegalStateException("OpenCV initialization failed")
        }
        true
    }

    override suspend fun preprocess(imageBytes: ByteArray): ByteArray {
        openCVInitialized
        val originalMat = Imgcodecs.imdecode(MatOfByte(*imageBytes), Imgcodecs.IMREAD_COLOR)
        val croppedMat = cropDocument(originalMat)
        val processedMat = preprocessForOcr(croppedMat)

        val outputMat = MatOfByte()
        Imgcodecs.imencode(".png", processedMat, outputMat)
        val resultBytes = outputMat.toArray()

        imageSaver?.save(resultBytes)

        originalMat.release()
        croppedMat.release()
        processedMat.release()
        return resultBytes
    }

    private fun cropDocument(src: Mat): Mat {
        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)

        val corrected = correctIllumination(gray, kernelSize = 51)

        val blurred = Mat()
        Imgproc.GaussianBlur(corrected, blurred, Size(5.0, 5.0), 0.0)
        val edges = Mat()
        Imgproc.Canny(blurred, edges, 50.0, 150.0)

        val contour = getDocumentContour(edges)

        if (contour == null) {
            return src.clone()
        }

        val rect = Imgproc.minAreaRect(MatOfPoint2f(*contour.toArray()))
        val boxPoints = arrayOfNulls<Point>(4)
        rect.points(boxPoints)
        val box = boxPoints.filterNotNull().map { it }

        val xs = box.map { it.x }
        val ys = box.map { it.y }
        val margin = 10.0
        val xMin = max(0.0, xs.min() - margin)
        val xMax = min(src.cols().toDouble(), xs.max() + margin)
        val yMin = max(0.0, ys.min() - margin)
        val yMax = min(src.rows().toDouble(), ys.max() + margin)

        val rectRegion = Rect(Point(xMin, yMin), Point(xMax, yMax))
        val cropped = Mat(src, rectRegion)

        gray.release()
        corrected.release()
        blurred.release()
        edges.release()
        return cropped
    }

    private fun correctIllumination(gray: Mat, kernelSize: Int): Mat {
        val kernel = Imgproc.getStructuringElement(
            Imgproc.MORPH_ELLIPSE,
            Size(kernelSize.toDouble(), kernelSize.toDouble())
        )
        val background = Mat()
        Imgproc.morphologyEx(gray, background, Imgproc.MORPH_OPEN, kernel)

        val corrected = Mat()
        Core.absdiff(gray, background, corrected)
        Core.normalize(corrected, corrected, 0.0, 255.0, Core.NORM_MINMAX)

        background.release()
        return corrected
    }

    private fun getDocumentContour(edges: Mat): MatOfPoint? {
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(5.0, 5.0))
        val closed = Mat()
        Imgproc.morphologyEx(edges, closed, Imgproc.MORPH_CLOSE, kernel, Point(-1.0, -1.0), 2)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(
            closed,
            contours,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        if (contours.isEmpty()) return null

        val imgArea = closed.rows() * closed.cols()
        val candidates = mutableListOf<Pair<Double, MatOfPoint>>()

        for (cnt in contours) {
            val area = Imgproc.contourArea(cnt)
            if (area < 0.1 * imgArea) continue

            val peri = Imgproc.arcLength(MatOfPoint2f(*cnt.toArray()), true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(MatOfPoint2f(*cnt.toArray()), approx, 0.02 * peri, true)

            if (approx.total() == 4L) {
                candidates.add(area to cnt)
            } else {
                val rect = Imgproc.minAreaRect(MatOfPoint2f(*cnt.toArray()))
                val pts = arrayOfNulls<Point>(4)
                rect.points(pts)
                val box = MatOfPoint(*pts.filterNotNull().toTypedArray())
                val boxArea = Imgproc.contourArea(box)
                if (area > 0 && abs(area - boxArea) / area < 0.3) {
                    candidates.add(area to cnt)
                }
            }
        }

        return when {
            candidates.isNotEmpty() -> candidates.maxByOrNull { it.first }?.second
            else -> contours.maxByOrNull { Imgproc.contourArea(it) }
        }
    }

    private fun preprocessForOcr(src: Mat): Mat {
        val gray = if (src.channels() == 3) {
            val g = Mat()
            Imgproc.cvtColor(src, g, Imgproc.COLOR_BGR2GRAY)
            g
        } else {
            src.clone()
        }

        val clahe = Imgproc.createCLAHE(2.0, Size(8.0, 8.0))
        val claheMat = Mat()
        clahe.apply(gray, claheMat)

        val bilateral = Mat()
        Imgproc.bilateralFilter(claheMat, bilateral, 9, 50.0, 50.0)

        val binaryInv = Mat()
        Imgproc.adaptiveThreshold(
            bilateral, binaryInv, 255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY_INV, 21, 8.0
        )

        val result = Mat()
        Core.bitwise_not(binaryInv, result)

        if (src.channels() == 3) gray.release()
        claheMat.release()
        bilateral.release()
        binaryInv.release()

        return result
    }
}