package com.chumakov123.casedocket.di

import com.chumakov123.casedocket.data.local.AppDatabase
import com.chumakov123.casedocket.data.local.dao.ConfirmedScheduleDao
import com.chumakov123.casedocket.data.local.dao.RecognitionTaskDao
import com.chumakov123.casedocket.data.repository.ConfirmedScheduleRepositoryImpl
import com.chumakov123.casedocket.data.repository.InternalStorageImageSaver
import com.chumakov123.casedocket.data.repository.OpenCvImagePreprocessorImpl
import com.chumakov123.casedocket.data.repository.OpenCvLayoutAnalyzerImpl
import com.chumakov123.casedocket.data.repository.RecognitionTaskRepositoryImpl
import com.chumakov123.casedocket.data.repository.TesseractOcrServiceImpl
import com.chumakov123.casedocket.domain.document.DocumentInterpreter
import com.chumakov123.casedocket.domain.document.ScheduleTableParser
import com.chumakov123.casedocket.domain.repository.ConfirmedScheduleRepository
import com.chumakov123.casedocket.domain.repository.ImageLayoutAnalyzer
import com.chumakov123.casedocket.domain.repository.ImagePreprocessor
import com.chumakov123.casedocket.domain.repository.ImageSaver
import com.chumakov123.casedocket.domain.repository.OcrService
import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository
import com.chumakov123.casedocket.domain.service.RecognitionServiceController
import com.chumakov123.casedocket.domain.service.ScheduleRecognitionManager
import com.chumakov123.casedocket.domain.usecase.RecognizeScheduleUseCase
import com.chumakov123.casedocket.domain.validator.ScheduleValidator
import com.chumakov123.casedocket.presentation.tracker.AppForegroundTracker
import com.chumakov123.casedocket.presentation.viewmodel.OcrViewModel
import com.chumakov123.casedocket.service.RecognitionServiceControllerImpl
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val domainModule = module {
    factory { DocumentInterpreter() }
    factory { ScheduleTableParser() }
    factory { ScheduleValidator() }

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
        OcrViewModel(
            recognizeScheduleUseCase = get(),
            scheduleValidator = get(),
            manager = get(),
            imageSaver = get()
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
    single<RecognitionTaskRepository> { RecognitionTaskRepositoryImpl(dao = get()) }
}

val appModules = listOf(
    domainModule,
    appModule,
    dataModule
)