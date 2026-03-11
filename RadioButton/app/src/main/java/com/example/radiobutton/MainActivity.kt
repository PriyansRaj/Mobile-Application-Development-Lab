package com.example.radiobutton

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val main = findViewById<LinearLayout>(R.id.main)
        val group = findViewById<RadioGroup>(R.id.radioGroup)

        group.setOnCheckedChangeListener { _, checkedId ->

            when (checkedId) {
                R.id.red -> main.setBackgroundColor(Color.RED)
                R.id.blue -> main.setBackgroundColor(Color.BLUE)
                R.id.green -> main.setBackgroundColor(Color.GREEN)
            }
        }
    }
}