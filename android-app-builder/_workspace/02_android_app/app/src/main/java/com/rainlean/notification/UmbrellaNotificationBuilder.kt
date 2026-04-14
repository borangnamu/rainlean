package com.rainlean.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.rainlean.MainActivity
import com.rainlean.R
import com.rainlean.core.DirectionMath
import com.rainlean.domain.model.Confidence
import com.rainlean.domain.model.UmbrellaGuidance
import com.rainlean.domain.model.WeatherSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UmbrellaNotificationBuilder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tapIntent: PendingIntent by lazy {
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun build(
        guidance: UmbrellaGuidance?,
        weather: WeatherSnapshot?,
        headingDeg: Double
    ): Notification {
        val relativeDir = if (guidance != null && weather != null) {
            DirectionMath.normalizeDeg(weather.windDirectionFromDeg - headingDeg)
        } else 0.0

        val largeIcon = if (guidance != null) {
            UmbrellaIconRenderer.render(relativeDir)
        } else {
            UmbrellaIconRenderer.renderNeutral()
        }

        val title = buildTitle(weather)
        val body = buildBody(guidance, relativeDir)

        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_UMBRELLA_BANNER)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setContentIntent(tapIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun buildTitle(weather: WeatherSnapshot?): String {
        if (weather == null) return "RainLean · 날씨 조회 중"
        val mm = "%.1f".format(weather.precipitationMmPerHour)
        val mps = weather.windSpeedMps.toInt()
        val timeStr = SimpleDateFormat("HH:mm", Locale.KOREAN)
            .format(Date(weather.observedAtEpochSec * 1000))
        return "강수 ${mm}mm/h · 풍속 ${mps}m/s · $timeStr"
    }

    private fun buildBody(guidance: UmbrellaGuidance?, relativeDir: Double): String {
        if (guidance == null) return "비 예보 없음 · 우산 기울임 불필요"
        val dirLabel = toDirectionLabel(relativeDir)
        val tilt = guidance.tiltDeg.toInt()
        val confidenceLabel = when (guidance.confidence) {
            Confidence.HIGH -> ""
            Confidence.MEDIUM -> " (신뢰도 보통)"
            Confidence.LOW -> " (신뢰도 낮음)"
        }
        return "우산을 $dirLabel ${tilt}° 기울이세요$confidenceLabel"
    }

    private fun toDirectionLabel(deg: Double): String {
        val normalized = DirectionMath.normalizeDeg(deg)
        return when (((normalized + 22.5) / 45).toInt() % 8) {
            0 -> "북"
            1 -> "북동"
            2 -> "동"
            3 -> "남동"
            4 -> "남"
            5 -> "남서"
            6 -> "서"
            7 -> "북서"
            else -> "?"
        }
    }
}
