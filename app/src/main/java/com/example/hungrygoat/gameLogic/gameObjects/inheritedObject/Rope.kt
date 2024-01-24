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
import com.example.hungrygoat.gameLogic.services.GridHandler
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureTimeMillis

class Rope(
    val objectFrom: GameObject,
    val objectTo: GameObject,
    private val isTiedToRope: Boolean,
    ropeLength: Float,
    tag: GameObjectTags,
) : RopeGameObject(objectFrom, tag), Draw {

    val color = Color.BLACK

    private val maxLength = ropeLength

    private val isObjFromMovable =
        objectFrom.gameObjectTag == GameObjectTags.DOG || objectFrom.gameObjectTag == GameObjectTags.GOAT
    private val isObjToMovable =
        objectTo.gameObjectTag == GameObjectTags.DOG || objectTo.gameObjectTag == GameObjectTags.GOAT
    val tiedToMovale = isObjToMovable || isObjFromMovable
    override fun draw(canvas: Canvas, paint: Paint) {
        paint.color = color
        canvas.drawLine(objectFrom.x, objectFrom.y, objectTo.x, objectTo.y, paint)
    }

    private fun getBoundinBoxIndecies(gridHandler: GridHandler): Pair<IntRange, IntRange> {
        val cellSize = gridHandler.cellSize
        val numColumns = gridHandler.numColumns
        val numRows = gridHandler.numRows

        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = 0f
        var maxY = 0f

        val offset = (maxLength - cellSize / 2)
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

    private fun getFilteredRanges(gridHandler: GridHandler): List<Pair<IntRange, IntRange>> {
        val (xRange, yRange) = getBoundinBoxIndecies(gridHandler)
        Log.d(
            "mytag",
            "xRange = $xRange \n yRange = $yRange"
        )
        return listOf(xRange to yRange)
    }

    fun setReachedSet(gridHandler: GridHandler) {
//        If not tied to a rope and non of obj's connected is a movable (goat or dog)
        if (!isTiedToRope && !(tiedToMovale))
            return

        val grid = gridHandler.getGrid()
        val baseRope = getRopeConnectedTo()

        val time1 = measureTimeMillis {
            getFilteredRanges(gridHandler).forEach { (colRange, rowRange) ->
                for (i in colRange)
                    for (j in rowRange)
                        if (canRopeReachCell(gridHandler, baseRope, grid[i][j]))
                            ropeReachedSet.add(grid[i][j])
            }
        }
        Log.d("mytag", "loop and call canRopeReachCell time = $time1")
    }

    private fun canReachCell(
        gridHandler: GridHandler,
        closest: GameObject,
        targetCell: GameObject,
    ): Boolean {
        return gridHandler.distBetween(
            closest,
            targetCell, "ropeCanReach"
        ) <= maxLength + gridHandler.cellSize / 2
    }

    private fun canRopeReachCell(
        gridHandler: GridHandler,
        baseRope: Rope?,
        targetCell: Cell,
    ): Boolean {
        if (isTiedToRope && baseRope != null && baseRope.ropeNodes.any { ropeNode ->
                val closest = gridHandler.getClosestCell(ropeNode.x, ropeNode.y) // TODO rework(?)
                canReachCell(gridHandler, closest, targetCell)
            })
            return true

        val anchor = getAnchorPoint() ?: return false
        return canReachCell(gridHandler, anchor, targetCell)
    }

    private fun getAnchorPoint(): GameObject? = getRopeNode() ?: getPeg()

    fun isTiedToThisRope(other: Rope): Boolean {

        val objectFromTheSame = objectFrom == other.objectTo || objectFrom == other.objectFrom
        val objectToTheSame = objectTo == other.objectTo || objectTo == other.objectFrom
        if (!(objectFromTheSame || objectToTheSame)) return false

        val otherAnchor = other.getAnchorPoint()
        ropeNodes.forEach {
            if (otherAnchor == it) return true
        }
        val cond = ropeNodes.any { otherAnchor == it }


        Log.d(
            "mytag",
            "objectFromTheSame = $objectFromTheSame\n objectToTheSame = $objectToTheSame\n cond = $cond "
        )


        return cond
    }


    private fun getRopeConnectedTo(): Rope? =
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

        val x1 = objectFrom.x
        val y1 = objectFrom.y

        val x2 = objectTo.x
        val y2 = objectTo.y

        val segmengLength = circleRadius * 2
        val segmentCount = ceil(maxLength / segmengLength).toInt()

        val dx = x2 - x1
        val dy = y2 - y1

        for (i in 1..segmentCount) {
            val fraction = i.toFloat() / segmentCount

            val nextX = x1 + fraction * dx
            val nexty = y1 + fraction * dy

            ropeNodes.add(RopeNode(this, nextX, nexty, GameObjectTags.RopeNode))
        }

    }
}