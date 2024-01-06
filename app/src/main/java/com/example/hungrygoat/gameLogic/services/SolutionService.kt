package com.example.hungrygoat.gameLogic.services

import android.util.Log
import com.example.hungrygoat.constants.LevelConditions
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
        targetShape: LevelConditions,
    ) =
        try {


            val cx = goat.bounds.map { it.x }.average().toFloat()
            val cy = goat.bounds.map { it.y }.average().toFloat()
            val r = goat.bounds.map { sqrt((it.x - cx).pow(2) + (it.y - cy).pow(2)) }.average()
                .toFloat() + cellSize / 2

            val testCircle = isCircle(goat, cx, cy, r)
            val testHalfCircle = isHalfCircle(goat, cx, cy, r)
            val testRing = isRing(goat, cellSize, cx, cy, r)
//        val testRect = isRect(goat)

            Log.d(
                "MyTag",
                "Current shape is:\n " +
                        "Circle - $testCircle\n " +
                        "Ring - $testRing\n " +
                        "HalfCircle - $testHalfCircle\n " + ""
//                    "Reactangle - $testRect "
            )

            when (targetShape) {
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

        } catch (e: Exception) {
            false
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
            return sqrt(goat.path.minOf { visitedCell ->
                val dx = abs(visitedCell.x - cx)
                val dy = abs(visitedCell.y - cy)

                dx * dx + dy * dy
            }) > cellSize / 2

        return false
    }

    private fun isRect(goat: Goat): Boolean {
//        val rect = goat.visited.first().rect
//
//        val minX = goat.bounds.minOfOrNull { it.x } ?: 0f
//        val minY = goat.bounds.minOfOrNull { it.y } ?: 0f
//        val maxX = goat.bounds.maxOfOrNull { it.x } ?: 0f
//        val maxY = goat.bounds.maxOfOrNull { it.y } ?: 0f
//
//        val pointA = goat.bounds.find { it.x == maxX && it.y == maxY } ?: return false
//        val pointB = goat.bounds.find { it.x == maxX && it.y == minY } ?: return false
//        val pointC = goat.bounds.find { it.x == minX && it.y == minY } ?: return false
//        val pointD = goat.bounds.find { it.x == minX && it.y == maxY } ?: return false
//
//        Log.d("MyTag", "Points found")
//        // Проверяем, что параметры прямоугольника равны
//        return rect.left == minX && rect.top == minY && rect.right == maxX && rect.bottom == maxY
        return true
    }
}
