package com.example.hungrygoat.app.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.hungrygoat.R
import com.example.hungrygoat.app.helpers.ButtonHelper
import com.example.hungrygoat.app.helpers.CountUpTimer
import com.example.hungrygoat.app.helpers.alertDialogs.DialogHelper
import com.example.hungrygoat.app.helpers.alertDialogs.GameDialog
import com.example.hungrygoat.constants.appContants.AppConstants
import com.example.hungrygoat.constants.appContants.SingletonAppConstantsInfo
import com.example.hungrygoat.constants.enums.EducationStepTags
import com.example.hungrygoat.constants.enums.GameStates
import com.example.hungrygoat.constants.enums.LevelConditions
import com.example.hungrygoat.constants.enums.PickedOptions
import com.example.hungrygoat.gameLogic.game.GameThread
import com.example.hungrygoat.gameLogic.game.GameView
import com.example.hungrygoat.gameLogic.interfaces.EducationStepDoneListener
import com.example.hungrygoat.gameLogic.interfaces.dogListeners.DogUnboundedListener
import com.example.hungrygoat.gameLogic.interfaces.enigneListeners.CheckSolutionListener
import com.example.hungrygoat.gameLogic.interfaces.goatListeners.GoatBoundsTouchEdgesListener

@Suppress("SameParameterValue")
class EducationActivity : AppCompatActivity(), View.OnClickListener, DogUnboundedListener,
    GoatBoundsTouchEdgesListener, CheckSolutionListener, EducationStepDoneListener {

    private lateinit var exitMainMenuImgButton: ImageButton
    private lateinit var revertLastMoveImgButton: ImageButton
    private lateinit var levelCondImgButton: ImageButton
    private lateinit var clearCanvasImgButton: ImageButton
    private lateinit var playImgButton: ImageButton
    private lateinit var timeTextView: TextView

    private lateinit var pegButton: ImageButton
    private lateinit var ropeButton: ImageButton
    private lateinit var goatButton: ImageButton
    private lateinit var dogButton: ImageButton
    private lateinit var eraserButton: ImageButton

    private lateinit var timer: CountUpTimer
    private lateinit var gameView: GameView

    private var lastTime = 0L
    private val duration = 1000L

    private lateinit var appConstants: AppConstants

    private lateinit var gameThread: GameThread

    private val dialogHelper = DialogHelper()
    private val buttonHelper = ButtonHelper()

    private var steps = listOf<Pair<String, EducationStepTags>>()
    private var levelCondition = LevelConditions.EMPTY

    private val toastText = "Прожорливая коза ест всю траву, до которой может дотянутся"
    private val part1 = listOf(
        "Поставьте козу на поле" to EducationStepTags.PLACE_GOAT,
        "Поставьте колышек рядом с козой" to EducationStepTags.PLACE_PEG,
        "Выберите веревку и нажмите по козе" to EducationStepTags.TIE_GOAT,
        "Нажмите по колышку" to EducationStepTags.TIE_PEG,
        "Нажмите на кнопку играть" to EducationStepTags.TRIGGER_PLAY_BUTTON,
        toastText to EducationStepTags.AWAIT,
    )

    private val part2 = listOf(
        "Поставьте колышек на поле" to EducationStepTags.PLACE_PEG,
        "Поставьте колышек в другом месте" to EducationStepTags.PLACE_PEG,
        "Выберите веревку и нажмите ей по колышку" to EducationStepTags.TIE_PEG,
        "Нажмите по другому колышку" to EducationStepTags.TIE_PEG,
        "Поставьте козу рядом с только что натянутой веревкой" to EducationStepTags.PLACE_GOAT,
        "Нажмите веревкой по козе" to EducationStepTags.TIE_GOAT,
        "Нажмите веревкой по веревке" to EducationStepTags.TIE_ROPE,
        "Нажмите на кнопку играть" to EducationStepTags.TRIGGER_PLAY_BUTTON,
        toastText to EducationStepTags.AWAIT,
    )

    private val part3 = listOf(
        "Поставьте козу на поле" to EducationStepTags.PLACE_GOAT,
        "Поставьте колышек рядом с козой" to EducationStepTags.PLACE_PEG,
        "Выберите веревку и нажмите ей по козе" to EducationStepTags.TIE_GOAT,
        "Нажмите веревкой по колышку" to EducationStepTags.TIE_PEG,
        "Поставьте ещё один колышек с другой стороны от козы" to EducationStepTags.PLACE_PEG,
        "Поставьте собаку в границах досягаемости козы" to EducationStepTags.PLACE_DOG,
        "Нажмите веревкой по недавно поставленному колышку" to EducationStepTags.TIE_PEG,
        "Нажмите веревкой по собаке" to EducationStepTags.TIE_DOG,
        "Нажмите на кнопку играть" to EducationStepTags.TRIGGER_PLAY_BUTTON,
        toastText to EducationStepTags.AWAIT,
    )

    private var curStep = 0
    private var categoryName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.level_layout_portrait)
        setViews()

        val lc = intent.getStringExtra("eduCondition")
        when (lc) {
            LevelConditions.CIRCLE.toString() -> {
                levelCondition = LevelConditions.CIRCLE
                categoryName = "Коза и колышки"
                steps = part1

                eraserButton.visibility = View.GONE
                dogButton.visibility = View.GONE
            }

            LevelConditions.OVAL.toString() -> {
                levelCondition = LevelConditions.OVAL
                categoryName = "Скользящая верёвка"
                steps = part2

                eraserButton.visibility = View.GONE
                dogButton.visibility = View.GONE
            }

            LevelConditions.MOON.toString() -> {
                levelCondition = LevelConditions.MOON
                categoryName = "Cобаки"
                steps = part3

                eraserButton.visibility = View.GONE
                dogButton.visibility = View.VISIBLE
            }
        }

        findViewById<ImageButton>(R.id.hintImageButton).setOnClickListener {
            Toast.makeText(applicationContext, "Следуйте инструкциям", Toast.LENGTH_SHORT).show()
        }

        Log.d("mytag", "lc - $lc")
        appConstants = SingletonAppConstantsInfo.getAppConst()
        appConstants.setResourse(resources)
        appConstants.setCurStepEnum(steps[curStep].second)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(applicationContext, StartActivity::class.java))
                appConstants.getEngine().clearObjects()
            }
        })

        setSettings()
        setTimer()

        resetCanvas()

        levelCondImgButton.callOnClick()
        greetingDialog()
    }

    override fun onResume() {
        super.onResume()
        val holder = gameView.holder

        appConstants.getEngine().apply {
            registerMovableErrorsListener(this@EducationActivity)
            regesterCheckSolutionListener(this@EducationActivity)
            registereEducationStepDoneListener(this@EducationActivity)
            getRenderSerivece().registerGoatBoundsTouchEdgesListener(this@EducationActivity)
        }

        gameThread = GameThread(holder, "MyFavoriteThread")
        gameView.setGameThread(gameThread)

        gameView.initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameThread.stopThread()

        appConstants.getEngine().apply {
            unregesterMovableErrorsListener()
            unregesterCheckSolutionListener()
            unregistereEducationStepDoneListener()
            getRenderSerivece().unregisterGoatBoundTouchEdgesListener()
        }
        appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)
        appConstants.getEngine().killEngine()
    }

    override fun onStepDone() {
        curStep += 1
        if (curStep !in steps.indices) {
            showCongratulationsDialog()
            return
        }
        appConstants.setCurStepEnum(steps[curStep].second)

        if (steps[curStep].second == EducationStepTags.AWAIT)
            Toast.makeText(applicationContext, steps[curStep].first, Toast.LENGTH_LONG).show()
        else levelCondImgButton.callOnClick()
    }


    private fun showCongratulationsDialog() {
        val intent = Intent(applicationContext, LevelSelectionActivity::class.java)
        val clickListener = {
            appConstants.getEngine().clearObjects()
            startActivity(intent)
        }

        val neg = ResourcesCompat.getDrawable(resources, R.mipmap.eye, theme)!!
        val pos = ResourcesCompat.getDrawable(resources, R.mipmap.next, theme)!!

        val text = "Поздравляем, вы решили базовую задачу из категории «$categoryName».\n" +
                "Удачи в прохождении остальных уровней!"

        val bundle = Bundle()
        bundle.putString("text", text)
        bundle.putBoolean("hideExitButton", true)
        dialogHelper.createDialog(
            this,
            bundle,
            neg,
            pos, {},
            clickListener
        )
        appConstants.win(levelCondition, 6)
    }

    private fun greetingDialog() {
        val pos = ResourcesCompat.getDrawable(resources, R.mipmap.ok, theme)!!
        val text =
            "В ходе этого этапа обучения вам будет по шагам показано как построить базовую фигуру из категории «$categoryName»."

        val bundle = Bundle()
        bundle.putString("text", text)
        bundle.putBoolean("hideExitButton", true)

        dialogHelper.createDialog(
            this,
            bundle,
            null,
            pos, {},
            {}
        )
    }

    override fun checkSolution(result: List<LevelConditions>, dogsSize: Int) {
        if (result.contains(levelCondition))
            onStepDone()
        else {
            val neg = ResourcesCompat.getDrawable(resources, R.mipmap.eye, theme)!!
            val pos = ResourcesCompat.getDrawable(resources, R.mipmap.restart, theme)!!
            val posClickListener = {
                restartEdu()
                resetCanvas()
            }

            val bundle = Bundle()
            bundle.putString("text", "Попробуйте ещё раз!")
            bundle.putBoolean("hideExitButton", true)

            dialogHelper.createDialog(
                this,
                bundle,
                neg, pos,
                {}, posClickListener
            )
        }
        Log.d("mytag", "Cur level cond - $levelCondition || $result ")
    }

    override fun onGoatBoundsTouchEdges() {
        appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)

        val neg = ResourcesCompat.getDrawable(
            resources, R.mipmap.next, theme
        )!!
        val pos = ResourcesCompat.getDrawable(
            resources, R.mipmap.eye, theme
        )!!

        val clickListener = {
            restartEdu()
            resetCanvas()
        }

        val bundle = Bundle()
        bundle.putString(
            "text",
            "Коза врезалась в границы экрана, попробуйте привязать козу по-другому"
        )
        bundle.putBoolean("hideExitButton", true)
        dialogHelper.createDialog(
            this,
            bundle,
            neg,
            pos, clickListener, clickListener
        )
    }

    override fun onDogUnbounded() {
        val negClickListener = {}
        val posClickListener = {
            appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)
            Toast.makeText(
                applicationContext,
                "Собака не привязана",
                Toast.LENGTH_SHORT
            ).show()
        }

        val pos = ResourcesCompat.getDrawable(
            resources, R.mipmap.next, theme
        )!!

        val bundle = Bundle()
        bundle.putString("text", "Козы боятся собак")
        bundle.putBoolean("hideExitButton", true)
        dialogHelper.createDialog(
            this,
            bundle,
            null,
            pos, negClickListener,
            posClickListener
        )
    }

    private fun setSettings() {
        val drawGoatBoundsResString = resources.getString(R.string.enableRenderGoatBounds)
        val drawDogBoundsResString = resources.getString(R.string.enableRenderDogBounds)
        val drawGrahamScanLines = resources.getString(R.string.enableGrahamScanLines)
        val changeObjectsSizeString = resources.getString(R.string.changeObjectsSize)

        val settings = applicationContext.getSharedPreferences(
            resources.getString(R.string.sharedPrefsSettingsName),
            MODE_PRIVATE
        )

        settings.apply {
            appConstants.setGameSettings(
                getBoolean(drawGoatBoundsResString, true),
                getBoolean(drawDogBoundsResString, true),
                getBoolean(drawGrahamScanLines, false),
                getFloat(changeObjectsSizeString, 40f)
            )
        }
    }

    private fun setViews() {
        fun setListeners() {
            exitMainMenuImgButton.setOnClickListener(this)
            revertLastMoveImgButton.setOnClickListener(this)
            levelCondImgButton.setOnClickListener(this)
            clearCanvasImgButton.setOnClickListener(this)
            playImgButton.setOnClickListener(this)

            pegButton.setOnClickListener(this)
            ropeButton.setOnClickListener(this)
            goatButton.setOnClickListener(this)
            dogButton.setOnClickListener(this)
            eraserButton.setOnClickListener(this)
        }

        fun findViews() {
            exitMainMenuImgButton = findViewById(R.id.exitButton)
            revertLastMoveImgButton = findViewById(R.id.reverLastMoveImgButton)
            levelCondImgButton = findViewById(R.id.levelConditionImgButton)
            clearCanvasImgButton = findViewById(R.id.clearCanvasImgButton)
            playImgButton = findViewById(R.id.playImgButton)
            timeTextView = findViewById(R.id.timeTextView)
            gameView = findViewById(R.id.gameView)

            pegButton = findViewById(R.id.pegButton)
            ropeButton = findViewById(R.id.ropeButton)
            goatButton = findViewById(R.id.goatButton)
            dogButton = findViewById(R.id.dogButton)
            eraserButton = findViewById(R.id.eraserButton)

            setListeners()
        }

        val defaultColor = Color.TRANSPARENT
        val clickedDrawable = ResourcesCompat.getDrawable(
            resources, R.drawable.border_background, theme
        )!!
        buttonHelper.setBackgrounds(defaultColor, clickedDrawable)

        findViews()
    }

    private fun setTimer() {
        timer = object : CountUpTimer(duration) {
            @SuppressLint("SetTextI18n")
            override fun onTick(second: Int) {
                val curTime = lastTime + second
                timeTextView.text = "$curTime"
            }

            override fun onFinish() {
                super.onFinish()
                lastTime += (duration / 1000)
                timer.start()
            }
        }

        updateTimer(GameStates.STATE_PLAYER_PLACE_OBJECTS, false)
    }

    private fun updateTimer(state: GameStates, resetTimer: Boolean) {
        lastTime = if (resetTimer) 0 else lastTime
        if (state == GameStates.STATE_OBJECTS_MOVING) {
            timer.cancel()
            timeTextView.text = "$lastTime"
        } else timer.start()
    }

    private fun resetCanvas() {
        updateTimer(GameStates.STATE_PLAYER_PLACE_OBJECTS, true)
        appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)
        appConstants.getEngine().clearObjects()

        buttonHelper.resetButtons(pegButton, ropeButton, goatButton, dogButton, eraserButton, null)
    }

    private fun restartEdu() {
        curStep = 0
        levelCondImgButton.callOnClick()
        appConstants.setCurStepEnum(steps[curStep].second)
        greetingDialog()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.exitButton -> {
                appConstants.getEngine().clearObjects()
                startActivity(
                    Intent(
                        applicationContext,
                        LevelSelectionActivity::class.java
                    )
                )
            }

            R.id.reverLastMoveImgButton -> appConstants.getEngine().revertLastMove()

            R.id.levelConditionImgButton -> {
                val curstepString =
                    steps[curStep].first

                val condInfo = curstepString.apply { first().uppercase() }
                val pos = ResourcesCompat.getDrawable(
                    resources, R.mipmap.next, theme
                )!!
                val posClick = if (steps[curStep].second == EducationStepTags.SKIP)
                    GameDialog.OnClickListener { onStepDone() }
                else GameDialog.OnClickListener {}


                val bundle = Bundle()
                bundle.putString("text", condInfo)
                bundle.putBoolean("hideExitButton", true)

                dialogHelper.createDialog(
                    this,
                    bundle,
                    null,
                    pos, {},
                    posClick,
                )
            }

            R.id.clearCanvasImgButton -> {
                if (appConstants.getCurEduStepTag() != EducationStepTags.TRIGGER_CLEAR_BUTTON) {
                    restartEdu()
                } else onStepDone()

                resetCanvas()
            }

            R.id.playImgButton -> {
                if (appConstants.getCurEduStepTag() != EducationStepTags.TRIGGER_PLAY_BUTTON) return
                else onStepDone()

                val currentState =
                    if (appConstants.getState() == GameStates.STATE_PLAYER_PLACE_OBJECTS)
                        GameStates.STATE_OBJECTS_MOVING
                    else
                        GameStates.STATE_PLAYER_PLACE_OBJECTS

                updateTimer(currentState, false)
                appConstants.changeState(currentState)
            }

            R.id.pegButton -> {
                buttonHelper.resetButtons(
                    pegButton,
                    ropeButton,
                    goatButton,
                    dogButton,
                    eraserButton,
                    pegButton
                )

                appConstants.changeOption(PickedOptions.PEG)
            }

            R.id.ropeButton -> {
                buttonHelper.resetButtons(
                    pegButton,
                    ropeButton,
                    goatButton,
                    dogButton,
                    eraserButton,
                    ropeButton
                )

                appConstants.changeOption(PickedOptions.ROPE)
            }

            R.id.goatButton -> {
                buttonHelper.resetButtons(
                    pegButton,
                    ropeButton,
                    goatButton,
                    dogButton,
                    eraserButton,
                    goatButton
                )

                appConstants.changeOption(PickedOptions.GOAT)

            }

            R.id.dogButton -> {
                buttonHelper.resetButtons(
                    pegButton,
                    ropeButton,
                    goatButton,
                    dogButton,
                    eraserButton,
                    dogButton
                )

                appConstants.changeOption(PickedOptions.DOG)

            }

            R.id.eraserButton -> {
                buttonHelper.resetButtons(
                    pegButton,
                    ropeButton,
                    goatButton,
                    dogButton,
                    eraserButton,
                    eraserButton
                )

                appConstants.changeOption(PickedOptions.ERASER)
            }
        }
    }
}