package com.perforators.clock.internal

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View

internal class ViewState : View.BaseSavedState {

    private var output: List<Bundle>? = null
    private var input: Parcel? = null

    constructor(superState: Parcelable?) : super(superState)

    private constructor(input: Parcel) : super(input) {
        this.input = input
    }

    fun write(action: () -> List<Bundle>) {
        output = action()
    }

    fun read(action: Parcel.() -> Unit) {
        input?.action()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        output?.forEach { out.writeBundle(it) }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ViewState> {
        override fun createFromParcel(parcel: Parcel): ViewState {
            return ViewState(parcel)
        }

        override fun newArray(size: Int): Array<ViewState?> {
            return arrayOfNulls(size)
        }
    }
}
