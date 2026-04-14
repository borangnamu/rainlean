# 02 Android 아키텍처 문서

## 기술 스택
- 플랫폼: Android (온디바이스 동작)
- 언어: Kotlin
- UI: Jetpack Compose + Material 3
- 상태 관리: ViewModel + StateFlow
- 네비게이션: Navigation Compose
- 로컬 저장: Room(이력), DataStore(설정·배너 상태·마지막 방위각)
- 네트워크: Retrofit + OkHttp + Kotlinx Serialization
- DI: Hilt + Hilt-Work
- 위치/센서: Fused Location Provider + Rotation Vector Sensor
- **백그라운드/알림**: WorkManager (15분 날씨 갱신), ForegroundService (5초 배너 갱신), NotificationCompat
- 최소/타깃 SDK: minSdk 26, targetSdk 35

## 프로젝트 구조
`app/src/main/java/.../`
- `core/` 공통 유틸, 수학 계산, 시간/권한 유틸
- `data/` API DTO, Repository 구현, 로컬 DB
- `domain/` 엔티티, 유스케이스, 점수화 로직
- `presentation/` Compose UI, ViewModel, 상태 모델
- `di/` Hilt 모듈
- `notification/` 배너 알림 레이어 (아래 참조)

### `notification/` 패키지 구성
| 파일 | 역할 |
|---|---|
| `UmbrellaBannerService.kt` | Foreground Service — 5초 틱으로 알림 갱신 |
| `WeatherRefreshWorker.kt` | Hilt CoroutineWorker — 15분 주기 날씨 조회 후 캐시 갱신 |
| `UmbrellaNotificationBuilder.kt` | NotificationCompat 빌더 — 제목/본문/아이콘 조립 |
| `UmbrellaIconRenderer.kt` | Canvas + Matrix로 우산 이모지 회전 → Bitmap |
| `NotificationChannels.kt` | 앱 시작 시 `umbrella_banner` 채널 등록 |
| `BootReceiver.kt` | 재부팅 후 WorkManager 재등록 |
| `UmbrellaGuidanceCache.kt` | Worker ↔ Service ↔ ViewModel 공유 StateFlow 싱글톤 |
| `WeatherRefreshScheduler.kt` | WorkManager 예약 헬퍼 (토글 ON/OFF, BootReceiver 공용) |

## 최적 모델(알고리즘) 선정
### 선택
- 주 모델: 물리 기반 휴리스틱 모델(강수량 + 풍향/풍속 + 사용자 헤딩)

### 선정 이유
- 서버/학습 인프라 없이 온디바이스 구현 가능
- 설명 가능성(왜 이 방향인지 사용자에게 설명 가능)
- 배터리/지연 시간 측면에서 경량

## 방향 계산 로직
1. 날씨 API에서 `wind direction (from)`/`wind speed`/`precipitation` 수집
2. 빗줄기 유입 방향 = `wind_from_direction` (바람이 불어오는 쪽)
3. 사용자 기준 상대 방향: `relative = normalize(wind_from_direction - user_heading)`
4. 우산 기울임 각도(권장): `tilt_deg = clamp(6 + 1.8 * wind_mps + 1.2 * rain_mmph, 6, 30)`
5. 강수 임계치 미만이면 `NoRain` 상태로 전환

## 주요 유스케이스
- `ComputeUmbrellaTiltUseCase`: 방향/각도 계산

## 권한 목록
| 권한 | 선언 방식 | 비고 |
|---|---|---|
| `ACCESS_FINE_LOCATION` | Manifest + 런타임 | 위치 기반 날씨 조회 |
| `ACCESS_COARSE_LOCATION` | Manifest + 런타임 | 폴백 |
| `INTERNET` | Manifest | 날씨 API 호출 |
| `POST_NOTIFICATIONS` | Manifest + **런타임**(Android 13+) | 배너 알림 표시, 거부 시 앱 설정 안내 |
| `FOREGROUND_SERVICE` | Manifest | 포그라운드 서비스 |
| `FOREGROUND_SERVICE_DATA_SYNC` | Manifest (Android 14+) | 서비스 타입 명시 |
| `RECEIVE_BOOT_COMPLETED` | Manifest | 재부팅 후 WorkManager 재등록 |
| `WAKE_LOCK` | (WorkManager 내부 암묵적) | 문서화 목적 |

## 배너 알림 갱신 주기 설계
- **날씨 데이터 갱신**: 15분 (WorkManager `PeriodicWorkRequest`)  
  - 앱이 닫혀있어도 동작, Doze 모드에서 약간 지연 허용
  - 네트워크 연결 필요 (`setRequiredNetworkType(CONNECTED)`)
- **배너 표시 갱신**: 5초 (`UmbrellaBannerService` 코루틴 틱)  
  - 최신 `UmbrellaGuidanceCache`와 마지막 방위각으로 아이콘/본문 다시 렌더링
  - `setOnlyAlertOnce(true)` + `IMPORTANCE_LOW`로 소리/진동 없이 업데이트
- **방위각(헤딩) 갱신**: 1초 (Rotation Vector Sensor, MainActivity 활성 시)  
  - 백그라운드 시 DataStore에 저장된 마지막 방위각 사용

## 에러 처리 전략
| 에러 유형 | 처리 방식 | UI 피드백 |
|---|---|---|
| 위치 권한 거부 | 기능 제한 모드 | 권한 요청 CTA |
| 날씨 API 실패 | 캐시 폴백 + 재시도(최대 3회) | 데이터 갱신 실패 배너 |
| 센서 품질 저하 | 방향 회전 안정화 | 신뢰도 낮음 표시 |
| `POST_NOTIFICATIONS` 거부 | 토글 OFF 유지 | Snackbar + "설정 열기" 액션 |
| `POST_NOTIFICATIONS` 영구 거부 | 설정 화면 링크 | `ACTION_APP_NOTIFICATION_SETTINGS` |
