package com.rainlean.notification

import android.app.NotificationManager
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.ServiceCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 배너 알림을 5초마다 갱신하는 포그라운드 서비스.
 *
 * 생명주기:
 *  - [MainActivity] 또는 [BootReceiver] 에서 startForegroundService() 로 시작
 *  - [BannerPreferences.bannerEnabled]가 false 로 바뀌면 스스로 종료
 *  - 앱 설정에서 토글 OFF 시 [WeatherRefreshScheduler.cancel]과 함께 stopService() 호출
 */
@AndroidEntryPoint
class UmbrellaBannerService : LifecycleService() {

    @Inject lateinit var cache: UmbrellaGuidanceCache
    @Inject lateinit var notificationBuilder: UmbrellaNotificationBuilder
    @Inject lateinit var bannerPreferences: BannerPreferences

    private var renderJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        // startForeground 는 onCreate 내에서 즉시 호출해야 함 (Android 14+ 10초 제한)
        val initialNotification = notificationBuilder.build(
            guidance = cache.guidance.value,
            weather = cache.weather.value,
            headingDeg = cache.headingDeg.value
        )
        ServiceCompat.startForeground(
            this,
            NotificationChannels.BANNER_NOTIFICATION_ID,
            initialNotification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }
        )
        startRenderLoop()
        watchBannerEnabled()
    }

    /**
     * 5초마다 cache 최신 값으로 알림을 갱신.
     * setOnlyAlertOnce(true) + IMPORTANCE_LOW 이므로 사운드/진동 없음.
     */
    private fun startRenderLoop() {
        val manager = getSystemService<NotificationManager>() ?: return
        renderJob = lifecycleScope.launch {
            while (isActive) {
                delay(RENDER_INTERVAL_MS)
                val notification = notificationBuilder.build(
                    guidance = cache.guidance.value,
                    weather = cache.weather.value,
                    headingDeg = cache.headingDeg.value
                )
                manager.notify(NotificationChannels.BANNER_NOTIFICATION_ID, notification)
            }
        }
    }

    /**
     * banner_enabled 가 false 로 바뀌면 스스로 종료.
     * 이 경로는 앱 외부(예: 앱 알림 설정에서 채널 비활성화)에서의 OFF 신호용.
     */
    private fun watchBannerEnabled() {
        lifecycleScope.launch {
            bannerPreferences.bannerEnabled.collect { enabled ->
                if (!enabled) {
                    stopSelf()
                }
            }
        }
    }

    override fun onDestroy() {
        renderJob?.cancel()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    companion object {
        private const val RENDER_INTERVAL_MS = 5_000L
    }
}
