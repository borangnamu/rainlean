package com.rainlean.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

object NotificationChannels {

    const val CHANNEL_UMBRELLA_BANNER = "umbrella_banner"
    const val BANNER_NOTIFICATION_ID = 1001

    fun register(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(CHANNEL_UMBRELLA_BANNER) != null) return

        val channel = NotificationChannel(
            CHANNEL_UMBRELLA_BANNER,
            "우산 방향 배너",
            NotificationManager.IMPORTANCE_LOW   // 무음·무진동 — 자주 갱신해도 사용자 방해 없음
        ).apply {
            description = "빗방향에 맞는 우산 기울임 방향을 배너로 표시합니다."
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
        }
        manager.createNotificationChannel(channel)
    }
}
