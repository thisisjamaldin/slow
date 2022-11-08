package com.joma.slow.model

import android.view.View

data class MSpeed(
    var view: View,
    var width: Int,
    var positionOnTimeLine: Int,
    var speedUp: Boolean
)