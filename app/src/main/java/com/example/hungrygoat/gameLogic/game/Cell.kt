package com.example.hungrygoat.gameLogic.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.CellGameObject

class Cell(val rect: RectF, vx: Float, vy: Float, val i: Int, val j: Int) :
    CellGameObject(vx, vy, GameObjectTags.CELL) {

    private val neighbors = hashSetOf<Cell>()
    fun getNeighbours(grid: Array<Array<Cell>>): HashSet<Cell> {
        if (neighbors.isEmpty())
            setNeighbors(grid)
        return neighbors
    }

    private fun setNeighbors(grid: Array<Array<Cell>>) {
        val cols = grid.size
        val rows = grid.first().size
        for (c in i - 1..i + 1)
            for (r in j - 1..j + 1) {
                if (c == i && r == j) continue
                val cInRange = c in 0 until cols
                val rInRange = r in 0 until rows
                if (cInRange && rInRange)
                    neighbors.add(grid[c][r])
            }
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        canvas.drawRect(rect, paint)
    }
}