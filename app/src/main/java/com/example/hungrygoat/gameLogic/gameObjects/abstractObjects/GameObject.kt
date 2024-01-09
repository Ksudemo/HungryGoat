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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameObject

        if (x != other.x) return false
        if (y != other.y) return false
        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}