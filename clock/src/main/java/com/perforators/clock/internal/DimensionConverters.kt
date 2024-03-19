package com.perforators.clock.internal

import android.content.Context
import android.util.TypedValue

internal fun Int.dpToPx(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    context.resources.displayMetrics
)

internal fun Int.spToPx(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP,
    this.toFloat(),
    context.resources.displayMetrics
)
