package com.example.hungrygoat.gameLogic.gameObjects.abstractObjects

import android.util.Log
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Rope
import com.example.hungrygoat.gameLogic.services.GridHandler
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.system.measureTimeMillis

abstract class MovableGameObject(
    private val vx: Float,
    private val vy: Float,
    tg: GameObjectTags,
) :
    GameObject(vx, vy, tg) {

    private val attachedRopeActions = mutableListOf<() -> Unit>()
    private val attachedRopes = mutableListOf<Rope>()

    protected val updatePerFrame = 100
    var hadAvailableCells = true

    var lastVisitedIndex = 0
    var path = listOf<Cell>()

    var reachedSet = hashSetOf<Cell>()  // Множество клеток, до которых может дотянутся
    var bounds = listOf<Cell>()  // границы

    fun invokeAction() {
        attachedRopeActions.forEach { it.invoke() }
    }

    fun attachRope(rope: Rope) {
        attachedRopes.add(rope)
        invokeAction()
    }

    fun movableAction(action: () -> Unit) {
        attachedRopeActions.add(action)
    }

    fun moveToStart() {
        lastVisitedIndex = 0
        path = emptyList()
        hadAvailableCells = true

        x = vx
        y = vy
    }

    fun calcReachedSet(gridHandler: GridHandler) {
        val time = measureTimeMillis {
            reachedSet = if (attachedRopes.isEmpty())
                gridHandler.getGrid().flatten().toHashSet()
            else
                attachedRopes.map { it.ropeReachedSet }
                    .reduce { acc, set -> acc.intersect(set).toHashSet() }
        }
        Log.d("mytag", "reachedSet calculated for $time ")
    }

    fun setBoundary(gridHandler: GridHandler) {
        fun angleBetween(center: Pair<Float, Float>, point: Cell): Float {
            val dx = point.x - center.first
            val dy = point.y - center.second
            return atan2(dy, dx)
        }

        val time = measureTimeMillis {
//        val temp = gridHandler.getBoundaryCells(reachedSet)
//
//        val centerX = temp.map { it.x }.average().toFloat()
//        val centerY = temp.map { it.y }.average().toFloat()
//        val center = Pair(centerX, centerY)
//        Log.v("MyTag", "Center counted")
            bounds = gridHandler.getBoundaryCells(reachedSet)
        }
        Log.d(
            "mytag",
            "bouds size = ${bounds.size}\nreached set size = ${reachedSet.size}, \n time for calc bounds = $time"
        )
        //removeCollinearCells(gridHandler.getBoundaryCells(reachedSet)) // temp.sortedBy { angleBetween(center, it) })
    }

    private fun removeCollinearCells(points: List<Cell>): List<Cell> {
        fun isOnTheSameLine(a: Cell, b: Cell, c: Cell, d: Cell) =
            abs((b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)) < 1e-9 &&
                    abs((c.x - b.x) * (d.y - b.y) - (c.y - b.y) * (d.x - b.x)) < 1e-9
        if (points.size < 4) return points

        val res = mutableListOf<Cell>()
        var pointA: Cell = points[0]
        var pointB = points[1]
        var pointC = points[2]

        for (i in 3 until points.size) {
            val pointD = points[i]
            val check = !isOnTheSameLine(pointA, pointB, pointC, pointD)
            if (check)
                res.add(pointB)
            pointA = pointB
            pointB = pointC
            pointC = pointD
        }
        res.add(pointC)

        return res
    }

    private fun removeRedundantCells(bounds: List<Cell>): List<Cell> {
        if (bounds.size < 3) return bounds

        val res = mutableListOf<Cell>()

        val iterator = bounds.iterator()

        var i = 0
        while (iterator.hasNext()) {
            val prevPoint = iterator.next()
            val currentPoint = iterator.next()
            val nextPoint = if (iterator.hasNext()) iterator.next() else break

            val p1 = prevPoint.x == nextPoint.x && currentPoint.x != prevPoint.x
            val p2 = prevPoint.y == nextPoint.y && currentPoint.y != prevPoint.y

            if (p1 || p2) {
                res.add(prevPoint)
            }
            i++
            Log.d("MyTag", "i = $i ")
        }
        Log.d("MyTag", "Done remove redundant cells")
        return res
    }
}