package com.perforators.clock.internal.drawables

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.graphics.withRotation
import androidx.core.os.bundleOf
import com.perforators.clock.ClockView
import com.perforators.clock.internal.Circle
import com.perforators.clock.internal.dpToPx

internal abstract class Arrow(
    private val owner: View,
    override val key: String,
    contextProvider: ContextProvider<Context>
) : DrawableObject.WithContext<Arrow.Context>(contextProvider) {

    private val paint = Paint().apply {
        isAntiAlias = true
        color = DEFAULT_ARROW_COLOR
        strokeWidth = DEFAULT_WIDTH_IN_DP.dpToPx(owner.context)
    }

    var isShow: Boolean = true
        set(value) {
            if (value == field) return
            field = value
            owner.invalidate()
        }

    var ratioLengthToRadius: Float = DEFAULT_LENGTH_RATIO
        set(value) {
            if (value == field) return
            field = value
            owner.invalidate()
        }

    var ratioOffsetFromCenterToLength: Float = DEFAULT_OFFSET_RATIO
        set(value) {
            if (value == field) return
            field = value
            owner.invalidate()
        }

    var color: Int = paint.color
        set(value) {
            if (field == value) return
            paint.color = value
            field = value
            owner.invalidate()
        }

    var width: Float = paint.strokeWidth
        set(value) {
            if (field == value) return
            paint.strokeWidth = value
            field = value
            owner.invalidate()
        }

    override fun draw(canvas: Canvas, context: Context) {
        val (timeInMillis, circle) = context
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
            KEY_SHOW to isShow,
            KEY_LENGTH to ratioLengthToRadius,
            KEY_OFFSET to ratioOffsetFromCenterToLength,
            KEY_COLOR to color,
            KEY_WIDTH to width
        )
    }

    override fun restoreState(state: Bundle) {
        isShow = state.getBoolean(KEY_SHOW)
        ratioLengthToRadius = state.getFloat(KEY_LENGTH)
        ratioOffsetFromCenterToLength = state.getFloat(KEY_OFFSET)
        color = state.getInt(KEY_COLOR)
        width = state.getFloat(KEY_WIDTH)
    }

    abstract fun calculateAngle(timeInMillis: Long): Float

    data class Context(
        var timeInMillis: Long,
        var circle: Circle
    )

    companion object {
        private const val DEFAULT_LENGTH_RATIO = 0.8f
        private const val DEFAULT_OFFSET_RATIO = 0.15f
        private const val DEFAULT_ARROW_COLOR = Color.BLACK
        private const val DEFAULT_WIDTH_IN_DP = 2
        private const val ADJUSTMENT_ANGLE = 90f

        private const val KEY_SHOW = "show"
        private const val KEY_LENGTH = "length"
        private const val KEY_OFFSET = "offset"
        private const val KEY_COLOR = "color"
        private const val KEY_WIDTH = "width"
    }
}

internal class SecondArrow(
    owner: ClockView,
    key: String,
    contextProvider: ContextProvider<Context>,
) : Arrow(owner, key, contextProvider) {

    init {
        width = DEFAULT_WIDTH_IN_DP.dpToPx(owner.context)
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

internal class MinuteArrow(
    owner: ClockView,
    key: String,
    contextProvider: ContextProvider<Context>,
) : Arrow(owner, key, contextProvider) {

    init {
        width = DEFAULT_WIDTH_IN_DP.dpToPx(owner.context)
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

internal class HourArrow(
    owner: ClockView,
    key: String,
    contextProvider: ContextProvider<Context>,
) : Arrow(owner, key, contextProvider) {

    init {
        width = DEFAULT_WIDTH_IN_DP.dpToPx(owner.context)
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
