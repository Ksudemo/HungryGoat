package com.example.hungrygoat.gameLogic.game

import android.graphics.RectF
import com.example.hungrygoat.gameLogic.services.GridHandler

class Cell(val rect: RectF, val x: Float, val y: Float) {

    var visited = false
    fun getNeighbors(gridHandler: GridHandler): List<Cell> {
        val result = mutableListOf<Cell>()
        val grid = gridHandler.getGrid()
        val indexes = getNeighborsIndexes(grid, gridHandler.numRows)
        indexes.forEach {
            result.add(grid[it])
        }

        return result
    }

    private fun getNeighborsIndexes(grid: List<Cell>, numRows: Int): List<Int> {
        val result = mutableListOf<Int>()

        val curIndex = grid.indexOf(this)
        val leftIndex = curIndex - numRows
        val rightIndex = curIndex + numRows
        val topIndex = curIndex - 1
        val bottomIndex = curIndex + 1

        val bottomLeft = curIndex + 1 - numRows
        val bottomRight = curIndex + 1 + numRows
        val topLeft = curIndex - 1 - numRows
        val topRight = curIndex - 1 + numRows

        val bottomExtraCond = bottomIndex % numRows != 0
        val topExtraCond = curIndex % numRows != 0

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
}