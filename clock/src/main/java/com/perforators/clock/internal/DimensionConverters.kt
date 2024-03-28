package com.perforators.clock.internal

import android.content.Context
import android.util.TypedValue

internal fun Int.dpToPx(context: Context) = unitToPx(context, TypedValue.COMPLEX_UNIT_DIP)
internal fun Int.spToPx(context: Context) = unitToPx(context, TypedValue.COMPLEX_UNIT_SP)

internal fun Int.unitToPx(context: Context, unit: Int): Float = TypedValue.applyDimension(
    unit,
    toFloat(),
    context.resources.displayMetrics
)



