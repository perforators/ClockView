package com.perforators.clock

import java.util.TimeZone

fun interface CurrentTimeInMillisProvider {

    /*
        When implementing the method, heavy calculations should be avoided,
        since the frequency of calling the method coincides with the refresh rate of the screen.
        Ideally, the method should simply return a value without calculations.
     */
    fun provide(): Long

    companion object {
        val DEFAULT = CurrentTimeInMillisProvider {
            val currentUtcTime = System.currentTimeMillis()
            val timeZone = TimeZone.getDefault()
            currentUtcTime + timeZone.getOffset(currentUtcTime)
        }
    }
}
