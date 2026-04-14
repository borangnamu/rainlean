package com.rainlean.notification

import com.rainlean.domain.model.Confidence
import com.rainlean.domain.model.UmbrellaGuidance
import com.rainlean.domain.model.WeatherSnapshot
import com.rainlean.domain.model.WeatherSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class UmbrellaGuidanceCacheTest {

    private lateinit var cache: UmbrellaGuidanceCache

    @Before
    fun setUp() {
        cache = UmbrellaGuidanceCache()
    }

    @Test
    fun `초기 상태는 guidance null, weather null, heading 0`() = runTest {
        assertNull(cache.guidance.first())
        assertNull(cache.weather.first())
        assertEquals(0.0, cache.headingDeg.first(), 0.001)
    }

    @Test
    fun `setGuidance 호출 후 StateFlow 값이 즉시 반영된다`() = runTest {
        val guidance = UmbrellaGuidance(
            relativeDirectionDeg = 45.0,
            tiltDeg = 20.0,
            confidence = Confidence.HIGH
        )
        val weather = WeatherSnapshot(
            precipitationMmPerHour = 3.0,
            windDirectionFromDeg = 90.0,
            windSpeedMps = 5.0,
            observedAtEpochSec = 1_000_000L,
            source = WeatherSource.OPEN_METEO
        )

        cache.setGuidance(guidance, weather)

        assertEquals(guidance, cache.guidance.first())
        assertEquals(weather, cache.weather.first())
    }

    @Test
    fun `setGuidance null 호출 시 guidance null 로 초기화된다`() = runTest {
        val guidance = UmbrellaGuidance(90.0, 15.0, Confidence.MEDIUM)
        val weather = WeatherSnapshot(1.0, 90.0, 3.0, 1000L, WeatherSource.KMA)
        cache.setGuidance(guidance, weather)

        cache.setGuidance(null, null)

        assertNull(cache.guidance.first())
        assertNull(cache.weather.first())
    }

    @Test
    fun `setHeading 호출 후 headingDeg 가 반영된다`() = runTest {
        cache.setHeading(270.0)
        assertEquals(270.0, cache.headingDeg.first(), 0.001)
    }

    @Test
    fun `setHeading 여러 번 호출 시 마지막 값이 유지된다`() = runTest {
        cache.setHeading(90.0)
        cache.setHeading(180.0)
        cache.setHeading(315.0)
        assertEquals(315.0, cache.headingDeg.first(), 0.001)
    }
}
