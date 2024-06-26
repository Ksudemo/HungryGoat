package com.ksudemo.hungrygoat.gameLogic.game

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.ksudemo.hungrygoat.constants.appContants.SingletonAppConstantsInfo

class GameView(context: Context, attrs: AttributeSet? = null) :
    SurfaceView(context, attrs), SurfaceHolder.Callback {

    private var gameThread: GameThread? = null

    init {
        initView()
    }

    fun initView() {
        val holder = holder
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (gameThread == null) {
            gameThread = GameThread(holder, "")
            gameThread!!.setRunning(true)
        }
        gameThread!!.start()
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        SingletonAppConstantsInfo.getAppConst().orientationChanged = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        SingletonAppConstantsInfo.getAppConst().apply {
            getEngine().handleTouch(event, getOption()!!, getState()!!, getSetttings().objectsSize)
        }
        return true
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (gameThread != null) {
            gameThread?.stopThread()
            try {
                gameThread?.join()
                gameThread = null
            } catch (e: InterruptedException) {
                Log.e("mytag", "GameView.surfaceDestroyed() ${e.printStackTrace()}")
            }
        }
    }

    fun getBitmap() = gameThread?.getCanvasBitmap(width, height)

    fun setGameThread(thread: GameThread) {
        gameThread = thread
    }
}
