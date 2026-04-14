package com.rainlean.presentation.guidance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainlean.data.repository.WeatherRepository
import com.rainlean.domain.model.UmbrellaGuidance
import com.rainlean.domain.model.WeatherSnapshot
import com.rainlean.domain.model.WeatherSource
import com.rainlean.domain.usecase.ComputeUmbrellaTiltUseCase
import com.rainlean.notification.BannerPreferences
import com.rainlean.notification.UmbrellaGuidanceCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.math.max
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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
    val devicePose: DevicePoseUi = DevicePoseUi(),
    val showPermissionRationale: Boolean = false
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
    private val computeUmbrellaTiltUseCase: ComputeUmbrellaTiltUseCase,
    private val cache: UmbrellaGuidanceCache,
    private val bannerPreferences: BannerPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuidanceUiState())
    val uiState: StateFlow<GuidanceUiState> = _uiState.asStateFlow()

    /** DataStore 기반 배너 활성 여부. UI에서 Switch 상태로 사용. */
    val bannerEnabled: StateFlow<Boolean> = bannerPreferences.bannerEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

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
        // 배너 서비스를 위해 캐시에도 방위각 업데이트
        cache.setHeading(azimuthDeg)
    }

    fun setShowPermissionRationale(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPermissionRationale = show)
    }

    /**
     * 위치 확보 후 호출. 15분마다 자동으로 날씨를 다시 조회한다.
     * [headingProvider]는 매 갱신 시점마다 최신 방위각을 반환하는 람다.
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

            // 배너 서비스가 읽을 수 있도록 캐시 갱신
            cache.setGuidance(guidance, weather)

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
