package com.rainlean.notification

import com.rainlean.domain.model.Confidence
import com.rainlean.domain.model.UmbrellaGuidance
import com.rainlean.domain.model.WeatherSnapshot
import com.rainlean.domain.model.WeatherSource
import com.rainlean.domain.usecase.ComputeUmbrellaTiltUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * WeatherRefreshWorker 핵심 로직 단위 테스트.
 *
 * WorkerParameters 생성이 Android 컨텍스트를 요구하므로,
 * 실제 Worker 실행은 androidTest에서 TestListenableWorkerBuilder로 검증.
 * 여기서는 Worker가 의존하는 컴포넌트들이 올바르게 연동되는지
 * fake 를 통해 확인한다.
 */
class WeatherRefreshWorkerTest {

    private lateinit var cache: UmbrellaGuidanceCache
    private lateinit var useCase: ComputeUmbrellaTiltUseCase

    @Before
    fun setUp() {
        cache = UmbrellaGuidanceCache()
        useCase = ComputeUmbrellaTiltUseCase()
    }

    @Test
    fun `ComputeUmbrellaTiltUseCase 가 정상 날씨로 guidance 를 반환한다`() {
        val weather = WeatherSnapshot(
            precipitationMmPerHour = 2.0,
            windDirectionFromDeg = 90.0,
            windSpeedMps = 5.0,
            observedAtEpochSec = 1_000_000L,
            source = WeatherSource.OPEN_METEO
        )

        val guidance = useCase.execute(weather, userHeadingDeg = 0.0)

        assertNotNull(guidance)
        assertEquals(90.0, guidance!!.relativeDirectionDeg, 0.001)
    }

    @Test
    fun `강수량 0 이면 guidance 가 null 을 반환한다`() {
        val weather = WeatherSnapshot(
            precipitationMmPerHour = 0.0,
            windDirectionFromDeg = 90.0,
            windSpeedMps = 3.0,
            observedAtEpochSec = 1_000_000L,
            source = WeatherSource.KMA
        )

        val guidance = useCase.execute(weather, userHeadingDeg = 0.0)

        assertEquals(null, guidance)
    }

    @Test
    fun `cache setGuidance 호출 후 guidance StateFlow 가 갱신된다`() {
        val guidance = UmbrellaGuidance(45.0, 18.0, Confidence.MEDIUM)
        val weather = WeatherSnapshot(1.5, 45.0, 4.0, 2_000_000L, WeatherSource.OPEN_METEO)

        // Worker 내부에서 수행하는 캐시 갱신 시뮬레이션
        cache.setGuidance(guidance, weather)

        assertEquals(guidance, cache.guidance.value)
        assertEquals(weather, cache.weather.value)
    }

    @Test
    fun `headingDeg 0 도 기준 상대 방향 계산이 올바르다`() {
        val weather = WeatherSnapshot(1.0, 270.0, 5.0, 1000L, WeatherSource.OPEN_METEO)

        val guidance = useCase.execute(weather, userHeadingDeg = 0.0)

        assertNotNull(guidance)
        assertEquals(270.0, guidance!!.relativeDirectionDeg, 0.001)
    }

    @Test
    fun `headingDeg 와 windDirection 이 같으면 relativeDirection 은 0 도이다`() {
        val weather = WeatherSnapshot(1.0, 45.0, 5.0, 1000L, WeatherSource.KMA)

        val guidance = useCase.execute(weather, userHeadingDeg = 45.0)

        assertNotNull(guidance)
        assertEquals(0.0, guidance!!.relativeDirectionDeg, 0.001)
    }
}

// ── Fake BannerPreferences (WorkerTest 보조용) ───────────────────────────────

class FakeBannerPreferences(private val enabled: Boolean, private val heading: Double) {
    val bannerEnabled: Flow<Boolean> = flowOf(enabled)
    val lastHeadingDeg: Flow<Double> = flowOf(heading)
    private val _enabled = MutableStateFlow(enabled)
    private val _heading = MutableStateFlow(heading)
    fun bannerEnabledFlow(): Flow<Boolean> = _enabled.asStateFlow()
    fun lastHeadingDegFlow(): Flow<Double> = _heading.asStateFlow()
}
