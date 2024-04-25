package com.ksudemo.hungrygoat.gameLogic.game.grid

import android.graphics.RectF
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.Cell

class GameGrid(val numRows: Int, val numCols: Int, val cellSize: Float) {

    private val grid = hashMapOf<Int, HashMap<Int, Cell>>().apply {
        for (i in 0 until numCols)
            put(i, hashMapOf())
    }

    fun getValues() = grid.values
    operator fun get(i: Int) = grid.getOrPut(i) { hashMapOf() }
    operator fun get(i: Int, j: Int) =
        grid.getOrPut(i) { hashMapOf() }.getOrPut(j) { createCell(i, j) }

    private fun createCell(i: Int, j: Int): Cell {
        fun getRect(i: Int, j: Int, cellWidth: Float, cellHeight: Float) = RectF(
            i * cellWidth,
            j * cellHeight,
            (i + 1) * cellWidth,
            (j + 1) * cellHeight
        )

        val rect = getRect(i, j, cellSize, cellSize)
        return Cell(rect, rect.centerX(), rect.centerY(), i, j)
    }


    fun intersectsBounds(bounds: List<Cell>): Set<Cell> =
        bounds.filter { it.i == 0 || it.j == 0 || it.i == numCols - 1 || it.j == numRows - 1 }
            .toSet()

    fun free() = grid.clear()

}