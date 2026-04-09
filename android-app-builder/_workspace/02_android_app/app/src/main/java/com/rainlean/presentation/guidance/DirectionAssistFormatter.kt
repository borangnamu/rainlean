package com.rainlean.presentation.guidance

import com.rainlean.core.DirectionMath.normalizeDeg

fun toClockDirectionLabel(relativeDirectionDeg: Double): String {
    val normalized = normalizeDeg(relativeDirectionDeg)
    val index = ((normalized + 15.0) / 30.0).toInt() % 12
    val hour = if (index == 0) 12 else index
    return "${hour}시 방향"
}
