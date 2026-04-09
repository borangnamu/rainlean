# 02 Android 아키텍처 문서

## 기술 스택
- 플랫폼: Android (온디바이스 동작)
- 언어: Kotlin
- UI: Jetpack Compose + Material 3
- 상태 관리: ViewModel + StateFlow
- 네비게이션: Navigation Compose
- 로컬 저장: Room(이력), DataStore(설정)
- 네트워크: Retrofit + OkHttp + Kotlinx Serialization
- DI: Hilt
- 위치/센서: Fused Location Provider + Rotation Vector Sensor
- 최소/타깃 SDK: minSdk 26, targetSdk 35

## 프로젝트 구조
`app/src/main/java/.../`
- `core/` 공통 유틸, 수학 계산, 시간/권한 유틸
- `data/` API DTO, Repository 구현, 로컬 DB
- `domain/` 엔티티, 유스케이스, 점수화 로직
- `presentation/` Compose UI, ViewModel, 상태 모델
- `di/` Hilt 모듈

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

## 에러 처리 전략
| 에러 유형 | 처리 방식 | UI 피드백 |
|---|---|---|
| 위치 권한 거부 | 기능 제한 모드 | 권한 요청 CTA |
| 날씨 API 실패 | 캐시 폴백 + 재시도 | 데이터 갱신 실패 배너 |
| 센서 품질 저하 | 방향 회전 안정화 | 신뢰도 낮음 표시 |
