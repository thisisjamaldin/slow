package com.joma.slow.ui.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent


class ProgressSlider : androidx.appcompat.widget.AppCompatSeekBar {
    private var mThumb: Drawable? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun setThumb(thumb: Drawable) {
        super.setThumb(thumb)
        mThumb = thumb
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (event.x >= mThumb!!.bounds.left && event.x <= mThumb!!.bounds.right && event.y <= mThumb!!.bounds.bottom && event.y >= mThumb!!.bounds.top) {
                super.onTouchEvent(event)
            } else {
                return false
            }
        } else if (event.action == MotionEvent.ACTION_UP) {
            return false
        } else {
            super.onTouchEvent(event)
        }
        return true
    }
}