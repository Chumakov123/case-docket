package com.chumakov123.casedocket.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.chumakov123.casedocket.data.local.AppDatabase
import com.chumakov123.casedocket.data.local.dao.ConfirmedScheduleDao
import com.chumakov123.casedocket.data.local.dao.RecognitionTaskDao
import com.chumakov123.casedocket.data.repository.ConfirmedScheduleRepositoryImpl
import com.chumakov123.casedocket.data.repository.InternalStorageImageSaver
import com.chumakov123.casedocket.data.repository.OpenCvImagePreprocessorImpl
import com.chumakov123.casedocket.data.repository.OpenCvLayoutAnalyzerImpl
import com.chumakov123.casedocket.data.repository.RecognitionTaskRepositoryImpl
import com.chumakov123.casedocket.data.repository.SettingsRepositoryImpl
import com.chumakov123.casedocket.data.repository.TesseractOcrServiceImpl
import com.chumakov123.casedocket.domain.document.DocumentInterpreter
import com.chumakov123.casedocket.domain.document.ScheduleTableParser
import com.chumakov123.casedocket.domain.repository.ConfirmedScheduleRepository
import com.chumakov123.casedocket.domain.repository.ImageLayoutAnalyzer
import com.chumakov123.casedocket.domain.repository.ImagePreprocessor
import com.chumakov123.casedocket.domain.repository.ImageSaver
import com.chumakov123.casedocket.domain.repository.OcrService
import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository
import com.chumakov123.casedocket.domain.repository.SettingsRepository
import com.chumakov123.casedocket.domain.service.RecognitionServiceController
import com.chumakov123.casedocket.domain.service.ScheduleRecognitionManager
import com.chumakov123.casedocket.domain.usecase.GetSettingsUseCase
import com.chumakov123.casedocket.domain.usecase.RecognizeScheduleUseCase
import com.chumakov123.casedocket.domain.usecase.confirmed.GetConfirmedScheduleByIdUseCase
import com.chumakov123.casedocket.domain.usecase.confirmed.GetConfirmedSchedulesUseCase
import com.chumakov123.casedocket.domain.usecase.confirmed.UpdateConfirmedScheduleUseCase
import com.chumakov123.casedocket.domain.usecase.draft.ConfirmDraftUseCase
import com.chumakov123.casedocket.domain.usecase.draft.GetDraftByIdUseCase
import com.chumakov123.casedocket.domain.usecase.draft.RejectDraftUseCase
import com.chumakov123.casedocket.domain.usecase.draft.UpdateDraftUseCase
import com.chumakov123.casedocket.domain.usecase.settings.UpdateSettingsUseCase
import com.chumakov123.casedocket.domain.validator.ScheduleValidator
import com.chumakov123.casedocket.presentation.tracker.AppForegroundTracker
import com.chumakov123.casedocket.presentation.viewmodel.ConfirmedListViewModel
import com.chumakov123.casedocket.presentation.viewmodel.DraftListViewModel
import com.chumakov123.casedocket.presentation.viewmodel.EditDraftViewModel
import com.chumakov123.casedocket.presentation.viewmodel.SettingsViewModel
import com.chumakov123.casedocket.service.RecognitionServiceControllerImpl
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val domainModule = module {
    factory { DocumentInterpreter() }
    factory { ScheduleTableParser() }
    factory { ScheduleValidator() }

    factory { ConfirmDraftUseCase(taskRepository = get(), confirmedRepository = get()) }
    factory { RejectDraftUseCase(taskRepository = get()) }
    factory { GetDraftByIdUseCase(repository = get()) }
    factory { UpdateDraftUseCase(repository = get()) }

    factory { GetConfirmedSchedulesUseCase(repository = get()) }
    factory { GetConfirmedScheduleByIdUseCase(repository = get()) }
    factory { UpdateConfirmedScheduleUseCase(repository = get()) }

    factory { GetSettingsUseCase(repository = get()) }
    factory { UpdateSettingsUseCase(repository = get()) }

    factory {
        RecognizeScheduleUseCase(
            preprocessor = get(),
            analyzer = get(),
            ocr = get(),
            interpreter = get(),
            tableParser = get()
        )
    }
}

val appModule = module {
    viewModel {
        EditDraftViewModel(
            getDraftByIdUseCase = get(),
            updateDraftUseCase = get(),
            confirmDraftUseCase = get(),
            rejectDraftUseCase = get(),
            getConfirmedScheduleUseCase = get(),
            updateConfirmedScheduleUseCase = get(),
            scheduleValidator = get()
        )
    }
    viewModel {
        DraftListViewModel(
            manager = get(),
            repository = get(),
            imageSaver = get()
        )
    }
    viewModel { ConfirmedListViewModel(getConfirmedSchedulesUseCase = get()) }
    viewModel {
        SettingsViewModel(
            getSettingsUseCase = get(),
            updateSettingsUseCase = get()
        )
    }

    factory { ScheduleRecognitionManager(get(), get()) }
    single<RecognitionServiceController> { RecognitionServiceControllerImpl(androidContext()) }
    single { AppForegroundTracker() }
}

val dataModule = module {
    single { Json { ignoreUnknownKeys = true } }

    single<ImageSaver> { InternalStorageImageSaver(context = androidContext()) }
    single<ImagePreprocessor> { OpenCvImagePreprocessorImpl(imageSaver = null) }
    single<ImageLayoutAnalyzer> { OpenCvLayoutAnalyzerImpl(imageSaver = null) }
    single<OcrService> {
        TesseractOcrServiceImpl(context = androidContext())
    }

    single<AppDatabase> { AppDatabase.getInstance(androidContext()) }
    single<RecognitionTaskDao> { get<AppDatabase>().recognitionTaskDao() }
    single<ConfirmedScheduleDao> { get<AppDatabase>().confirmedScheduleDao() }

    single<ConfirmedScheduleRepository> {
        ConfirmedScheduleRepositoryImpl(
            dao = get(),
            json = get()
        )
    }
    single<RecognitionTaskRepository> { RecognitionTaskRepositoryImpl(dao = get(), json = get()) }

    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create(
            produceFile = {
                androidContext().applicationContext.filesDir.resolve("settings.preferences_pb")
            }
        )
    }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
}

val appModules = listOf(
    domainModule,
    appModule,
    dataModule
)