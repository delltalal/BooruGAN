package com.example.boorugan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var list:ListView= findViewById(R.id.list)
        list.setOnItemClickListener { _, _, index, _ ->
            if (index == 0) {// if first item is clicked
                val intent = Intent(this, Upload::class.java)
                startActivity(intent)
            }
            if (index == 1) {// if second item is clicked
                val intent = Intent(this, Generate::class.java)
                startActivity(intent)
            }
        }

    }
}