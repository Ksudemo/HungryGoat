package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.rope

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.example.hungrygoat.constants.enums.GameObjectTags
import com.example.hungrygoat.gameLogic.game.grid.GameGrid
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.RopeGameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Peg
import com.example.hungrygoat.gameLogic.interfaces.enigneListeners.DrawListener
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
    private val radius: Float
) : RopeGameObject(objectFrom, objectTo, tag), DrawListener {

    var id = 0
    val depthLevel: Int = (getRopeConnectedTo()?.depthLevel ?: -1) + 1

    override fun draw(canvas: Canvas, paint: Paint) {
        canvas.drawLine(objectFrom.x, objectFrom.y, objectTo.x, objectTo.y, paint.apply {
            color = Color.DKGRAY
        })
    }

    fun update() {
        ropeSegments.forEach { it.update() }
    }

    fun getRopeBoundinBoxIndecies(grid: GameGrid): Pair<IntRange, IntRange> {
        val cellSize = grid.cellSize
        val numColumns = grid.numCols
        val numRows = grid.numRows

        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = 0f
        var maxY = 0f

        val offset = ropeLength
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

    fun getRopeConnectedTo() = if (isTiedToRope) getRopeNode()?.baseRope else null

    fun getAnchorPoint(): GameObject? = getRopeNode() ?: getPeg()
    private fun getRopeNode(): RopeSegment? =
        when {
            objectFrom.gameObjectTag == GameObjectTags.RopeSegment -> objectFrom as? RopeSegment
            objectTo.gameObjectTag == GameObjectTags.RopeSegment -> objectTo as? RopeSegment
            else -> null
        }

    private fun getPeg(): Peg? =
        when {
            objectFrom.gameObjectTag == GameObjectTags.PEG -> objectFrom as? Peg
            objectTo.gameObjectTag == GameObjectTags.PEG -> objectTo as? Peg
            else -> null
        }

    fun setRopeSegments() {
        val time = measureTime {
            val x1 = objectFrom.x
            val y1 = objectFrom.y

            val x2 = objectTo.x
            val y2 = objectTo.y

            Log.d("mytag", "rope from $x1 , $y1 to $x2 , $y2")
            val segmentLength = radius * 2
            val segmentCount = ceil(ropeLength / segmentLength).toInt()

            val dx = x2 - x1
            val dy = y2 - y1

            ropeSegments.clear() // Clear existing segments before creating new ones

            for (i in 0 until segmentCount) {
                val fractionStart = i.toFloat() / segmentCount
                val fractionEnd = (i + 1).toFloat() / segmentCount

                val startX = x1 + fractionStart * dx
                val startY = y1 + fractionStart * dy

                val endX = x1 + fractionEnd * dx
                val endY = y1 + fractionEnd * dy

                val seg = RopeSegment(
                    this,
                    startX,
                    startY,
                    endX,
                    endY,
                    radius,
                    GameObjectTags.RopeSegment
                )
                ropeSegments.add(seg)
            }
        }
        Log.d("mytag", "rope.setRopeSegments time - $time")
    }


    override fun toString(): String =
        "(Rope id = $id, objTo - ${objectTo.gameObjectTag} , objFrom - ${objectFrom.gameObjectTag}, segments size - ${ropeSegments.size})"
}