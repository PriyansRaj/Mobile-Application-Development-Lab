package com.example.validationapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pinandpancodevalidation.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etPan = findViewById<EditText>(R.id.etPan)
        val etPincode = findViewById<EditText>(R.id.etPincode)
        val btnValidate = findViewById<Button>(R.id.btnValidate)

        btnValidate.setOnClickListener {

            val pan = etPan.text.toString().trim()
            val pincode = etPincode.text.toString().trim()

            // i) Both fields should not be empty
            if (pan.isEmpty() || pincode.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ii) PAN must be alphanumeric and exactly 10 characters
            val panRegex = Regex("^[A-Z0-9]{10}$")

            if (!panRegex.matches(pan)) {
                Toast.makeText(this, "Invalid PAN Number (10 Alphanumeric Characters Required)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // iii) Pincode must be numeric and exactly 6 digits
            val pinRegex = Regex("^[0-9]{6}$")

            if (!pinRegex.matches(pincode)) {
                Toast.makeText(this, "Invalid Pincode (6 Digit Number Required)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Validation Successful ✅", Toast.LENGTH_LONG).show()
        }
    }
}