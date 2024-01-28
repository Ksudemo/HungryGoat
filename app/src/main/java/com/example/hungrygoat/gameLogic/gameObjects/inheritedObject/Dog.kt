package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.MovableGameObject
import com.example.hungrygoat.gameLogic.interfaces.DogUpdate
import com.example.hungrygoat.gameLogic.interfaces.Draw
import com.example.hungrygoat.gameLogic.services.grid.GridHandler

class Dog(x: Float, y: Float, tag: GameObjectTags) :
    MovableGameObject(x, y, tag), Draw, DogUpdate {
    val color = Color.RED
    override fun draw(canvas: Canvas, paint: Paint) {
        paint.color = color
        canvas.drawCircle(x, y, circleRadius, paint)
    }

    override fun update(gridHandler: GridHandler, goatHaveAvailableCells: Boolean) =
        repeat(updatePerFrame) {
            if (goatHaveAvailableCells) {
                if (path.isEmpty())
                    preparePath(gridHandler)

                path[lastVisitedIndex++].apply {
                    this@Dog.x = this.x
                    this@Dog.y = this.y
                }

                if (lastVisitedIndex == path.size)
                    lastVisitedIndex = 0
            }
        }


    private fun preparePath(gridHandler: GridHandler) {
        path = bounds//.sortedBy { gridHandler.distBetween(this, it.x, it.y) }
    }
}