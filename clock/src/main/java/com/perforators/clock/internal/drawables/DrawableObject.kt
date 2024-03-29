package com.perforators.clock.internal.drawables

import android.graphics.Canvas
import android.os.Bundle
import android.os.Parcelable

internal interface DrawableObject : Restorable<Bundle> {
    fun draw(canvas: Canvas)
    fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int) = Unit

    abstract class WithContext<Context>(
        private val contextProvider: ContextProvider<Context>
    ) : DrawableObject {
        override fun draw(canvas: Canvas) {
            draw(canvas, contextProvider.provide())
        }

        abstract fun draw(canvas: Canvas, context: Context)
    }
}

internal fun interface ContextProvider<Context> {
    fun provide(): Context

    abstract class Reusable<Context> :  ContextProvider<Context> {
        private var cachedContext: Context? = null

        override fun provide(): Context {
            return cachedContext?.also { update(it) } ?: createNew().also { cachedContext = it }
        }

        abstract fun createNew(): Context
        abstract fun update(context: Context)
    }
}

internal interface Restorable<T : Parcelable> {
    val key: String
    fun saveState(): T
    fun restoreState(state: T)
}
