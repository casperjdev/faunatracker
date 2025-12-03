package com.example.faunatracker.tools

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.faunatracker.R

object ToastHelper {

    fun show(context: Context?, message: String, duration: Int = Toast.LENGTH_SHORT) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 12, 24, 12)
        }

        val textView = TextView(context).apply {
            text = message
            textSize = 16f
        }

        layout.addView(textView)

        Toast(context).apply {
            this.duration = duration
            this.view = layout
            show()
        }
    }
}