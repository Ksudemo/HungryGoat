package com.example.hungrygoat.gameLogic.services.grid

import android.util.Log
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.services.PhysicService
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.math.ceil
import kotlin.math.min

class GridHandler {

    private var distances = HashMap<Pair<Pair<Float, Float>, Pair<Float, Float>>, Float>()

    private lateinit var grid: GameGrid
    fun getGrid() = grid
    fun setGrid(width: Int, height: Int, theoreticalCellSize: Float) {
        fun getSize(num: Int, canvasSize: Int) =
            if (num * theoreticalCellSize == canvasSize.toFloat()) theoreticalCellSize else canvasSize / num.toFloat()

        var numRows = ceil(height / theoreticalCellSize).toInt()
        var numColumns = ceil(width / theoreticalCellSize).toInt()

        val cellHeight = getSize(numRows, height)
        val cellWidth = getSize(numColumns, width)

        val cellSize = min(cellHeight, cellWidth)
        numRows = ceil(height / cellSize).toInt()
        numColumns = ceil(width / cellSize).toInt()

        grid = GameGrid(numRows, numColumns, cellSize)
        FirebaseCrashlytics.getInstance().log(
            " Cell size - $cellSize\n Grid size - ${numColumns * numRows}"
        )
        Log.v("mytag", "Cell size - $cellSize")
        Log.v(
            "mytag",
            "Grid size - ${numColumns * numRows}"
        )
    }

    fun getObjectCell(obj: GameObject?): Cell? {
        try {
            if (obj == null) return null

            val approxCol = (obj.x / grid.cellSize).toInt()
            val approxRow = (obj.y / grid.cellSize).toInt()

            if (approxCol in 0 until grid.numCols && approxRow in 0 until grid.numRows)
                return grid[approxCol, approxRow]

        } catch (e: Exception) {
            Log.e("mytag", "GridHandler.getObjectCell() Exception ${e.printStackTrace()} ")
        }
        return null
    }

    fun getClosestCell(x: Float, y: Float): Cell {
        val i = (x / grid.cellSize).toInt().coerceIn(0 until grid.numCols)
        val j = (y / grid.cellSize).toInt().coerceIn(0 until grid.numRows)

        return grid[i, j]
    }

    fun distBetween(obj: GameObject, other: GameObject): Float {
        val p1 = Pair(obj.x, obj.y)
        val p2 = Pair(other.x, other.y)
        val key = if (p1.hashCode() < p2.hashCode()) Pair(p1, p2) else Pair(p2, p1)

        if (distances.containsKey(key))
            return distances[key]!!

        val d = PhysicService().distBetween(obj.x, obj.y, other.x, other.y)
        distances[key] = d
        return d
    }

    fun getBoundaryCells(availableTargets: Set<Pair<Int, Int>>): List<Cell> {

        val numCols = grid.numCols
        val numRows = grid.numRows

        return when (availableTargets.isNotEmpty()) {
            true -> {
                val res = mutableListOf<Cell>()

                val horizontalBoundaries = availableTargets
                    .groupBy { it.first }
                    .mapValues { entry -> entry.value.map { it.second } }

                val verticalBoundaries = availableTargets
                    .groupBy { it.second }
                    .mapValues { entry -> entry.value.map { it.first } }

                for ((row, widths) in horizontalBoundaries) {
                    val minCol = widths.minOrNull() ?: continue
                    val maxCol = widths.maxOrNull() ?: continue
                    res.add(grid[row, minCol])
                    res.add(grid[row, maxCol])
                }

                for ((col, heights) in verticalBoundaries) {
                    val minRow = heights.minOrNull() ?: continue
                    val maxRow = heights.maxOrNull() ?: continue
                    res.add(grid[minRow, col])
                    res.add(grid[maxRow, col])
                }

                res
            }

            false -> {
                (0 until numCols).flatMap { listOf(grid[it, 0], grid[it, numRows - 1]) } +
                        (0 until numRows).flatMap { listOf(grid[0, it], grid[numCols - 1, it]) }
            }
        }
    }

    fun freeGrid() {
        grid.free()
    }
}
