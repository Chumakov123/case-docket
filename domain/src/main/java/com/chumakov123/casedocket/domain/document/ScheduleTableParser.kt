package com.chumakov123.casedocket.domain.document

import com.chumakov123.casedocket.domain.model.imaging.DocumentLayout
import com.chumakov123.casedocket.domain.model.imaging.ImageRegion

class ScheduleTableParser {

    fun extractRows(layout: DocumentLayout): List<ExtractedRow> {
        // Проверяем количество колонок
        val processedCells = if (hasMoreThanFourColumns(layout.tableCells)) {
            removeNarrowestColumns(layout.tableCells)
        } else {
            layout.tableCells
        }

        return processedCells
            .drop(2) // Пропускаем заголовки
            .filter { it.size >= 3 }
            .map { row ->
                ExtractedRow(
                    caseNumber = row[0],
                    time = row[1],
                    description = row[2]
                )
            }
    }

    private fun hasMoreThanFourColumns(tableCells: List<List<ImageRegion>>): Boolean {
        return tableCells.isNotEmpty() && tableCells[0].size > 4
    }

    private fun removeNarrowestColumns(tableCells: List<List<ImageRegion>>): List<List<ImageRegion>> {
        if (tableCells.isEmpty()) return tableCells

        val columnCount = tableCells[0].size
        val columnsToRemove = columnCount - 4 // Сколько колонок нужно удалить

        if (columnsToRemove <= 0) return tableCells

        // Вычисляем среднюю ширину каждой колонки
        val columnWidths = calculateColumnWidths(tableCells)

        // Находим индексы самых узких колонок для удаления
        val indexesToRemove = findNarrowestColumnIndexes(columnWidths, columnsToRemove)

        // Удаляем указанные колонки из всех строк
        return tableCells.map { row ->
            row.filterIndexed { index, _ -> index !in indexesToRemove }
        }
    }

    private fun calculateColumnWidths(tableCells: List<List<ImageRegion>>): List<Float> {
        if (tableCells.isEmpty()) return emptyList()

        val columnCount = tableCells[0].size
        val columnWidths = MutableList(columnCount) { 0f }
        val columnCounts = MutableList(columnCount) { 0 }

        for (row in tableCells) {
            for (i in row.indices) {
                columnWidths[i] += row[i].width
                columnCounts[i]++
            }
        }

        // Усредняем ширину по каждой колонке
        return columnWidths.mapIndexed { index, totalWidth ->
            if (columnCounts[index] > 0) totalWidth / columnCounts[index] else 0f
        }
    }

    private fun findNarrowestColumnIndexes(columnWidths: List<Float>, countToRemove: Int): Set<Int> {
        return columnWidths
            .mapIndexed { index, width -> index to width }
            .sortedBy { it.second } // Сортируем по ширине (от узких к широким)
            .take(countToRemove)
            .map { it.first }
            .toSet()
    }
}

data class ExtractedRow(
    val caseNumber: ImageRegion,
    val time: ImageRegion,
    val description: ImageRegion
)