package com.perforators.clock.internal.drawables

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.core.graphics.withSave
import androidx.core.os.bundleOf
import com.perforators.clock.internal.Circle
import com.perforators.clock.internal.dpToPx
import com.perforators.clock.internal.spToPx
import kotlin.math.sqrt

internal class HourLabels(
    private val owner: View,
    override val key: String,
    hours: List<Int> = DEFAULT_HOURS,
    contextProvider: ContextProvider<Circle>,
) : DrawableObject.WithContext<Circle>(contextProvider) {

    private val paint = Paint().apply {
        isAntiAlias = true
        textSize = DEFAULT_TEXT_SIZE_IN_SP.spToPx(owner.context)
        color = DEFAULT_TEXT_COLOR
    }

    var size = paint.textSize
        set(value) {
            if (field == value) return
            field = value
            paint.textSize = value
            owner.invalidate()
        }

    var color = paint.color
        set(value) {
            if (field == value) return
            field = value
            paint.color = value
            owner.invalidate()
        }

    var offsetFromEdge = DEFAULT_OFFSET_IN_DP.dpToPx(owner.context)
        set(value) {
            if (field == value) return
            field = value
            owner.invalidate()
        }

    private val labels = hours.map(::Label)
    private var maxLabelDiagonal: Double = 0.0

    init {
        require(hours.all { it <= MAXIMUM_HOUR_LIMIT }) {
            "Hour must no more then $MAXIMUM_HOUR_LIMIT"
        }
    }

    override fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        maxLabelDiagonal = 0.0
        labels.forEach { label ->
            val labelText = label.hour.toString()
            paint.getTextBounds(labelText, 0, labelText.length, label.bounds)
            maxLabelDiagonal = maxOf(maxLabelDiagonal, label.diagonal)
        }
    }

    override fun draw(canvas: Canvas, context: Circle) {
        val labelsOffsetFromCenter = context.radius - offsetFromEdge - maxLabelDiagonal / 2
        labels.forEach { label ->
            canvas.drawLabel(label, labelsOffsetFromCenter.toFloat(), context)
        }
    }

    private fun Canvas.drawLabel(label: Label, offsetFromCenter: Float, circle: Circle) {
        withSave {
            val angle = label.angle
            translate(circle.pivotX, circle.pivotY)
            rotate(angle)
            translate(offsetFromCenter, 0f)
            rotate(-angle)
            translate(-label.bounds.width() / 2f, label.bounds.height() / 2f)
            drawText(label.hour.toString(), 0f, 0f, paint)
        }
    }

    override fun saveState(): Bundle {
        return bundleOf(
            KEY_SIZE to size,
            KEY_COLOR to color,
            KEY_OFFSET to offsetFromEdge
        )
    }

    override fun restoreState(state: Bundle) {
        size = state.getFloat(KEY_SIZE)
        color = state.getInt(KEY_COLOR)
        offsetFromEdge = state.getFloat(KEY_OFFSET)
    }

    private class Label(val hour: Int) {
        val bounds = Rect()
        val diagonal: Double get() = with(bounds) {
            sqrt(width().toDouble() * width() + height() * height())
        }
        val angle: Float = hour * HOUR_TO_ANGLE_MULTIPLIER - ADJUSTMENT_ANGLE

        companion object {
            private const val HOUR_TO_ANGLE_MULTIPLIER = 30f
            private const val ADJUSTMENT_ANGLE = 90f
        }
    }

    companion object {
        private val DEFAULT_HOURS = (0..11).toList()

        private const val DEFAULT_TEXT_SIZE_IN_SP = 10
        private const val DEFAULT_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_OFFSET_IN_DP = 4
        private const val MAXIMUM_HOUR_LIMIT = 11

        private const val KEY_SIZE = "size"
        private const val KEY_COLOR = "color"
        private const val KEY_OFFSET = "offset"
    }
}
