package com.example.hungrygoat.gameLogic.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.CellGameObject
import com.example.hungrygoat.gameLogic.services.grid.GameGrid

class Cell(private val rect: RectF, vx: Float, vy: Float, val i: Int, val j: Int) :
    CellGameObject(vx, vy, GameObjectTags.CELL) {

    private var neighbors = hashSetOf<Cell>()
    fun getNeighbours(grid: GameGrid): HashSet<Cell> {
        if (neighbors.isEmpty())
            neighbors = setNeighbors(grid)
        return neighbors
    }

    private fun setNeighbors(grid: GameGrid): HashSet<Cell> {
        val res = hashSetOf<Cell>()
        val cols = grid.numCols
        val rows = grid.numRows
        for (c in i - 1..i + 1)
            for (r in j - 1..j + 1) {
                if (c == i && r == j) continue
                val cInRange = c in 0 until cols
                val rInRange = r in 0 until rows
                if (cInRange && rInRange)
                    res.add(grid[c, r])
            }
        return res
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        canvas.drawRect(rect, paint)
    }
}