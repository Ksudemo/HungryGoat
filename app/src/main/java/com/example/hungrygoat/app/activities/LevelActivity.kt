package com.example.hungrygoat.app.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.hungrygoat.R
import com.example.hungrygoat.app.helpers.CountUpTimer
import com.example.hungrygoat.app.helpers.alertDialogs.LevelInfoDialog
import com.example.hungrygoat.constants.AppConstants
import com.example.hungrygoat.constants.GameStates
import com.example.hungrygoat.constants.PickedOptions
import com.example.hungrygoat.constants.SingletonAppConstantsInfo
import com.example.hungrygoat.gameLogic.game.GameThread
import com.example.hungrygoat.gameLogic.game.GameView

@Suppress("SameParameterValue")
class LevelActivity() : AppCompatActivity(), OnClickListener, GameThread.LevelDoneListener {

    private lateinit var backImgButton: ImageButton
    private lateinit var levelCondImgButton: ImageButton
    private lateinit var clearCanvasImgButton: ImageButton
    private lateinit var playImgButton: ImageButton
    private lateinit var timeTextView: TextView

    private lateinit var timer: CountUpTimer
    private lateinit var gameView: GameView

    private var lastTime = 0L
    private val duration = 1000L

    private val buttons = mutableListOf<Button>()

    private lateinit var defaultBackgroundDrawable: Drawable
    private lateinit var clickedBackgroundDrawable: Drawable

    private lateinit var appConstants: AppConstants

    private val dialogTag = "MyDialog"

    private var levelCondition = ""

