package com.ksudemo.hungrygoat.gameLogic.game.grid

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ksudemo.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.Cell
import com.ksudemo.hungrygoat.gameLogic.services.PhysicService
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
        numColumns = ceil(width / cellSize).toInt() - 2

        grid = GameGrid(numRows, numColumns, cellSize)

        FirebaseCrashlytics.getInstance().log(
            " Cell size - $cellSize\n " +
                    "Grid size - $numColumns * $numRows = ${numColumns * numRows} "
        )

        Log.v(
            "mytag",
            " Cell size - $cellSize\n " +
                    "Grid size - $numColumns * $numRows = ${numColumns * numRows} "
        )
    }

    fun getObjectGridIndecies(obj: GameObject?): Pair<Int, Int> {
        try {
            if (obj == null) return 0 to 0

            val approxCol = (obj.x / grid.cellSize).toInt().coerceIn(0 until grid.numCols)
            val approxRow = (obj.y / grid.cellSize).toInt().coerceIn(0 until grid.numRows)

            return approxCol to approxRow
        } catch (e: Exception) {
            Log.e("mytag", "GridHandler.getObjectGridIndecies() Exception ${e.printStackTrace()} ")
        }
        return 0 to 0
    }

    fun getObjectCell(obj: GameObject?): Cell? {
        val (i, j) = getObjectGridIndecies(obj)
        return grid[i, j]
    }

    fun distBetween(obj: GameObject, other: GameObject): Float {
        val p1 = Pair(obj.x, obj.y)
        val p2 = Pair(other.x, other.y)
        val key = if (p1.hashCode() < p2.hashCode()) Pair(p1, p2) else Pair(p2, p1)

        return distances.getOrPut(key) {
            PhysicService().distBetween(obj.x, obj.y, other.x, other.y)
        }
    }

    fun getBoundaryCells(availableTargets: Set<Pair<Int, Int>>): List<Cell> {

        val numCols = grid.numCols
        val numRows = grid.numRows

        return if (availableTargets.isNotEmpty()) {

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
        } else
            (0 until numCols).flatMap { listOf(grid[it, 0], grid[it, numRows - 1]) } +
                    (0 until numRows).flatMap { listOf(grid[0, it], grid[numCols - 1, it]) }
    }

    fun freeGrid() {
        grid.free()
        distances.clear()
    }
}
