package com.example.hungrygoat.gameLogic.services

import android.graphics.RectF
import android.util.Log
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.EmptyObject
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.properties.Delegates

@Suppress("unused")
class GridHandler {

    private lateinit var grid: List<Cell> // TODO Change grid2 to grid
    private lateinit var grid2: Array<Array<Cell>>

    private var distances = mutableMapOf<GameObject, MutableMap<GameObject, Float>>()

    private var neighbors = mutableMapOf<Int, List<Int>>()
    private var cellToObjects = mutableMapOf<GameObject, Cell>()

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

//        val temp = mutableListOf<Cell>()
//        var atLeastOneCellRemovedFromBottom = false
//        var atLeastOneCellRemovedFromLeft = false
//
//
//        for (i in 0 until numColumns)
//            for (j in 0 until numRows) {
//                val rect = getRect(i, j, cellSize, cellSize)
//
//                if (rect.bottom > height) {
//                    atLeastOneCellRemovedFromBottom = true
//                    continue
//                }
//
//                if (rect.left > width) {
//                    atLeastOneCellRemovedFromLeft = true
//                    continue
//                }
//
//                temp.add(Cell(rect, rect.centerX(), rect.centerY()))
//            }
//        if (atLeastOneCellRemovedFromBottom)
//            numRows -= 1
//        if (atLeastOneCellRemovedFromLeft)
//            numColumns -= 1

        val temp = Array(numColumns) { i ->
            Array(numRows) { j ->
                val rect = getRect(i, j, cellSize, cellSize)
                if (rect.bottom > height || rect.left > width) null
                else
                    Cell(rect, rect.centerX(), rect.centerY())
            }
        }.map { row -> row.filterNotNull() }.map { it.toTypedArray() }.toTypedArray()

        numColumns = temp.size
        numRows = temp.firstOrNull()?.size ?: 0

        grid = mutableListOf()// temp
        Log.v("mytag", "Cell size - $theoreticalCellSize")
        Log.v("mytag", "Grid size - ${grid.size}")
    }

    private fun checkIsInRect(obj: GameObject, rect: RectF) =
        (obj.x > rect.left && obj.x < rect.right && obj.y > rect.top && obj.y < rect.bottom)


    fun getObjectCell(obj: GameObject?) =
        try {
            if (obj == null) null
            else {
                if (cellToObjects.contains(obj))
                    cellToObjects[obj]
                else {
                    val cell = grid.first { checkIsInRect(obj, it.rect) }
                    cellToObjects[obj] = cell
                    cell
                }
            }
        } catch (e: Exception) {
            null
        }

    private fun distBetween(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

    fun getClosestObject(obj: GameObject, objs: List<GameObject>) =
        objs.minByOrNull { distBetween(obj, it) }

    fun distBetween(obj: GameObject, otherX: Float, otherY: Float) =
        distBetween(obj, EmptyObject(otherX, otherY, GameObjectTags.EMPTY))

    fun distBetween(obj1: GameObject, obj2: GameObject): Float {
        if (distances.contains(obj1) && distances[obj1]?.contains(obj2) == true)
            return distances[obj1]!![obj2]!!

        if (distances.containsKey(obj1) && distances[obj1]!!.containsKey(obj2))
            return distances[obj1]!![obj2]!!

        val d = distBetween(obj1.x, obj1.y, obj2.x, obj2.y)
        distances.getOrPut(obj1) { mutableMapOf() }[obj2] = d
        distances.getOrPut(obj2) { mutableMapOf() }[obj1] = d

        return d
    }

    private fun calcDistanceBetween(objFromGridIndex: Int, objToGridIndex: Int): Float {
        val xFrom = grid[objFromGridIndex].x
        val yFrom = grid[objFromGridIndex].y

        val x = grid[objToGridIndex].x
        val y = grid[objToGridIndex].y

        val dx = xFrom - x
        val dy = yFrom - y

        return sqrt(hypot(dx, dy))
    }

    fun getBoundaryCells(availableTargets: List<Cell>): List<Cell> =
        if (availableTargets.size == grid.size) {
            val res = mutableSetOf<Int>()
            for (i in 0 until numColumns)
                res += i * numRows
            for (i in 0 until numColumns)
                res += i * numRows + (numRows - 1)
            for (i in 0 until numRows)
                res += i
            for (i in 0 until numRows)
                res += (numColumns - 1) * numRows + i

            res.map { grid[it] }
        } else
            availableTargets.filter { isBoundaryCell(availableTargets, it) }

    private fun isBoundaryCell(bounds: List<Cell>, cell: Cell): Boolean {
        val cellNeighbors = getCellNeighbors(cell)
        return cellNeighbors.size < 8 || cellNeighbors.any { !bounds.contains(grid[it]) }
    }

    private fun getCellNeighbors(curCell: Cell): List<Int> =
        neighbors.getOrPut(grid.indexOf(curCell)) {
            curCell.getNeighbors(this).map { grid.indexOf(it) }
        }

    fun freeGrid() {
        grid = emptyList()
    }
}