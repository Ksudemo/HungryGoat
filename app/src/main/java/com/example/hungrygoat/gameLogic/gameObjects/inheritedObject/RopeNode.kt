package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.interfaces.Draw

class RopeNode(val baseRope: Rope, vx: Float, vy: Float, tag: GameObjectTags) :
    GameObject(vx, vy, tag), Draw {

    override fun draw(canvas: Canvas, paint: Paint) {
        canvas.drawCircle(x, y, circleRadius, paint.apply {
            color = Color.DKGRAY
        })
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || (this.javaClass != other.javaClass)) return false

        other as GameObject
        if (!(other.x == this.x && other.y == this.y)) return false
        if (this === other || this.gameObjectTag == other.gameObjectTag) return true



        return true
    }

    override fun toString(): String =
        "baseRope - $baseRope, x = $x, y = $y , tag = $gameObjectTag"
}