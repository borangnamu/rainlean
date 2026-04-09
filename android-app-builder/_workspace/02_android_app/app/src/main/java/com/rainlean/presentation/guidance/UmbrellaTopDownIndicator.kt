package com.rainlean.presentation.guidance

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * 위에서 내려다본 우산 기울임 방향 인디케이터 (2D Canvas).
 *
 * - 화면 위쪽(12시) = 사용자가 현재 바라보는 방향 (항상 고정)
 * - 화살표 = 바람이 불어오는 방향, 즉 우산을 기울여야 할 방향 ([relativeDirectionDeg])
 * - 캐노피 원의 오프셋 = 기울임 정도 ([tiltDeg])
 *
 * @param relativeDirectionDeg 사용자 정면 기준 바람 방향 (0°=정면/12시, 90°=오른쪽/3시)
 * @param tiltDeg              권장 기울임 각도 (8°~45°)
 */
@Composable
fun UmbrellaTopDownIndicator(
    relativeDirectionDeg: Double,
    tiltDeg: Double,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = minOf(cx, cy) * 0.88f

        // ── 1. 배경 원 ────────────────────────────────────────────────────────
        drawCircle(
            color = surfaceVariantColor,
            radius = radius,
            center = Offset(cx, cy)
        )

        // ── 2. 시계 눈금 (12개) ──────────────────────────────────────────────
        for (i in 0 until 12) {
            // 시계 각도: 0° = 12시(위), 90° = 3시(오른쪽), 시계방향
            // 캔버스 각도: -90° 보정 (캔버스 0° = 오른쪽)
            val canvasRad = Math.toRadians(i * 30.0 - 90.0)
            val isMajor = i % 3 == 0
            val innerR = if (isMajor) radius * 0.81f else radius * 0.88f
            val outerR = radius * 0.95f
            drawLine(
                color = onSurfaceColor.copy(alpha = if (isMajor) 0.45f else 0.20f),
                start = Offset(
                    cx + (innerR * cos(canvasRad)).toFloat(),
                    cy + (innerR * sin(canvasRad)).toFloat()
                ),
                end = Offset(
                    cx + (outerR * cos(canvasRad)).toFloat(),
                    cy + (outerR * sin(canvasRad)).toFloat()
                ),
                strokeWidth = if (isMajor) 3f else 1.5f
            )
        }

        // ── 3. 시 레이블 (12 / 3 / 6 / 9) ───────────────────────────────────
        val labelStyle = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = onSurfaceColor.copy(alpha = 0.70f)
        )
        listOf(0f to "12", 90f to "3", 180f to "6", 270f to "9").forEach { (clockDeg, label) ->
            val canvasRad = Math.toRadians(clockDeg.toDouble() - 90.0)
            val labelR = radius * 0.67f
            val lx = cx + (labelR * cos(canvasRad)).toFloat()
            val ly = cy + (labelR * sin(canvasRad)).toFloat()
            val measured = textMeasurer.measure(label, labelStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                style = labelStyle,
                topLeft = Offset(
                    lx - measured.size.width / 2f,
                    ly - measured.size.height / 2f
                )
            )
        }

        // relativeDirectionDeg → 캔버스 방향 변환
        // relativeDirectionDeg: 0°=위(12시), 90°=오른쪽(3시), 시계방향
        val dirRad = Math.toRadians(relativeDirectionDeg - 90.0)

        // ── 4. 캐노피 원 (기울임 방향으로 오프셋) ────────────────────────────
        // tiltDeg(8~45°)를 0.0~1.0 으로 정규화해 오프셋 비율에 반영
        val tiltNorm = ((tiltDeg - 8.0) / (45.0 - 8.0)).coerceIn(0.0, 1.0).toFloat()
        val canopyOffset = tiltNorm * radius * 0.30f
        val canopyR = radius * 0.40f
        val canopyX = cx + (canopyOffset * cos(dirRad)).toFloat()
        val canopyY = cy + (canopyOffset * sin(dirRad)).toFloat()

        drawCircle(
            color = primaryColor.copy(alpha = 0.14f),
            radius = canopyR,
            center = Offset(canopyX, canopyY)
        )
        drawCircle(
            color = primaryColor.copy(alpha = 0.58f),
            radius = canopyR,
            center = Offset(canopyX, canopyY),
            style = Stroke(width = 2.5f)
        )

        // ── 5. 기울임 방향 화살표 ────────────────────────────────────────────
        val arrowLen = radius * 0.50f
        val tipX = cx + (arrowLen * cos(dirRad)).toFloat()
        val tipY = cy + (arrowLen * sin(dirRad)).toFloat()

        drawLine(
            color = primaryColor,
            start = Offset(cx, cy),
            end = Offset(tipX, tipY),
            strokeWidth = 4.5f,
            cap = StrokeCap.Round
        )

        val headSize = radius * 0.11f
        val headPath = Path().apply {
            moveTo(tipX, tipY)
            lineTo(
                (tipX + headSize * cos(dirRad + Math.toRadians(145.0))).toFloat(),
                (tipY + headSize * sin(dirRad + Math.toRadians(145.0))).toFloat()
            )
            lineTo(
                (tipX + headSize * cos(dirRad - Math.toRadians(145.0))).toFloat(),
                (tipY + headSize * sin(dirRad - Math.toRadians(145.0))).toFloat()
            )
            close()
        }
        drawPath(headPath, color = primaryColor)

        // ── 6. 손잡이 점 (중심) ──────────────────────────────────────────────
        drawCircle(
            color = onSurfaceColor.copy(alpha = 0.75f),
            radius = 6f,
            center = Offset(cx, cy)
        )
    }
}
