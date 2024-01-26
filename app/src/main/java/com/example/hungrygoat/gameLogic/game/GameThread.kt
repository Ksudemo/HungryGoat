package com.example.hungrygoat.gameLogic.game

import android.graphics.Canvas
import android.graphics.Paint
import android.os.HandlerThread
import android.view.SurfaceHolder
import com.example.hungrygoat.constants.GameStates
import com.example.hungrygoat.constants.LevelConditions
import com.example.hungrygoat.constants.SingletonAppConstantsInfo
import com.example.hungrygoat.gameLogic.interfaces.LevelCompleteListener
import com.example.hungrygoat.gameLogic.interfaces.LevelFailedListener

class GameThread(
    private val surfaceHolder: SurfaceHolder,
    threadName: String,
    private var levelCondition: LevelConditions,
) : HandlerThread(threadName) {

    private var backgroundPaint: Paint = Paint()

    private var isRunning: Boolean = true

    private var solutionNotChecked = true

    private var levelCompleteListener: LevelCompleteListener? = LevelCompleteListener { }
    private var levelFailedListener: LevelFailedListener? = LevelFailedListener {}

    fun registerLevelEndingListeners(
        levelComplete: LevelCompleteListener,
        levelFailed: LevelFailedListener,
    ) {
        levelCompleteListener = levelComplete
        levelFailedListener = levelFailed
    }

    fun unregisterLevelEndingListeners() {
        levelCompleteListener = null
        levelFailedListener = null
    }

    override fun run() {
        val appC = SingletonAppConstantsInfo.getAppConst()
        val engine = appC.getEngine()
        var canvas: Canvas
        while (isRunning) {
            canvas = surfaceHolder.lockCanvas(null) ?: return
            if (appC.orientationChanged)
                appC.initEngine(canvas.width, canvas.height)

            when (appC.getState()) {
                GameStates.STATE_OBJECTS_MOVING -> {
                    engine.update()
                    solutionNotChecked = true
                }

                GameStates.STATE_CHECK_SOLUTION -> {
                    if (solutionNotChecked) {

                        solutionNotChecked = false
                        if (engine.checkSolution(levelCondition))
                            levelCompleteListener?.onLevelComplete()
                        else
                            levelFailedListener?.onLevelFailed()
                    }
                }

                else -> {
                    solutionNotChecked = true
                }
            }

            synchronized(surfaceHolder) {
                engine.draw(canvas)
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
        }
    }

    fun setRunning(b: Boolean) {
        isRunning = b
    }

    fun stopThread() {
        isRunning = false
    }

    fun changeLevelCondition(lc: LevelConditions) {
        levelCondition = lc
    }
}