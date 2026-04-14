package com.rainlean.notification

import com.rainlean.domain.model.UmbrellaGuidance
import com.rainlean.domain.model.WeatherSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Worker ↔ Service ↔ ViewModel 사이의 인메모리 공유 상태.
 * WorkManager 15분 주기 조회 결과와 MainActivity 센서 헤딩을 여기에 기록한다.
 * UmbrellaBannerService가 5초마다 이 값을 읽어 알림을 갱신한다.
 */
@Singleton
class UmbrellaGuidanceCache @Inject constructor() {

    private val _guidance = MutableStateFlow<UmbrellaGuidance?>(null)
    val guidance: StateFlow<UmbrellaGuidance?> = _guidance.asStateFlow()

    private val _weather = MutableStateFlow<WeatherSnapshot?>(null)
    val weather: StateFlow<WeatherSnapshot?> = _weather.asStateFlow()

    private val _headingDeg = MutableStateFlow(0.0)
    val headingDeg: StateFlow<Double> = _headingDeg.asStateFlow()

    fun setGuidance(guidance: UmbrellaGuidance?, weather: WeatherSnapshot?) {
        _guidance.value = guidance
        _weather.value = weather
    }

    fun setHeading(deg: Double) {
        _headingDeg.value = deg
    }
}
