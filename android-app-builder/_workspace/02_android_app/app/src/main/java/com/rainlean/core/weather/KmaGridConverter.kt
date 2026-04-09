package com.rainlean.core.weather

import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan

object KmaGridConverter {
    data class GridPoint(val nx: Int, val ny: Int)

    // DFS Lambert Conformal Conic conversion used by KMA village forecast APIs.
    fun latLonToGrid(lat: Double, lon: Double): GridPoint {
        val re = 6371.00877
        val grid = 5.0
        val slat1 = 30.0
        val slat2 = 60.0
        val olon = 126.0
        val olat = 38.0
        val xo = 43.0
        val yo = 136.0

        val degrad = PI / 180.0
        val reVal = re / grid
        val slat1Val = slat1 * degrad
        val slat2Val = slat2 * degrad
        val olonVal = olon * degrad
        val olatVal = olat * degrad

        var sn = tan(PI * 0.25 + slat2Val * 0.5) / tan(PI * 0.25 + slat1Val * 0.5)
        sn = ln(cos(slat1Val) / cos(slat2Val)) / ln(sn)
        var sf = tan(PI * 0.25 + slat1Val * 0.5)
        sf = sf.pow(sn) * cos(slat1Val) / sn
        var ro = tan(PI * 0.25 + olatVal * 0.5)
        ro = reVal * sf / ro.pow(sn)
        var ra = tan(PI * 0.25 + lat * degrad * 0.5)
        ra = reVal * sf / ra.pow(sn)
        var theta = lon * degrad - olonVal
        if (theta > PI) theta -= 2.0 * PI
        if (theta < -PI) theta += 2.0 * PI
        theta *= sn

        val nx = floor(ra * sin(theta) + xo + 0.5).toInt()
        val ny = floor(ro - ra * cos(theta) + yo + 0.5).toInt()
        return GridPoint(nx = nx, ny = ny)
    }
}

