package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.interfaces.Draw

class Peg(x: Float, y: Float, tag: GameObjectTags) : GameObject(x, y, tag), Draw {

    val color = Color.MAGENTA
    override fun draw(canvas: Canvas, paint: Paint) {
        paint.color = color
        canvas.drawCircle(x, y, circleRadius, paint)
    }
}