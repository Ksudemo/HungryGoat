package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.RopeGameObject
import com.example.hungrygoat.gameLogic.services.GridHandler
import kotlin.math.ceil

class Rope(
    val objectFrom: GameObject,
    val objectTo: GameObject,
    private val isTiedToRope: Boolean,
    ropeLength: Float,
    tag: GameObjectTags,
) : RopeGameObject(objectFrom, tag) {

    val color = Color.BLACK

    private val maxLength = ropeLength

    private val isObjsMovable =
        objectFrom.gameObjectTag == GameObjectTags.WOLF || objectFrom.gameObjectTag == GameObjectTags.GOAT
                || objectTo.gameObjectTag == GameObjectTags.WOLF || objectTo.gameObjectTag == GameObjectTags.GOAT

    override fun draw(canvas: Canvas, paint: Paint) {
        paint.color = color
        canvas.drawLine(objectFrom.x, objectFrom.y, objectTo.x, objectTo.y, paint)
    }

//    fun setReachedSet(gridHandler: GridHandler) = coroutineScope {
//        if (!isTiedToRope && !(isObjsMovable)) return@coroutineScope
//
//        val baseRope = getRopeConnectedTo()
//
//        ropeReachedSetDeferred = async(Dispatchers.Default) {
//            gridHandler.getGrid().filter { cell ->
//                val canReach = canRopeReachCell(gridHandler, baseRope, cell)
//                canReach
//            }
//        }
//    }

    fun setReachedSet(gridHandler: GridHandler) {
        if (!isTiedToRope && !(isObjsMovable)) return

        val baseRope = getRopeConnectedTo()

        ropeReachedSet =
            gridHandler.getGrid().filter { cell ->
                val canReach = canRopeReachCell(gridHandler, baseRope, cell)
                canReach
            }
    }

    private fun canRopeReachCell(
        gridHandler: GridHandler,
        baseRope: Rope?,
        targetCell: Cell,
    ): Boolean {
        if (isTiedToRope && baseRope != null) {
            val canReach = baseRope.ropePath.any { ropeNode ->
                val d = gridHandler.distBetween(
                    ropeNode,
                    targetCell.x, targetCell.y
                )
                d <= maxLength
            }
            if (canReach) return true
        }

        val d =
            gridHandler.distBetween(
                getAnchorPoint() ?: return false,
                targetCell.x, targetCell.y
            )

        return d <= maxLength
    }

    private fun getAnchorPoint() = getRopeNode() ?: getPeg()

    private fun getRopeConnectedTo(): Rope? =
        if (!isTiedToRope)
            null
        else
            getRopeNode()?.baseRope

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
        val nodes = mutableListOf<RopeNode>()

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

            nodes.add(RopeNode(this, nextX, nexty, GameObjectTags.RopeNode))
        }

        ropePath += nodes

    }
}