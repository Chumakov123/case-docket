package com.chumakov123.casedocket.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft
import com.chumakov123.casedocket.domain.usecase.RecognizeScheduleUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class OcrState {
    object Idle : OcrState()
    object Loading : OcrState()
    data class Success(val schedule: CourtScheduleDraft) : OcrState()
    data class Error(val message: String) : OcrState()
}

class OcrViewModel(
    private val recognizeScheduleUseCase: RecognizeScheduleUseCase
) : ViewModel() {

    // Состояние распознавания
    private val _ocrState = MutableStateFlow<OcrState>(OcrState.Idle)
    val ocrState: StateFlow<OcrState> = _ocrState

    // Время обработки
    private val _processingTime = MutableStateFlow(0L)
    val processingTime: StateFlow<Long> = _processingTime

    /**
     * Распознавание расписания из assets
     */
    fun recognizeScheduleFromAssets(context: Context, filename: String = "test_schedule.jpg") {
        viewModelScope.launch {
            _ocrState.value = OcrState.Loading
            val startTime = System.currentTimeMillis()

            try {
                val imageBytes = withContext(Dispatchers.IO) {
                    loadImageBytesFromAssets(context, filename)
                }

                if (imageBytes != null) {
                    recognizeSchedule(imageBytes)
                } else {
                    _ocrState.value = OcrState.Error("Файл $filename не найден в assets")
                }

            } catch (e: Exception) {
                _ocrState.value = OcrState.Error("Ошибка загрузки: ${e.message}")
            } finally {
                _processingTime.value = System.currentTimeMillis() - startTime
            }
        }
    }

    /**А
     * Основная функция распознавания через UseCase
     */
    private suspend fun recognizeSchedule(imageBytes: ByteArray) {
        try {
            val schedule = withContext(Dispatchers.IO) {
                recognizeScheduleUseCase.execute(imageBytes)
            }
            _ocrState.value = OcrState.Success(schedule)
        } catch (e: Exception) {
            _ocrState.value = OcrState.Error("Ошибка распознавания: ${e.message}")
        }
    }

    /**
     * Сброс состояния
     */
    fun resetState() {
        _ocrState.value = OcrState.Idle
        _processingTime.value = 0
    }

    private suspend fun loadImageBytesFromAssets(context: Context, filename: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                context.assets.open(filename).use { stream ->
                    stream.readBytes()
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}