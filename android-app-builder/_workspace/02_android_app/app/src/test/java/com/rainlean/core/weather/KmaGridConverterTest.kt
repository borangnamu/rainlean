package com.rainlean.core.weather

import org.junit.Assert.assertEquals
import org.junit.Test

class KmaGridConverterTest {

    // Reference values from KMA official conversion documentation.
    @Test
    fun `converts Seoul city hall coordinates correctly`() {
        // 37.5665° N, 126.9780° E → expected KMA grid (60, 127)
        val grid = KmaGridConverter.latLonToGrid(37.5665, 126.9780)
        assertEquals(60, grid.nx)
        assertEquals(127, grid.ny)
    }

    @Test
    fun `converts Busan coordinates correctly`() {
        // 35.1796° N, 129.0756° E → expected KMA grid (98, 76)
        val grid = KmaGridConverter.latLonToGrid(35.1796, 129.0756)
        assertEquals(98, grid.nx)
        assertEquals(76, grid.ny)
    }

    @Test
    fun `converts Jeju coordinates correctly`() {
        // 33.4996° N, 126.5312° E → KMA grid (53, 38) per DFS LCC algorithm
        val grid = KmaGridConverter.latLonToGrid(33.4996, 126.5312)
        assertEquals(53, grid.nx)
        assertEquals(38, grid.ny)
    }
}
