package com.ksudemo.hungrygoat.gameLogic.services.solution

import android.util.Log
import com.ksudemo.hungrygoat.gameLogic.game.grid.GridHandler
import com.ksudemo.hungrygoat.gameLogic.gameObjects.abstractObjects.MovableGameObject
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.Cell
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.EmptyObject
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.rope.Rope
import com.ksudemo.hungrygoat.gameLogic.services.PhysicService
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class SolutionUtility {
    private val physics = PhysicService()
    lateinit var gridHandler: GridHandler

    data class MovableInfo(
        val x: Float,
        val y: Float,
        val r: Float,
        val bounds: List<Cell>
    )

    fun isGridHandlerInitialized() = ::gridHandler.isInitialized
    fun setGridHadler(gh: GridHandler) {
        gridHandler = gh
    }

    fun getCoords(
        movable: MovableGameObject?,
        cellSize: Float,
        cells: List<Cell>? = null
    ): MovableInfo {
        return when {
            movable != null -> {
                val bounds = cells ?: movable.bounds
                val cx = bounds.map { it.x }.average().toFloat()
                val cy = bounds.map { it.y }.average().toFloat()
                val r =
                    bounds.map { sqrt((it.x - cx).pow(2) + (it.y - cy).pow(2)) }.average()
                        .toFloat() + cellSize / 2
                MovableInfo(cx, cy, r, bounds)
            }

            else -> MovableInfo(0f, 0f, 0f, emptyList())
        }
    }

    fun getAngles(corners: List<Cell>): List<Int> {
        if (corners.size < 3) return emptyList()

        val res = mutableListOf<Int>()

        var prev = corners[corners.size - 2]
        var cur = corners[corners.size - 1]
        var next = corners[0]
        for (i in corners.indices) {
            val angle = physics.calcAngleBetween(prev, cur, next).roundToInt()
            res.add(180 - angle)

            prev = cur
            cur = next
            next = corners[(i + 1) % corners.size]
        }

        return res
    }

    fun getSideLengths(corners: List<Cell>): List<Float> {
        if (corners.isEmpty())
            return emptyList()

        val pairs = corners.map { EmptyObject(it.x, it.y, 0f) }.windowed(2).toMutableList()
        val f = corners.first()
        val l = corners.last()
        pairs.add(listOf(EmptyObject(f.x, f.y, 0f), EmptyObject(l.x, l.y, 0f)))

        return pairs.map { gridHandler.distBetween(it[0], it[1]) }
    }

    fun circleIntersects(gridHandler: GridHandler, ropeA: Rope, ropeB: Rope): Boolean {
        if (ropeA.isTiedToRope || ropeB.isTiedToRope)
            return false

        val distBetweenCircleCenters = gridHandler.distBetween(
            ropeA.getAnchorPoint() ?: return false,
            ropeB.getAnchorPoint() ?: return false
        )
        return distBetweenCircleCenters < ropeA.ropeLength + ropeB.ropeLength
    }

    fun getCorners(bounds: List<Cell>, cellSize: Float): List<Cell> {
        val grah = grahamScan(bounds, cellSize)
        return filteredGrahamScan(grah, true)
    }

    fun filteredGrahamScan(
        oldPoints: List<Cell>,
        shouldLog: Boolean = false
    ): List<Cell> {
        fun isValidAngle(a: Cell, b: Cell, c: Cell, angleThreshold: Float = 17f): Boolean {
            val angle = physics.calcAngleBetween(a, b, c)
            return angle > angleThreshold && angle < 180
        }

        fun isValidDistance(a: Cell, b: Cell, distThreshold: Float): Boolean =
            physics.distBetween(a.x, a.y, b.x, b.y) > distThreshold


        val points = oldPoints.toMutableList()

        val smoothedPoints = mutableListOf<Cell>()
        val firstOkPoint = (
                points.windowed(3).firstOrNull { (a, b, c) -> isValidAngle(a, b, c) }
                    ?: return emptyList())[1]

        val firstIndex = points.indexOf(firstOkPoint)
        smoothedPoints.add(firstOkPoint)
        for (i in points.indices) {
            val a = smoothedPoints.last()
            val b = points[(i + firstIndex) % points.size]
            val c = points[(i + 1 + firstIndex) % points.size]

            if (isValidAngle(a, b, c))
                smoothedPoints.add(b)
        }

        if (shouldLog)
            for (i in smoothedPoints.indices) {
                val bIndex = (i + 1) % smoothedPoints.size
                val cIndex = (i + 2) % smoothedPoints.size
                val a = smoothedPoints[i]
                val b = smoothedPoints[bIndex]
                val c = smoothedPoints[cIndex]

                val orientation = getOrientation(a, b, c, 8f)
                Log.d("mytag", "$i -> $bIndex -> $cIndex = $orientation")
            }


        val lens = getSideLengths(smoothedPoints.distinct())
        if (lens.isEmpty()) {
            Log.d("mytag", "lengths is empty")
            return emptyList()
        }

        val averageSideLen = (lens.sum() - lens.max() - lens.min()) / (lens.size - 2)
        val distThreshold = averageSideLen * 0.15f

        if (shouldLog) {
            Log.d("mytag", "smoothed size - ${smoothedPoints.size}")
            Log.d("mytag", "distThreshold - $distThreshold")
        }

        val resList = mutableListOf<Cell>()
        resList.add(smoothedPoints.first())
        for (i in 1 until smoothedPoints.size) {
            val cur = smoothedPoints[i]
            val resListLast = resList.last()

            if (!isValidDistance(cur, resListLast, distThreshold)) {
                val middleCell =
                    Cell(cur.rect, (cur.x + resListLast.x) / 2, (cur.y + resListLast.y) / 2)
                resList.removeLast()
                resList.add(middleCell)
            } else resList.add(cur)
        }

        if (!isValidDistance(resList.first(), resList.last(), distThreshold)) {
            val first = resList.removeFirst()
            val last = resList.removeLast()
            val middle = Cell(first.rect, (first.x + last.x) / 2, (first.y + last.y) / 2)

            resList.add(middle)
        }


        return resList.distinct()
    }

    private fun getOrientation(a: Cell, b: Cell, c: Cell, cellSize: Float): Int {
        val eps = cellSize * 8
        val value = (b.y - a.y) * (c.x - b.x) - (b.x - a.x) * (c.y - b.y)
        return when {
            value in (-eps..eps) -> 0 // коллинеарны
            value > eps -> 1 // по часовой стрелке
            else -> 2 // против часовой стрелки
        }
    }

    fun grahamScan(points: List<Cell>, cellSize: Float): List<Cell> {
        if (points.size < 3) return points

        val sortedPoints = points.sortedWith(compareBy({ it.y }, { it.x }))

        val hull = mutableListOf<Cell>()

        for (point in sortedPoints) {
            while (hull.size >= 2 && getOrientation(
                    hull[hull.size - 2],
                    hull.last(),
                    point,
                    cellSize
                ) != 2
            )
                hull.removeAt(hull.size - 1)
            hull.add(point)
        }

        val upperHullStartIndex = hull.size + 1
        for (i in sortedPoints.size - 2 downTo 0) {
            val point = sortedPoints[i]
            while (hull.size >= upperHullStartIndex && getOrientation(
                    hull[hull.size - 2],
                    hull.last(),
                    point,
                    cellSize
                ) != 2
            )
                hull.removeAt(hull.size - 1)

            hull.add(point)
        }

        return hull.distinct()
    }
}