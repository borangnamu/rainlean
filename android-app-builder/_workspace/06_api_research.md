# 06 API 조사 근거 (2026-04-09 기준)

## 조사 목적
- 한국 사용자 대상 앱에서 정확도/구현성/비용/무서버 제약을 만족하는 날씨 API 조합 선택

## 확인한 공식 문서
- Open-Meteo Docs: https://open-meteo.com/en/docs
- 공공데이터포털(기상청 동네예보 계열): https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15058629

## 핵심 확인 포인트
- Open-Meteo에서 현재 강수/풍향/풍속 항목 사용 가능
- 기상청 OpenAPI는 한국 상세 예보 체계를 제공(격자 변환 필요)

## 최종 선정
- 1순위 날씨: 기상청 OpenAPI
- 2순위 날씨 폴백: Open-Meteo

## 선정 이유 요약
- 한국 로컬 정확도와 사용자 체감 품질을 위해 기상청 우선
- 무서버 조건에서 초기 개발 속도를 위해 Open-Meteo 폴백 유지
