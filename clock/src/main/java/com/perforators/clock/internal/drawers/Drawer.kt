package com.perforators.clock.internal.drawers

import android.content.res.TypedArray
import android.os.Bundle

internal interface Drawer : Restorable {
    fun initialize(typedArray: TypedArray)
}

internal interface Restorable {
    fun saveState(): Bundle
    fun restoreState(bundle: Bundle)
}