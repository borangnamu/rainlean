# 03 API 연동 명세

## 원칙
- 앱 서버는 두지 않는다.
- 단말에서 직접 외부 API 호출한다.
- API 키는 `BuildConfig`/`local.properties`로 주입한다.

## API 후보 비교
| 구분 | 후보 | 장점 | 단점 | 채택 |
|---|---|---|---|---|
| 날씨 | 기상청 단기/초단기(OpenAPI) | 한국 지역 정확도 기대치 높음 | 격자 변환/응답 스키마 복잡 | 1순위 |
| 날씨 | Open-Meteo Forecast API | 키 없이 빠른 연동, `precipitation`, `wind_direction_10m` 제공 | 사업 SLA/정책 검토 필요 | 2순위(폴백) |

## 최종 조합
- 강수/풍향: 기상청 OpenAPI 우선 + Open-Meteo 폴백

## 인터페이스 설계
```kotlin
interface WeatherRepository {
    suspend fun getNow(lat: Double, lon: Double): WeatherSnapshot
}
```

## API 호출 주기
- 위치/센서: 1초 주기(전면 화면일 때만)
- 날씨: 60초 주기

## 보안/운영 주의
- 완전 무서버 구조는 API 키 추출 위험이 있다.
- 난독화/키 제한(패키지명, SHA-1, IP 정책)을 적용한다.
- 향후 상용화 시에는 서버 프록시 전환을 옵션으로 남긴다.

## 데이터 모델
```kotlin
data class WeatherSnapshot(
    val precipitationMmPerHour: Double,
    val windDirectionFromDeg: Double,
    val windSpeedMps: Double,
    val observedAtEpochSec: Long,
    val source: WeatherSource
)
```
