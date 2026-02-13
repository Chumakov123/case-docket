package com.chumakov123.casedocket.domain.document

import com.chumakov123.casedocket.domain.model.imaging.DocumentLayout
import com.chumakov123.casedocket.domain.model.imaging.ImageRegion

class ScheduleTableParser {
    fun extractRows(layout: DocumentLayout): List<ExtractedRow> {
        return layout.tableCells
            .drop(2)
            .filter { it.size >= 3 }
            .map { row ->
                ExtractedRow(
                    caseNumber = row[0],
                    time = row[1],
                    description = row[2]
                )
            }
    }
}

data class ExtractedRow(
    val caseNumber: ImageRegion,
    val time: ImageRegion,
    val description: ImageRegion
)