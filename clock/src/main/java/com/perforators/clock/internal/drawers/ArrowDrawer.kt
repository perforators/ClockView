package com.perforators.clock.internal.drawers

import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.core.graphics.withRotation
import androidx.core.os.bundleOf
import com.perforators.clock.ClockView
import com.perforators.clock.R
import com.perforators.clock.internal.Circle
import com.perforators.clock.internal.dpToPx

internal abstract class ArrowDrawer(private val view: ClockView) : Drawer {

    private val paint = Paint().apply {
        isAntiAlias = true
        color = DEFAULT_ARROW_COLOR
        strokeWidth = DEFAULT_WIDTH_IN_DP.dpToPx(view.context)
    }

    var isShow: Boolean = true
        set(value) {
            if (value == field) return
            field = value
            view.invalidate()
        }

    var ratioLengthToRadius: Float = DEFAULT_LENGTH_RATIO
        set(value) {
            if (value == field) return
            field = value
            view.invalidate()
        }

    var ratioOffsetFromCenterToLength: Float = DEFAULT_OFFSET_RATIO
        set(value) {
            if (value == field) return
            field = value
            view.invalidate()
        }

    var color: Int = paint.color
        set(value) {
            if (field == value) return
            paint.color = value
            field = value
            view.invalidate()
        }

    var width: Float = paint.strokeWidth
        set(value) {
            if (field == value) return
            paint.strokeWidth = value
            field = value
            view.invalidate()
        }

    override fun initialize(typedArray: TypedArray) {
        isShow = typedArray.getBoolean(R.styleable.ClockView_showSecondArrow, isShow)
        color = typedArray.getColor(R.styleable.ClockView_secondArrowColor, color)
        width = typedArray.getDimension(R.styleable.ClockView_secondArrowWidth, width)
    }

    fun draw(canvas: Canvas, timeInMillis: Long, circle: Circle) {
        if (!isShow) return
        canvas.withRotation(
            degrees = calculateAngle(timeInMillis) - ADJUSTMENT_ANGLE,
            pivotX = circle.pivotX,
            pivotY = circle.pivotY
        ) {
            val arrowLength = circle.radius * ratioLengthToRadius
            val offsetLength = arrowLength * ratioOffsetFromCenterToLength
            val startX = circle.pivotX - offsetLength
            val stopX = startX + arrowLength
            drawLine(startX, circle.pivotY, stopX, circle.pivotY, paint)
        }
    }

    override fun saveState(): Bundle {
        return bundleOf(
            KEY_LENGTH to ratioLengthToRadius,
            KEY_OFFSET to ratioOffsetFromCenterToLength,
            KEY_COLOR to color,
            KEY_WIDTH to width
        )
    }

    override fun restoreState(bundle: Bundle) {
        ratioLengthToRadius = bundle.getFloat(KEY_LENGTH)
        ratioOffsetFromCenterToLength = bundle.getFloat(KEY_OFFSET)
        color = bundle.getInt(KEY_COLOR)
        width = bundle.getFloat(KEY_WIDTH)
    }

    abstract fun calculateAngle(timeInMillis: Long): Float

    companion object {
        private const val DEFAULT_LENGTH_RATIO = 0.8f
        private const val DEFAULT_OFFSET_RATIO = 0.15f
        private const val DEFAULT_ARROW_COLOR = Color.BLACK
        private const val DEFAULT_WIDTH_IN_DP = 2
        private const val ADJUSTMENT_ANGLE = 90f

        private const val KEY_LENGTH = "length"
        private const val KEY_OFFSET = "offset"
        private const val KEY_COLOR = "color"
        private const val KEY_WIDTH = "width"
    }
}

internal class SecondArrowDrawer(view: ClockView) : ArrowDrawer(view) {

    init {
        width = DEFAULT_WIDTH_IN_DP.dpToPx(view.context)
        ratioLengthToRadius = DEFAULT_RATIO
    }

    override fun calculateAngle(timeInMillis: Long): Float {
        val seconds = timeInMillis / MILLIS_IN_SECOND.toDouble()
        return seconds.mod(SECONDS_IN_MINUTE.toDouble()).toFloat() * SECONDS_TO_ANGLE_MULTIPLIER
    }

    companion object {
        private const val DEFAULT_RATIO = 0.9f
        private const val DEFAULT_WIDTH_IN_DP = 2
        private const val SECONDS_TO_ANGLE_MULTIPLIER = 6
        private const val MILLIS_IN_SECOND = 1000
        private const val SECONDS_IN_MINUTE = 60
    }
}

internal class MinuteArrowDrawer(view: ClockView) : ArrowDrawer(view) {

    init {
        width = DEFAULT_WIDTH_IN_DP.dpToPx(view.context)
        ratioLengthToRadius = DEFAULT_RATIO
    }

    override fun calculateAngle(timeInMillis: Long): Float {
        val minutes = timeInMillis / MILLIS_IN_MINUTE.toDouble()
        return minutes.mod(MINUTES_IN_HOURS.toDouble()).toFloat() * MINUTES_TO_ANGLE_MULTIPLIER
    }

    companion object {
        private const val DEFAULT_RATIO = 0.7f
        private const val DEFAULT_WIDTH_IN_DP = 3
        private const val MINUTES_TO_ANGLE_MULTIPLIER = 6
        private const val MILLIS_IN_MINUTE = 60 * 1000
        private const val MINUTES_IN_HOURS = 60
    }
}

internal class HourArrowDrawer(view: ClockView) : ArrowDrawer(view) {

    init {
        width = DEFAULT_WIDTH_IN_DP.dpToPx(view.context)
        ratioLengthToRadius = DEFAULT_RATIO
    }

    override fun calculateAngle(timeInMillis: Long): Float {
        val hours = timeInMillis / MILLIS_IN_HOUR.toDouble()
        return hours.mod(HOURS_IN_HALF_DAY.toDouble()).toFloat() * HOURS_TO_ANGLE_MULTIPLIER
    }

    companion object {
        private const val DEFAULT_RATIO = 0.6f
        private const val DEFAULT_WIDTH_IN_DP = 4
        private const val HOURS_TO_ANGLE_MULTIPLIER = 30
        private const val MILLIS_IN_HOUR = 60 * 60 * 1000
        private const val HOURS_IN_HALF_DAY = 12
    }
}
