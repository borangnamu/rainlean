package com.rainlean.notification

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import com.rainlean.data.repository.WeatherRepository
import com.rainlean.domain.usecase.ComputeUmbrellaTiltUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * WorkManager가 15분 주기로 실행하는 날씨 조회 Worker.
 *
 * 1. 마지막 알려진 위치 조회 (lastLocation)
 * 2. WeatherRepository.getNow() 호출
 * 3. UmbrellaGuidanceCache 갱신
 *
 * 일시적 실패 시 Result.retry() (최대 3회 재시도, 지수 백오프).
 */
@HiltWorker
class WeatherRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val computeUmbrellaTiltUseCase: ComputeUmbrellaTiltUseCase,
    private val cache: UmbrellaGuidanceCache,
    private val bannerPreferences: BannerPreferences
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "WeatherRefreshWorker"
        private const val DEFAULT_LAT = 37.5665
        private const val DEFAULT_LON = 126.9780
    }

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: 날씨 갱신 시작")

        // 배너가 비활성화됐으면 조기 종료
        val bannerEnabled = bannerPreferences.bannerEnabled.first()
        if (!bannerEnabled) {
            Log.d(TAG, "doWork: 배너 비활성 — 작업 취소")
            return Result.success()
        }

        // 위치 조회
        val (lat, lon) = try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(applicationContext)
            val location = Tasks.await(fusedClient.lastLocation)
            if (location != null) {
                location.latitude to location.longitude
            } else {
                DEFAULT_LAT to DEFAULT_LON
            }
        } catch (e: Exception) {
            Log.w(TAG, "위치 조회 실패, 기본값 사용: ${e.message}")
            DEFAULT_LAT to DEFAULT_LON
        }

        // 날씨 조회
        val weatherResult = runCatching { weatherRepository.getNow(lat, lon) }
        if (weatherResult.isFailure) {
            Log.w(TAG, "날씨 조회 실패 (재시도 ${runAttemptCount + 1}/3): ${weatherResult.exceptionOrNull()?.message}")
            return if (runAttemptCount < 2) Result.retry() else Result.failure()
        }

        val weather = weatherResult.getOrThrow()
        val heading = bannerPreferences.lastHeadingDeg.first()
        val guidance = computeUmbrellaTiltUseCase.execute(weather, heading)

        cache.setGuidance(guidance, weather)
        Log.d(TAG, "doWork: 캐시 갱신 완료 — 강수=${weather.precipitationMmPerHour}mm/h")

        return Result.success()
    }
}
