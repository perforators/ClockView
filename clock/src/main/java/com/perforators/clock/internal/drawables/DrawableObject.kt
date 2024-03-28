package com.perforators.clock.internal.drawables

import android.graphics.Canvas
import android.os.Parcelable

internal interface DrawableObject : Restorable {
    fun draw(canvas: Canvas)
    fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int) = Unit

    abstract class WithContext<Context>(
        private val contextFactory: ContextFactory<Context>
    ) : DrawableObject {
        override fun draw(canvas: Canvas) {
            draw(canvas, contextFactory.provide())
        }

        abstract fun draw(canvas: Canvas, context: Context)
    }
}

internal fun interface ContextFactory<Context> {
    fun provide(): Context

    abstract class Reusable<Context> :  ContextFactory<Context> {
        private var cachedContext: Context? = null

        override fun provide(): Context {
            return cachedContext?.also { update(it) } ?: createNew().also { cachedContext = it }
        }

        abstract fun createNew(): Context
        abstract fun update(context: Context)
    }
}

internal interface Restorable {
    val key: String
    fun saveState(): Parcelable
    fun restoreState(state: Parcelable)
}