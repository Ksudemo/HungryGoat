package com.example.hungrygoat.gameLogic.game

import android.graphics.Paint
import android.os.HandlerThread
import android.view.SurfaceHolder
import com.example.hungrygoat.constants.GameStates
import com.example.hungrygoat.constants.LevelConditions
import com.example.hungrygoat.constants.SingletonAppConstantsInfo

class GameThread(
    private val surfaceHolder: SurfaceHolder,
    threadName: String,
    private val levelCondition: LevelConditions,
) : HandlerThread(threadName) {

    private var backgroundPaint: Paint = Paint()

    private var engineInitialized = false
    private var isRunning: Boolean = true

    private var solutionNotChecked = true

    fun interface LevelDoneListener {
        fun onLevelDoneEvent()
    }

    private val listeners = mutableListOf<LevelDoneListener>()

    fun registerEventListener(listener: LevelDoneListener) =
        listeners.add(listener)


    fun unregisterEventListener(listener: LevelDoneListener) =
        listeners.remove(listener)

    private fun triggerEvent() =
        listeners.forEach {
            it.onLevelDoneEvent()
        }


    override fun run() {
        while (isRunning) {
            val appC = SingletonAppConstantsInfo.getAppConst()

            val canvas = surfaceHolder.lockCanvas(null) ?: return

            val engine = appC.getEngine()
            if (!engineInitialized || appC.orientationChanged) {
                appC.orientationChanged = false
                engineInitialized = true

                engine.setGrid(
                    canvas.width,
                    canvas.height,
                    appC.getSetttings().gridHandlerCellSize
                )
            }

            when (appC.getState()) {
                GameStates.STATE_OBJECTS_MOVING -> {
                    engine.update()
                    solutionNotChecked = true
                }

                GameStates.STATE_CHECK_SOLUTION -> {
                    if (solutionNotChecked) {
                        if (engine.checkSolution(levelCondition))
                            triggerEvent()
                        solutionNotChecked = false
                    }
                }

                else -> {
                    solutionNotChecked = true
                }
            }

            synchronized(surfaceHolder) {
                canvas.drawRect(
                    0f,
                    0f,
                    canvas.width.toFloat(),
                    canvas.height.toFloat(),
                    backgroundPaint
                )

                engine.draw(canvas)
            }

            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    fun setRunning(b: Boolean) {
        isRunning = b
    }

    fun stopThread() {
        isRunning = false
    }
}