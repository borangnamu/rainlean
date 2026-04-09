package com.rainlean.core

import kotlin.math.abs

object MotionMath {
    fun lerp(current: Double, target: Double, alpha: Double): Double {
        return current + (target - current) * alpha
    }

    fun circularLerp(current: Double, target: Double, alpha: Double): Double {
        val delta = (((target - current + 540.0) % 360.0) - 180.0)
        return (current + delta * alpha + 360.0) % 360.0
    }

    fun isFlatDevice(pitchDeg: Double, rollDeg: Double, thresholdDeg: Double = 12.0): Boolean {
        return abs(pitchDeg) < thresholdDeg && abs(rollDeg) < thresholdDeg
    }

    fun updateFlatState(
        currentIsFlat: Boolean,
        pitchDeg: Double,
        rollDeg: Double,
        enterThresholdDeg: Double,
        exitThresholdDeg: Double
    ): Boolean {
        val absPitch = abs(pitchDeg)
        val absRoll = abs(rollDeg)
        return if (currentIsFlat) {
            absPitch < exitThresholdDeg && absRoll < exitThresholdDeg
        } else {
            absPitch < enterThresholdDeg && absRoll < enterThresholdDeg
        }
    }
}
