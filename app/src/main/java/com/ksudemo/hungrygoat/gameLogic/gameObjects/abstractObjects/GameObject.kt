package com.ksudemo.hungrygoat.gameLogic.gameObjects.abstractObjects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.ksudemo.hungrygoat.constants.enums.GameObjectTags
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.rope.Rope
import com.ksudemo.hungrygoat.gameLogic.interfaces.enigneListeners.DrawListener

abstract class GameObject(vx: Float, vy: Float, tg: GameObjectTags, val r: Float) :
    DrawListener {

    var x = vx
    var y = vy
    val gameObjectTag = tg
    var attachedRopes = mutableListOf<Rope>()
    var isTempOnRopeSet = false

    val scaleFactor = 2.15
    fun drawBase(canvas: Canvas, paint: Paint) {
        canvas.drawCircle(x, y, r, paint.apply {
            color = Color.WHITE
        })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameObject

        return !(x != other.x || y != other.y)
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    override fun toString(): String =
        "{${gameObjectTag}, ( $x, $y ), isTempOnRopeSet - $isTempOnRopeSet}"
}