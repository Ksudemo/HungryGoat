package com.example.hungrygoat.gameLogic.gameObjects.abstractObjects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.interfaces.Draw

abstract class GameObject(vx: Float, vy: Float, tg: GameObjectTags) : Draw {

    var x = vx
    var y = vy
    val gameObjectTag = tg
    var circleRadius = 40f
    var isTempOnRopeSet = false

    fun drawBase(canvas: Canvas, paint: Paint) {
        canvas.drawCircle(x, y, circleRadius, paint.apply {
            color = Color.WHITE
        })
    }
}