package com.perforators.clock

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Choreographer
import android.view.Choreographer.FrameCallback
import android.view.View
import com.perforators.clock.internal.Circle
import com.perforators.clock.internal.HourLabel
import com.perforators.clock.internal.ViewState
import com.perforators.clock.internal.drawers.BackgroundDrawer
import com.perforators.clock.internal.drawers.HourArrowDrawer
import com.perforators.clock.internal.drawers.HourLabelsDrawer
import com.perforators.clock.internal.drawers.MinuteArrowDrawer
import com.perforators.clock.internal.drawers.SecondArrowDrawer

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

    private val secondArrowDrawer = SecondArrowDrawer(this)
    var showSecondArrow by secondArrowDrawer::isShow
    var secondArrowColor by secondArrowDrawer::color
    var secondArrowWidth by secondArrowDrawer::width

    private val minuteArrowDrawer = MinuteArrowDrawer(this)
    var showMinuteArrow by minuteArrowDrawer::isShow
    var minuteArrowColor by minuteArrowDrawer::color
    var minuteArrowWidth by minuteArrowDrawer::width

    private val hourArrowDrawer = HourArrowDrawer(this)
    var showHourArrow by hourArrowDrawer::isShow
    var hourArrowColor by hourArrowDrawer::color
    var hourArrowWidth by hourArrowDrawer::width

    private val hourLabelsDrawer = HourLabelsDrawer(this, HOUR_LABELS)
    var labelsSize by hourLabelsDrawer::size
    var labelsColor by hourLabelsDrawer::color
    var labelsOffsetFromEdge by hourLabelsDrawer::offsetFromEdge

    private val backgroundDrawer = BackgroundDrawer(this)
    var clockBackgroundColor by backgroundDrawer::backgroundColor
    var borderWidth by backgroundDrawer::borderWidth
    var borderColor by backgroundDrawer::borderColor

    private var circle = Circle()

    private val drawers = mapOf(
        secondArrowDrawer::class to secondArrowDrawer,
        minuteArrowDrawer::class to minuteArrowDrawer,
        hourArrowDrawer::class to hourArrowDrawer,
        backgroundDrawer::class to backgroundDrawer,
        hourLabelsDrawer::class to hourLabelsDrawer,
    )

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ClockView)
        drawers.values.forEach { drawer ->
            drawer.initialize(typedArray)
        }
        typedArray.recycle()
        runTimePolling()
    }

    private fun runTimePolling() {
        val choreographer = Choreographer.getInstance()
        var frameCallback: FrameCallback? = null
        frameCallback = FrameCallback {
            currentTimeInMillis = timeProvider.provide()
            choreographer.postFrameCallback(frameCallback)
        }
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        hourLabelsDrawer.measureLabels()
    }

    override fun onDraw(canvas: Canvas) {
        updateCircle()
        backgroundDrawer.draw(canvas, circle)
        hourLabelsDrawer.draw(canvas, circle)
        hourArrowDrawer.draw(canvas, currentTimeInMillis, circle)
        minuteArrowDrawer.draw(canvas, currentTimeInMillis, circle)
        secondArrowDrawer.draw(canvas, currentTimeInMillis, circle)
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
        val superState = super.onSaveInstanceState()
        return ViewState(superState).apply {
            write { drawers.values.map { it.saveState() } }
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val viewState = state as ViewState
        super.onRestoreInstanceState(viewState.superState)
        viewState.read {
            val bundle = readBundle(ClockView::class.java.classLoader) ?: return@read
            drawers.values.forEach { drawer -> drawer.restoreState(bundle) }
        }
    }

    companion object {
        private val HOUR_LABELS = (0..11).map { HourLabel(it) }
    }
}
