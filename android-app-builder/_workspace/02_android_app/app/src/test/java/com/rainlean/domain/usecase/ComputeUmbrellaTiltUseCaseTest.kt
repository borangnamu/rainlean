package com.rainlean.domain.usecase

import com.rainlean.domain.model.WeatherSnapshot
import com.rainlean.domain.model.WeatherSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ComputeUmbrellaTiltUseCaseTest {
    private val useCase = ComputeUmbrellaTiltUseCase()

    @Test
    fun `returns null when rain is too low`() {
        val weather = WeatherSnapshot(
            precipitationMmPerHour = 0.0,
            windDirectionFromDeg = 180.0,
            windSpeedMps = 3.0,
            observedAtEpochSec = 0L,
            source = WeatherSource.OPEN_METEO
        )

        val result = useCase.execute(weather, userHeadingDeg = 0.0)
        assertNull(result)
    }

    @Test
    fun `computes relative direction and tilt in expected range`() {
        val weather = WeatherSnapshot(
            precipitationMmPerHour = 1.0,
            windDirectionFromDeg = 200.0,
            windSpeedMps = 5.0,
            observedAtEpochSec = 0L,
            source = WeatherSource.OPEN_METEO
        )

        val result = useCase.execute(weather, userHeadingDeg = 20.0)
        assertNotNull(result)
        assertEquals(180.0, result!!.relativeDirectionDeg, 0.01)
        // Updated formula: (8 + 2.5*5 + 1.5*1) = 22.0°
        assertEquals(22.0, result.tiltDeg, 0.01)
        assertEquals(true, result.tiltDeg in 8.0..45.0)
    }

    @Test
    fun `tilt is capped at 45 degrees under extreme conditions`() {
        val weather = WeatherSnapshot(
            precipitationMmPerHour = 20.0,
            windDirectionFromDeg = 0.0,
            windSpeedMps = 20.0,
            observedAtEpochSec = 0L,
            source = WeatherSource.KMA
        )
        val result = useCase.execute(weather, userHeadingDeg = 0.0)
        assertNotNull(result)
        assertEquals(45.0, result!!.tiltDeg, 0.01)
    }

    @Test
    fun `tilt minimum is 8 degrees even in light rain`() {
        val weather = WeatherSnapshot(
            precipitationMmPerHour = 0.1,
            windDirectionFromDeg = 0.0,
            windSpeedMps = 0.0,
            observedAtEpochSec = 0L,
            source = WeatherSource.OPEN_METEO
        )
        val result = useCase.execute(weather, userHeadingDeg = 0.0)
        assertNotNull(result)
        assertEquals(8.15, result!!.tiltDeg, 0.001) // 8 + 0 + 1.5*0.1 = 8.15
    }

    @Test
    fun `relative direction normalizes correctly across 0 degree boundary`() {
        // Wind from 10°, user heading 350° → relative = 10 - 350 = -340 → normalized 20°
        val weather = WeatherSnapshot(
            precipitationMmPerHour = 1.0,
            windDirectionFromDeg = 10.0,
            windSpeedMps = 3.0,
            observedAtEpochSec = 0L,
            source = WeatherSource.OPEN_METEO
        )
        val result = useCase.execute(weather, userHeadingDeg = 350.0)
        assertNotNull(result)
        assertEquals(20.0, result!!.relativeDirectionDeg, 0.01)
    }
}

