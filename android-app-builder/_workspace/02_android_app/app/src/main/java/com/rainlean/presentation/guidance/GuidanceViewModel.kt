package com.rainlean.presentation.guidance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainlean.data.repository.WeatherRepository
import com.rainlean.domain.model.UmbrellaGuidance
import com.rainlean.domain.model.WeatherSnapshot
import com.rainlean.domain.model.WeatherSource
import com.rainlean.domain.usecase.ComputeUmbrellaTiltUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.math.max
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val AUTO_REFRESH_INTERVAL_MS = 15 * 60 * 1000L // 15분

data class GuidanceUiState(
    val isLoading: Boolean = false,
    val guidance: UmbrellaGuidance? = null,
    val weatherSnapshot: WeatherSnapshot? = null,
    val message: String? = null,
    val isForcedTestMode: Boolean = false,
    val isLocationFallback: Boolean = false,
    val lastUpdatedEpochSec: Long? = null,
    val devicePose: DevicePoseUi = DevicePoseUi()
)

data class DevicePoseUi(
    val pitchDeg: Double = 0.0,
    val rollDeg: Double = 0.0,
    val azimuthDeg: Double = 0.0,
    val isFlat: Boolean = false,
    val isShaking: Boolean = false
)

@HiltViewModel
class GuidanceViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val computeUmbrellaTiltUseCase: ComputeUmbrellaTiltUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuidanceUiState())
    val uiState: StateFlow<GuidanceUiState> = _uiState.asStateFlow()

    private var autoRefreshJob: Job? = null

    fun updateDevicePose(
        pitchDeg: Double,
        rollDeg: Double,
        azimuthDeg: Double,
        isFlat: Boolean,
        isShaking: Boolean
    ) {
        _uiState.value = _uiState.value.copy(
            devicePose = DevicePoseUi(
                pitchDeg = pitchDeg,
                rollDeg = rollDeg,
                azimuthDeg = azimuthDeg,
                isFlat = isFlat,
                isShaking = isShaking
            )
        )
    }

    /**
     * 위치 확보 후 호출. 15분마다 자동으로 날씨를 다시 조회한다.
     * [headingProvider]는 매 갱신 시점마다 최신 방위각을 반환하는 람다로,
     * 앱 사용 중 기기 방향이 바뀌어도 올바른 상대 방향을 계산한다.
     */
    fun startAutoRefresh(
        lat: Double,
        lon: Double,
        isLocationFallback: Boolean,
        headingProvider: () -> Double
    ) {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                refresh(lat, lon, headingProvider(), isLocationFallback = isLocationFallback)
                delay(AUTO_REFRESH_INTERVAL_MS)
            }
        }
    }

    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    fun refresh(
        lat: Double,
        lon: Double,
        userHeadingDeg: Double,
        forceGuidanceForDryWeather: Boolean = false,
        isLocationFallback: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)

            val weatherResult = if (forceGuidanceForDryWeather) {
                Result.success(
                    WeatherSnapshot(
                        precipitationMmPerHour = 1.0,
                        windDirectionFromDeg = 0.0,
                        windSpeedMps = 3.0,
                        observedAtEpochSec = System.currentTimeMillis() / 1000,
                        source = WeatherSource.CACHE
                    )
                )
            } else {
                runCatching { weatherRepository.getNow(lat, lon) }
            }

            if (weatherResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "데이터를 불러오지 못했습니다. 네트워크와 권한을 확인해 주세요."
                )
                return@launch
            }

            val weather = weatherResult.getOrThrow()
            val weatherForGuidance = weather.withForcedRainIfNeeded(forceGuidanceForDryWeather)
            val guidance = computeUmbrellaTiltUseCase.execute(weatherForGuidance, userHeadingDeg)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                guidance = guidance,
                weatherSnapshot = weather,
                isLocationFallback = isLocationFallback,
                lastUpdatedEpochSec = System.currentTimeMillis() / 1000,
                isForcedTestMode = forceGuidanceForDryWeather,
                message = when {
                    forceGuidanceForDryWeather ->
                        "테스트 모드: 샘플 데이터로 안내를 표시 중입니다."
                    else -> null
                }
            )
        }
    }
}

private fun WeatherSnapshot.withForcedRainIfNeeded(forceGuidanceForDryWeather: Boolean): WeatherSnapshot {
    if (!forceGuidanceForDryWeather || precipitationMmPerHour >= 0.1) return this
    return copy(precipitationMmPerHour = max(precipitationMmPerHour, 1.0))
}
