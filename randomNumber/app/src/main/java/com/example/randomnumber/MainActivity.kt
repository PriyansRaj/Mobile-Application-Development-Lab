package com.example.randomnumberapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.randomnumber.R
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etMin = findViewById<EditText>(R.id.etMin)
        val etMax = findViewById<EditText>(R.id.etMax)
        val btnGenerate = findViewById<Button>(R.id.btnGenerate)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        btnGenerate.setOnClickListener {

            val minText = etMin.text.toString().trim()
            val maxText = etMax.text.toString().trim()


            if (minText.isEmpty() || maxText.isEmpty()) {
                Toast.makeText(this, "Please enter both values", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val min = minText.toInt()
            val max = maxText.toInt()


            if (min > max) {
                Toast.makeText(this, "Minimum should be less than Maximum", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val randomNumber = Random.nextInt(min, max + 1)

            tvResult.text = "Generated Number: $randomNumber"
        }
    }
}