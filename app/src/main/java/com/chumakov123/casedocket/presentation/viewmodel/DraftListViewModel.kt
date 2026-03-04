package com.chumakov123.casedocket.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.model.TaskStatus
import com.chumakov123.casedocket.domain.repository.ImageSaver
import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository
import com.chumakov123.casedocket.domain.service.ScheduleRecognitionManager
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
    val tasks: StateFlow<List<RecognitionTask>> = _tasks

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
}