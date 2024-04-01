package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.hungrygoat.constants.enums.GameObjectTags
import com.example.hungrygoat.gameLogic.game.grid.GameGrid
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.CellGameObject

class Cell(val rect: RectF? = null, vx: Float, vy: Float, val i: Int = 0, val j: Int = 0) :
    CellGameObject(vx, vy, GameObjectTags.CELL, 0f) {

    fun getNeighbours(grid: GameGrid): HashSet<Cell> {
        val res = hashSetOf<Cell>()
        for (r in i - 1..i + 1)
            for (c in j - 1..j + 1) {
                if (r == c) continue
                val cell = grid[r, c]
                res.add(cell)
            }

        return res
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        canvas.drawRect(rect!!, paint)
    }

    override fun toString(): String = "($gameObjectTag , x = $x , y = $y , i = $i , j = $j)"

}