package com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.ksudemo.hungrygoat.constants.enums.GameObjectTags
import com.ksudemo.hungrygoat.gameLogic.game.grid.GridHandler
import com.ksudemo.hungrygoat.gameLogic.gameObjects.abstractObjects.MovableGameObject
import com.ksudemo.hungrygoat.gameLogic.interfaces.dogListeners.DogUpdateListener
import com.ksudemo.hungrygoat.gameLogic.interfaces.enigneListeners.DrawListener

class Dog(
    x: Float,
    y: Float,
    private val drawable: Drawable?,
    private val radius: Float,
    tag: GameObjectTags = GameObjectTags.DOG,
) :
    MovableGameObject(x, y, tag, radius), DrawListener, DogUpdateListener {
    val color = Color.RED
    override fun draw(canvas: Canvas, paint: Paint) {
        paint.color = color
        Rect(
            (x - radius * (scaleFactor - 0.25)).toInt(),
            (y - radius * (scaleFactor - 0.25)).toInt(),
            (x + radius * (scaleFactor - 0.25)).toInt(),
            (y + radius * (scaleFactor - 0.25)).toInt()
        ).also { drawable?.bounds = it }
        drawable?.draw(canvas)
    }

    override fun update(gridHandler: GridHandler) {
        if (path.isEmpty())
            preparePath()

        path[lastVisitedIndex++].apply {
            this@Dog.x = this.x
            this@Dog.y = this.y
        }

        if (lastVisitedIndex == path.size)
            lastVisitedIndex = 0
    }

    private fun preparePath() {
        path = bounds//.sortedBy { gridHandler.distBetween(this, it.x, it.y) }
    }
}