package com.rainlean.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 기기 재부팅 / 앱 업데이트 후 배너 알림 WorkManager를 재등록.
 *
 * BOOT_COMPLETED 와 MY_PACKAGE_REPLACED 두 인텐트 모두 수신.
 * DataStore의 banner_enabled 가 true 일 때만 서비스와 Worker를 재시작.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var bannerPreferences: BannerPreferences

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        Log.d("BootReceiver", "수신: $action")

        val pendingResult = goAsync()
        scope.launch {
            try {
                val enabled = bannerPreferences.bannerEnabled.first()
                if (enabled) {
                    Log.d("BootReceiver", "배너 활성 — WorkManager 재등록")
                    WeatherRefreshScheduler.schedule(context)
                    val serviceIntent = Intent(context, UmbrellaBannerService::class.java)
                    ContextCompat.startForegroundService(context, serviceIntent)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
