package com.joma.slow.ui.custom

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.ImageView
import com.joma.slow.R

class WarningDialog (context: Context, listener: Confirm) {

    init {
        val dialog = Dialog(context)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_warning)
        val tick = dialog.findViewById<ImageView>(R.id.tick)
        val close = dialog.findViewById<ImageView>(R.id.close)
        close.setOnClickListener {
            dialog.dismiss()
        }
        tick.setOnClickListener {
            listener.tick()
            dialog.dismiss()
        }
        dialog.show()
    }

    interface Confirm{
        fun tick()
    }
}