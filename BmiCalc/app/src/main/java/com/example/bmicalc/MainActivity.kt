package com.example.bmicalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bmicalc.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        btnCalculate.setOnClickListener {

            val weightText = etWeight.text.toString().trim()
            val heightText = etHeight.text.toString().trim()

            // Check empty fields
            if (weightText.isEmpty() || heightText.isEmpty()) {
                Toast.makeText(this, "Please enter weight and height", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weight = weightText.toDouble()
            val height = heightText.toDouble()

            if (height == 0.0) {
                Toast.makeText(this, "Height cannot be zero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // BMI Formula
            val bmi = weight / (height * height)

            // Categorize BMI
            val category = when {
                bmi < 18.5 -> "Underweight"
                bmi < 24.9 -> "Normal Weight"
                bmi < 29.9 -> "Overweight"
                else -> "Obese"
            }

            tvResult.text = "BMI: %.2f\nCategory: %s".format(bmi, category)
        }
    }
}