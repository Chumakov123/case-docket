package com.chumakov123.casedocket.domain.usecase

import com.chumakov123.casedocket.domain.document.DocumentInterpreter
import com.chumakov123.casedocket.domain.document.ScheduleTableParser
import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft
import com.chumakov123.casedocket.domain.model.imaging.TableRowText
import com.chumakov123.casedocket.domain.repository.ImageLayoutAnalyzer
import com.chumakov123.casedocket.domain.repository.ImagePreprocessor
import com.chumakov123.casedocket.domain.repository.OcrService

class RecognizeScheduleUseCase(
    private val analyzer: ImageLayoutAnalyzer,
    private val preprocessor: ImagePreprocessor,
    private val ocr: OcrService,
    private val interpreter: DocumentInterpreter,
    private val tableParser: ScheduleTableParser
) {

    suspend fun execute(image: ByteArray): CourtScheduleDraft {
        val processedImage = preprocessor.preprocess(image)

        val layout = analyzer.analyze(processedImage)

        val headerText = ocr.recognizeTextInRegion(layout.headerImage)

        val extractedRows = tableParser.extractRows(layout)

        val tableRowsText = extractedRows.map { row ->
            TableRowText(
                ocr.recognizeTextInRegion(layout.tableImage, row.caseNumber),
                ocr.recognizeTextInRegion(layout.tableImage, row.time),
                ocr.recognizeTextInRegion(layout.tableImage, row.description)
            )
        }

        return interpreter.interpret(headerText, tableRowsText)
    }
}