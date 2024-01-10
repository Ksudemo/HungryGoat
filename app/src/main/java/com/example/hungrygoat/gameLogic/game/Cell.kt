package com.example.hungrygoat.gameLogic.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.CellGameObject

class Cell(val rect: RectF, vx: Float, vy: Float, private val i: Int, private val j: Int) :
    CellGameObject(vx, vy, GameObjectTags.CELL) {

    fun getNeighbors(grid: Array<Array<Cell>>): List<Cell> {
        val neighbors = mutableListOf<Cell>()

        if (i > 0) neighbors.add(grid[i - 1][j])
        if (i < grid.size - 1) neighbors.add(grid[i + 1][j])
        if (j > 0) neighbors.add(grid[i][j - 1])
        if (j < grid[0].size - 1) neighbors.add(grid[i][j + 1])

        if (i > 0 && j > 0) neighbors.add(grid[i - 1][j - 1])
        if (i > 0 && j < grid[0].size - 1) neighbors.add(grid[i - 1][j + 1])
        if (i < grid.size - 1 && j > 0) neighbors.add(grid[i + 1][j - 1])
        if (i < grid.size - 1 && j < grid[0].size - 1) neighbors.add(grid[i + 1][j + 1])

        return neighbors
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        canvas.drawRect(rect, paint)
    }
}