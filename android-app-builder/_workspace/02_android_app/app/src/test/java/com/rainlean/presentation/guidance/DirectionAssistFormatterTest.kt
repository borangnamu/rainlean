package com.rainlean.presentation.guidance

import org.junit.Assert.assertEquals
import org.junit.Test

class DirectionAssistFormatterTest {
    @Test
    fun `maps front direction to 12 oclock`() {
        assertEquals("12시 방향", toClockDirectionLabel(0.0))
        assertEquals("12시 방향", toClockDirectionLabel(359.0))
    }

    @Test
    fun `maps clockwise sectors to expected clock labels`() {
        assertEquals("1시 방향", toClockDirectionLabel(30.0))
        assertEquals("3시 방향", toClockDirectionLabel(90.0))
        assertEquals("6시 방향", toClockDirectionLabel(180.0))
        assertEquals("9시 방향", toClockDirectionLabel(270.0))
    }

    @Test
    fun `rounds near boundaries to nearest clock sector`() {
        assertEquals("1시 방향", toClockDirectionLabel(15.0))
        assertEquals("12시 방향", toClockDirectionLabel(14.9))
    }
}