    private lateinit var gameThread: GameThread
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.level_layout)

        val extras = intent.extras
        levelCondition = extras?.getString("levelCondition") ?: "Ошибка в определнии условия"

        setViews()

        appConstants = SingletonAppConstantsInfo.getAppConst()

        val s1 = resources.getString(R.string.pick_cell_size)
        val s2 = resources.getString(R.string.enableDrawCellIndex)
        val s3 = resources.getString(R.string.enableDrawRopeNodes)
        val s4 = resources.getString(R.string.enableRenderGoatBounds)
        val s5 = resources.getString(R.string.enableRenderWolfBounds)

        val settings = applicationContext.getSharedPreferences(
            resources.getString(R.string.sharedPrefsSettingsName),
            Context.MODE_PRIVATE
        )

        settings.apply {
            appConstants.setGameSettings(
                getFloat(s1, 20f),
                "",
                getBoolean(s2, false),
                getBoolean(s3, false),
                getBoolean(s4, false),
                getBoolean(s5, false)
            )
        }
        setTimer()
    }
    override fun onResume() {
        super.onResume()
        val holder = gameView.holder

        gameThread = GameThread(holder, gameView, "MyFavoriteThread", levelCondition)
        gameThread.registerEventListener(this)

        gameView.setGameThread(gameThread)

        gameView.initView()
    }

    override fun onStop() {
        super.onStop()

        gameThread.unregisterEventListener(this)
        appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)
        appConstants.getEngine().killEngine()
    }

    override fun onLevelDoneEvent() {
        Log.d("MyTag", "Level done!")
//        val manager = supportFragmentManager
//        val transaction = manager.beginTransaction()
//        val dialog = LevelDoneDialog()
//
//        val bundle = Bundle()
//        bundle.putString("title", "Поздравляю!")
//        bundle.putString(
//            "text",
//            "Поздравляю, вы прошли уровень за ${timeTextView.text}!\n Хотите попробовать ещё раз или перейти к следующему уровню?"
//        )
//        bundle.putString("posButton", "Следующий уровень")
//        bundle.putString("negButton", "Ещё раз")
//
//        dialog.arguments = bundle
//        dialog.show(manager, dialogTag)
//        transaction.commit()

//        while (dialog.isPositiveButtonClicked() == null) {}

//        val shouldStartNewLevel = dialog.isPositiveButtonClicked()!!
//
//        if (shouldStartNewLevel) {
//            createDialog("агаа", "Будут уровни - будет и переход между ними (:", "Окк", "Бубубу")
//            //
//            appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)
//            updateTimer(appConstants.getState()!!, false)
//            // New Code level
//        } else {
//            appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)
//            updateTimer(appConstants.getState()!!, false)
//        }
    }

    private fun setViews() {
        backImgButton = findViewById(R.id.backImgButton)
        levelCondImgButton = findViewById(R.id.levelConditionImgButton)
        clearCanvasImgButton = findViewById(R.id.clearCanvasImgButton)
        playImgButton = findViewById(R.id.playImgButton)
        timeTextView = findViewById(R.id.timeTextView)

        val ll = findViewById<LinearLayout>(R.id.levelLinearLayout)

        gameView = GameView(applicationContext)
        gameView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { weight = 1f }
        ll.addView(gameView)

        ll.addView(getButtonsLinearLayout(4, listOf("Колышек", "Верёвка", "Коза", "Собака")))

        backImgButton.setOnClickListener(this)
        levelCondImgButton.setOnClickListener(this)
        clearCanvasImgButton.setOnClickListener(this)
        playImgButton.setOnClickListener(this)
    }

    private fun setTimer() {
        timer = object : CountUpTimer(duration) {
            @SuppressLint("SetTextI18n")
            override fun onTick(second: Int) {
                val curTime = lastTime + second
                timeTextView.text = "$curTime сек."
            }

            override fun onFinish() {
                super.onFinish()
                lastTime += (duration / 1000)
                timer.start()
            }
        }

        updateTimer(GameStates.STATE_PLAYER_PLACE_OBJECTS, false)
    }

    private fun createDialog(
        titleString: String,
        textString: String,
        posString: String,
        negString: String,
    ) {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        val dialog = LevelInfoDialog()

        val bundle = Bundle()
        bundle.putString("title", titleString)
        bundle.putString("text", textString)
        bundle.putString("posButton", posString)
        bundle.putString("negButton", negString)

        dialog.arguments = bundle
        dialog.show(manager, dialogTag)
        transaction.commit()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backImgButton -> {
                startActivity(
                    Intent(
                        applicationContext,
                        StartActivity::class.java
                    )
                )
            }

            R.id.levelConditionImgButton -> {
                createDialog(
                    "Условие уровня",
                    "Нарисуйте $levelCondition",
                    "Круто!",
                    "Не круто :("
                )
            }

            R.id.clearCanvasImgButton -> {
                updateTimer(GameStates.STATE_PLAYER_PLACE_OBJECTS, true)
                appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)
                appConstants.getEngine().clearObjects()

                recheckButtons(buttons, -1)
            }

            R.id.playImgButton -> {
                val currentState =
                    if (appConstants.getState() == GameStates.STATE_PLAYER_PLACE_OBJECTS)
                        GameStates.STATE_OBJECTS_MOVING
                    else
                        GameStates.STATE_PLAYER_PLACE_OBJECTS

                updateTimer(currentState, false)
                appConstants.changeState(currentState)
            }

            else -> return
        }
    }

    private fun updateTimer(state: GameStates, resetTimer: Boolean) {
        lastTime = if (resetTimer) 0 else lastTime
        if (state == GameStates.STATE_OBJECTS_MOVING) {
            timer.cancel()
//            timeTextView.text = "" //resources.getText(R.string.pause)
        } else timer.start()
    }

    private fun recheckButtons(buttons: MutableList<Button>, i: Int) {
        buttons.forEach {
            it.background = defaultBackgroundDrawable
        }
        if (i in buttons.indices)
            buttons[i - 1].background = clickedBackgroundDrawable
    }

    private fun getButtonsLinearLayout(count: Int, names: List<String>): LinearLayout {

        val buttonsPaneLinearLayout = LinearLayout(this)

        val buttonsPanelParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        buttonsPaneLinearLayout.layoutParams = buttonsPanelParams
        buttonsPaneLinearLayout.orientation = LinearLayout.HORIZONTAL
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.weight = 1F

        clickedBackgroundDrawable = ResourcesCompat.getDrawable(
            resources,
            androidx.appcompat.R.color.material_blue_grey_800, theme
        )!!

        for (i in 1..count) {
            val b = Button(applicationContext).apply {
                background = ResourcesCompat.getDrawable(resources, R.drawable.button_style, theme)
                defaultBackgroundDrawable = background
                id = i
                text = names[i - 1]
                layoutParams = params
                setOnClickListener {
                    val pickedOption = when (i) {
                        1 -> PickedOptions.PEG
                        2 -> PickedOptions.ROPE
                        3 -> PickedOptions.GOAT
                        4 -> PickedOptions.WOLF
                        else -> PickedOptions.CLEAR
                    }
                    recheckButtons(buttons, i)
                    appConstants.changeOption(pickedOption)
                }
                buttonsPaneLinearLayout.addView(this)
            }
            buttons.add(b)
        }
        return buttonsPaneLinearLayout
    }

}