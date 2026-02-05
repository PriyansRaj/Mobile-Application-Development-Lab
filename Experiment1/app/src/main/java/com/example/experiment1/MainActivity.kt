package com.example.experiment1

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvMsg = findViewById<TextView>(R.id.textView)
        val changeBtn = findViewById<Button>(R.id.ChangeButton)

        var clickCount = 0

        changeBtn.setOnClickListener {

            if (clickCount % 2 == 0) {
                tvMsg.setTextColor(Color.RED)
                tvMsg.typeface = Typeface.SERIF
            } else {
                tvMsg.setTextColor(Color.BLUE)
                tvMsg.typeface = Typeface.MONOSPACE
            }

            clickCount++

            Toast.makeText(
                this,
                "Font and Color Changed",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
