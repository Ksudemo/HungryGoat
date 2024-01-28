package com.example.hungrygoat.gameLogic.services.solution

import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.MovableGameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Rope
import com.example.hungrygoat.gameLogic.services.PhysicService
import com.example.hungrygoat.gameLogic.services.grid.GridHandler
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class SolutionUtility {
    private val physics = PhysicService()
    fun getCoords(movable: MovableGameObject?, cellSize: Float): SolutionService.Coords {
        return when {
            movable != null -> {
                val cx = movable.bounds.map { it.x }.average().toFloat()
                val cy = movable.bounds.map { it.y }.average().toFloat()
                val r =
                    movable.bounds.map { sqrt((it.x - cx).pow(2) + (it.y - cy).pow(2)) }.average()
                        .toFloat() + cellSize / 2
                SolutionService.Coords(cx, cy, r)
            }

            else -> SolutionService.Coords(0f, 0f, 0f)
        }
    }

    fun getBoundgBox(bounds: List<Cell>): List<Cell?> {
        val minX = bounds.minOfOrNull { it.x } ?: 0f
        val minY = bounds.minOfOrNull { it.y } ?: 0f
        val maxX = bounds.maxOfOrNull { it.x } ?: 0f
        val maxY = bounds.maxOfOrNull { it.y } ?: 0f

        val rightTop = bounds.find { it.x == maxX && it.y == minY }
        val rightBot = bounds.find { it.x == maxX && it.y == maxY }
        val leftBot = bounds.find { it.x == minX && it.y == maxY }
        val leftTop = bounds.find { it.x == minX && it.y == minY }

        return listOf(rightTop, rightBot, leftBot, leftTop)
    }

    fun getAngles(corners: List<Cell>): List<Int> {
        return when (corners.size) {
            3 -> {
                val a1 =
                    physics.calcAngleBetweenInDeg(corners[0], corners[1], corners[2]).roundToInt()
                val a2 =
                    physics.calcAngleBetweenInDeg(corners[1], corners[2], corners[0]).roundToInt()
                val a3 =
                    physics.calcAngleBetweenInDeg(corners[2], corners[0], corners[1]).roundToInt()
                listOf(a1, a2, a3)
            }

            4 -> {
                val a1 =
                    physics.calcAngleBetweenInDeg(corners[3], corners[0], corners[1]).roundToInt()
                val a2 =
                    physics.calcAngleBetweenInDeg(corners[0], corners[1], corners[2]).roundToInt()
                val a3 =
                    physics.calcAngleBetweenInDeg(corners[1], corners[2], corners[3]).roundToInt()
                val a4 =
                    physics.calcAngleBetweenInDeg(corners[2], corners[3], corners[0]).roundToInt()
                listOf(a1, a2, a3, a4)
            }

            else -> emptyList()
        }
    }

    fun getSideLengths(corners: List<Cell>): List<Float> {
        return when (corners.size) {
            3 -> {
                val lenSide1 =
                    physics.distBetween(corners[0].x, corners[0].y, corners[1].x, corners[1].y)
                val lenSide2 =
                    physics.distBetween(corners[0].x, corners[0].y, corners[2].x, corners[2].y)
                val lenSide3 =
                    physics.distBetween(corners[2].x, corners[2].y, corners[1].x, corners[1].y)
                listOf(lenSide1, lenSide2, lenSide3)
            }

            else -> emptyList()
        }
    }

    fun circleIntersects(gridHandler: GridHandler, ropeA: Rope, ropeB: Rope): Boolean {
        if (ropeA.isTiedToRope || ropeB.isTiedToRope)
            return false

        val distBetweenCircleCenters =
            gridHandler.distBetween(
                ropeA.getAnchorPoint() ?: return false,
                ropeB.getAnchorPoint() ?: return false
            )
        return distBetweenCircleCenters < ropeA.ropeLength + ropeB.ropeLength
    }

}