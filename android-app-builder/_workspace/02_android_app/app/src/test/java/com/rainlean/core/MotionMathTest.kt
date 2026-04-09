package com.rainlean.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionMathTest {
    @Test
    fun `circular lerp crosses 360 boundary without jumping`() {
        val result = MotionMath.circularLerp(current = 359.0, target = 1.0, alpha = 0.5)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `flat detection returns true only for low pitch and roll`() {
        assertTrue(MotionMath.isFlatDevice(pitchDeg = 4.0, rollDeg = -5.0))
        assertFalse(MotionMath.isFlatDevice(pitchDeg = 20.0, rollDeg = 3.0))
    }

    @Test
    fun `flat hysteresis keeps state stable near threshold`() {
        val entered = MotionMath.updateFlatState(
            currentIsFlat = false,
            pitchDeg = 9.0,
            rollDeg = 8.0,
            enterThresholdDeg = 10.0,
            exitThresholdDeg = 16.0
        )
        assertTrue(entered)

        val stillFlat = MotionMath.updateFlatState(
            currentIsFlat = entered,
            pitchDeg = 14.5,
            rollDeg = 12.0,
            enterThresholdDeg = 10.0,
            exitThresholdDeg = 16.0
        )
        assertTrue(stillFlat)

        val exited = MotionMath.updateFlatState(
            currentIsFlat = stillFlat,
            pitchDeg = 17.0,
            rollDeg = 8.0,
            enterThresholdDeg = 10.0,
            exitThresholdDeg = 16.0
        )
        assertFalse(exited)
    }

    @Test
    fun `lerp moves proportionally toward target`() {
        val result = MotionMath.lerp(current = 10.0, target = 20.0, alpha = 0.2)
        assertEquals(12.0, result, 0.001)
    }
}
