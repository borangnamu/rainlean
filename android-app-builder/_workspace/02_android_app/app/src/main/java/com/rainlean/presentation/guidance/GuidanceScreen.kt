package com.rainlean.presentation.guidance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rainlean.domain.model.Confidence
import com.rainlean.domain.model.WeatherSnapshot

@Composable
fun GuidanceScreen(
    viewModel: GuidanceViewModel,
    onRefresh: () -> Unit,
    onForceRefreshForDryWeather: () -> Unit,
    onBannerToggle: (Boolean) -> Unit,
    onOpenNotificationSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val bannerEnabled by viewModel.bannerEnabled.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 권한 거부 시 Snackbar 표시
    LaunchedEffect(state.showPermissionRationale) {
        if (state.showPermissionRationale) {
            val result = snackbarHostState.showSnackbar(
                message = "알림 권한이 필요합니다",
                actionLabel = "설정 열기"
            )
            if (result == SnackbarResult.ActionPerformed) {
                onOpenNotificationSettings()
            }
            viewModel.setShowPermissionRationale(false)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "RainLean",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "위치와 기기 방향으로 우산 각도를 안내합니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                StatusSection(state = state)
                PrimaryGuidanceSection(state = state)
                BannerSection(
                    bannerEnabled = bannerEnabled,
                    onToggle = onBannerToggle
                )
                ActionSection(
                    onRefresh = onRefresh,
                    onForceRefreshForDryWeather = onForceRefreshForDryWeather
                )
            }
        }
    }
}

// ─── Banner Section ───────────────────────────────────────────────────────────

@Composable
private fun BannerSection(
    bannerEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "배너 알림",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (bannerEnabled) "활성화됨 — 5초마다 방향 갱신"
                               else "비활성화됨",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = bannerEnabled,
                    onCheckedChange = onToggle
                )
            }
            Text(
                text = "잠금 화면/알림창에 우산 방향(☂)을 표시합니다.\n날씨는 15분마다 자동 갱신됩니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Status Section ───────────────────────────────────────────────────────────

@Composable
private fun StatusSection(state: GuidanceUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "상태",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                }
            }

            if (state.guidance != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("강수 감지됨")
                    ConfidenceBadge(state.guidance.confidence)
                }
            } else if (!state.isLoading) {
                Text("현재 우산이 필요 없는 날씨입니다.")
            }

            if (state.isLocationFallback) {
                Text(
                    "위치를 가져오지 못해 서울 기준으로 계산합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (state.guidance != null && state.devicePose.isFlat) {
                Text(
                    "기기를 세워서 들면 방향 감지가 더 정확합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (state.guidance != null && state.devicePose.isShaking) {
                Text(
                    "흔들림 감지 — 방향을 잠시 안정화합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            state.message?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }

            state.lastUpdatedEpochSec?.let { epochSec ->
                var minutesAgo by remember(epochSec) { mutableIntStateOf(0) }
                LaunchedEffect(epochSec) {
                    while (true) {
                        minutesAgo = ((System.currentTimeMillis() / 1000 - epochSec) / 60).toInt()
                        delay(60_000L)
                    }
                }
                val label = if (minutesAgo < 1) "방금 갱신" else "${minutesAgo}분 전 갱신"
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: Confidence) {
    val (label, color) = when (confidence) {
        Confidence.HIGH -> "신뢰도 높음" to MaterialTheme.colorScheme.tertiary
        Confidence.MEDIUM -> "신뢰도 보통" to MaterialTheme.colorScheme.secondary
        Confidence.LOW -> "신뢰도 낮음" to MaterialTheme.colorScheme.outline
    }
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

// ─── Primary Guidance Section ─────────────────────────────────────────────────

@Composable
private fun PrimaryGuidanceSection(state: GuidanceUiState) {
    val guidance = state.guidance
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "우산 안내",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (guidance == null) {
                NoRainCard(snapshot = state.weatherSnapshot)
                return@Column
            }

            Text(
                text = "기울임 각도: ${guidance.tiltDeg.toInt()}°",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "방향: ${toClockDirectionLabel(guidance.relativeDirectionDeg)}  (${guidance.relativeDirectionDeg.toInt()}°)",
                style = MaterialTheme.typography.bodyMedium
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                UmbrellaTopDownIndicator(
                    relativeDirectionDeg = guidance.relativeDirectionDeg,
                    tiltDeg = guidance.tiltDeg,
                    modifier = Modifier.size(210.dp)
                )
            }

            Text(
                text = "실시간 기울기: pitch ${state.devicePose.pitchDeg.toInt()}°  roll ${state.devicePose.rollDeg.toInt()}°",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoRainCard(snapshot: WeatherSnapshot?) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "비가 감지되면 기울임 방향 안내가 표시됩니다.",
            style = MaterialTheme.typography.bodyMedium
        )
        if (snapshot != null) {
            Text(
                "현재 풍속: ${snapshot.windSpeedMps.toInt()} m/s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "현재 풍향: ${snapshot.windDirectionFromDeg.toInt()}° 방향에서",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Action Section ───────────────────────────────────────────────────────────

@Composable
private fun ActionSection(
    onRefresh: () -> Unit,
    onForceRefreshForDryWeather: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "동작",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onRefresh, modifier = Modifier.weight(1f)) {
                    Text("지금 갱신")
                }
                OutlinedButton(
                    onClick = onForceRefreshForDryWeather,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("건조 날씨 테스트")
                }
            }
            Text(
                text = "날씨는 15분마다 자동으로 갱신됩니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
