# RainLean Android Scaffold

이 디렉토리는 실제 앱 구현을 시작하기 위한 Android 프로젝트 스캐폴드다.

## 포함 내용
- 도메인 모델(`WeatherSnapshot`, `UmbrellaGuidance`)
- 우산 방향/각도 계산 유스케이스
- 메인 화면 ViewModel 뼈대

## 준비
`local.properties`에 아래 값을 추가한다.

```properties
sdk.dir=C\:\\Android\\Sdk
KMA_SERVICE_KEY=your_kma_service_key_encoded
```

`KMA_SERVICE_KEY`는 공공데이터포털에서 제공되는 URL 인코딩된 키를 그대로 넣는다.

## 실행
1. Android Studio로 `02_android_app` 폴더를 연다.
2. Gradle Sync 후 앱을 실행한다.

## CLI 테스트(검증 완료)
프로젝트 루트(`02_android_app`)에서:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

현재 기준 결과:
- 단위 테스트 통과
- debug APK 생성 성공

생성 APK:
- `app/build/outputs/apk/debug/app-debug.apk`

## 실제 폰에서 직접 테스트
1. 개발자 옵션에서 `USB 디버깅` 활성화
2. USB 연결 후 아래 명령 실행

```powershell
C:\Users\sdf85\AppData\Local\Android\Sdk\platform-tools\adb.exe devices -l
C:\Users\sdf85\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
```

3. 앱 실행 후 권한 허용
- 위치 권한 허용
- 네트워크 사용 가능 상태 확인

4. 기능 점검
- 메인 화면에서 `현재 위치로 다시 계산` 클릭
- 비가 없으면 "우산 기울임 불필요" 메시지 확인
- 비가 오면 권장 방향/기울임 각도 확인

## 에뮬레이터 오류 대응
에뮬레이터가 안 붙을 때:

```powershell
C:\Users\sdf85\AppData\Local\Android\Sdk\platform-tools\adb.exe kill-server
C:\Users\sdf85\AppData\Local\Android\Sdk\platform-tools\adb.exe start-server
C:\Users\sdf85\AppData\Local\Android\Sdk\emulator\emulator.exe -list-avds
```

`CreateNamedPipe ... Access denied` 유형이 보이면:
- Android Studio Device Manager에서 에뮬레이터를 다시 시작
- 남아있는 `emulator/qemu/adb` 프로세스 종료 후 재시도
- 가능하면 실제 USB 기기 테스트를 우선 권장

## 현재 상태
- Open-Meteo 날씨 조회 구현 완료
- KMA(기상청) 초단기실황 + 격자 변환 + Open-Meteo 폴백 구조 구현 완료
- 3D 우산 안내: Filament(SceneView) + GLB 모델 로딩 적용

## 3D 모델 출처
- 파일: `app/src/main/assets/models/umbrella.glb`
- 출처: Poly Pizza (CreativeTrio) `Closed Umbrella`
- 라이선스: CC0 1.0 (Public Domain)
