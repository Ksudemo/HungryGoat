package com.ksudemo.hungrygoat.gameLogic.game

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.HandlerThread
import android.util.Log
import android.view.SurfaceHolder
import com.ksudemo.hungrygoat.constants.appContants.SingletonAppConstantsInfo
import com.ksudemo.hungrygoat.constants.enums.GameStates

class GameThread(
    private val surfaceHolder: SurfaceHolder,
    threadName: String,
) : HandlerThread(threadName) {

    private var isRunning: Boolean = true
    override fun run() {
        val appC = SingletonAppConstantsInfo.getAppConst()
        val engine = appC.getEngine()
        var canvas: Canvas

        while (isRunning) {
            try {
                synchronized(surfaceHolder) {
                    canvas = surfaceHolder.lockCanvas(null) ?: return

                    if (appC.orientationChanged) {
                        appC.initEngine(canvas.width, canvas.height)
                    }

                    if (appC.getState() == GameStates.STATE_OBJECTS_MOVING)
                        engine.update()

                    engine.draw(canvas)
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            } catch (e: Exception) {
                Log.d("mytag", "exception in GameThread.run() ${e.printStackTrace()}")
                continue
            }
            Thread.sleep(10)
        }
    }

    fun getCanvasBitmap(w: Int, h: Int): Bitmap {
        isRunning = false
        Thread.sleep(20)

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        SingletonAppConstantsInfo.appConstants.getEngine().draw(canvas)

        isRunning = true
        return bitmap
    }

    fun setRunning(b: Boolean) {
        while (isRunning != b)
            isRunning = b
    }

    fun stopThread() {
        while (isRunning)
            isRunning = false
    }
}