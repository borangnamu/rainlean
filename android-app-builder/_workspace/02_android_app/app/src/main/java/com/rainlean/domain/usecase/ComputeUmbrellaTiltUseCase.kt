package com.rainlean.domain.usecase

import com.rainlean.core.DirectionMath.normalizeDeg
import com.rainlean.domain.model.Confidence
import com.rainlean.domain.model.UmbrellaGuidance
import com.rainlean.domain.model.WeatherSnapshot
import javax.inject.Inject

class ComputeUmbrellaTiltUseCase @Inject constructor() {
    fun execute(
        weather: WeatherSnapshot,
        userHeadingDeg: Double
    ): UmbrellaGuidance? {
        if (weather.precipitationMmPerHour < 0.1) return null

        val relative = normalizeDeg(weather.windDirectionFromDeg - userHeadingDeg)
        // Tilt formula: base 8° + wind contribution + rain contribution, capped at 45°.
        // windSpeedMps 10 m/s(강풍) + rain 5 mm/h → 8 + 25 + 7.5 = 40.5°, 약 41° 권장.
        val tilt = (8.0 + 2.5 * weather.windSpeedMps + 1.5 * weather.precipitationMmPerHour)
            .coerceIn(8.0, 45.0)

        val confidence = when {
            weather.windSpeedMps >= 6.0 && weather.precipitationMmPerHour >= 1.0 -> Confidence.HIGH
            weather.windSpeedMps >= 3.0 -> Confidence.MEDIUM
            else -> Confidence.LOW
        }
        return UmbrellaGuidance(relativeDirectionDeg = relative, tiltDeg = tilt, confidence = confidence)
    }
}
