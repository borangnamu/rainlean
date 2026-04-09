package com.rainlean.core.weather

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object KmaBaseTimeResolver {
    data class Base(val date: String, val time: String)

    // Ultra short nowcast is published hourly; subtract ~45m to avoid requesting unpublished slot.
    fun resolve(nowSeoul: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))): Base {
        val aligned = nowSeoul.minusMinutes(45).withMinute(0).withSecond(0).withNano(0)
        return Base(
            date = aligned.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
            time = aligned.format(DateTimeFormatter.ofPattern("HHmm"))
        )
    }
}

