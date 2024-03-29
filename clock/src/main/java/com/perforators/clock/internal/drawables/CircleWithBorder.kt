package com.perforators.clock.internal.drawables

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.os.bundleOf
import com.perforators.clock.internal.Circle
import com.perforators.clock.internal.dpToPx

internal class CircleWithBorder(
    private val owner: View,
    override val key: String,
    contextProvider: ContextProvider<Circle>
) : DrawableObject.WithContext<Circle>(contextProvider) {

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        color = DEFAULT_BACKGROUND_COLOR
    }

    private val borderPaint = Paint().apply {
        isAntiAlias = true
        color = DEFAULT_BORDER_COLOR
    }

    var borderWidth = DEFAULT_BORDER_WIDTH_IN_DP.dpToPx(owner.context)
        set(value) {
            if (field == value) return
            field = value
            owner.invalidate()
        }

    var borderColor = borderPaint.color
        set(value) {
            if (field == value) return
            field = value
            borderPaint.color = value
            owner.invalidate()
        }

    var backgroundColor = backgroundPaint.color
        set(value) {
            if (field == value) return
            field = value
            backgroundPaint.color = value
            owner.invalidate()
        }

    override fun draw(canvas: Canvas, context: Circle) {
        canvas.drawCircle(
            context.pivotX,
            context.pivotY,
            context.radius + borderWidth,
            borderPaint
        )
        canvas.drawCircle(
            context.pivotX,
            context.pivotY,
            context.radius,
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

    override fun restoreState(state: Parcelable) {
        val bundle = state as Bundle
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
