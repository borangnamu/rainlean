package com.rainlean.domain.model

data class UmbrellaGuidance(
    val relativeDirectionDeg: Double,
    val tiltDeg: Double,
    val confidence: Confidence
)

enum class Confidence {
    HIGH,
    MEDIUM,
    LOW
}

