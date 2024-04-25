package com.ksudemo.hungrygoat.constants.appContants

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import com.ksudemo.hungrygoat.constants.enums.EducationStepTags
import com.ksudemo.hungrygoat.constants.enums.GameStates
import com.ksudemo.hungrygoat.constants.enums.LevelConditions
import com.ksudemo.hungrygoat.constants.enums.PickedOptions
import com.ksudemo.hungrygoat.gameLogic.game.GameEngine


val translatedMap = mapOf(
    LevelConditions.CIRCLE to "Круг",
    LevelConditions.LEAF to "Лист (Пересечение кругов)",
    LevelConditions.OVAL to "Стадиончик",
    LevelConditions.HALFCIRCLE_WITHOUT_DOGS to "Полукруг (Без собак)",
    LevelConditions.RECTANGLE to "Прямоугольник",
    LevelConditions.PARALLELOGRAM to "Параллелограмм",
    LevelConditions.HEXAGON to "Шестиугольник",
    LevelConditions.TRIANGLE_WITHOUT_DOGS to "Треугольник (Без собак)",
    LevelConditions.TRIANGLE_WITH_DOGS to "Треугольник (С собаками)",
    LevelConditions.RAINDROP to "Капля",
    LevelConditions.ARROW to "Стрелка (Домик)",
    LevelConditions.MOON to "Месяц",
    LevelConditions.RING to "Кольцо",
    LevelConditions.HALFCIRCLE_WITH_DOGS to "Полукруг (С собаками)",
    LevelConditions.HALFRING to "Полукольцо",
)
const val deffultCellSize = 7f


val educationLevelConditions = listOf(LevelConditions.CIRCLE, LevelConditions.OVAL)

class AppConstants {
    val updatePerFrame = 20
    private val engine = GameEngine()

    val needStarsToUnlockLevels = listOf(0..1 to 0, 2..9 to 5, 10..14 to 22)

    fun getEngine() = engine
    fun initEngine(w: Int, h: Int) {
        orientationChanged = false

        engine.setGrid(
            w,
            h,
            deffultCellSize
        )
    }

    private lateinit var gameSettings: GameSettings
    private lateinit var levelConditionPreferences: SharedPreferences

    var levelsInfo = listOf<LevelConditionInfo>()
    fun resetLevelsMap() {
        val editor = levelConditionPreferences.edit()
        levelsInfo.forEach {
            val key = it.levelCondition.toString()
            val ratingKey = "$key/rating"
            val timeKey = "$key/time"

            editor.putInt(ratingKey, 0)
            editor.putLong(timeKey, -1)

            it.rating = 0
            it.time = -1
        }
        editor.apply()
    }

    fun setLevelsMap(context: Context? = null) {
        try {
            if (!::levelConditionPreferences.isInitialized)
                levelConditionPreferences =
                    context!!.getSharedPreferences("LevelConditionPreffsInt", Context.MODE_PRIVATE)

            levelsInfo = LevelConditions.entries.mapNotNull {
                if (translatedMap.containsKey(it))
                    it to translatedMap[it]!!
                else null
            }.associateWith {
                val key = it.first
                val ratingKey = "$key/rating"
                val timeKey = "$key/time"
                val rating = levelConditionPreferences.getInt(ratingKey, 0)
                val time = levelConditionPreferences.getLong(timeKey, -1)

                LevelConditionInfo(
                    it.first,
                    it.second,
                    rating,
                    time
                )
            }.map { it.value }

            Log.d("mytag", "setLevelsMap - $levelsInfo")
        } catch (e: Exception) {
            Log.d("mytag", "setLevelsMap - $e")
        }
    }

    // TODO Remove in release
    fun abuseRating() {
        levelsInfo.forEach {
            val key = it.levelCondition.toString()
            val ratingKey = "$key/rating"
            val timeKey = "$key/time"
            levelConditionPreferences.edit().apply {
                putInt(ratingKey, 6)
                putLong(timeKey, 999)
            }.apply()
        }
        setLevelsMap()
    }


    fun win(lc: LevelConditions, rating: Int, time: Long) {
        val index = levelsInfo.indexOfFirst { it.levelCondition == lc }

        val indexCond = index !in levelsInfo.indices
        val timeCond = (levelsInfo[index].time <= time && levelsInfo[index].time != -1L)
        if (indexCond || timeCond)
            return

        Log.d("mytag", "win")

        levelsInfo[index].rating = rating
        levelsInfo[index].time = time

        val key = lc.toString()
        val ratingKey = "$key/rating"
        val timeKey = "$key/time"

        val editor = levelConditionPreferences.edit()
        editor.putInt(ratingKey, rating)
        editor.putLong(timeKey, time)
        editor.apply()
    }

    fun nextLevelCondition(lc: LevelConditions): LevelConditions? {
        val index = levelsInfo.indexOfFirst { it.levelCondition == lc }
        return if (index + 1 in levelsInfo.indices) levelsInfo[index + 1].levelCondition
        else null
    }

    fun canGoToNextLevel(lc: LevelConditions): Boolean {
        val countStars = levelsInfo.sumOf { it.rating } / 2f

        val index = levelsInfo.indexOfFirst { it.levelCondition == lc } + 1
        val needStars = needStarsToUnlockLevels.find { index in it.first }?.second ?: 0

        Log.d("mytag", "$countStars, $needStars")
        return countStars >= needStars
    }


    private var curEduStepTag: EducationStepTags? = null
    fun getCurEduStepTag() = curEduStepTag
    fun setCurStepEnum(curStepTag: EducationStepTags?) {
        curEduStepTag = curStepTag
    }

    fun setGameSettings(
        drawGoatBounds: Boolean,
        drawDogBounds: Boolean,
        drawGrahamScanLines: Boolean,
        changeObjectsSize: Float
    ) {
        if (::gameSettings.isInitialized)
            engine.killEngine()
        gameSettings = GameSettings(
            drawGoatBounds,
            drawDogBounds,
            drawGrahamScanLines,
            changeObjectsSize
        )
    }

    fun getSetttings() = if (::gameSettings.isInitialized) gameSettings else GameSettings()

    var orientationChanged = true

    private var currentState: GameStates? = null
    fun getState() = currentState
    fun changeState(state: GameStates) {
        currentState = state

        if (currentState == GameStates.STATE_PLAYER_PLACE_OBJECTS)
            getEngine().restoreInitialState()
    }


    private var currentOption: PickedOptions? = null
    fun getOption() = currentOption

    fun goatAvaliable() = getEngine().goatAvaliable()

    fun changeOption(option: PickedOptions) {
        if (option != PickedOptions.ROPE)
            engine.resetTempObj()
        currentOption = option
        Log.d("mytag", "Picked option - $currentOption")
    }

    lateinit var resources: Resources
    fun setResourse(res: Resources) {
        resources = res
    }
}