package com.example.hungrygoat.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.hungrygoat.R

class StartActivity : AppCompatActivity(), OnClickListener {
    private lateinit var toolbar: Toolbar

    private lateinit var playLastButton: Button
    private lateinit var levelSelectButton: Button
    private lateinit var settingsButton: ImageButton
    private lateinit var rulesButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_layout)
        setViews()
    }

    private fun setViews() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Главное меню"
        setSupportActionBar(toolbar)

        playLastButton = findViewById(R.id.playLastButton)
        settingsButton = findViewById(R.id.settingsButton)
        levelSelectButton = findViewById(R.id.levelSelectButton)
        rulesButton = findViewById(R.id.rulesButton)

        playLastButton.setOnClickListener(this)
        settingsButton.setOnClickListener(this)
        levelSelectButton.setOnClickListener(this)
        rulesButton.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        val intent: Intent = when (view?.id) {
            R.id.playLastButton -> Intent(
                applicationContext,
                LevelActivity::class.java
            ).apply { putExtra("caller", StartActivity::class.java) }

            R.id.settingsButton -> Intent(applicationContext, SettingsActivity::class.java)
            R.id.levelSelectButton -> Intent(applicationContext, LevelSelectionActivity::class.java)
            R.id.rulesButton -> Intent(applicationContext, RulesActivity::class.java)
            else -> Intent(applicationContext, SettingsActivity::class.java)
        }
        startActivity(intent)
    }
}