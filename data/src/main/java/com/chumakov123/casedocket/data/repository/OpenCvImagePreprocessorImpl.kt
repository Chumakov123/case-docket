package com.chumakov123.casedocket.data.repository

import com.chumakov123.casedocket.domain.repository.ImagePreprocessor
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

class OpenCvImagePreprocessorImpl() : ImagePreprocessor {
    override suspend fun preprocess(imageBytes: ByteArray): ByteArray {
        // Загружаем изображение из byte array
        val originalMat = Imgcodecs.imdecode(MatOfByte(*imageBytes), Imgcodecs.IMREAD_COLOR)

        // 1. Конвертируем в градации серого
        val gray = Mat()
        Imgproc.cvtColor(originalMat, gray, Imgproc.COLOR_BGR2GRAY)

        // 2. Применяем CLAHE для улучшения контраста
        val clahe = Imgproc.createCLAHE(2.0, Size(8.0, 8.0))
        val claheMat = Mat()
        clahe.apply(gray, claheMat)

        // 3. Bilateral Filter для сохранения границ при удалении шума
        val bilateral = Mat()
        Imgproc.bilateralFilter(claheMat, bilateral, 9, 50.0, 50.0)

        // 4. Первая адаптивная бинаризация (для поиска контуров таблицы)
        val binaryForContours = Mat()
        Imgproc.adaptiveThreshold(
            bilateral, binaryForContours, 255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY_INV, 21, 8.0
        )

        // 5. Инвертируем обратно
        val finalResult = Mat()
        Core.bitwise_not(binaryForContours, finalResult)

        // Конвертируем обратно в byte array
        val outputMat = MatOfByte()
        Imgcodecs.imencode(".png", finalResult, outputMat)

        // Освобождаем ресурсы
        originalMat.release()
        gray.release()
        claheMat.release()
        bilateral.release()
        binaryForContours.release()

        return outputMat.toArray()
    }
}