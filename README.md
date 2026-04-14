# RainLean

비 오는 날 우산을 어느 방향으로 기울일지 안내하는 Android 앱.

## 주요 기능

- **실시간 우산 방향 안내** — 현재 위치의 강수/풍향 데이터를 기반으로 우산 기울임 방향(각도) 즉시 제공
- **3D 우산 시각화** — Filament(SceneView) + GLB 모델을 통한 직관적 방향 표시
- **배너 알림 (Background Banner)** — 앱 내 토글 활성화 시, 잠금 화면/알림 바에 우산 이모지(☂)를 방향에 맞게 회전시켜 표시
  - 날씨 데이터: 15분 주기 자동 갱신 (WorkManager)
  - 알림 표시: 5초마다 방향 갱신
  - Android 13+ 기기에서 알림 권한(`POST_NOTIFICATIONS`) 필요
- **온디바이스 처리** — 위치/센서 데이터는 기기 내부에서만 처리, 외부 전송은 날씨 API 좌표 조회에 한정
- **이중 날씨 소스** — 기상청(KMA) 초단기실況 우선, Open-Meteo 폴백

## 기술 스택

- Kotlin + Jetpack Compose + Material 3
- Hilt (DI), Retrofit + OkHttp, WorkManager, DataStore
- Fused Location Provider + Rotation Vector Sensor
- Filament / SceneView (3D 렌더링)
- minSdk 26 / targetSdk 35

## 빌드

```properties
# local.properties
sdk.dir=C\:\\Android\\Sdk
KMA_SERVICE_KEY=your_kma_service_key_encoded
```

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
```

생성 APK: `app/build/outputs/apk/debug/app-debug.apk`

## 권한

| 권한 | 용도 |
|---|---|
| `ACCESS_FINE_LOCATION` | 현재 위치 기반 날씨 조회 |
| `ACCESS_COARSE_LOCATION` | 대략 위치 폴백 |
| `POST_NOTIFICATIONS` | 배너 알림 표시 (Android 13+, 런타임 요청) |
| `FOREGROUND_SERVICE` | 배너 알림 포그라운드 서비스 |
| `RECEIVE_BOOT_COMPLETED` | 재부팅 후 배너 알림 재등록 |
