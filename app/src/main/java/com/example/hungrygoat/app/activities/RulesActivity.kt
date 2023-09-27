package com.example.hungrygoat.app.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hungrygoat.R

class RulesActivity : AppCompatActivity() {
    private lateinit var gameRulesTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rules_layout)

        gameRulesTextView = findViewById(R.id.gameRulesTextView)
    }
}
