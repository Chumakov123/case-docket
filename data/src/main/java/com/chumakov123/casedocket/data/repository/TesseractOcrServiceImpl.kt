package com.chumakov123.casedocket.data.repository

import com.chumakov123.casedocket.domain.model.imaging.ImageRegion
import com.chumakov123.casedocket.domain.repository.OcrService
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.content.Context
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

class TesseractOcrServiceImpl(
    private val context: Context
) : OcrService {

    private var tessApi: TessBaseAPI? = null
    private var isInitialized = false

    init {
        initializeTesseract()
    }

    private fun initializeTesseract() {
        try {
            // 1. Подготовка директорий
            val tessDir = File(context.filesDir, "tesseract")
            val tessdataDir = File(tessDir, "tessdata")

            if (!tessdataDir.exists()) {
                tessdataDir.mkdirs()
            }

            // 2. Копирование обученной модели из assets
            val modelFile = File(tessdataDir, "rus.traineddata")
            if (!modelFile.exists()) {
                copyModelFromAssets(modelFile)
            }

            // 3. Инициализация Tesseract
            tessApi = TessBaseAPI().apply {
                val initSuccess = init(tessDir.absolutePath, "rus")

                if (initSuccess) {
                    isInitialized = true
                    // Настройки для улучшения распознавания
                    setVariable(
                        TessBaseAPI.VAR_CHAR_WHITELIST,
                        "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя0123456789.,!?@#$%^&*()_-+={}[]|\\:;\"'<>,./? ")
                    pageSegMode = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK
                } else {
                    cleanup()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            cleanup()
        }
    }

    private fun copyModelFromAssets(targetFile: File) {
        try {
            context.assets.open("tessdata/rus.traineddata").use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun recognizeTextInRegion(
        imageBytes: ByteArray,
        region: ImageRegion?
    ): String = withContext(Dispatchers.Default) {

        if (!isInitialized || tessApi == null) {
            return@withContext ""
        }

        try {
            // 1. Декодируем ByteArray → Bitmap
            val originalBitmap = BitmapFactory.decodeByteArray(
                imageBytes,
                0,
                imageBytes.size
            ) ?: return@withContext ""

            var result = ""

            if (region == null) {
                // Распознаем весь bitmap целиком
                tessApi!!.clear()
                tessApi!!.setImage(originalBitmap)
                result = tessApi!!.utF8Text ?: ""
                originalBitmap.recycle()
            } else {
                // 2. Защита от выхода за границы изображения
                val safeRect = Rect(
                    max(0, region.x),
                    max(0, region.y),
                    min(originalBitmap.width, region.x + region.width),
                    min(originalBitmap.height, region.y + region.height)
                )

                if (safeRect.width() <= 0 || safeRect.height() <= 0) {
                    originalBitmap.recycle()
                    return@withContext ""
                }

                // 3. Кроп региона
                val croppedBitmap = Bitmap.createBitmap(
                    originalBitmap,
                    safeRect.left,
                    safeRect.top,
                    safeRect.width(),
                    safeRect.height()
                )

                originalBitmap.recycle()

                // 4. Tesseract ОЧИСТКА (ВАЖНО)
                tessApi!!.clear()

                // 5. Передаем изображение
                tessApi!!.setImage(croppedBitmap)

                // (необязательно, но сильно повышает качество)
                tessApi!!.setRectangle(0, 0, croppedBitmap.width, croppedBitmap.height)

                // 6. Распознавание
                result = tessApi!!.utF8Text ?: ""

                croppedBitmap.recycle()
            }



            // 7. Постобработка
            result
                .replace("\n", " ")
                .replace(Regex("\\s+"), " ")
                .trim()

            val confidence = tessApi!!.meanConfidence()
            //println("${confidence}% $result")

            if (confidence < 30) {
                //println("TEXT REJECTED (low confidence)")
                return@withContext ""
            } else {
                result
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun cleanup() {
        tessApi?.recycle()
        tessApi = null
        isInitialized = false
    }
}