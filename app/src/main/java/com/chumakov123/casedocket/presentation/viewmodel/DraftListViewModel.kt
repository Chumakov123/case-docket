package com.chumakov123.casedocket.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.casedocket.domain.model.ErrorMessage
import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.model.TaskStatus
import com.chumakov123.casedocket.domain.repository.ImageHandler
import com.chumakov123.casedocket.domain.service.ScheduleRecognitionManager
import com.chumakov123.casedocket.domain.usecase.task.DeleteTaskUseCase
import com.chumakov123.casedocket.domain.usecase.task.RetryTaskUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskGroup(
    val pendingProcessing: List<RecognitionTask>, // PENDING + PROCESSING
    val completed: List<RecognitionTask>,         // COMPLETED
    val failed: List<RecognitionTask>             // FAILED
)

class DraftListViewModel(
    private val manager: ScheduleRecognitionManager,
    private val imageHandler: ImageHandler,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val retryTaskUseCase: RetryTaskUseCase
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<RecognitionTask>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _errorMessage = MutableStateFlow<ErrorMessage?>(null)
    val errorMessage: StateFlow<ErrorMessage?> = _errorMessage

    val taskGroups: StateFlow<TaskGroup> = _tasks.map { tasks ->
        TaskGroup(
            pendingProcessing = tasks.filter { it.status == TaskStatus.PENDING || it.status == TaskStatus.PROCESSING },
            completed = tasks.filter { it.status == TaskStatus.COMPLETED },
            failed = tasks.filter { it.status == TaskStatus.FAILED }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskGroup(emptyList(), emptyList(), emptyList())
    )

    init {
        viewModelScope.launch {
            manager.observeTasks().collect { list ->
                _tasks.value = list
            }
        }
    }

    fun processSelectedImages(uriStrings: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val result = imageHandler.processSelectedImages(uriStrings)
            _isLoading.value = false
            if (result.errorCount > 0) {
                _errorMessage.value =
                    ErrorMessage.UploadSummary(result.successCount, result.errorCount)
            }
        }
    }

    fun processCapturedImage(uriString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val result = imageHandler.processCapturedImage(uriString)
            _isLoading.value = false
            result.errorMessage?.let { _errorMessage.value = it }
        }
    }

    fun retryTask(taskId: Long) {
        viewModelScope.launch {
            retryTaskUseCase(taskId)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun showError(message: ErrorMessage) {
        _errorMessage.value = message
    }
}