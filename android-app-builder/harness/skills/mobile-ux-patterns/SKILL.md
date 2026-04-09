---
name: mobile-ux-patterns
description: "안드로이드 UX 설계 패턴 라이브러리. Material Design 3 가이드라인, Navigation Compose 패턴, 제스처 인터랙션, 반응형 레이아웃, 접근성 체크리스트를 제공하는 ux-designer 확장 스킬이다. Android 앱 UI/UX 설계 시 사용한다."
---

# Mobile UX Patterns — Android UX 설계 패턴 라이브러리

ux-designer 에이전트가 안드로이드 앱 UX 설계 시 활용하는 Material 3, 네비게이션, 디자인 토큰 레퍼런스.

## 대상 에이전트

`ux-designer` — 이 스킬의 UX 패턴과 가이드라인을 앱 설계에 직접 적용한다.

## Material Design 3 핵심 원칙

| 요소 | 기준 |
|------|------|
| 네비게이션 | Bottom Navigation, Navigation Rail, Top App Bar |
| 버튼 스타일 | Filled, Filled Tonal, Outlined, Text |
| 타이포 | Material 3 타입 스케일 |
| 아이콘 | Material Symbols 우선 |
| 모달 | Modal Bottom Sheet, Dialog |
| 색상 | Material You 기반 색상 체계 |
| 터치 타깃 | 최소 48x48dp |

## 네비게이션 패턴

| 패턴 | 적합 | 예시 |
|------|------|------|
| Bottom Navigation | 3~5개 주요 섹션 | 콘텐츠/커머스 앱 |
| Navigation Drawer | 6개 이상 섹션 | 설정이 많은 앱 |
| Navigation Rail | 태블릿/폴더블 | 대화면 생산성 앱 |
| Stack | 계층형 탐색 | 상세/설정 화면 |
| Bottom Sheet | 임시 선택/필터 | 지도, 미디어 앱 |

## 화면 설계 규칙

- 최대 3~4 depth를 넘기지 않는다
- 시스템 뒤로가기와 충돌하지 않게 설계한다
- Empty, Loading, Error, Success, Partial 상태를 모두 정의한다
- 실제 데이터에 가까운 문구와 예시를 사용한다

## 디자인 토큰

### 스페이싱 스케일

| 토큰 | 값 |
|------|---|
| xs | 4dp |
| sm | 8dp |
| md | 16dp |
| lg | 24dp |
| xl | 32dp |
| 2xl | 48dp |

### 접근성 체크리스트

- [ ] 터치 타깃 최소 48x48dp
- [ ] 색상 대비 4.5:1 이상
- [ ] 색상만으로 정보 전달하지 않기
- [ ] TalkBack용 라벨과 역할 설명 제공
- [ ] 동적 글꼴 크기 지원
- [ ] 모션 축소 설정 대응
- [ ] 키보드/외부 입력 지원
- [ ] 포커스 순서 논리적 배치
