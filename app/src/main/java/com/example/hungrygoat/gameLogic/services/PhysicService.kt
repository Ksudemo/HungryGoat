package com.example.hungrygoat.gameLogic.services

import android.graphics.RectF
import com.example.hungrygoat.constants.enums.GameObjectTags
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Cell
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.EmptyObject
import com.example.hungrygoat.gameLogic.services.solution.SolutionUtility
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

class PhysicService {

    fun calcAngleBetween(a: GameObject?, b: GameObject?): Double {
        if (a == null || b == null) return -1.0
        val c = EmptyObject(b.x + 1, b.y, 0f, GameObjectTags.EMPTY)
        return calcAngleBetween(a, b, c)
    }

    fun calcAngleBetween(p1: GameObject?, p2: GameObject?, p3: GameObject?): Double {
        if (p1 == null || p2 == null || p3 == null) return -1.0

        val vectorP1P2 = Pair(p2.x - p1.x, p2.y - p1.y)
        val vectorP2P3 = Pair(p3.x - p2.x, p3.y - p2.y)

        val dotProduct = vectorP1P2.first * vectorP2P3.first + vectorP1P2.second * vectorP2P3.second
        val magnitudeP1P2 =
            sqrt(vectorP1P2.first * vectorP1P2.first + vectorP1P2.second * vectorP1P2.second)
        val magnitudeP2P3 =
            sqrt(vectorP2P3.first * vectorP2P3.first + vectorP2P3.second * vectorP2P3.second)

        val cosTheta = dotProduct / (magnitudeP1P2 * magnitudeP2P3.toDouble())

        return Math.toDegrees(acos(cosTheta))
    }

    fun distBetween(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

    fun isLineInsideBounds(start: Cell, end: Cell, bounds: List<Cell>, cellSize: Float): Boolean {
        if (!(isPointInsideBounds(start, bounds, cellSize) ||
                    isPointInsideBounds(end, bounds, cellSize))
        ) return false

        return isLineSegmentInsideBounds(start, end, bounds, cellSize)
    }

    private fun isPointInsideBounds(point: Cell, bounds: List<Cell>, cellSize: Float): Boolean {
        for (boundaryPoint in bounds) {
            val distance = distBetween(point.x, point.y, boundaryPoint.x, boundaryPoint.y)
            if (distance < 2 * cellSize)
                return true
        }
        return false
    }

    private fun isLineSegmentInsideBounds(
        start: Cell,
        end: Cell,
        bounds: List<Cell>,
        cellSize: Float
    ): Boolean {
        val steps = 50 // Adjust the number of steps based on the required precision
        val stepSizeX = (end.x - start.x) / steps
        val stepSizeY = (end.y - start.y) / steps

        for (i in 0..steps) {
            val currentPoint = Cell(RectF(), start.x + i * stepSizeX, start.y + i * stepSizeY)
            if (!isPointInsideBounds(currentPoint, bounds, cellSize))
                return false
        }
        return true
    }

    fun isCircleContainsAnotherCircle(
        coordsA: SolutionUtility.MovableInfo,
        coordsB: SolutionUtility.MovableInfo
    ): Boolean {
        val x1 = coordsA.x
        val x2 = coordsB.x

        val y1 = coordsA.y
        val y2 = coordsB.y

        val r1 = coordsA.r
        val r2 = coordsB.r

        val distBetween = sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
        return distBetween <= abs(r1 - r2)
    }

    fun isTwoCircleIntersects(
        coordsA: SolutionUtility.MovableInfo,
        coordsB: SolutionUtility.MovableInfo
    ): Boolean {
        val x1 = coordsA.x
        val x2 = coordsB.x

        val y1 = coordsA.y
        val y2 = coordsB.y

        val r1 = coordsA.r
        val r2 = coordsB.r

        val distBetween = sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
        return distBetween > abs(r1 - r2) && distBetween < (r1 + r2)
    }

}