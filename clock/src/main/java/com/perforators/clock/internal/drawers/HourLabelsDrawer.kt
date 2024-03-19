package com.perforators.clock.internal.drawers

import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.core.graphics.withSave
import androidx.core.os.bundleOf
import com.perforators.clock.ClockView
import com.perforators.clock.R
import com.perforators.clock.internal.Circle
import com.perforators.clock.internal.HourLabel
import com.perforators.clock.internal.dpToPx
import com.perforators.clock.internal.spToPx

internal class HourLabelsDrawer(
    private val view: ClockView,
    private val labels: List<HourLabel>
) : Drawer {

    private val paint = Paint().apply {
        isAntiAlias = true
        textSize = DEFAULT_TEXT_SIZE_IN_SP.spToPx(view.context)
        color = DEFAULT_TEXT_COLOR
    }

    var size = paint.textSize
        set(value) {
            if (field == value) return
            field = value
            paint.textSize = value
            view.invalidate()
        }

    var color = paint.color
        set(value) {
            if (field == value) return
            field = value
            paint.color = value
            view.invalidate()
        }

    var offsetFromEdge = DEFAULT_OFFSET_IN_DP.dpToPx(view.context)
        set(value) {
            if (field == value) return
            field = value
            view.invalidate()
        }

    private var labelWithMaxDiagonal: HourLabel? = null

    override fun initialize(typedArray: TypedArray) {
        size = typedArray.getDimension(R.styleable.ClockView_labelsSize, size)
        color = typedArray.getColor(R.styleable.ClockView_labelsColor, color)
        offsetFromEdge =
            typedArray.getDimension(R.styleable.ClockView_labelsOffsetFromEdge, offsetFromEdge)
    }

    fun measureLabels() {
        labels.forEach { label ->
            val labelText = label.hour.toString()
            paint.getTextBounds(labelText, 0, labelText.length, label.bounds)
            if (label.diagonal > (labelWithMaxDiagonal?.diagonal ?: 0.0)) {
                labelWithMaxDiagonal = label
            }
        }
    }

    fun draw(canvas: Canvas, circle: Circle) {
        val maxDiagonal = labelWithMaxDiagonal?.diagonal ?: return
        val labelsOffsetFromCenter = circle.radius - offsetFromEdge - maxDiagonal / 2
        labels.forEach { label ->
            canvas.drawLabel(label, labelsOffsetFromCenter.toFloat(), circle)
        }
    }

    private fun Canvas.drawLabel(label: HourLabel, offsetFromCenter: Float, circle: Circle) {
        withSave {
            val angle = label.hour * HOUR_TO_ANGLE_MULTIPLIER - ADJUSTMENT_ANGLE
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

    override fun restoreState(bundle: Bundle) {
        size = bundle.getFloat(KEY_SIZE)
        color = bundle.getInt(KEY_COLOR)
        offsetFromEdge = bundle.getFloat(KEY_OFFSET)
    }

    companion object {
        private const val DEFAULT_TEXT_SIZE_IN_SP = 10
        private const val DEFAULT_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_OFFSET_IN_DP = 4
        private const val HOUR_TO_ANGLE_MULTIPLIER = 30f
        private const val ADJUSTMENT_ANGLE = 90f

        private const val KEY_SIZE = "size"
        private const val KEY_COLOR = "color"
        private const val KEY_OFFSET = "offset"
    }
}
