package com.rainlean.core.weather

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class KmaBaseTimeResolverTest {

    private val seoul = ZoneId.of("Asia/Seoul")

    @Test
    fun `resolves to previous hour minus 45 minutes when current time is 30 minutes past`() {
        // 14:30 KST → minus 45m → 13:45 → truncate to hour → 13:00
        val now = ZonedDateTime.of(2024, 6, 1, 14, 30, 0, 0, seoul)
        val base = KmaBaseTimeResolver.resolve(now)
        assertEquals("20240601", base.date)
        assertEquals("1300", base.time)
    }

    @Test
    fun `resolves correctly when current time is exactly on the hour`() {
        // 15:00 KST → minus 45m → 14:15 → truncate to hour → 14:00
        val now = ZonedDateTime.of(2024, 6, 1, 15, 0, 0, 0, seoul)
        val base = KmaBaseTimeResolver.resolve(now)
        assertEquals("20240601", base.date)
        assertEquals("1400", base.time)
    }

    @Test
    fun `resolves across midnight boundary`() {
        // 00:30 KST → minus 45m → previous day 23:45 → truncate → 23:00
        val now = ZonedDateTime.of(2024, 6, 2, 0, 30, 0, 0, seoul)
        val base = KmaBaseTimeResolver.resolve(now)
        assertEquals("20240601", base.date)
        assertEquals("2300", base.time)
    }

    @Test
    fun `resolves to two hours back when current time is 44 minutes past`() {
        // 02:44 KST → minus 45m → 01:59 → truncate → 01:00
        val now = ZonedDateTime.of(2024, 6, 1, 2, 44, 0, 0, seoul)
        val base = KmaBaseTimeResolver.resolve(now)
        assertEquals("20240601", base.date)
        assertEquals("0100", base.time)
    }
}
