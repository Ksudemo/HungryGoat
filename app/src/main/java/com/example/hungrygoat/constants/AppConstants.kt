package com.example.hungrygoat.constants

import android.content.Context
import android.content.SharedPreferences
import com.example.hungrygoat.gameLogic.game.GameEngine

object SingletonAppConstantsInfo {
    lateinit var appConstants: AppConstants

    fun getAppConst(): AppConstants {
        if (!::appConstants.isInitialized) {
            appConstants = AppConstants()
            appConstants.changeOption(PickedOptions.NULL)
            appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)
        }

        return appConstants
    }
}


val translatedMap = mapOf(
    LevelConditions.CIRCLE to "Круг",
    LevelConditions.HALFCIRCLE to "Полукруг",
    LevelConditions.RING to "Кольцо",
    LevelConditions.HALFRING to "Полукольцо",
    LevelConditions.MOON to "Месяц",
    LevelConditions.RECTANGLE to "Прямоугольник",
    LevelConditions.TRIANGLE to "Треугольник",
    LevelConditions.LEAF to "(Лист) Пересечение кругов",
    LevelConditions.HEXAGON to "Шестиугольник",
    LevelConditions.ARROW to "Стрелка",
    LevelConditions.RAINDROP to "Капля",
)

const val deffultCellSize = 20f

data class GameSettings(
    var gridHandlerCellSize: Float = deffultCellSize,
    var renderType: String = "",
    var drawCellIndex: Boolean = false,
    var drawRopeNodes: Boolean = false,
    var drawGoatBounds: Boolean = false,
    var drawDogBounds: Boolean = false,
)

data class LevelCondTranslated(
    val levelCondition: LevelConditions,
    val translatedLevelCondition: String,
)

class AppConstants {

    private val engine = GameEngine()
    fun getEngine() = engine
    fun initEngine(w: Int, h: Int) {
        orientationChanged = false

        engine.setGrid(
            w,
            h,
            getSetttings().gridHandlerCellSize
        )
    }

    private lateinit var gameSettings: GameSettings
    private lateinit var levelConditionPreferences: SharedPreferences

    var levelsMap: MutableMap<LevelCondTranslated, Boolean> = mutableMapOf()

    fun resetLevelsMap() {
        levelConditionPreferences.edit().apply {
            levelsMap.forEach {
                val key = it.key
                putBoolean(key.levelCondition.toString(), false)
                levelsMap[key] = false
            }
        }.apply()
    }

    fun setLevelConditionsList(context: Context) {
        levelConditionPreferences =
            context.getSharedPreferences("LevelConditionPrefs", Context.MODE_PRIVATE)

        levelsMap = LevelConditions.entries.mapNotNull {
            if (translatedMap.containsKey(it)) LevelCondTranslated(
                it,
                translatedMap[it]!!
            )
            else null
        }.associateWith {
            levelConditionPreferences.getBoolean(it.levelCondition.toString(), false)
        }.toMutableMap()
    }

    fun nextLevelCondition(lc: LevelConditions): LevelConditions? {
        levelConditionPreferences.edit().putBoolean(lc.toString(), true).apply()
        levelsMap[levelsMap.keys.find { it.levelCondition == lc }!!] = true
        levelsMap.toList().apply {
            val index = indexOfFirst { it.first.levelCondition == lc }
            return if (index + 1 in this.indices)
                this[index + 1].first.levelCondition
            else null
        }
    }

    fun setGameSettings(
        gridHandlerCellSize: Float,
        renderType: String,
        drawCellIndex: Boolean,
        drawRopeNodes: Boolean,
        drawGoatBounds: Boolean,
        drawDogBounds: Boolean,
    ) {
        gameSettings = GameSettings(
            gridHandlerCellSize,
            renderType,
            drawCellIndex,
            drawRopeNodes,
            drawGoatBounds,
            drawDogBounds,
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

    fun changeOption(option: PickedOptions) {
        if (option != PickedOptions.ROPE)
            engine.resetTempObj()
        currentOption = option
    }

}