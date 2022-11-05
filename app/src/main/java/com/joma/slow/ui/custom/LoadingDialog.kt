package com.joma.slow.ui.custom

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import com.joma.slow.R

class LoadingDialog (context: Context) {

    private var dialog: Dialog = Dialog(context)
    private var textView: TextView
    private var process: ProgressBar

    init {
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_loading)
        textView = dialog.findViewById(R.id.dialog_percentage)
        process = dialog.findViewById(R.id.dialog_loading)
    }

    fun showLoading(){
        dialog.show()
    }

    fun progress(v: Int) {
        textView.text = "${if (v>0) v else 0}%"
        process.progress = v
    }

    fun hideLoading() {
        dialog.dismiss()
    }
}