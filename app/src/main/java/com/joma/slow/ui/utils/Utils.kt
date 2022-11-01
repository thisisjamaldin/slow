package com.joma.slow.ui.utils

import android.content.Context
import android.util.TypedValue

fun dpToPx(context: Context, dp: Int): Int {
    val dMatrix = context.resources.displayMetrics
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        dMatrix
    ).toInt()
}

fun millisecondsToTime(time:String):String{
    val seconds = (time.toLongOrNull()?:0)/1000
    val s: Long = seconds % 60
    val m: Long = seconds / 60
    return String.format("%02d:%02d", m, s)
}