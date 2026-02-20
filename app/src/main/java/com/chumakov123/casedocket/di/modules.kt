package com.chumakov123.casedocket.di

import com.chumakov123.casedocket.data.repository.InternalStorageImageSaver
import com.chumakov123.casedocket.presentation.viewmodel.OcrViewModel
import com.chumakov123.casedocket.domain.document.DocumentInterpreter
import com.chumakov123.casedocket.domain.repository.ImageLayoutAnalyzer
import com.chumakov123.casedocket.domain.repository.ImagePreprocessor
import com.chumakov123.casedocket.domain.repository.OcrService
import com.chumakov123.casedocket.domain.usecase.RecognizeScheduleUseCase
import com.chumakov123.casedocket.data.repository.OpenCvImagePreprocessorImpl
import com.chumakov123.casedocket.data.repository.OpenCvLayoutAnalyzerImpl
import com.chumakov123.casedocket.data.repository.TesseractOcrServiceImpl
import com.chumakov123.casedocket.domain.document.ScheduleTableParser
import com.chumakov123.casedocket.domain.repository.ImageSaver
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val domainModule = module {
    factory { DocumentInterpreter() }
    factory { ScheduleTableParser() }

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
    viewModel { OcrViewModel(recognizeScheduleUseCase = get()) }
}

val dataModule = module {
    single<ImageSaver> { InternalStorageImageSaver(context = androidContext()) }
    single<ImagePreprocessor> { OpenCvImagePreprocessorImpl(imageSaver = null) }
    single<ImageLayoutAnalyzer> { OpenCvLayoutAnalyzerImpl(imageSaver = null) }
    single<OcrService> {
        TesseractOcrServiceImpl(context = androidContext())
    }
}

val appModules = listOf(
    domainModule,
    appModule,
    dataModule
)