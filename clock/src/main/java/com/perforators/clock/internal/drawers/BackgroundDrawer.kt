package com.perforators.clock.internal.drawers

import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.core.os.bundleOf
import com.perforators.clock.ClockView
import com.perforators.clock.R
import com.perforators.clock.internal.Circle
import com.perforators.clock.internal.dpToPx

internal class BackgroundDrawer(private val view: ClockView) : Drawer {

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        color = DEFAULT_BACKGROUND_COLOR
    }

    private val borderPaint = Paint().apply {
        isAntiAlias = true
        color = DEFAULT_BORDER_COLOR
    }

    var borderWidth = DEFAULT_BORDER_WIDTH_IN_DP.dpToPx(view.context)
        set(value) {
            if (field == value) return
            field = value
            view.invalidate()
        }

    var borderColor = borderPaint.color
        set(value) {
            if (field == value) return
            field = value
            borderPaint.color = value
            view.invalidate()
        }

    var backgroundColor = backgroundPaint.color
        set(value) {
            if (field == value) return
            field = value
            backgroundPaint.color = value
            view.invalidate()
        }

    override fun initialize(typedArray: TypedArray) {
        borderWidth = typedArray.getDimension(R.styleable.ClockView_borderWidth, borderWidth)
        borderColor = typedArray.getColor(R.styleable.ClockView_borderColor, borderColor)
        backgroundColor = typedArray.getColor(R.styleable.ClockView_borderColor, backgroundColor)
    }

    fun draw(canvas: Canvas, circle: Circle) {
        canvas.drawCircle(
            circle.pivotX,
            circle.pivotY,
            circle.radius + borderWidth,
            borderPaint
        )
        canvas.drawCircle(
            circle.pivotX,
            circle.pivotY,
            circle.radius,
            backgroundPaint
        )
    }

    override fun saveState(): Bundle {
        return bundleOf(
            KEY_BACKGROUND_COLOR to backgroundColor,
            KEY_BORDER_COLOR to borderColor,
            KEY_BORDER_WIDTH to borderWidth
        )
    }

    override fun restoreState(bundle: Bundle) {
        backgroundColor = bundle.getInt(KEY_BACKGROUND_COLOR)
        borderColor = bundle.getInt(KEY_BORDER_COLOR)
        borderWidth = bundle.getFloat(KEY_BORDER_WIDTH)
    }

    companion object {
        private const val DEFAULT_BACKGROUND_COLOR = Color.WHITE
        private const val DEFAULT_BORDER_COLOR = Color.BLACK
        private const val DEFAULT_BORDER_WIDTH_IN_DP = 4

        private const val KEY_BACKGROUND_COLOR = "background_color"
        private const val KEY_BORDER_COLOR = "border_color"
        private const val KEY_BORDER_WIDTH = "border_width"
    }
}
