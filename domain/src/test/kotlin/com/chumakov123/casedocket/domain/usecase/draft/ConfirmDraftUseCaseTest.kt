package com.chumakov123.casedocket.domain.usecase.draft

import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.repository.ConfirmedScheduleRepository
import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository
import com.chumakov123.casedocket.domain.usecase.notification.RescheduleNotificationsUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ConfirmDraftUseCaseTest {

    private val taskRepository: RecognitionTaskRepository = mockk()
    private val confirmedRepository: ConfirmedScheduleRepository = mockk()
    private val rescheduleNotificationsUseCase: RescheduleNotificationsUseCase =
        mockk(relaxed = true)

    private val useCase = ConfirmDraftUseCase(
        taskRepository,
        confirmedRepository,
        rescheduleNotificationsUseCase
    )

    @Test
    fun `invoke should save confirmed schedule, delete task and reschedule notifications`() =
        runTest {
            val taskId = 1L
            val confirmedSchedule = mockk<CourtSchedule>()
            val task = mockk<RecognitionTask>()

            coEvery { taskRepository.getTaskById(taskId) } returns task
            coEvery { confirmedRepository.addSchedule(confirmedSchedule) } returns 5L
            coEvery { taskRepository.deleteTask(taskId) } just Runs

            useCase(taskId, confirmedSchedule)

            coVerify(exactly = 1) { taskRepository.getTaskById(taskId) }
            coVerify(exactly = 1) { confirmedRepository.addSchedule(confirmedSchedule) }
            coVerify(exactly = 1) { taskRepository.deleteTask(taskId) }
            coVerify(exactly = 1) { rescheduleNotificationsUseCase() }

            confirmVerified(taskRepository, confirmedRepository, rescheduleNotificationsUseCase)
        }

    @Test
    fun `invoke should do nothing if task not found`() = runTest {
        val taskId = 999L
        val confirmedSchedule = mockk<CourtSchedule>()

        coEvery { taskRepository.getTaskById(taskId) } returns null

        useCase(taskId, confirmedSchedule)
        
        coVerify(exactly = 1) { taskRepository.getTaskById(taskId) }
        coVerify(exactly = 0) { confirmedRepository.addSchedule(any()) }
        coVerify(exactly = 0) { taskRepository.deleteTask(taskId) }
        coVerify(exactly = 0) { rescheduleNotificationsUseCase() }

        confirmVerified(taskRepository, confirmedRepository, rescheduleNotificationsUseCase)
    }

}