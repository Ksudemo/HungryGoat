package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject

class RopeNode(val baseRope: Rope, vx: Float, vy: Float, tag: GameObjectTags) :
    GameObject(vx, vy, tag) {

    override fun draw(canvas: Canvas, paint: Paint) {
        canvas.drawCircle(x, y, circleRadius, paint.apply {
            color = Color.DKGRAY
        })
    }

    override fun toString(): String =
        "baseRope - $baseRope, x = $x, y = $y , tag = $gameObjectTag"
}