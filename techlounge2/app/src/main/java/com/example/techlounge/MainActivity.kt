package com.example.techlounge

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        var count = 0
        val stdNo = findViewById<TextView>(R.id.stdNo)
        val checkIn = findViewById<Button>(R.id.checkIn)
        val checkOut = findViewById<Button>(R.id.checkOut)
        checkIn.setOnClickListener {
            count++
            stdNo.setText(count.toString())
        }
        checkOut.setOnClickListener {
            count--
            if (count <=0) {
                count = 0
            }
            stdNo.setText(count.toString())
        }
    }
}