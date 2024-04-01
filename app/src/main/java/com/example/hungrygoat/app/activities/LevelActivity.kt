package com.example.hungrygoat.app.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
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
import com.example.hungrygoat.constants.appContants.AppConstants
import com.example.hungrygoat.constants.appContants.SingletonAppConstantsInfo
import com.example.hungrygoat.constants.appContants.educationLevelConditions
import com.example.hungrygoat.constants.appContants.translatedMap
import com.example.hungrygoat.constants.enums.GameStates
import com.example.hungrygoat.constants.enums.LevelConditions
import com.example.hungrygoat.constants.enums.PickedOptions
import com.example.hungrygoat.gameLogic.game.GameThread
import com.example.hungrygoat.gameLogic.game.GameView
import com.example.hungrygoat.gameLogic.interfaces.dogListeners.DogUnboundedListener
import com.example.hungrygoat.gameLogic.interfaces.enigneListeners.CheckSolutionListener
import com.example.hungrygoat.gameLogic.interfaces.enigneListeners.RopeDepthLevelBoundsListener
import com.example.hungrygoat.gameLogic.interfaces.goatListeners.GoatBoundsTouchEdgesListener
import com.example.hungrygoat.gameLogic.interfaces.goatListeners.GoatUnboundedListener


@Suppress("SameParameterValue")
class LevelActivity : AppCompatActivity(), View.OnClickListener, GoatBoundsTouchEdgesListener,
    GoatUnboundedListener, DogUnboundedListener, CheckSolutionListener,
    RopeDepthLevelBoundsListener {

    private lateinit var exitButton: ImageButton
    private lateinit var hintImgButton: ImageButton
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
    private var firstStart = true

    private lateinit var appConstants: AppConstants

    private var levelCondition: LevelConditions = LevelConditions.EMPTY

    private lateinit var gameThread: GameThread

    private val buttonHelper = ButtonHelper()
    private val dialogHelper = DialogHelper()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            when (resources.configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> R.layout.level_layout_portrait
                Configuration.ORIENTATION_LANDSCAPE -> R.layout.level_layout_landscape
                else -> R.layout.level_layout_portrait
            }
        )
        Log.d("mytag", "onCreate")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(applicationContext, StartActivity::class.java))
            }
        })

        levelCondition = getLevelCondition()
        appConstants = SingletonAppConstantsInfo.getAppConst()
        appConstants.setResourse(resources)
        appConstants.setCurStepEnum(null)


        setViews()
        setSettings()

        resetCanvas()
        if (levelCondition == LevelConditions.MOON)
            noteAbautDogs() else levelCondImgButton.callOnClick()
    }

    override fun onResume() {
        super.onResume()
        Log.d("mytag", "onResume")
        val holder = gameView.holder
        appConstants.getEngine().apply {
            registerMovableErrorsListener(this@LevelActivity, this@LevelActivity)
            regesterCheckSolutionListener(this@LevelActivity)
            registerRopeDepthLevelBoundsListener(this@LevelActivity)
            getRenderSerivece().registerGoatBoundsTouchEdgesListener(this@LevelActivity)
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
            unregisterRopeDepthLevelBoundsListener()
            getRenderSerivece().unregisterGoatBoundTouchEdgesListener()
        }
        appConstants.getEngine().killEngine()
        appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)
    }

    private fun getLevelCondition(): LevelConditions {
        val extras = intent.extras
        @Suppress("DEPRECATION")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras?.getSerializable("levelCondition", LevelConditions::class.java)
                ?: LevelConditions.EMPTY
        } else extras?.getSerializable("levelCondition") as LevelConditions
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

    private fun nextLevel() {
        Log.d("mytag", "nextLevel")
        if (!appConstants.canGoToNextLevel(levelCondition)) {
            val intent = Intent(applicationContext, LevelSelectionActivity::class.java)
            startActivity(intent)
        }

        val newCond = appConstants.nextLevelCondition(levelCondition)
        if (educationLevelConditions.contains(newCond)) {
            val intent = Intent(applicationContext, EducationActivity::class.java)
            intent.putExtra("eduCondition", newCond.toString())
            startActivity(intent)
        }

        if (newCond == null) {
            val neg = ResourcesCompat.getDrawable(
                resources, R.mipmap.exit_2, theme
            )!!
            val pos = ResourcesCompat.getDrawable(
                resources, R.mipmap.next, theme
            )!!
            val bundle = Bundle()
            bundle.putString("text", "Ура!")

            dialogHelper.createDialog(
                this,
                bundle,
                neg,
                pos,
                { startActivity(Intent(applicationContext, StartActivity::class.java)) },
                { levelCondition = appConstants.levelsInfo.first().levelCondition }
            )
        } else
            levelCondition = newCond

        resetEngineAndState()
        buttonHelper.resetButtons(pegButton, ropeButton, goatButton, dogButton, eraserButton, null)

        firstStart = true
        if (levelCondition == LevelConditions.MOON)
            noteAbautDogs()
        else levelCondImgButton.callOnClick()
    }

    private fun onLevelComplete() {
        val repeatClickListener = {
            resetCanvas()
        }

        val nextLevelClickListener = {
            nextLevel()
        }
        val rating = when {
            lastTime <= 30 -> 6
            lastTime in 31..60 -> 5
            lastTime in 61..90 -> 4
            lastTime in 91..120 -> 3
            lastTime in 121..150 -> 2
            lastTime in 151..180 -> 1
            else -> 0
        }

        appConstants.win(levelCondition, rating)

        val neg = ResourcesCompat.getDrawable(
            resources, R.mipmap.restart, theme
        )!!
        val pos = ResourcesCompat.getDrawable(
            resources, R.mipmap.next, theme
        )!!
        val bundle = Bundle()
        bundle.putString("text", "$lastTime сек.")
        dialogHelper.createDialog(
            this,
            bundle,
            neg,
            pos,
            repeatClickListener,
            nextLevelClickListener,
            rating
        )
    }


    private fun onLevelFailed() {
        val negClickListener = { resetCanvas() }
        val posClickListener = {
            Toast.makeText(
                applicationContext,
                "Не расстривайся!",
                Toast.LENGTH_SHORT
            ).show()
        }
        val neg = ResourcesCompat.getDrawable(resources, R.mipmap.restart, theme)!!
        val pos = ResourcesCompat.getDrawable(resources, R.mipmap.eye, theme)!!

        val bundle = Bundle()
        bundle.putString("text", "Вы не прошли уровень")
        dialogHelper.createDialog(
            this,
            bundle,
            neg,
            pos,
            negClickListener,
            posClickListener
        )
    }

    override fun checkSolution(result: List<LevelConditions>, dogsSize: Int) {
        if (levelCondition == LevelConditions.SANDBOX) return
        Log.d("mytag", "$result\n dogs size - $dogsSize")

        val levelCondIsDogStuff = levelCondition in listOf(
            LevelConditions.TRIANGLE_WITH_DOGS,
            LevelConditions.HALFCIRCLE_WITH_DOGS,
        )

        if (levelCondIsDogStuff) {
            if (!result.contains(LevelConditions.TRIANGLE_WITH_DOGS)
                && result.contains(LevelConditions.TRIANGLE_WITHOUT_DOGS)
            ) {
                dogUnplaced()
                return
            }
            if (!result.contains(LevelConditions.HALFCIRCLE_WITH_DOGS)
                && result.contains(LevelConditions.HALFCIRCLE_WITHOUT_DOGS)
            ) {
                dogUnplaced()
                return
            }
        }

        if (result.contains(levelCondition))
            onLevelComplete()
        else {
            Log.d("mytag", "Cur level cond - $levelCondition || $result ")
            onLevelFailed()
        }
    }

    override fun onRopeToHighDepth() {
        val neg = ResourcesCompat.getDrawable(
            resources, R.mipmap.exit, theme
        )!!
        val pos = ResourcesCompat.getDrawable(
            resources, R.mipmap.ok, theme
        )!!

        val bundle = Bundle()
        bundle.putString("text", "Не нужна вам такая сложная система веревок")
        dialogHelper.createDialog(this,
            bundle,
            neg, pos,
            {}, {})
    }

    override fun onGoatBoundsTouchEdges() {
        if (levelCondition == LevelConditions.SANDBOX) return

        val negClickListener = {}
        val posClickListener = {
            Toast.makeText(
                applicationContext,
                "Коза врезалась в границы экрана",
                Toast.LENGTH_SHORT
            ).show()
        }

        appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)
        val neg = ResourcesCompat.getDrawable(
            resources, R.mipmap.restart, theme
        )!!
        val pos = ResourcesCompat.getDrawable(
            resources, R.mipmap.eye, theme
        )!!
        val bundle = Bundle()
        bundle.putString("text", "Вы не прошли уровень")
        dialogHelper.createDialog(
            this,
            bundle,
            neg,
            pos, negClickListener,
            posClickListener
        )
    }

    private fun createUIThreadToast(text: String, length: Int) {
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                text,
                length
            ).show()
            appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)
            updateTimer(appConstants.getState()!!, false)
        }
    }

    private fun dogUnplaced() {
        Log.d("mytag", "dogUnplaced")
        createUIThreadToast(
            "Этот уровень должен быть пройден с использованием собак",
            Toast.LENGTH_LONG
        )
    }

    override fun onDogUnbounded() {
        Log.d("mytag", "onDogUnbounded")
        createUIThreadToast(
            "Привяжите собаку, козы боятся собак",
            Toast.LENGTH_LONG
        )
    }

    override fun onGoatUnbounded() {
        Log.d("mytag", "onGoatUnbounded")
        createUIThreadToast(
            "Коза не привязана",
            Toast.LENGTH_SHORT
        )
    }

    private fun setViews() {
        fun setListeners() {
            exitButton.setOnClickListener(this)
            hintImgButton.setOnClickListener(this)
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
            exitButton = findViewById(R.id.exitButton)
            hintImgButton = findViewById(R.id.hintImageButton)
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

            Log.d("mytag", "lc - $levelCondition")
            dogButton.visibility = if (levelCondition in listOf(
                    LevelConditions.MOON,
                    LevelConditions.HALFCIRCLE_WITH_DOGS,
                    LevelConditions.RING,
                    LevelConditions.TRIANGLE_WITH_DOGS,
                    LevelConditions.HALFRING,
                    LevelConditions.SANDBOX,
                )
            ) View.VISIBLE else View.GONE

            timeTextView.text = "0"

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

    @SuppressLint("SetTextI18n")
    private fun updateTimer(state: GameStates, resetTimer: Boolean) {
        lastTime = if (resetTimer) 0 else lastTime
        if (state == GameStates.STATE_OBJECTS_MOVING) {
            timer.cancel()
            timeTextView.text = "$lastTime"
        } else timer.start()
    }

    private fun resetEngineAndState() {
        appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)
        appConstants.getEngine().clearObjects()
    }

    private fun resetCanvas() {
        if (::timer.isInitialized)
            updateTimer(GameStates.STATE_PLAYER_PLACE_OBJECTS, true)
        resetEngineAndState()

        buttonHelper.resetButtons(pegButton, ropeButton, goatButton, dogButton, eraserButton, null)
    }

    private fun noteAbautDogs() {
        val preffs = getSharedPreferences("noteAbautDogs", Context.MODE_PRIVATE)
        val isShown = preffs.getBoolean("isShown", false)

        Log.d("mytag", "isShown - $isShown")
        if (!isShown) {
            preffs.edit().putBoolean("isShown", true).apply()

            val bundle = Bundle()
            bundle.putString(
                "text",
                "Вы открыли категорию «Собаки».\n При прохождении уровней этой категории необходимо использовать собак."
            )
            bundle.putBoolean("hideExitButton", true)
            val pos = ResourcesCompat.getDrawable(resources, R.mipmap.ok, theme)!!
            val posClick = {
                levelCondImgButton.callOnClick()
                Unit
            }
            dialogHelper.createDialog(
                this,
                bundle, null, pos, {}, posClick
            )
        } else levelCondImgButton.callOnClick()
    }

    override fun onClick(view: View?) =
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

            R.id.hintImageButton -> {
                val hintString = when (levelCondition) {
                    LevelConditions.LEAF -> "Больше кругов" // 2
                    LevelConditions.HALFCIRCLE_WITHOUT_DOGS -> "Круг плюс прямая" // 4
                    LevelConditions.RECTANGLE -> "Стадиончики" // 5
                    LevelConditions.PARALLELOGRAM -> "Параллельные прямые" // 6
                    LevelConditions.HEXAGON -> "Больше прямых" // 7
                    LevelConditions.TRIANGLE_WITHOUT_DOGS -> "Кусочек параллелограмма" // 8
                    LevelConditions.RAINDROP -> "Не круг" // 9
                    LevelConditions.ARROW -> "Ещё больше прямых" // 10
                    LevelConditions.MOON -> "Круг минус круг" // 11
                    LevelConditions.RING -> "Ещё раз круг минус круг" // 12
                    LevelConditions.TRIANGLE_WITH_DOGS -> "Все ещё кусочек параллелограмма" // 13
                    LevelConditions.HALFCIRCLE_WITH_DOGS -> "Круг плюс прямая" // 14
                    LevelConditions.HALFRING -> "Полукруг плюс кольцо" // 15
                    else -> ""
                }

                val pos = ResourcesCompat.getDrawable(resources, R.mipmap.ok, theme)!!
                val bundle = Bundle()
                bundle.putString("text", hintString)
                bundle.putBoolean("hideExitButton", true)
                dialogHelper.createDialog(
                    this,
                    bundle,
                    null, pos,
                    {}, { }
                )
            }

            R.id.reverLastMoveImgButton -> appConstants.getEngine().revertLastMove()

            R.id.levelConditionImgButton -> run {
                if (firstStart)
                    timeTextView.text = "0"


                if (levelCondition == LevelConditions.SANDBOX) {
                    val testString = "Развлекайтесь"
                    val pos = ResourcesCompat.getDrawable(
                        resources, R.mipmap.ok, theme
                    )!!
                    val bundle = Bundle()
                    bundle.putString("text", testString)
                    bundle.putBoolean("hideExitButton", true)
                    dialogHelper.createDialog(
                        this,
                        bundle,
                        null, pos,
                        {}, { }
                    )

                    return@run
                }

                val posCLick = {
                    Log.d("mytag", "first start - $firstStart")
                    if (firstStart) {
                        setTimer()
                        updateTimer(appConstants.getState()!!, true)
                        firstStart = false
                    }
                }

                val str = (translatedMap[levelCondition] ?: "").split(" ").first()
                dialogHelper.createLevelConditionDialog(
                    this, getCondDrawableID(), str, posCLick
                )
            }

            R.id.clearCanvasImgButton -> resetCanvas()

            R.id.playImgButton -> run {
                val currentState =
                    if (appConstants.getState() == GameStates.STATE_PLAYER_PLACE_OBJECTS)
                        GameStates.STATE_OBJECTS_MOVING
                    else
                        GameStates.STATE_PLAYER_PLACE_OBJECTS

                if (currentState == GameStates.STATE_OBJECTS_MOVING && !appConstants.goatAvaliable()) {
                    Toast.makeText(applicationContext, "Поставьте козу", Toast.LENGTH_SHORT).show()
                    return@run
                }

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

            else -> Unit
        }


    fun getCondDrawableID(): Int =
        when (levelCondition) {
            LevelConditions.CIRCLE -> R.mipmap.condition_circle
            LevelConditions.LEAF -> R.mipmap.condition_leaf
            LevelConditions.OVAL -> R.mipmap.condition_oval
            LevelConditions.HALFCIRCLE_WITHOUT_DOGS -> R.mipmap.condition_halfcircle
            LevelConditions.RECTANGLE -> R.mipmap.condition_rectangle
            LevelConditions.PARALLELOGRAM -> R.mipmap.condition_parallelogram
            LevelConditions.HEXAGON -> R.mipmap.condition_hexagon
            LevelConditions.TRIANGLE_WITHOUT_DOGS -> R.mipmap.condition_triangle
            LevelConditions.RAINDROP -> R.mipmap.condition_raindrop
            LevelConditions.ARROW -> R.mipmap.condition_arrow
            LevelConditions.MOON -> R.mipmap.condition_moon
            LevelConditions.RING -> R.mipmap.condition_ring
            LevelConditions.TRIANGLE_WITH_DOGS -> R.mipmap.condition_triangle
            LevelConditions.HALFCIRCLE_WITH_DOGS -> R.mipmap.condition_halfcircle
            LevelConditions.HALFRING -> R.mipmap.condition_halfring
            LevelConditions.EMPTY -> -1
            LevelConditions.SANDBOX -> -1
            LevelConditions.EDUCATION -> -1
        }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setContentView(
            when (resources.configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> R.layout.level_layout_portrait
                Configuration.ORIENTATION_LANDSCAPE -> R.layout.level_layout_landscape
                else -> R.layout.level_layout_portrait
            }
        )
        Log.d("mytag", "config changed")
        setViews()
        updateTimer(appConstants.getState()!!, false)

        resetEngineAndState()
        levelCondImgButton.callOnClick()
    }
}