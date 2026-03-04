package com.chumakov123.casedocket.domain.document

import com.chumakov123.casedocket.domain.model.court.CaseTime
import com.chumakov123.casedocket.domain.model.court.CourtCaseDescription
import com.chumakov123.casedocket.domain.model.court.Judge
import com.chumakov123.casedocket.domain.model.court.ScheduleDate
import com.chumakov123.casedocket.domain.model.court.draft.CourtCaseDraft
import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft
import com.chumakov123.casedocket.domain.model.imaging.TableRowText

class DocumentInterpreter {

    fun interpret(
        headerText: String,
        rows: List<TableRowText>
    ): CourtScheduleDraft {
        return CourtScheduleDraft(
            date = extractDate(headerText),
            judge = extractJudge(headerText),
            cases = rows.mapNotNull { row ->
                val caseNumber = cleanCaseNumber(row.caseNumberText)
                val time = rawTimeToCaseTime(row.timeText)
                val description = CourtCaseDescription(cleanDescription(row.descriptionText))

                // Создаем элемент только если хотя бы одно из полей не пустое
                when {
                    caseNumber.isNotBlank() -> CourtCaseDraft(
                        caseNumber = caseNumber,
                        time = time,
                        description = description
                    )

                    time != null -> CourtCaseDraft(
                        caseNumber = caseNumber,
                        time = time,
                        description = description
                    )

                    description.text.isNotBlank() -> CourtCaseDraft(
                        caseNumber = caseNumber,
                        time = time,
                        description = description
                    )

                    else -> null
                }
            }
        )
    }

    private fun cleanCaseNumber(rawCaseNumber: String): String {
        return rawCaseNumber.lines().joinToString("\n") { line ->
            val cleanedLine = line.replace(Regex("[^A-Za-zА-Яа-яЁё0-9/-]"), "")
            cleanedLine.trim()
        }.trim()
    }

    private fun rawTimeToCaseTime(rawTime: String): CaseTime? {
        val s = rawTime.replace(Regex("[^0-9:]"), "")
        if (s.isEmpty()) return null

        if (s.contains(":")) {
            val parts = s.split(":")

            val leftDigits = parts.getOrNull(0)?.filter { it.isDigit() } ?: ""
            val rightDigits = parts.getOrNull(1)?.filter { it.isDigit() } ?: ""

            if (leftDigits.isEmpty() || rightDigits.isEmpty()) return null

            val hours = leftDigits.takeLast(2).padStart(2, '0')
            val minutes = rightDigits.take(2).padEnd(2, '0')

            return validateTime(h = hours, m = minutes)
        }

        val digits = s.filter { it.isDigit() }
        if (digits.length < 3) return null

        val hours = digits.take(2)
        val minutes = digits.drop(2).take(2)

        return validateTime(h = hours, m = minutes)
    }

    private fun validateTime(h: String, m: String): CaseTime? {
        val hours = h.toIntOrNull() ?: return null
        val minutes = m.toIntOrNull() ?: return null

        if (hours !in 0..23) return null
        if (minutes !in 0..59) return null

        return CaseTime(hours, minutes)
    }

    private fun cleanDescription(rawDescription: String): String {
        val step1 = rawDescription.replace(Regex("[*`'\"«»„“”‘’]"), "\"")

        val step2 = step1.replace("[", "(").replace("]", ")")

        val allowedPattern = Regex("[^A-Za-zА-Яа-яЁё0-9\\s:;\",.()\\-–]")
        val step3 = step2.replace(allowedPattern, "")

        val cleanedLines = step3.lines()
            .map { line ->
                line.trim().replace(Regex("\\s+"), " ")
            }
            .filter { it.isNotEmpty() }

        return cleanedLines.joinToString("\n")
    }

    private fun extractDate(headerText: String): ScheduleDate? {
        val datePattern = Regex("""\b\d{2}\.\d{2}\.\d{4}\b""")

        return headerText.lines()
            .flatMap { datePattern.findAll(it).toList() }
            .firstNotNullOfOrNull { ScheduleDate.parse(it.value) }
    }

    private fun extractJudge(headerText: String): Judge {
        val lines = headerText.lines()
        val judgePrefix = "Судья:"

        for (line in lines) {
            val index = line.indexOf(judgePrefix)
            if (index != -1) {
                val judgeName = line.substring(index + judgePrefix.length).trim()
                if (judgeName.isNotEmpty()) {
                    return Judge(text = judgeName)
                }
            }
        }

        return Judge(text = "")
    }
}