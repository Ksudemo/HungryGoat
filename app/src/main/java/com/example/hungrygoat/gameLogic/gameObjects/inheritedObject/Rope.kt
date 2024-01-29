package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.RopeGameObject
import com.example.hungrygoat.gameLogic.interfaces.Draw
import com.example.hungrygoat.gameLogic.services.grid.GameGrid
import com.example.hungrygoat.gameLogic.services.grid.GridHandler
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.time.measureTime

class Rope(
    val objectFrom: GameObject,
    val objectTo: GameObject,
    val isTiedToRope: Boolean,
    val ropeLength: Float,
    tag: GameObjectTags,
) : RopeGameObject(objectFrom, tag), Draw {

    val color = Color.BLACK

    private val isObjFromMovable =
        objectFrom.gameObjectTag == GameObjectTags.DOG || objectFrom.gameObjectTag == GameObjectTags.GOAT
    private val isObjToMovable =
        objectTo.gameObjectTag == GameObjectTags.DOG || objectTo.gameObjectTag == GameObjectTags.GOAT
    val tiedToMovale = isObjToMovable || isObjFromMovable
    override fun draw(canvas: Canvas, paint: Paint) {
        paint.color = color
        canvas.drawLine(objectFrom.x, objectFrom.y, objectTo.x, objectTo.y, paint)
    }

    private fun getBoundinBoxIndecies(grid: GameGrid): Pair<IntRange, IntRange> {
        val cellSize = grid.cellSize
        val numColumns = grid.numCols
        val numRows = grid.numRows

        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = 0f
        var maxY = 0f

        val offset = (ropeLength - cellSize / 2)
        when (val anchorPoint = getAnchorPoint()) {
            is Peg -> {
                maxX = anchorPoint.x
                minX = anchorPoint.x
                maxY = anchorPoint.y
                minY = anchorPoint.y
            }

            else -> {
                var curRope = getRopeConnectedTo()
                while (curRope != null) {
                    val objFrom = curRope.objectFrom
                    val objTo = curRope.objectTo

                    minX = min(minX, min(objFrom.x, objTo.x))
                    maxX = max(maxX, max(objFrom.x, objTo.x))

                    minY = min(minY, min(objFrom.y, objTo.y))
                    maxY = max(maxY, max(objFrom.y, objTo.y))

                    curRope = curRope.getRopeConnectedTo()
                }
            }
        }

        minX -= offset
        minY -= offset
        maxX += offset
        maxY += offset

        val minCol = (minX / cellSize).toInt().coerceIn(0 until numColumns)
        val maxCol = (maxX / cellSize).toInt().coerceIn(0 until numColumns)
        val minRow = (minY / cellSize).toInt().coerceIn(0 until numRows)
        val maxRow = (maxY / cellSize).toInt().coerceIn(0 until numRows)

        val xRange = (min(minCol, maxCol)..max(minCol, maxCol))
        val yRange = (min(minRow, maxRow)..max(minRow, maxRow))

        return xRange to yRange
    }


    fun setReachedSet(gridHandler: GridHandler): Set<Pair<Int, Int>> {
//        If not tied to a rope and non of obj's connected is a movable (goat or dog)
        if (!isTiedToRope && !(tiedToMovale))
            return emptySet()

        val grid = gridHandler.getGrid()
        val ropeConnectedTo = getRopeConnectedTo()
        val anchorPoint = getAnchorPoint()

        val (rows, cols) = getBoundinBoxIndecies(grid)

        return rows.flatMap { i -> cols.map { j -> i to j } }.filter { (i, j) ->
            canRopeReachCell(
                gridHandler,
                ropeConnectedTo,
                anchorPoint,
                grid[i, j]
            )
        }.toSet()

    }

    private fun canReachCell(
        gridHandler: GridHandler,
        objFrom: GameObject,
        cellTo: Cell,
    ): Boolean {
        val distance = gridHandler.distBetween(cellTo, objFrom)
        return distance <= ropeLength + gridHandler.getGrid().cellSize / 2
    }

    private fun canRopeReachCell(
        gridHandler: GridHandler,
        baseRope: Rope?,
        anchor: GameObject?,
        targetCell: Cell,
    ) =
        baseRope?.ropeNodes?.any { ropeNode ->
            canReachCell(gridHandler, ropeNode, targetCell)
        } ?: false || (anchor != null && canReachCell(gridHandler, anchor, targetCell))


    fun getAnchorPoint(): GameObject? = getRopeNode() ?: getPeg()

    fun isTiedToThisRope(other: Rope): Boolean {
        val objectFromTheSame = objectFrom == other.objectTo || objectFrom == other.objectFrom
        val objectToTheSame = objectTo == other.objectTo || objectTo == other.objectFrom
        if (!(objectFromTheSame || objectToTheSame)) return false

        val otherAnchor = other.getAnchorPoint()
        ropeNodes.forEach {
            if (otherAnchor == it) return true
        }
        return ropeNodes.any { otherAnchor == it }
    }

    fun getRopeConnectedTo() =
        when (isTiedToRope) {
            true -> getRopeNode()?.baseRope
            false -> null
        }

    private fun getRopeNode(): RopeNode? {
        val ropeNodeTag = GameObjectTags.RopeNode
        return if (objectFrom.gameObjectTag == ropeNodeTag)
            objectFrom as RopeNode
        else
            if (objectTo.gameObjectTag == ropeNodeTag)
                objectTo as RopeNode
            else null
    }

    private fun getPeg(): Peg? {
        val pegTag = GameObjectTags.PEG
        return if (objectFrom.gameObjectTag == pegTag)
            objectFrom as Peg
        else
            if (objectTo.gameObjectTag == pegTag)
                objectTo as Peg
            else null
    }

    fun setRopeNodes() {
        val time = measureTime {
            val x1 = objectFrom.x
            val y1 = objectFrom.y

            val x2 = objectTo.x
            val y2 = objectTo.y

            val segmengLength = circleRadius * 2
            val segmentCount = ceil(ropeLength / segmengLength).toInt()

            val dx = x2 - x1
            val dy = y2 - y1

            for (i in 1..segmentCount) {
                val fraction = i.toFloat() / segmentCount

                val nextX = x1 + fraction * dx
                val nexty = y1 + fraction * dy

                ropeNodes.add(RopeNode(this, nextX, nexty, GameObjectTags.RopeNode))
            }
        }
        Log.d("mytag", "rope.setRopeNodes time - $time")
    }
}