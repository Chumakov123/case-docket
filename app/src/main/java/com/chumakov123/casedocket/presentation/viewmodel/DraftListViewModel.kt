package com.chumakov123.casedocket.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.model.TaskStatus
import com.chumakov123.casedocket.domain.repository.ImageSaver
import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository
import com.chumakov123.casedocket.domain.service.ScheduleRecognitionManager
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
    private val repository: RecognitionTaskRepository,
    private val imageSaver: ImageSaver
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<RecognitionTask>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

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

    fun addTestImage(context: Context, filename: String = "test_schedule.jpg") {
        viewModelScope.launch {
            try {
                val bytes = context.assets.open(filename).use { it.readBytes() }
                val path = imageSaver.save(bytes, filename)
                if (path != null) {
                    manager.submitImage("file://$path")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки тестового изображения: ${e.message}"
            }
        }
    }

    fun processSelectedImages(context: Context, uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            var successCount = 0
            var errorCount = 0
            try {
                uris.forEach { uri ->
                    try {
                        val bytes =
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        if (bytes != null) {
                            val filename =
                                "gallery_${System.currentTimeMillis()}_${uri.hashCode()}.jpg"
                            val savedPath = imageSaver.save(bytes, filename)
                            if (savedPath != null) {
                                manager.submitImage("file://$savedPath")
                                successCount++
                            } else {
                                errorCount++
                            }
                        } else {
                            errorCount++
                        }
                    } catch (e: Exception) {
                        errorCount++
                    }
                }
            } finally {
                _isLoading.value = false
                if (errorCount > 0) {
                    _errorMessage.value = "Загружено: $successCount, ошибок: $errorCount"
                }
            }
        }
    }

    fun processCapturedImage(context: Context, imageUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val bytes =
                    context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                if (bytes != null) {
                    val filename = "camera_${System.currentTimeMillis()}.jpg"
                    val savedPath = imageSaver.save(bytes, filename)
                    if (savedPath != null) {
                        manager.submitImage("file://$savedPath")
                    } else {
                        _errorMessage.value = "Не удалось сохранить фото"
                    }
                } else {
                    _errorMessage.value = "Не удалось прочитать фото"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обработке фото: ${e.message}"
            } finally {
                _isLoading.value = false
                try {
                    context.contentResolver.delete(imageUri, null, null)
                } catch (e: Exception) {

                }
            }
        }
    }

    fun retryTask(taskId: Long) {
        viewModelScope.launch {
            val task = _tasks.value.find { it.id == taskId } ?: return@launch
            manager.submitImage(task.imageUri)
            repository.deleteTask(taskId)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun showError(message: String) {
        _errorMessage.value = message
    }
}