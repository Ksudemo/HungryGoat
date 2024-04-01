package com.example.hungrygoat.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.hungrygoat.R
import com.example.hungrygoat.constants.appContants.AppConstants
import com.example.hungrygoat.constants.appContants.SingletonAppConstantsInfo
import com.example.hungrygoat.constants.enums.LevelConditions

class StartActivity : AppCompatActivity(), OnClickListener {
    private lateinit var toolbar: Toolbar

    private lateinit var exitButton: ImageButton
    private lateinit var settingsButton: ImageButton
    private lateinit var rulesButton: ImageButton

    private lateinit var levelSelectButton: ImageButton
    private lateinit var playSandboxButton: ImageButton

    private lateinit var appConstants: AppConstants
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.splash_screen)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_layout)

        setViews()
        appConstants = SingletonAppConstantsInfo.getAppConst()
        appConstants.setLevelsMap(applicationContext)
    }

    private fun setViews() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        exitButton = findViewById(R.id.closeAppButton)
        settingsButton = findViewById(R.id.settingsButton)
        playSandboxButton = findViewById(R.id.playSandboxButtom)
        levelSelectButton = findViewById(R.id.levelSelectButton)
        rulesButton = findViewById(R.id.rulesButton)


        exitButton.setOnClickListener(this)
        settingsButton.setOnClickListener(this)
        playSandboxButton.setOnClickListener(this)
        levelSelectButton.setOnClickListener(this)
        rulesButton.setOnClickListener(this)
    }

    override fun onClick(view: View?) {

        fun getExtras(s: LevelConditions, shouldRestart: Boolean) =
            Bundle().apply {
                putBoolean("shouldRestart", shouldRestart)
                putSerializable("caller", StartActivity::class.java)
                putSerializable("levelCondition", s)
            }

        fun getIntent(s: LevelConditions, shouldRestart: Boolean, actToStart: Class<*>) =
            Intent(applicationContext, actToStart).putExtras(getExtras(s, shouldRestart))

        val intent: Intent = when (view?.id) {

            R.id.playSandboxButtom -> getIntent(
                LevelConditions.SANDBOX,
                false,
                LevelActivity::class.java
            )

            R.id.closeAppButton -> Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            R.id.settingsButton -> Intent(applicationContext, SettingsActivity::class.java)

            R.id.levelSelectButton -> Intent(applicationContext, LevelSelectionActivity::class.java)

            R.id.rulesButton -> Intent(applicationContext, RulesActivity::class.java)

            else -> Intent(applicationContext, SettingsActivity::class.java)
        }

        startActivity(intent)
    }
}