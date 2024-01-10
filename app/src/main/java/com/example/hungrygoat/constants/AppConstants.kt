package com.example.hungrygoat.constants

import android.util.Log
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

class AppConstants {

    private val engine = GameEngine()
    fun getEngine() = engine

    private lateinit var gameSettings: GameSettings

    var levelsList = mutableListOf<Pair<LevelConditions, Boolean>>()
    var translatedList = listOf<Pair<String, Boolean>>()
    fun setLevelConditionsList() {
        levelsList = LevelConditions.entries.map { it to true }.toMutableList()
        levelsList.removeIf { it.first == LevelConditions.EMPTY }
        translatedList = levelsList.map { (translatedMap[it.first] ?: "") to it.second }
        Log.d("MyTag", "$levelsList")
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

        Log.d("MyTag", "$currentState")
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