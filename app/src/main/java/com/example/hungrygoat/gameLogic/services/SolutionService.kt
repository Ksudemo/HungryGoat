package com.example.hungrygoat.gameLogic.services

import android.util.Log
import com.example.hungrygoat.constants.LevelConditions
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class SolutionService {

    fun checkSolution(
        goat: Goat,
        dog: Dog?,
        cellSize: Float,
        grid: List<Cell>,
        targetShape: LevelConditions,
    ): Boolean {

        val cx = goat.bounds.map { it.x }.average().toFloat()
        val cy = goat.bounds.map { it.y }.average().toFloat()
        val r = goat.bounds.map { sqrt((it.x - cx).pow(2) + (it.y - cy).pow(2)) }.average()
            .toFloat() + cellSize / 2

        val boundsWithoutGridEdges = mutableListOf<Cell>()
        boundsWithoutGridEdges.addAll(goat.bounds)

        boundsWithoutGridEdges.removeIf { grid.contains(it) }

        val testCircle = isCircle(goat, cx, cy, r)
        val testHalfCircle = isHalfCircle(goat, cx, cy, r)
        val testRing = isRing(goat, cellSize, cx, cy, r)
        val testRect = isRect(goat)


        Log.d("MyTag", "current shape is circle - $testCircle")
        Log.d("MyTag", "current shape is halfCircle - $testHalfCircle")

        Log.d("MyTag", "current shape is ring - $testRing")

        val sol = when (targetShape) {
            LevelConditions.CIRCLE -> isCircle(goat, cx, cy, r)
            LevelConditions.HALFCIRCLE -> true// isHalfCircle(goat, cellSize)

            LevelConditions.RING -> isRing(goat, cellSize, cx, cy, r)
            LevelConditions.HALFRING -> true

            LevelConditions.MOON -> true

            LevelConditions.SQUARE -> true
            LevelConditions.TRIANGLE -> true

            LevelConditions.LEAF -> true
            LevelConditions.HEXAGON -> true

            LevelConditions.ARROW -> true

            LevelConditions.RAINDROP -> true

            else -> true
        }


        return sol
    }

    private fun isCircle(goat: Goat, cx: Float, cy: Float, r: Float): Boolean {
        return goat.bounds.all { boundCell ->
            val dx = abs(boundCell.x - cx)
            val dy = abs(boundCell.y - cy)

            dx < r && dy < r
        }
    }

    private fun isHalfCircle(goat: Goat, cx: Float, cy: Float, r: Float): Boolean {
        val isCircle = goat.bounds.all { boundCell ->
            val dx = abs(boundCell.x - cx)
            val dy = abs(boundCell.y - cy)

            dx * dx + dy * dy <= r * r
        }

        return isCircle && goat.bounds.size in (goat.bounds.size / 2 - 1..goat.bounds.size / 2 + 1)
    }

    private fun isRing(
        goat: Goat,
        cellSize: Float,
        cx: Float,
        cy: Float,
        r: Float,
    ): Boolean {
        val isCircle = isCircle(goat, cx, cy, r)
        if (isCircle)
            return sqrt(goat.visited.minOf { visitedCell ->
                val dx = abs(visitedCell.x - cx)
                val dy = abs(visitedCell.y - cy)

                dx * dx + dy * dy
            }) > cellSize / 2

        return false
    }

    private fun isRect(goat: Goat): Boolean {
        for (i in 0 until goat.bounds.size - 2) {
            val p1 = goat.bounds[i]
            val p2 = goat.bounds[i + 1]
            val angle = p1.angleBetween(p2)
            Log.d("MyTag", "angle = $angle")
        }

        return true
    }
}
