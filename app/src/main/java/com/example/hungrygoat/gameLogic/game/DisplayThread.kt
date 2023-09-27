package com.example.hungrygoat.gameLogic.game

import android.graphics.Paint
import android.view.SurfaceHolder
import com.example.hungrygoat.constants.GameStates
import com.example.hungrygoat.constants.SingletonAppConstantsInfo

class DisplayThread(private var surfaceHolder: SurfaceHolder) : Thread() {
    private var backgroundPaint: Paint = Paint()
    private var engineInitialized = false

    private val delay: Long = 1
    private var running: Boolean

    init {
        backgroundPaint.setARGB(255, 0, 0, 0)
        running = true
    }

    override fun run() {
        while (running) {
            val appC = SingletonAppConstantsInfo.getAppConst()

            val canvas = surfaceHolder.lockCanvas(null)

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

            if (appC.getState() == GameStates.STATE_PLAY)
                engine.update()

            synchronized(surfaceHolder) {
                canvas.drawRect(
                    0f,
                    0f,
                    canvas.width.toFloat(),
                    canvas.height.toFloat(),
                    backgroundPaint
                )

                engine.draw(canvas, appC.getSetttings())
            }

            surfaceHolder.unlockCanvasAndPost(canvas)

            try {
                sleep(delay)
            } catch (ex: InterruptedException) {
//                Make log
            }
        }
    }

    fun setIsRunning(state: Boolean) {
        running = state
    }
}