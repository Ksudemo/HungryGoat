package com.example.hungrygoat.constants

import com.example.hungrygoat.gameLogic.game.GameEngine

object SingletonAppConstantsInfo {
    lateinit var appConstants: AppConstants

    fun getAppConst(): AppConstants {
        if (!::appConstants.isInitialized) {
            appConstants = AppConstants()
            appConstants.changeOption(PickedOptions.CLEAR)
            appConstants.changeState(GameStates.STATE_PAUSED)
        }

        return appConstants
    }
}

const val deffultCellSize = 20f

data class GameSettings(
    var gridHandlerCellSize: Float = deffultCellSize,
    var renderType: String = "",
    var drawCellIndex: Boolean = false,
    var drawRopeNodes: Boolean = false,
    var drawGoatBounds: Boolean = false,
    var drawWolfBounds: Boolean = false,
)

class AppConstants {

    private val engine = GameEngine()
    fun getEngine() = engine

    private lateinit var gameSettings: GameSettings

    fun setGameSettings(
        gridHandlerCellSize: Float,
        renderType: String,
        drawCellIndex: Boolean,
        drawRopeNodes: Boolean,
        drawGoatBounds: Boolean,
        drawWolfBounds: Boolean,
    ) {
        gameSettings = GameSettings(
            gridHandlerCellSize,
            renderType,
            drawCellIndex,
            drawRopeNodes,
            drawGoatBounds,
            drawWolfBounds,
        )
    }

    fun getSetttings() = if (::gameSettings.isInitialized) gameSettings else GameSettings()


    var orientationChanged = true

    private var currentState: GameStates? = null
    fun getState() = currentState
    fun changeState(state: GameStates) {
        currentState = state

        if (currentState == GameStates.STATE_PAUSED)
            getEngine().restoreInitialState()
    }


    private var currentOption: PickedOptions? = null
    fun getOption() = currentOption

    fun changeOption(option: PickedOptions) {
        if (option != PickedOptions.ROPE)
            engine.resetTempObj()
        currentOption = option
    }


    fun stopThread(thread: Thread) {
        var retry = true
        while (retry) {
            try {
                thread.join()
                retry = false
            } catch (_: Exception) {
            }
        }
    }

}