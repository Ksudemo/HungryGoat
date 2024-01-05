package com.example.hungrygoat.gameLogic.interfaces

import android.graphics.Canvas
import android.graphics.Paint
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog
import com.example.hungrygoat.gameLogic.services.GridHandler

interface Draw {
    fun draw(canvas: Canvas, paint: Paint)
}

interface DogUpdate {
    fun update(gridHandler: GridHandler, goatHaveAvailableCells: Boolean)
}

interface GoatUpdate {
    fun update(
        gridHandler: GridHandler, dogObj: Dog?,
    ): Boolean
}

interface Move {
    fun move(cellToMove: Cell?)
}