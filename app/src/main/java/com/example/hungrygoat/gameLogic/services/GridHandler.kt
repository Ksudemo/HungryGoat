package com.example.hungrygoat.gameLogic.services

import android.graphics.RectF
import android.util.Log
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.properties.Delegates

@Suppress("unused")
class GridHandler {
    //    TODO Remove that two variables after test
    val testMap = mutableMapOf<String, Pair<Int, Int>>()

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

        val temp: Array<Array<Cell>> = Array(numColumns) { i ->
            Array(numRows) { j ->
                val rect = getRect(i, j, cellSize, cellSize)
                Cell(rect, rect.centerX(), rect.centerY(), i, j)
            }
        }

        grid = temp
        numColumns = temp.size
        numRows = temp.firstOrNull()?.size ?: 0

        Log.v("mytag", "Cell size - $cellSize")
        Log.v(
            "mytag",
            "Grid size - ${grid.size} * ${grid.first().size} = ${grid.size * grid.first().size}"
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


    private fun distBetween(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

    fun getClosestCell(x: Float, y: Float): Cell {
        val i = (x / cellSize).toInt().coerceIn(0 until numColumns)
        val j = (y / cellSize).toInt().coerceIn(0 until numRows)

        return grid[i][j]
    }

    fun distBetween(obj: GameObject, other: GameObject, caller: String) =
        distBetween(obj.x, obj.y, other.x, other.y, caller)

    fun distBetween(x: Float, y: Float, otherX: Float, otherY: Float, caller: String): Float {
        testMap.compute(caller) { _, value ->
            if (value == null)
                Pair(1, 0)
            else {
                Pair(value.first + 1, value.second)
            }
        }

        val p1 = Pair(x, y)
        val p2 = Pair(otherX, otherY)
        val key = if (p1.hashCode() < p2.hashCode()) Pair(p1, p2) else Pair(p2, p1)

        if (distances.containsKey(key)) {
            testMap.compute(caller) { _, value ->
                Pair(value!!.first, value.second + 1)
            }
            return distances[key]!!
        }
        val d = distBetween(x, y, otherX, otherY)
        distances[key] = d
        return d
    }

    fun getBoundaryCells(availableTargets: Set<Cell>): List<Cell> {
        if (availableTargets.size == grid.size * grid.first().size) {
            val res = mutableListOf<Cell>()
            for (i in 0 until numColumns) {
                res.add(grid[i][0])
                res.add(grid[i][numRows - 1])
            }
            for (i in 0 until numRows) {
                res.add(grid[0][i])
                res.add(grid[numColumns - 1][i])
            }
            return res
        }

        val minX =
            availableTargets.minOfOrNull { it.x } ?: 0f
        val minY =
            availableTargets.minOfOrNull { it.y } ?: 0f
        val maxX =
            availableTargets.maxOfOrNull { it.x } ?: 0f
        val maxY =
            availableTargets.maxOfOrNull { it.y } ?: 0f

        val filteredTargets =
            availableTargets.filter {
                it.x in (minX..maxX) && it.y in (minY..maxY)
            }.toSet()

        return filteredTargets.filter { isBoundaryCell(filteredTargets, it) }
    }

    private fun isBoundaryCell(avalibleTargets: Set<Cell>, cell: Cell): Boolean {
        val cellNeighbors = cell.getNeighbors(grid)
        return cellNeighbors.size < 8 || cellNeighbors.any { !avalibleTargets.contains(it) }
    }

    fun freeGrid() {
        grid = emptyArray()
    }
}
