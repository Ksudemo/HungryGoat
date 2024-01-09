package com.example.hungrygoat.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.hungrygoat.R
import com.example.hungrygoat.constants.AppConstants
import com.example.hungrygoat.constants.SingletonAppConstantsInfo

class StartActivity : AppCompatActivity(), OnClickListener {
    private lateinit var toolbar: Toolbar

    private lateinit var playLastButton: Button
    private lateinit var levelSelectButton: Button
    private lateinit var settingsButton: ImageButton
    private lateinit var rulesButton: Button

    private lateinit var appConstants: AppConstants

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_layout)

        setViews()
        appConstants = SingletonAppConstantsInfo.getAppConst()
        appConstants.setLevelConditionsList()
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
            R.id.playLastButton -> {

                val lastLevelPlayedSharedPrefsStr =
                    resources.getString(R.string.sharedPrefsSettingsName)
                val lastLevelPlayedStr = resources.getString(R.string.lastLevelPlayed)
                val lastLevelPlayedPrefs = applicationContext.getSharedPreferences(
                    lastLevelPlayedSharedPrefsStr,
                    Context.MODE_PRIVATE
                )
                val lastLevelPlayed = lastLevelPlayedPrefs.getInt(lastLevelPlayedStr, 0)
                val lastLevelPlayedCond =
                    appConstants.levelsList[if (lastLevelPlayed in appConstants.levelsList.indices) lastLevelPlayed else 0]

                val extras = Bundle().apply {
                    putSerializable("caller", StartActivity::class.java)
                    putSerializable("levelCondition", lastLevelPlayedCond.first)
                }

                Intent(
                    applicationContext,
                    LevelActivity::class.java
                ).putExtras(extras)
            }

            R.id.settingsButton -> Intent(applicationContext, SettingsActivity::class.java)
            R.id.levelSelectButton -> Intent(applicationContext, LevelSelectionActivity::class.java)
            R.id.rulesButton -> Intent(applicationContext, RulesActivity::class.java)
            else -> Intent(applicationContext, SettingsActivity::class.java)
        }

        startActivity(intent)
    }
}