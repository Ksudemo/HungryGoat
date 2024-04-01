package com.example.hungrygoat.gameLogic.interfaces.enigneListeners

import android.graphics.Canvas
import android.graphics.Paint

fun interface DrawListener {
    fun draw(canvas: Canvas, paint: Paint)
}