package com.perforators.clock

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import com.perforators.clock.internal.Circle
import com.perforators.clock.internal.drawables.Arrow
import com.perforators.clock.internal.drawables.CircleWithBorder
import com.perforators.clock.internal.drawables.ContextFactory
import com.perforators.clock.internal.drawables.HourArrow
import com.perforators.clock.internal.drawables.HourLabels
import com.perforators.clock.internal.drawables.MinuteArrow
import com.perforators.clock.internal.drawables.SecondArrow

class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var timeProvider: CurrentTimeInMillisProvider = CurrentTimeInMillisProvider.DEFAULT

    var currentTimeInMillis: Long = timeProvider.provide()
        private set(value) {
            if (value == field) return
            field = value
            invalidate()
        }

    private val circle = Circle()

    private val arrowContextFactory = object : ContextFactory.Reusable<Arrow.Context>() {
        override fun createNew() = Arrow.Context(currentTimeInMillis, circle)
        override fun update(context: Arrow.Context) {
            context.timeInMillis = currentTimeInMillis
            context.circle = circle
        }
    }

    private val secondArrow = SecondArrow(this, SECOND_ARROW_DRAWER_KEY, arrowContextFactory)
    var showSecondArrow by secondArrow::isShow
    var secondArrowColor by secondArrow::color
    var secondArrowWidth by secondArrow::width

    private val minuteArrow = MinuteArrow(this, MINUTE_ARROW_DRAWER_KEY, arrowContextFactory)
    var showMinuteArrow by minuteArrow::isShow
    var minuteArrowColor by minuteArrow::color
    var minuteArrowWidth by minuteArrow::width

    private val hourArrow = HourArrow(this, HOUR_ARROW_DRAWER_KEY, arrowContextFactory)
    var showHourArrow by hourArrow::isShow
    var hourArrowColor by hourArrow::color
    var hourArrowWidth by hourArrow::width

    private val hourLabels = HourLabels(this, HOUR_LABELS_DRAWER_KEY, HOURS) { circle }
    var labelsSize by hourLabels::size
    var labelsColor by hourLabels::color
    var labelsOffsetFromEdge by hourLabels::offsetFromEdge

    private val background = CircleWithBorder(this, BACKGROUND_DRAWER_KEY) { circle }
    var clockBackgroundColor by background::backgroundColor
    var borderWidth by background::borderWidth
    var borderColor by background::borderColor

    private val drawableObjects = listOf(
        background, hourLabels, hourArrow, minuteArrow, secondArrow
    )

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ClockView)
        secondArrow.apply {
            isShow = typedArray.getBoolean(R.styleable.ClockView_showSecondArrow, isShow)
            color = typedArray.getColor(R.styleable.ClockView_secondArrowColor, color)
            width = typedArray.getDimension(R.styleable.ClockView_secondArrowWidth, width)
        }
        minuteArrow.apply {
            isShow = typedArray.getBoolean(R.styleable.ClockView_showMinuteArrow, isShow)
            color = typedArray.getColor(R.styleable.ClockView_minuteArrowColor, color)
            width = typedArray.getDimension(R.styleable.ClockView_minuteArrowWidth, width)
        }
        hourArrow.apply {
            isShow = typedArray.getBoolean(R.styleable.ClockView_showHourArrow, isShow)
            color = typedArray.getColor(R.styleable.ClockView_hourArrowColor, color)
            width = typedArray.getDimension(R.styleable.ClockView_hourArrowWidth, width)
        }
        hourLabels.apply {
            size = typedArray.getDimension(R.styleable.ClockView_labelsSize, size)
            color = typedArray.getColor(R.styleable.ClockView_labelsColor, color)
            offsetFromEdge =
                typedArray.getDimension(R.styleable.ClockView_labelsOffsetFromEdge, offsetFromEdge)
        }
        background.apply {
            borderWidth = typedArray.getDimension(R.styleable.ClockView_borderWidth, borderWidth)
            borderColor = typedArray.getColor(R.styleable.ClockView_borderColor, borderColor)
            backgroundColor =
                typedArray.getColor(R.styleable.ClockView_clockBackgroundColor, backgroundColor)
        }
        typedArray.recycle()
        runTimePooling()
    }

    private fun runTimePooling() {
        val choreographer = Choreographer.getInstance()
        var frameCallback: Choreographer.FrameCallback? = null
        frameCallback = Choreographer.FrameCallback {
            currentTimeInMillis = timeProvider.provide()
            choreographer.postFrameCallback(frameCallback)
        }
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        drawableObjects.forEach { it.measure(widthMeasureSpec, heightMeasureSpec) }
    }

    override fun onDraw(canvas: Canvas) {
        updateCircle()
        drawableObjects.forEach { it.draw(canvas) }
    }

    private fun updateCircle() {
        circle.apply {
            pivotX = width / 2f
            pivotY = height / 2f
            radius = minOf(widthWithoutPaddings, heightWithoutPaddings) / 2f - borderWidth
        }
    }

    private val View.widthWithoutPaddings: Int
        get() = width - paddingLeft - paddingRight

    private val View.heightWithoutPaddings: Int
        get() = height - paddingTop - paddingBottom

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        drawableObjects.forEach { bundle.putParcelable(it.key, it.saveState()) }
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        drawableObjects.forEach {
            val innerState = bundle.getBundle(it.key) ?: return@forEach
            it.restoreState(innerState)
        }
        super.onRestoreInstanceState(bundle.getParcelable(KEY_SUPER_STATE))
    }

    companion object {
        private const val KEY_SUPER_STATE = "clock_view_super_state"
        private val HOURS = (0..11).toList()

        private const val HOUR_ARROW_DRAWER_KEY = "hour_drawer"
        private const val MINUTE_ARROW_DRAWER_KEY = "minute_drawer"
        private const val SECOND_ARROW_DRAWER_KEY = "second_drawer"
        private const val BACKGROUND_DRAWER_KEY = "background_drawer"
        private const val HOUR_LABELS_DRAWER_KEY = "hour_labels_drawer"
    }
}
