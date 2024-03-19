package com.perforators.clock.internal

import android.graphics.Rect
import kotlin.math.sqrt

internal class HourLabel(val hour: Int) {
    val bounds = Rect()

    val diagonal: Double by lazy {
        with(bounds) {
            sqrt(width().toDouble() * width() + height() * height())
        }
    }
}
