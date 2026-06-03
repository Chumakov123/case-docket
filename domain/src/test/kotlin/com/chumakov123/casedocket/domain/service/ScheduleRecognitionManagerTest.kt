package com.chumakov123.casedocket.domain.service

import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.model.TaskStatus
import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft
import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScheduleRecognitionManagerTest {

    private val repository: RecognitionTaskRepository = mockk()
    private val serviceController: RecognitionServiceController = mockk(relaxed = true)
    private val manager = ScheduleRecognitionManager(repository, serviceController)

    private fun mockTasks(tasks: List<RecognitionTask>) {
        coEvery { repository.observeTasks() } returns flowOf(tasks)
    }

    // submitImage
    @Test
    fun `submitImage should add task and ensure service running`() = runTest {
        val imageUri = "test_uri"
        coEvery { repository.addTask(imageUri) } returns 1L

        manager.submitImage(imageUri)

        coVerify(exactly = 1) { repository.addTask(imageUri) }
        coVerify(exactly = 1) { serviceController.ensureServiceRunning() }
    }

    // processNextTask
    @Test
    fun `processNextTask when pending task exists returns task and updates status`() = runTest {
        val task = RecognitionTask(id = 1, imageUri = "uri", status = TaskStatus.PENDING)
        coEvery { repository.getNextPendingTask() } returns task
        coEvery { repository.updateTask(any()) } just Runs

        val result = manager.processNextTask()

        assertEquals(task.id, result?.id)
        coVerify(exactly = 1) { repository.updateTask(match { it.status == TaskStatus.PROCESSING }) }
    }

    @Test
    fun `processNextTask when no pending task returns null`() = runTest {
        coEvery { repository.getNextPendingTask() } returns null

        val result = manager.processNextTask()

        assertNull(result)
        coVerify(exactly = 0) { repository.updateTask(any()) }
    }

    // completeTask
    @Test
    fun `completeTask when task exists updates task and stops service if queue empty`() = runTest {
        val taskId = 1L
        val existingTask =
            RecognitionTask(id = taskId, imageUri = "uri", status = TaskStatus.PROCESSING)
        mockTasks(listOf(existingTask))

        val draft = mockk<CourtScheduleDraft>()
        coEvery { repository.updateTask(any()) } just Runs

        manager.completeTask(taskId, draft)

        coVerify(exactly = 1) {
            repository.updateTask(match {
                it.id == taskId &&
                        it.status == TaskStatus.COMPLETED &&
                        it.resultDraft == draft &&
                        it.completedAt != null
            })
        }
        coVerify(exactly = 1) { serviceController.stopIfQueueEmpty() }
    }

    @Test
    fun `completeTask when task not found does nothing`() = runTest {
        mockTasks(emptyList())
        val draft = mockk<CourtScheduleDraft>()

        manager.completeTask(999L, draft)

        coVerify(exactly = 0) { repository.updateTask(any()) }
        coVerify(exactly = 0) { serviceController.stopIfQueueEmpty() }
    }

    // failTask
    @Test
    fun `failTask when task exists updates task and stops service`() = runTest {
        val taskId = 1L
        val existingTask =
            RecognitionTask(id = taskId, imageUri = "uri", status = TaskStatus.PROCESSING)
        mockTasks(listOf(existingTask))

        val errorMsg = "error"
        coEvery { repository.updateTask(any()) } just Runs

        manager.failTask(taskId, errorMsg)

        coVerify(exactly = 1) {
            repository.updateTask(match {
                it.id == taskId &&
                        it.status == TaskStatus.FAILED &&
                        it.errorMessage == errorMsg &&
                        it.completedAt != null
            })
        }
        coVerify(exactly = 1) { serviceController.stopIfQueueEmpty() }
    }

    @Test
    fun `failTask when task not found does nothing`() = runTest {
        mockTasks(emptyList())

        manager.failTask(999L, "error")

        coVerify(exactly = 0) { repository.updateTask(any()) }
        coVerify(exactly = 0) { serviceController.stopIfQueueEmpty() }
    }

    // repairStuckTasks
    @Test
    fun `repairStuckTasks resets stuck tasks to pending`() = runTest {
        // Given
        val stuckTask1 = RecognitionTask(id = 1L, imageUri = "uri1", status = TaskStatus.PROCESSING)
        val stuckTask2 = RecognitionTask(id = 2L, imageUri = "uri2", status = TaskStatus.PROCESSING)
        val normalTask = RecognitionTask(id = 3L, imageUri = "uri3", status = TaskStatus.COMPLETED)
        mockTasks(listOf(stuckTask1, stuckTask2, normalTask))

        coEvery { repository.updateTask(any()) } just Runs

        // When
        manager.repairStuckTasks()

        // Then
        coVerify(exactly = 2) { repository.updateTask(any()) }
        coVerify {
            repository.updateTask(match { it.id == 1L && it.status == TaskStatus.PENDING })
            repository.updateTask(match { it.id == 2L && it.status == TaskStatus.PENDING })
        }
        coVerify(exactly = 0) { repository.updateTask(match { it.id == 3L }) }
    }

    // hasPendingTask
    @Test
    fun `hasPendingTask returns true when pending task exists`() = runTest {
        coEvery { repository.getNextPendingTask() } returns mockk()
        assertTrue(manager.hasPendingTask())
    }

    @Test
    fun `hasPendingTask returns false when no pending task`() = runTest {
        coEvery { repository.getNextPendingTask() } returns null
        assertFalse(manager.hasPendingTask())
    }

    // observeTasks
    @Test
    fun `observeTasks returns flow from repository`() = runTest {
        val flow = flowOf(emptyList<RecognitionTask>())
        coEvery { repository.observeTasks() } returns flow
        assertSame(flow, manager.observeTasks())
    }
}