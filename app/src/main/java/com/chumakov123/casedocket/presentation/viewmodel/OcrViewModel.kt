package com.chumakov123.casedocket.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.model.court.CourtCase
import com.chumakov123.casedocket.domain.model.court.CourtCaseDescription
import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.model.court.Judge
import com.chumakov123.casedocket.domain.model.court.ScheduleDate
import com.chumakov123.casedocket.domain.model.court.draft.CourtCaseDraft
import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft
import com.chumakov123.casedocket.domain.model.court.toCaseTimeOrNull
import com.chumakov123.casedocket.domain.model.validation.DraftValidation
import com.chumakov123.casedocket.domain.repository.ImageSaver
import com.chumakov123.casedocket.domain.service.ScheduleRecognitionManager
import com.chumakov123.casedocket.domain.usecase.RecognizeScheduleUseCase
import com.chumakov123.casedocket.domain.validator.ScheduleValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class OcrState {
    object Idle : OcrState()
    object Loading : OcrState()
    object Success : OcrState()
    data class Error(val message: String) : OcrState()
}

class OcrViewModel(
    private val recognizeScheduleUseCase: RecognizeScheduleUseCase,
    private val scheduleValidator: ScheduleValidator,
    private val manager: ScheduleRecognitionManager,
    private val imageSaver: ImageSaver
) : ViewModel() {

    // Черновик расписания
    private val _currentDraft = MutableStateFlow<CourtScheduleDraft?>(null)
    val currentDraft: StateFlow<CourtScheduleDraft?> = _currentDraft

    // Результат валидации
    private val _validation = MutableStateFlow(
        DraftValidation(
            isValid = false,
            dateError = false,
            judgeError = false,
            casesError = false,
            casesValidations = emptyList()
        )
    )
    val validation: StateFlow<DraftValidation> = _validation

    // Состояние процесса распознавания
    private val _ocrState = MutableStateFlow<OcrState>(OcrState.Idle)
    val ocrState: StateFlow<OcrState> = _ocrState

    // Время обработки
    private val _processingTime = MutableStateFlow(0L)
    val processingTime: StateFlow<Long> = _processingTime

    private val _tasks = MutableStateFlow<List<RecognitionTask>>(emptyList())
    val tasks: StateFlow<List<RecognitionTask>> = _tasks

    init {
        viewModelScope.launch {
            manager.observeTasks().collect { tasks ->
                _tasks.value = tasks
            }
        }
        // При каждом изменении черновика пересчитываем валидацию
        viewModelScope.launch {
            _currentDraft.collect { draft ->
                if (draft != null) {
                    _validation.value = scheduleValidator.validate(draft)
                }
            }
        }
    }

    fun submitTestImage(context: Context, filename: String) {
        viewModelScope.launch {
            val bytes = loadImageBytesFromAssets(context, filename) ?: return@launch
            val path = imageSaver.save(bytes, filename)
            if (path != null) {
                val uri = "file://$path"
                manager.submitImage(uri)
            } else {
                _ocrState.value = OcrState.Error("Не удалось сохранить изображение")
            }
        }
    }

    // Распознавание изображения (адаптировано)
    fun recognizeScheduleFromAssets(context: Context, filename: String) {
        viewModelScope.launch {
            _ocrState.value = OcrState.Loading
            val startTime = System.currentTimeMillis()
            try {
                val imageBytes = loadImageBytesFromAssets(context, filename)
                if (imageBytes != null) {
                    recognizeSchedule(imageBytes)
                } else {
                    _ocrState.value = OcrState.Error("Файл не найден")
                }
            } finally {
                _processingTime.value = System.currentTimeMillis() - startTime
            }
        }
    }

    private suspend fun recognizeSchedule(imageBytes: ByteArray) {
        try {
            val schedule = withContext(Dispatchers.IO) {
                recognizeScheduleUseCase.execute(imageBytes)
            }
            _currentDraft.value = schedule
            _ocrState.value = OcrState.Success
        } catch (e: Exception) {
            _ocrState.value = OcrState.Error("Ошибка распознавания: ${e.message}")
        }
    }

    // Методы редактирования
    fun updateJudge(text: String) {
        _currentDraft.update { draft ->
            draft?.copy(judge = Judge(text))
        }
    }

    fun updateDate(date: ScheduleDate) {
        _currentDraft.update { draft ->
            draft?.copy(date = date)
        }
    }

    fun updateCaseNumber(index: Int, number: String) {
        _currentDraft.update { draft ->
            draft?.let {
                val updatedCases = it.cases.toMutableList().apply {
                    this[index] = this[index].copy(caseNumber = number.ifBlank { null })
                }
                it.copy(cases = updatedCases)
            }
        }
    }

    fun updateCaseTime(index: Int, timeString: String) {
        val caseTime = timeString.toCaseTimeOrNull()
        _currentDraft.update { draft ->
            draft?.let {
                val updatedCases = it.cases.toMutableList().apply {
                    this[index] = this[index].copy(time = caseTime)
                }
                it.copy(cases = updatedCases)
            }
        }
    }

    fun updateCaseDescription(index: Int, description: String) {
        _currentDraft.update { draft ->
            draft?.let {
                val updatedCases = it.cases.toMutableList().apply {
                    this[index] = this[index].copy(description = CourtCaseDescription(description))
                }
                it.copy(cases = updatedCases)
            }
        }
    }

    // Подтверждение расписания (конвертация и вызов use case)
    fun confirmSchedule() {
        val draft = _currentDraft.value ?: return
        if (!_validation.value.isValid) return

        draft.toCourtSchedule() // extension функция, см. ниже
        viewModelScope.launch {
            //confirmScheduleUseCase(schedule)
            // например, переход на другой экран
        }
    }

    // Вспомогательная функция для загрузки из assets (без изменений)
    private suspend fun loadImageBytesFromAssets(context: Context, filename: String): ByteArray? =
        withContext(Dispatchers.IO) {
            try {
                context.assets.open(filename).use { it.readBytes() }
            } catch (e: Exception) {
                null
            }
        }

    fun resetState() {
        _ocrState.value = OcrState.Idle
        _processingTime.value = 0
        _currentDraft.value = null
    }
}

// Extension для конвертации валидного черновика в финальную модель
fun CourtScheduleDraft.toCourtSchedule(): CourtSchedule = CourtSchedule(
    date = requireNotNull(date) { "Date must not be null" },
    judge = judge,
    cases = cases.map { it.toCourtCase() }
)

fun CourtCaseDraft.toCourtCase(): CourtCase = CourtCase(
    caseNumber = requireNotNull(caseNumber) { "Case number must not be null" },
    time = requireNotNull(time) { "Time must not be null" },
    description = description
)