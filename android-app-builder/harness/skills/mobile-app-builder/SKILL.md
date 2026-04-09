---
name: mobile-app-builder
description: "안드로이드 앱 개발 풀 파이프라인. 요구사항 정리→Material 3 UX 설계→Kotlin/Jetpack Compose 구조 설계→API 연동 명세→Google Play 배포 문서→QA 검증까지 에이전트 팀이 협업하여 수행한다. '안드로이드 앱 만들어줘', 'Jetpack Compose 앱 설계', '앱 API 연동 문서', 'Google Play 배포 문서 작성' 같은 요청에 사용한다. 실제 Gradle 빌드, 서명, Play Console 제출, CI/CD 구축은 범위에 포함하지 않는다."
---

# Mobile App Builder — Android 앱 개발 풀 파이프라인

안드로이드 앱의 UX 설계, 구조 정의, API 연동 문서화, Google Play 배포 준비를 한 흐름으로 정리한다.

## 실행 모드

**에이전트 팀** — 5명이 직접 역할을 나눠 교차 검증한다.

## 에이전트 구성

| 에이전트 | 파일 | 역할 | 타입 |
|---------|------|------|------|
| ux-designer | `harness/agents/ux-designer.md` | Android UX/UI 설계, Material 3 화면 구조, 상태 정의 | general-purpose |
| app-developer | `harness/agents/app-developer.md` | Kotlin/Jetpack Compose 구조와 코드 스캐폴드 설계 | general-purpose |
| api-integrator | `harness/agents/api-integrator.md` | Retrofit/OkHttp 계층, 인증, 캐싱, 오프라인 전략 | general-purpose |
| store-manager | `harness/agents/store-manager.md` | Google Play 메타데이터, 정책 체크, 출시 전략 | general-purpose |
| qa-engineer | `harness/agents/qa-engineer.md` | Android 품질 검증, 정합성 확인 | general-purpose |

## 워크플로우

### Phase 1: 준비

1. 사용자 입력에서 추출한다:
   - **앱 유형**: 생산성, 커머스, 커뮤니티, 유틸리티 등
   - **대상 플랫폼**: Android
   - **기본 기술 스택**: Kotlin, Jetpack Compose, Material 3
   - **추가 요구사항**: 로그인, 푸시, 오프라인, 결제, 위치, 카메라 등
   - **백엔드 API** (선택): 기존 명세 또는 샘플 응답
   - **기존 파일** (선택): 디자인, 기획서, API 문서, 기존 코드
2. `_workspace/` 디렉토리를 프로젝트 루트에 생성한다
3. 입력을 정리하여 `_workspace/00_input.md`에 저장한다
4. 기존 파일이 있으면 `_workspace/`에 복사하고 해당 단계를 건너뛴다
5. 요청 범위에 따라 실행 모드를 결정한다

### Phase 2: 팀 실행

| 순서 | 작업 | 담당 | 의존 | 산출물 |
|------|------|------|------|--------|
| 1 | UX/UI 설계 | ux-designer | 없음 | `_workspace/01_ux_design.md` |
| 2a | Android 구조/코드 생성 | app-developer | 작업 1 | `_workspace/02_android_app/`, `_workspace/02_android_architecture.md` |
| 2b | Google Play 메타데이터 | store-manager | 작업 1 | `_workspace/04_play_store_listing.md` |
| 3 | API 연동 | api-integrator | 작업 1, 2a | `_workspace/03_api_integration.md` |
| 4 | QA 검증 | qa-engineer | 작업 2a, 2b, 3 | `_workspace/05_qa_report.md` |

작업 2a와 2b는 병렬 실행한다.

## 팀원 간 소통 흐름

- ux-designer → app-developer: 화면 구조, 상태별 UI, Material 3 토큰 전달
- ux-designer → store-manager: 스크린샷 시나리오와 핵심 가치 문구 전달
- ux-designer → api-integrator: 화면별 필요 데이터 필드 전달
- app-developer → api-integrator: Repository 인터페이스, 데이터 모델 초안 전달
- app-developer → store-manager: 권한 목록, 기능 요약, 최소 SDK 전달
- api-integrator → app-developer: API 클라이언트 구조와 에러 타입 전달
- qa-engineer → 전원: 필수 수정 사항을 최대 2회까지 환류

### Phase 3: 최종 정리

- UX 설계 — `01_ux_design.md`
- Android 아키텍처 — `02_android_architecture.md`
- Android 코드 — `02_android_app/`
- API 연동 — `03_api_integration.md`
- Google Play 배포 — `04_play_store_listing.md`
- QA 보고서 — `05_qa_report.md`

## 작업 규모별 모드

| 사용자 요청 패턴 | 실행 모드 | 투입 에이전트 |
|----------------|----------|-------------|
| "안드로이드 앱 만들어줘", "Compose 앱 기획부터 문서화해줘" | 풀 파이프라인 | 5명 전원 |
| "앱 UX만 설계해줘" | UX 모드 | ux-designer + qa-engineer |
| "이 요구사항으로 Android 구조 문서 만들어줘" | 코드 모드 | app-developer + api-integrator + qa-engineer |
| "Google Play 배포 문서 준비해줘" | 스토어 모드 | store-manager + qa-engineer |
| "이 안드로이드 앱 문서 검토해줘" | 리뷰 모드 | qa-engineer 단독 |

## 에러 핸들링

| 에러 유형 | 전략 |
|----------|------|
| 플랫폼 미지정 | Android 네이티브를 기본 선택하고 이유를 `00_input.md`에 기록 |
| UI 스택 미지정 | Jetpack Compose + Material 3를 기본 선택 |
| 백엔드 API 없음 | Mock API 설계와 실제 API 교체 포인트를 함께 제안 |
| QA에서 필수 이슈 발견 | 해당 담당자에게 재작업 요청 후 재검증 |
| Android 제약 충돌 | 최소 SDK, 권한, 백그라운드 제한을 기준으로 대안 제시 |

## 테스트 시나리오

### 정상 흐름

**프롬프트**: "할 일 관리 안드로이드 앱을 만들어줘. 할 일 추가/삭제/완료 체크, 카테고리 분류, 알림 기능이 필요해"

**기대 결과**:
- UX 설계: 5개 화면, 네비게이션 구조, Material 3 토큰
- 앱 구조: Kotlin + Jetpack Compose + Hilt + Room + Navigation Compose
- API 연동: CRUD 엔드포인트, 인증, 로컬 캐시, 오프라인 큐 전략
- 스토어: Google Play 메타데이터와 스크린샷 시나리오
- QA: 정합성 매트릭스와 수정 권고

## 에이전트별 확장 스킬

| 스킬 | 대상 에이전트 | 역할 |
|------|-------------|------|
| `mobile-ux-patterns` | ux-designer | Material 3, Navigation Compose, 디자인 토큰, 접근성 패턴 |
| `app-store-optimization` | store-manager | Google Play 메타데이터 최적화, 키워드 전략, 심사 대응 |
