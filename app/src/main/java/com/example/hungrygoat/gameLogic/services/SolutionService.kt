package com.example.hungrygoat.gameLogic.services

import android.util.Log
import com.example.hungrygoat.constants.LevelConditions
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class SolutionService {
    fun checkSolution(
        goat: Goat,
        gridHandler: GridHandler,
        targetShape: LevelConditions,
    ) =
        try {
            val goatBounds = goat.bounds
            val goatPath = goat.path
            val cellSize = gridHandler.cellSize

            val cx = goatBounds.map { it.x }.average().toFloat()
            val cy = goatBounds.map { it.y }.average().toFloat()
            val r = goatBounds.map { sqrt((it.x - cx).pow(2) + (it.y - cy).pow(2)) }.average()
                .toFloat() + cellSize / 2

            val testCircle = isCircle(goatBounds, cx, cy, r)
            val testHalfCircle = isHalfCircle(goatBounds, cx, cy, r)
            val testRing = isRing(testCircle, goatPath, cellSize, cx, cy)
            val testRect = isRect(goatBounds)

            Log.d(
                "MyTag",
                "Current shape is:\n " +
                        "Circle - $testCircle\n " +
                        "Ring - $testRing\n " +
                        "HalfCircle - $testHalfCircle\n " +
                        "Reactangle (TODO) - $testRect "
            )

            when (targetShape) {
                LevelConditions.CIRCLE -> isCircle(goatBounds, cx, cy, r)
                LevelConditions.HALFCIRCLE -> isHalfCircle(goatBounds, cx, cy, r)

                LevelConditions.RING -> isRing(
                    isCircle(goatBounds, cx, cy, r), goatPath, cellSize, cx, cy
                )

                LevelConditions.HALFRING -> true

                LevelConditions.MOON -> true

                LevelConditions.RECTANGLE -> isRect(goatBounds)
                LevelConditions.TRIANGLE -> true

                LevelConditions.LEAF -> true
                LevelConditions.HEXAGON -> true

                LevelConditions.ARROW -> true

                LevelConditions.RAINDROP -> true

                else -> true
            }

        } catch (e: Exception) {
            Log.e("mytag", "Exception in SolutionService ${e.printStackTrace()}")
            false
        }


    private fun isCircle(bounds: List<Cell>, cx: Float, cy: Float, r: Float): Boolean {
        return bounds.all { boundCell ->
            val dx = abs(boundCell.x - cx)
            val dy = abs(boundCell.y - cy)

            dx < r && dy < r
        }
    }

    private fun isHalfCircle(bounds: List<Cell>, cx: Float, cy: Float, r: Float): Boolean {
        val isCircle = bounds.all { boundCell ->
            val dx = abs(boundCell.x - cx)
            val dy = abs(boundCell.y - cy)

            dx * dx + dy * dy <= r * r
        }

        return isCircle && bounds.size in (bounds.size / 2 - 1..bounds.size / 2 + 1)
    }

    private fun isRing(
        isCircle: Boolean,
        path: List<Cell>,
        cellSize: Float,
        cx: Float,
        cy: Float,
    ): Boolean {
        if (isCircle)
            return sqrt(path.minOf { visitedCell ->
                val dx = abs(visitedCell.x - cx)
                val dy = abs(visitedCell.y - cy)

                dx * dx + dy * dy
            }) > cellSize / 2

        return false
    }

    private fun isRect(bounds: List<Cell>): Boolean {
        return bounds.isNotEmpty()
    }
}
