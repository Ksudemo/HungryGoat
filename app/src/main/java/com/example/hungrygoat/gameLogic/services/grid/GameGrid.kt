package com.example.hungrygoat.gameLogic.services.grid

import android.graphics.RectF
import com.example.hungrygoat.gameLogic.game.Cell

class GameGrid(val numRows: Int, val numCols: Int, val cellSize: Float) {

    private val grid = hashMapOf<Int, HashMap<Int, Cell>>().apply {
        for (i in 0 until numCols)
            put(i, hashMapOf())
    }

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

    fun free() {
        grid.clear()
    }
}