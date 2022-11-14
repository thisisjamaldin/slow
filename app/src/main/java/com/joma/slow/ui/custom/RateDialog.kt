package com.joma.slow.ui.custom

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.joma.slow.R

class RateDialog(context: Context) : View.OnClickListener {

    var star1: ImageView
    var star2: ImageView
    var star3: ImageView
    var star4: ImageView
    var star5: ImageView

    init {
        val dialog = Dialog(context)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_rate)
        val submit = dialog.findViewById<TextView>(R.id.submit)
        val close = dialog.findViewById<ImageView>(R.id.close)
        submit.setOnClickListener { dialog.dismiss() }
        close.setOnClickListener { dialog.dismiss() }
        star1 = dialog.findViewById(R.id.star_1)
        star2 = dialog.findViewById(R.id.star_2)
        star3 = dialog.findViewById(R.id.star_3)
        star4 = dialog.findViewById(R.id.star_4)
        star5 = dialog.findViewById(R.id.star_5)
        star1.setOnClickListener(this)
        star2.setOnClickListener(this)
        star3.setOnClickListener(this)
        star4.setOnClickListener(this)
        star5.setOnClickListener(this)
        dialog.show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.star_1 -> {
                star1.setImageResource(R.drawable.ic_star_filled)
                star2.setImageResource(R.drawable.ic_star)
                star3.setImageResource(R.drawable.ic_star)
                star4.setImageResource(R.drawable.ic_star)
                star5.setImageResource(R.drawable.ic_star)
            }
            R.id.star_2 -> {
                star1.setImageResource(R.drawable.ic_star_filled)
                star2.setImageResource(R.drawable.ic_star_filled)
                star3.setImageResource(R.drawable.ic_star)
                star4.setImageResource(R.drawable.ic_star)
                star5.setImageResource(R.drawable.ic_star)
            }
            R.id.star_3 -> {
                star1.setImageResource(R.drawable.ic_star_filled)
                star2.setImageResource(R.drawable.ic_star_filled)
                star3.setImageResource(R.drawable.ic_star_filled)
                star4.setImageResource(R.drawable.ic_star)
                star5.setImageResource(R.drawable.ic_star)
            }
            R.id.star_4 -> {
                star1.setImageResource(R.drawable.ic_star_filled)
                star2.setImageResource(R.drawable.ic_star_filled)
                star3.setImageResource(R.drawable.ic_star_filled)
                star4.setImageResource(R.drawable.ic_star_filled)
                star5.setImageResource(R.drawable.ic_star)
            }
            R.id.star_5 -> {
                star1.setImageResource(R.drawable.ic_star_filled)
                star2.setImageResource(R.drawable.ic_star_filled)
                star3.setImageResource(R.drawable.ic_star_filled)
                star4.setImageResource(R.drawable.ic_star_filled)
                star5.setImageResource(R.drawable.ic_star_filled)
            }
        }
    }
}