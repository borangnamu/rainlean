package com.rainlean.notification

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.sin

/**
 * 우산 방향 아이콘을 relativeDirectionDeg 만큼 회전시킨 Bitmap으로 렌더링한다.
 * 알림 대형 아이콘(LargeIcon)에 사용.
 *
 * 이모지 렌더링은 OEM마다 불안정하므로, Canvas 기반의 직접 드로잉을 사용한다.
 * - 원형 배경 + 우산 실루엣(호 + 손잡이) + 방향 화살표 삼각형
 * 결과는 relativeDirectionDeg=0 이 북쪽(12시), 시계방향 양의 각도.
 */
object UmbrellaIconRenderer {

    fun render(relativeDirectionDeg: Double, sizePx: Int = 128): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val r = sizePx / 2f

        // 배경 원
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(230, 30, 120, 200) // 파란색
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, r, bgPaint)

        // 캔버스를 방향각만큼 회전 (북=0도 기준, 시계방향)
        canvas.save()
        canvas.rotate(relativeDirectionDeg.toFloat(), cx, cy)

        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.06f
            strokeCap = Paint.Cap.ROUND
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        val scale = sizePx / 128f

        // 우산 반구 (호)
        val umbrellaRect = RectF(
            cx - 38f * scale, cy - 38f * scale,
            cx + 38f * scale, cy + 12f * scale
        )
        canvas.drawArc(umbrellaRect, 180f, 180f, false, iconPaint)

        // 우산 중심 기둥
        canvas.drawLine(cx, cy - 6f * scale, cx, cy + 30f * scale, iconPaint)

        // 우산 손잡이 (J자)
        val handlePath = Path().apply {
            moveTo(cx, cy + 30f * scale)
            quadTo(cx, cy + 44f * scale, cx - 10f * scale, cy + 44f * scale)
        }
        canvas.drawPath(handlePath, iconPaint)

        // 방향 삼각형 화살표 (위 꼭짓점 → 이동 방향)
        val arrowSize = 14f * scale
        val arrowTip = cy - 48f * scale
        val arrowPath = Path().apply {
            moveTo(cx, arrowTip)
            lineTo(cx - arrowSize * 0.6f, arrowTip + arrowSize)
            lineTo(cx + arrowSize * 0.6f, arrowTip + arrowSize)
            close()
        }
        canvas.drawPath(arrowPath, fillPaint)

        canvas.restore()
        return bitmap
    }

    /**
     * NoRain 상태용 — 회전 없는 중립 아이콘.
     */
    fun renderNeutral(sizePx: Int = 128): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val r = sizePx / 2f

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 100, 140, 100) // 초록빛 — 비 없음
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, r, bgPaint)

        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.06f
            strokeCap = Paint.Cap.ROUND
        }
        val scale = sizePx / 128f

        val umbrellaRect = RectF(
            cx - 38f * scale, cy - 38f * scale,
            cx + 38f * scale, cy + 12f * scale
        )
        canvas.drawArc(umbrellaRect, 180f, 180f, false, iconPaint)
        canvas.drawLine(cx, cy - 6f * scale, cx, cy + 30f * scale, iconPaint)

        val handlePath = Path().apply {
            moveTo(cx, cy + 30f * scale)
            quadTo(cx, cy + 44f * scale, cx - 10f * scale, cy + 44f * scale)
        }
        canvas.drawPath(handlePath, iconPaint)

        return bitmap
    }
}
