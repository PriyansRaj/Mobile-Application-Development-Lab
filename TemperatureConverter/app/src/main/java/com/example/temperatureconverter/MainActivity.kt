package com.example.temperatureconverter

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val etInput = findViewById<EditText>(R.id.editText)
        val btn = findViewById<Button>(R.id.convertBtn)
        val resView = findViewById<TextView>(R.id.resultText)

        btn.setOnClickListener {
            val tempStr = etInput.text.toString().trim()

            if (tempStr.isEmpty()) {
                Toast.makeText(this, "Enter temperature", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val celsius = tempStr.toDoubleOrNull()
            if (celsius == null) {
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fahrenheit = (celsius * 9 / 5) + 32
            resView.text = "The temperature in Fahrenheit is $fahrenheit"
        }
    }
}