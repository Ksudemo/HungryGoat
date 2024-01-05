package com.example.hungrygoat.gameLogic.game

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.hungrygoat.constants.LevelConditions
import com.example.hungrygoat.constants.SingletonAppConstantsInfo

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
            gameThread = GameThread(holder, this, "", LevelConditions.EMPTY)
            gameThread!!.setRunning(true)
            gameThread!!.start()
        } else {
            gameThread!!.start()
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        SingletonAppConstantsInfo.getAppConst().orientationChanged = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            SingletonAppConstantsInfo.getAppConst().apply {
                getEngine().createNewObject(
                    event.x, event.y,
                    getOption()!!
                )
            }
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            SingletonAppConstantsInfo.getAppConst().apply {
                getEngine().tempCreateNewObjectOnMove(
                    event.x, event.y,
                    getOption()!!
                )
            }
        }
        return true
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Здесь можно остановить и очистить gameThread
        if (gameThread != null) {
            gameThread?.stopThread()
            try {
                gameThread?.join()
                gameThread = null
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    // Добавьте метод для установки GameThread из MainActivity
    fun setGameThread(thread: GameThread) {
        gameThread = thread
    }
}
