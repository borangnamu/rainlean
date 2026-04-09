package com.rainlean.core

object DirectionMath {
    fun normalizeDeg(deg: Double): Double {
        var v = deg % 360.0
        if (v < 0) v += 360.0
        return v
    }

    fun shortestDiffDeg(a: Double, b: Double): Double {
        val d = normalizeDeg(a - b)
        return if (d > 180.0) 360.0 - d else d
    }
}

