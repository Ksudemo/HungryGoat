package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.example.hungrygoat.constants.enums.GameObjectTags
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.interfaces.enigneListeners.DrawListener

class Peg(
    x: Float,
    y: Float,
    private val drawable: Drawable?,
    private val radius: Float,
    tag: GameObjectTags = GameObjectTags.PEG,
) :
    GameObject(x, y, tag, radius),
    DrawListener {

    val color = Color.MAGENTA
    override fun draw(canvas: Canvas, paint: Paint) {
        Rect(
            (x - radius * (scaleFactor - 0.75)).toInt(),
            (y - radius * (scaleFactor - 0.75)).toInt(),
            (x + radius * (scaleFactor - 0.75)).toInt(),
            (y + radius * (scaleFactor - 0.75)).toInt()
        ).also { drawable?.bounds = it }
        drawable?.draw(canvas)
    }
}