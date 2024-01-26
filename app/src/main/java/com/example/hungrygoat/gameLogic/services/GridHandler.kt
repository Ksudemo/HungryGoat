package com.example.hungrygoat.gameLogic.services

import android.graphics.RectF
import android.util.Log
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import kotlin.math.ceil
import kotlin.math.min
import kotlin.properties.Delegates

@Suppress("unused")
class GridHandler {
    private lateinit var grid: Array<Array<Cell>>
    private var distances = HashMap<Pair<Pair<Float, Float>, Pair<Float, Float>>, Float>()

    var cellSize by Delegates.notNull<Float>()
    var numRows by Delegates.notNull<Int>()
    var numColumns by Delegates.notNull<Int>()

    fun getGrid() = grid
    fun setGrid(width: Int, height: Int, theoreticalCellSize: Float) {
        fun getRect(i: Int, j: Int, cellWidth: Float, cellHeight: Float) = RectF(
            i * cellWidth,
            j * cellHeight,
            (i + 1) * cellWidth,
            (j + 1) * cellHeight
        )

        fun getSize(num: Int, canvasSize: Int) =
            if (num * theoreticalCellSize == canvasSize.toFloat()) theoreticalCellSize else canvasSize / num.toFloat()

        numRows = ceil(height / theoreticalCellSize).toInt()
        numColumns = ceil(width / theoreticalCellSize).toInt()

        val cellHeight = getSize(numRows, height)
        val cellWidth = getSize(numColumns, width)

        cellSize = min(cellHeight, cellWidth)
        numRows = ceil(height / cellSize).toInt()
        numColumns = ceil(width / cellSize).toInt()

        grid = Array(numColumns) { i ->
            Array(numRows) { j ->
                val rect = getRect(i, j, cellSize, cellSize)
                val cell = Cell(rect, rect.centerX(), rect.centerY(), i, j)
                cell
            }
        }
        Log.v("mytag", "Cell size - $cellSize")
        Log.v(
            "mytag",
            "Grid size - ${grid.size * grid.first().size}"
        )
    }

    fun getObjectCell(obj: GameObject?): Cell? {
        try {
            if (obj == null) return null

            val approxCol = (obj.x / cellSize).toInt()
            val approxRow = (obj.y / cellSize).toInt()

            if (approxCol in 0 until numColumns && approxRow in 0 until numRows)
                return grid[approxCol][approxRow]

        } catch (e: Exception) {
            Log.e("mytag", "GridHandler.getObjectCell() Exception ${e.printStackTrace()} ")
        }
        return null
    }

    fun getClosestCell(x: Float, y: Float): Cell {
        val i = (x / cellSize).toInt().coerceIn(0 until numColumns)
        val j = (y / cellSize).toInt().coerceIn(0 until numRows)

        return grid[i][j]
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

    fun getBoundaryCells(availableTargets: HashSet<Cell>): List<Cell> {
        fun isBoundaryCell(avalibleTargets: HashSet<Cell>, cell: Cell): Boolean {
            cell.getNeighbours(grid).apply {
                return this.size < 8 || this.any { !avalibleTargets.contains(it) }
            }
        }

        return when (availableTargets.isEmpty()) {
            true -> {
                (0 until numColumns).flatMap { listOf(grid[it][0], grid[it][numRows - 1]) } +
                        (0 until numRows).flatMap { listOf(grid[0][it], grid[numColumns - 1][it]) }
            }

            false -> availableTargets.filter { isBoundaryCell(availableTargets, it) }
        }
    }

    fun freeGrid() {
        grid = emptyArray()
    }
}
