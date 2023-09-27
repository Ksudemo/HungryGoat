package com.example.hungrygoat.gameLogic.game

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.hungrygoat.constants.SingletonAppConstantsInfo

class GameView(context: Context, attrs: AttributeSet? = null) :
    SurfaceView(context, attrs), SurfaceHolder.Callback {
    private lateinit var displayThread: DisplayThread

    init {
        initView()
    }

    fun initView() {
        val holder = holder
        holder.addCallback(this)

        displayThread = DisplayThread(holder)
        isFocusable = true
    }


    override fun surfaceCreated(arg0: SurfaceHolder) {
        //Starts the display thread
        if (!::displayThread.isInitialized) {
            displayThread = DisplayThread(holder)
            displayThread.start()
        } else {
            displayThread.start()
        }
    }

    override fun surfaceChanged(arg0: SurfaceHolder, arg1: Int, arg2: Int, arg3: Int) {
        SingletonAppConstantsInfo.getAppConst().orientationChanged = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            SingletonAppConstantsInfo.getAppConst().apply {
                getEngine().createNewObject(
                    event.x, event.y,
                    getOption()!!
                )
            }
        }
        return true
    }

    override fun surfaceDestroyed(arg0: SurfaceHolder) {
        displayThread.setIsRunning(false)
        SingletonAppConstantsInfo.getAppConst().stopThread(displayThread)

    }
}