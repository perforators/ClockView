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
import com.perforators.clock.internal.drawables.TimeArrow
import com.perforators.clock.internal.drawables.CircleWithBorder
import com.perforators.clock.internal.drawables.ContextProvider
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

    private var circle = Circle()

    private val timeArrowContextProvider = object : ContextProvider.Reusable<TimeArrow.Context>() {
        override fun createNew() = TimeArrow.Context(currentTimeInMillis, circle)
        override fun update(context: TimeArrow.Context) {
            context.timeInMillis = currentTimeInMillis
            context.circle = circle
        }
    }

    private val secondArrow = SecondArrow(this, SECOND_ARROW_KEY, timeArrowContextProvider)
    var showSecondArrow by secondArrow::isShow
    var secondArrowColor by secondArrow::color
    var secondArrowWidth by secondArrow::width

    private val minuteArrow = MinuteArrow(this, MINUTE_ARROW_KEY, timeArrowContextProvider)
    var showMinuteArrow by minuteArrow::isShow
    var minuteArrowColor by minuteArrow::color
    var minuteArrowWidth by minuteArrow::width

    private val hourArrow = HourArrow(this, HOUR_ARROW_KEY, timeArrowContextProvider)
    var showHourArrow by hourArrow::isShow
    var hourArrowColor by hourArrow::color
    var hourArrowWidth by hourArrow::width

    private val hourLabels = HourLabels(this, HOUR_LABELS_KEY, HOURS) { circle }
    var labelsSize by hourLabels::size
    var labelsColor by hourLabels::color
    var labelsOffsetFromEdge by hourLabels::offsetFromEdge

    private val background = CircleWithBorder(this, BACKGROUND_KEY) { circle }
    var clockBackgroundColor by background::backgroundColor
    var borderColor by background::borderColor
    var borderWidth: Float
        get() = background.borderWidth
        set(value) {
            background.borderWidth = value
            updateCircle()
        }

    private val drawableObjects = listOf(
        background, hourLabels, hourArrow, minuteArrow, secondArrow
    )

    init {
        val attributes: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ClockView)
        with(attributes) {
            showSecondArrow = getBoolean(R.styleable.ClockView_showSecondArrow, showSecondArrow)
            secondArrowColor = getColor(R.styleable.ClockView_secondArrowColor, secondArrowColor)
            secondArrowWidth =
                getDimension(R.styleable.ClockView_secondArrowWidth, secondArrowWidth)
            showMinuteArrow = getBoolean(R.styleable.ClockView_showMinuteArrow, showMinuteArrow)
            minuteArrowColor = getColor(R.styleable.ClockView_minuteArrowColor, minuteArrowColor)
            minuteArrowWidth =
                getDimension(R.styleable.ClockView_minuteArrowWidth, minuteArrowWidth)
            showHourArrow = getBoolean(R.styleable.ClockView_showSecondArrow, showSecondArrow)
            hourArrowColor = getColor(R.styleable.ClockView_hourArrowColor, hourArrowColor)
            hourArrowWidth = getDimension(R.styleable.ClockView_hourArrowWidth, hourArrowWidth)
            labelsSize = getDimension(R.styleable.ClockView_labelsSize, labelsSize)
            labelsColor = getColor(R.styleable.ClockView_labelsColor, labelsColor)
            labelsOffsetFromEdge =
                getDimension(R.styleable.ClockView_labelsOffsetFromEdge, labelsOffsetFromEdge)
            borderWidth = getDimension(R.styleable.ClockView_borderWidth, borderWidth)
            borderColor = getColor(R.styleable.ClockView_borderColor, borderColor)
            clockBackgroundColor =
                getColor(R.styleable.ClockView_clockBackgroundColor, clockBackgroundColor)
        }
        attributes.recycle()
        runTimePolling()
    }

    private fun runTimePolling() {
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateCircle()
    }

    private fun updateCircle() {
        val widthWithoutPaddings = width - paddingStart - paddingEnd
        val heightWithoutPaddings = height - paddingBottom - paddingTop
        circle = Circle(
            pivotX = paddingStart + widthWithoutPaddings / 2f,
            pivotY = paddingTop + heightWithoutPaddings / 2f,
            radius = minOf(widthWithoutPaddings, heightWithoutPaddings) / 2f - borderWidth
        )
    }

    override fun onDraw(canvas: Canvas) {
        drawableObjects.forEach { it.draw(canvas) }
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        drawableObjects.forEach { bundle.putBundle(it.key, it.saveState()) }
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

        private const val HOUR_ARROW_KEY = "hour_arrow"
        private const val MINUTE_ARROW_KEY = "minute_arrow"
        private const val SECOND_ARROW_KEY = "second_arrow"
        private const val BACKGROUND_KEY = "background"
        private const val HOUR_LABELS_KEY = "hour_labels"
    }
}
