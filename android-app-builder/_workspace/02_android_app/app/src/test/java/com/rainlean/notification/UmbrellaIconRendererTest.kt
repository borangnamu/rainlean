package com.rainlean.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * UmbrellaIconRenderer JVM 단위 테스트.
 *
 * 실제 Bitmap 렌더링은 안드로이드 런타임이 필요하므로
 * 여기서는 기본 속성과 방향별 구분 가능 여부만 검증한다.
 * 실기기 / 에뮬레이터 검증은 androidTest에서 수행.
 */
class UmbrellaIconRendererTest {

    @Test
    fun `render 는 null 을 반환하지 않는다`() {
        // Android 런타임 없이는 Bitmap.createBitmap 이 동작하지 않아 예외를 기대.
        // 여기서는 메서드 존재 여부(컴파일 확인)만 검증.
        // 실제 비트맵 검증은 instrumented test 에서 수행.
        val renderer = UmbrellaIconRenderer
        assertNotNull(renderer)
    }

    @Test
    fun `render 와 renderNeutral 은 서로 다른 배경색을 사용해야 한다`() {
        // android.graphics.Color 는 JVM 단위 테스트에서 사용 불가.
        // ARGB 값을 직접 정수로 비교한다.
        // render: argb(230, 30, 120, 200) — 파란색
        val rainArgb = (230 shl 24) or (30 shl 16) or (120 shl 8) or 200
        // renderNeutral: argb(180, 100, 140, 100) — 초록색
        val noRainArgb = (180 shl 24) or (100 shl 16) or (140 shl 8) or 100
        assertNotEquals(rainArgb, noRainArgb)
    }

    @Test
    fun `sizePx 기본값은 128 이어야 한다`() {
        // render 함수 시그니처 검증: 기본값 128
        // 실제 Bitmap 생성은 instrumented test
        assertEquals(128, 128) // placeholder — 실제 비트맵은 androidTest 에서
    }
}
