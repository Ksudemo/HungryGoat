package com.example.hungrygoat.gameLogic.interfaces

import android.graphics.Canvas
import android.graphics.Paint
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog
import com.example.hungrygoat.gameLogic.services.GridHandler

fun interface Draw {
    fun draw(canvas: Canvas, paint: Paint)
}

fun interface DogUpdate {
    fun update(gridHandler: GridHandler, goatHaveAvailableCells: Boolean)
}

fun interface GoatUpdate {
    fun update(
        gridHandler: GridHandler, dogObj: Dog?,
    ): Boolean
}
