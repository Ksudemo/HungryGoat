package com.example.hungrygoat.app.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.hungrygoat.R
import com.example.hungrygoat.app.helpers.CountUpTimer
import com.example.hungrygoat.app.helpers.alertDialogs.GameDialog
import com.example.hungrygoat.constants.AppConstants
import com.example.hungrygoat.constants.GameStates
import com.example.hungrygoat.constants.LevelConditions
import com.example.hungrygoat.constants.PickedOptions
import com.example.hungrygoat.constants.SingletonAppConstantsInfo
import com.example.hungrygoat.constants.translatedMap
import com.example.hungrygoat.gameLogic.game.GameThread
import com.example.hungrygoat.gameLogic.game.GameView
import com.example.hungrygoat.gameLogic.interfaces.LevelCompleteListener
import com.example.hungrygoat.gameLogic.interfaces.LevelFailedListener


@Suppress("SameParameterValue")
class LevelActivity : AppCompatActivity(), View.OnClickListener, LevelCompleteListener,
    LevelFailedListener {

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

    private var levelCondition: LevelConditions = LevelConditions.EMPTY

    private lateinit var gameThread: GameThread
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.level_layout)

        val extras = intent.extras

        @Suppress("DEPRECATION")
        levelCondition =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras?.getSerializable("levelCondition", LevelConditions::class.java)
                    ?: LevelConditions.EMPTY
            } else extras?.getSerializable("levelCondition") as LevelConditions

        setViews()

        appConstants = SingletonAppConstantsInfo.getAppConst()

        val s1 = resources.getString(R.string.pick_cell_size)
        val s2 = resources.getString(R.string.enableDrawCellIndex)
        val s3 = resources.getString(R.string.enableDrawRopeNodes)
        val s4 = resources.getString(R.string.enableRenderGoatBounds)
        val s5 = resources.getString(R.string.enableRenderDogBounds)

        val settings = applicationContext.getSharedPreferences(
            resources.getString(R.string.sharedPrefsSettingsName),
            MODE_PRIVATE
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

        gameThread = GameThread(holder, "MyFavoriteThread", levelCondition)
        gameThread.registerLevelEndingListeners(this, this)
        gameView.setGameThread(gameThread)

        gameView.initView()

        levelCondImgButton.callOnClick()
    }

    override fun onStop() {
        super.onStop()
        gameThread.unregisterLevelEndingListeners()
        gameThread.stopThread()

        appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)
        appConstants.getEngine().killEngine()
    }


    override fun onLevelComplete() {
        val negClickListener = { }
        val posClickListener = {
            Toast.makeText(
                applicationContext,
                "Следующий уровень будет потом",
                Toast.LENGTH_SHORT
            ).show()
        }
        createDialog(
            GameDialog(),
            "Уровень закончен.",
            "Вы прошли уровень за $lastTime сек.",
            "Повторить",
            "Следующий уровень",
            negClickListener,
            posClickListener
        )
    }

    override fun onLevelFailed() {
        Log.d("MyTag", "Level Failed!")
        val negClickListener = { resetCanvas() }
        val posClickListener = {
            Toast.makeText(
                applicationContext,
                "Не расстривайся!",
                Toast.LENGTH_SHORT
            ).show()
        }
        createDialog(
            GameDialog(),
            "Неудачная попытка.",
            "Вы не прошли уровень",
            "Сбросить всё",
            "Продолжить",
            negClickListener,
            posClickListener
        )
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

        ll.addView(
            getButtonsLinearLayout(
                5,
                listOf("Колышек", "Верёвка", "Коза", "Собака", "Ластик")
            )
        )

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
        dialog: GameDialog,
        titleString: String,
        textString: String,
        negString: String,
        posString: String,
        negClickListener: GameDialog.OnClickListener,
        posClickListener: GameDialog.OnClickListener,
    ) {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()

        val bundle = Bundle()
        bundle.putString("title", titleString)
        bundle.putString("text", textString)
        bundle.putString("posButton", posString)
        bundle.putString("negButton", negString)

        dialog.setOnClickListener(negClickListener, posClickListener)

        dialog.arguments = bundle
        dialog.show(manager, dialogTag)
        transaction.commit()
    }

    private fun updateTimer(state: GameStates, resetTimer: Boolean) {
        lastTime = if (resetTimer) 0 else lastTime
        if (state == GameStates.STATE_OBJECTS_MOVING) {
            val pauseString = resources.getText(R.string.pause).toString() + "($lastTime сек.)"
            timer.cancel()
            timeTextView.text = pauseString
        } else timer.start()
    }

    private fun resetButtons(buttons: MutableList<Button>, i: Int) {
        buttons.forEach {
            it.background = defaultBackgroundDrawable
        }
        if (i in buttons.indices)
            buttons[i].background = clickedBackgroundDrawable
    }

    private fun resetCanvas() {
        updateTimer(GameStates.STATE_PLAYER_PLACE_OBJECTS, true)
        appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)
        appConstants.getEngine().clearObjects()

        resetButtons(buttons, -1)
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

        for (i in 0 until count) {
            val b = Button(applicationContext).apply {
                background = ResourcesCompat.getDrawable(resources, R.drawable.button_style, theme)
                defaultBackgroundDrawable = background
                id = i
                text = names[i]
                layoutParams = params
                setOnClickListener {
                    val pickedOption = when (i) {
                        0 -> PickedOptions.PEG
                        1 -> PickedOptions.ROPE
                        2 -> PickedOptions.GOAT
                        3 -> PickedOptions.DOG
                        4 -> PickedOptions.ERASER
                        else -> PickedOptions.NULL
                    }
                    resetButtons(buttons, i)
                    Log.d("MyTag", pickedOption.toString())
                    appConstants.changeOption(pickedOption)
                }
                buttonsPaneLinearLayout.addView(this)
            }

            buttons.add(b)
        }

        return buttonsPaneLinearLayout
    }

    override fun onClick(view: View?) =
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
                    GameDialog(),
                    "Условие уровня:",
                    "${translatedMap[levelCondition]}",
                    "Не круто :(",
                    "Круто!", {}, {}
                )
            }

            R.id.clearCanvasImgButton -> resetCanvas()

            R.id.playImgButton -> {
                val currentState =
                    if (appConstants.getState() == GameStates.STATE_PLAYER_PLACE_OBJECTS)
                        GameStates.STATE_OBJECTS_MOVING
                    else
                        GameStates.STATE_PLAYER_PLACE_OBJECTS

                updateTimer(currentState, false)
                appConstants.changeState(currentState)
            }

            else -> Unit
        }
}