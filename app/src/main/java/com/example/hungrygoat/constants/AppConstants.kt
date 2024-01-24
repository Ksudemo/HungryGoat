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


val translatedLevelConditions = mapOf(
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
    LevelConditions.EMPTY to ""
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

class AppConstants {

    private var engine = GameEngine()
    fun getEngine() = engine

    private lateinit var gameSettings: GameSettings
    private lateinit var preffs: SharedPreferences


    var levelCondMap = mutableMapOf<LevelConditions, Boolean>()
    var translatedLevelCondMap = mapOf<String, Boolean>()

    fun setLevelConditionsList(applicationContext: Context) {
        preffs = applicationContext.getSharedPreferences(
            "levelConditionsPrefferences",
            Context.MODE_PRIVATE
        )
        setLevelCondMap()
        translateLevelCondMap()
    }

    private fun setLevelCondMap() {
        levelCondMap =
            LevelConditions.entries.associateWith { preffs.getBoolean(it.toString(), false) }
                .toMutableMap()
    }

    private fun translateLevelCondMap() {
        translatedLevelCondMap =
            levelCondMap.keys.associate { translatedLevelConditions[it]!! to levelCondMap[it]!! }
                .toMutableMap().apply { remove("") }
    }


    fun nextLevelCondition(prevCond: LevelConditions): LevelConditions { // TOOD Rework
        val list = levelCondMap.toList()
        val entryIndex = list.indexOfFirst { it.first == prevCond }
        if ((entryIndex + 1) in list.indices)
            return list[entryIndex + 1].first
        return LevelConditions.EMPTY
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

    fun markAsCompleted(levelCondition: LevelConditions) {
        levelCondMap[levelCondition] = true
        translateLevelCondMap()
        preffs.edit().apply {
            putBoolean(levelCondition.toString(), true)
            apply()
        }
    }

}