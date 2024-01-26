package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.MovableGameObject
import com.example.hungrygoat.gameLogic.interfaces.Draw
import com.example.hungrygoat.gameLogic.interfaces.GoatUpdate
import com.example.hungrygoat.gameLogic.services.GridHandler

class Goat(vx: Float, vy: Float, tag: GameObjectTags) :
    MovableGameObject(vx, vy, tag), Draw, GoatUpdate {

    val color = Color.YELLOW
    private var pathSetted = false
    override fun draw(canvas: Canvas, paint: Paint) {
        paint.color = color
        canvas.drawCircle(x, y, circleRadius, paint)
    }

    override fun update(gridHandler: GridHandler, dogObj: Dog?): Boolean {
        repeat(updatePerFrame) {
            if (!hadAvailableCells) return false

            if (path.isNotEmpty() && pathSetted)
                path[lastVisitedIndex++].apply {
                    this@Goat.x = this.x
                    this@Goat.y = this.y
                    lastVisitedIndex = path.size // TODO remove after test
                }
            else
                preparePath(gridHandler, dogObj)

            hadAvailableCells = lastVisitedIndex != path.size
        }
        return hadAvailableCells
    }


    fun preparePath(gridHandler: GridHandler, dogObj: Dog?) {
        path = (reachedSet - ((dogObj?.reachedSet ?: emptySet()).toSet())).toList()
        pathSetted = true
        Log.d("mytag", "goat path size - ${path.size}")
//                .sortedBy { gridHandler.distBetween(this, it.x, it.y) }
    }
}