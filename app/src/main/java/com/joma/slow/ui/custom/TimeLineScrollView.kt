package com.joma.slow.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView

class TimeLineScrollView : HorizontalScrollView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun fling(velocityX: Int) {
//        super.fling(velocityX)
    }

}