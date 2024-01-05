package com.example.hungrygoat.gameLogic.game

import android.graphics.RectF
import com.example.hungrygoat.gameLogic.services.GridHandler
import kotlin.math.acos
import kotlin.math.sqrt

class Cell(val rect: RectF, val x: Float, val y: Float) {

    fun getNeighbors(gridHandler: GridHandler): List<Cell> {
        val result = mutableListOf<Cell>()

        val grid = gridHandler.getGrid()

        val indexes = getNeighborsIndexes(gridHandler)
        indexes.forEach {
            result.add(grid[it])
        }

        return result
    }

    private fun getNeighborsIndexes(gridHandler: GridHandler): List<Int> {
        val result = mutableListOf<Int>()

        val grid = gridHandler.getGrid()

        val index = grid.indexOf(this)
        val numRows = gridHandler.numRows

        val leftIndex = index - numRows
        val rightIndex = index + numRows
        val topIndex = index - 1
        val bottomIndex = index + 1

        val bottomLeft = index + 1 - numRows
        val bottomRight = index + 1 + numRows
        val topLeft = index - 1 - numRows
        val topRight = index - 1 + numRows

        val bottomExtraCond = bottomIndex % numRows != 0
        val topExtraCond = index % numRows != 0

        if (leftIndex in grid.indices) result.add(leftIndex)
        if (rightIndex in grid.indices) result.add(rightIndex)
        if (topIndex in grid.indices) result.add(topIndex)

        if (bottomExtraCond && bottomIndex in grid.indices) result.add(bottomIndex)
        if (bottomExtraCond && bottomLeft in grid.indices) result.add(bottomLeft)
        if (bottomExtraCond && bottomRight in grid.indices) result.add(bottomRight)

        if (topExtraCond && topLeft in grid.indices) result.add(topLeft)
        if (topExtraCond && topRight in grid.indices) result.add(topRight)

        return result
    }

    fun angleBetween(other: Cell): Double {
        val dotProduct = this.x * other.x + this.y * other.y
        val magP1 = sqrt(this.x * this.x + this.y * this.y)
        val magP2 = sqrt(other.x * other.x + other.y * other.y)

        return Math.toDegrees(acos(dotProduct / (magP1 * magP2).toDouble()))
    }
}