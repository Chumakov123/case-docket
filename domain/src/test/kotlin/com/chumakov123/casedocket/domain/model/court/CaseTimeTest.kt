package com.chumakov123.casedocket.domain.model.court

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CaseTimeTest {

    @Test
    fun `toHHMM returns formatted string with leading zeros`() {
        val caseTime = CaseTime(hours = 9, minutes = 5)
        val result = caseTime.toHHMM()
        assertEquals("09:05", result)
    }

    @Test
    fun `toHHMM returns correct format for 24-hour boundary`() {
        val caseTime = CaseTime(hours = 23, minutes = 59)
        assertEquals("23:59", caseTime.toHHMM())
    }

    @Test
    fun `String toCaseTimeOrNull returns CaseTime for valid input`() {
        val input = "14:30"
        val result = input.toCaseTimeOrNull()

        assertNotNull(result)
        assertEquals(14, result?.hours)
        assertEquals(30, result?.minutes)
    }

    @Test
    fun `String toCaseTimeOrNull returns null for invalid format`() {
        val inputs = listOf("invalid", "25:00", "12:60", "12-30", "")
        inputs.forEach { input ->
            assertNull(input.toCaseTimeOrNull(), "Input: $input")
        }
    }

    @Test
    fun `String toCaseTimeOrNull returns null for format without colon`() {
        assertNull("1430".toCaseTimeOrNull())
    }
}