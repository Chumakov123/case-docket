package com.chumakov123.casedocket.domain.repository

interface ImageSaver {
    /**
     * Сохраняет байты изображения.
     * @param imageBytes данные в формате PNG (или ином)
     * @param nameHint подсказка для имени файла (может использоваться или игнорироваться)
     * @return путь к сохранённому файлу или null в случае ошибки
     */
    suspend fun save(imageBytes: ByteArray, nameHint: String? = null): String?
}