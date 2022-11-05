package com.joma.slow.ui.utils

import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener

abstract class SimpleSeekBarListener: OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }
}