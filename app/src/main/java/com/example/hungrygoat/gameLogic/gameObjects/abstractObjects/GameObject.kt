package com.example.hungrygoat.gameLogic.gameObjects.abstractObjects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.hungrygoat.constants.GameObjectTags

abstract class GameObject(vx: Float, vy: Float, tg: GameObjectTags) {

    var x = vx
    var y = vy
    val gameObjectTag = tg
    var circleRadius = 40f
    var isSelected = false

    open fun draw(canvas: Canvas, paint: Paint) {}
    fun drawBase(canvas: Canvas, paint: Paint) {
        canvas.drawCircle(x, y, circleRadius, paint.apply {
            color = Color.WHITE
        })
    }
}