package com.joma.slow.ui.custom

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.TextView
import com.joma.slow.R

class HowItWorksDialog (context: Context) {

    init {
        val dialog = Dialog(context, android.R.style.Theme_Black)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_how_to_use)
        val textView = dialog.findViewById<TextView>(R.id.got_it)
        textView.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}