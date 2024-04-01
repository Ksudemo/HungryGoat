package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.rope

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.hungrygoat.constants.enums.GameObjectTags
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.interfaces.enigneListeners.DrawListener


class RopeSegment(
    val baseRope: Rope,
    private val startX: Float,
    private val startY: Float,
    private val endX: Float,
    private val endY: Float,
    private val radius: Float,
    tag: GameObjectTags
) :
    GameObject((startX + endX) / 2, (startY + endY) / 2, tag, radius), DrawListener {

    private val segmentColor = Color.DKGRAY// Color.BLACK
    override fun draw(canvas: Canvas, paint: Paint) {
        canvas.drawLine(startX, startY, endX, endY, paint.apply {
            color = segmentColor
        })
    }

    fun update() {

    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || (this.javaClass != other.javaClass)) return false

        other as GameObject
        return when {
            other.x != this.x && other.y != this.y -> false
            this === other || this.gameObjectTag == other.gameObjectTag -> false
            else -> true
        }
    }

    override fun toString(): String =
        "(Tag = ${gameObjectTag}, baseRope - $baseRope , isTempOnRopeSet - $isTempOnRopeSet)"
}