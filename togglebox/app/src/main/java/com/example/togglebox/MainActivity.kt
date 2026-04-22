package com.example.togglebuttondemo


import android.os.Bundle
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.example.togglebox.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toggle = findViewById<ToggleButton>(R.id.toggleBtn)
        val message = findViewById<TextView>(R.id.message)

        toggle.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                message.visibility = TextView.VISIBLE
            } else {
                message.visibility = TextView.GONE
            }

        }
    }
}