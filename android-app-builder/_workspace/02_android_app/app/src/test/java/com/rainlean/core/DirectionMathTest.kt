package com.rainlean.core

import org.junit.Assert.assertEquals
import org.junit.Test

class DirectionMathTest {

    @Test
    fun `normalizeDeg wraps negative angle to positive`() {
        assertEquals(270.0, DirectionMath.normalizeDeg(-90.0), 0.001)
    }

    @Test
    fun `normalizeDeg leaves 0 as 0`() {
        assertEquals(0.0, DirectionMath.normalizeDeg(0.0), 0.001)
    }

    @Test
    fun `normalizeDeg wraps 360 to 0`() {
        assertEquals(0.0, DirectionMath.normalizeDeg(360.0), 0.001)
    }

    @Test
    fun `normalizeDeg wraps values greater than 360`() {
        assertEquals(45.0, DirectionMath.normalizeDeg(405.0), 0.001)
    }

    @Test
    fun `shortestDiffDeg returns correct angle when a is ahead`() {
        // 90° - 45° = 45°
        assertEquals(45.0, DirectionMath.shortestDiffDeg(90.0, 45.0), 0.001)
    }

    @Test
    fun `shortestDiffDeg returns correct angle across 0 boundary`() {
        // 350° vs 10° → shortest diff is 20°
        assertEquals(20.0, DirectionMath.shortestDiffDeg(350.0, 10.0), 0.001)
    }

    @Test
    fun `shortestDiffDeg is symmetric`() {
        val a = DirectionMath.shortestDiffDeg(30.0, 330.0)
        val b = DirectionMath.shortestDiffDeg(330.0, 30.0)
        assertEquals(a, b, 0.001)
    }

    @Test
    fun `shortestDiffDeg maximum is 180`() {
        assertEquals(180.0, DirectionMath.shortestDiffDeg(0.0, 180.0), 0.001)
    }
}
