package com.example.hungrygoat.gameLogic.services

import android.util.Log
import com.example.hungrygoat.constants.LevelConditions
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class SolutionService {
    fun checkSolution(
        goat: Goat,
        dog: Dog?,
        gridHandler: GridHandler,
        targetShape: LevelConditions,
    ) =
        try {
            val goatBounds = goat.bounds
            val goatReachedSet = goat.reachedSet // TODO w/o dog
            val goatPath = goat.path
            val cellSize = gridHandler.cellSize

            val cx = goatBounds.map { it.x }.average().toFloat()
            val cy = goatBounds.map { it.y }.average().toFloat()
            val r = goatBounds.map { sqrt((it.x - cx).pow(2) + (it.y - cy).pow(2)) }.average()
                .toFloat() + cellSize / 2

            val testCircle = isCircle(goatBounds, cx, cy, r, cellSize)
            val testRing = isRing(testCircle, goatPath, cellSize, cx, cy)
            val testRect = isRect(goatBounds)
            val testTrinagle = isTriangle(goatBounds)
            val testHalfCircle = isHalfCircle(goatBounds, cx, cy, r)
            val testHexagon = true;
            val testLeaf = true;
            val testHalfRing = true;
            val testMoon = true;
            val testArrow = true;
            val testRaindrop = true;

            Log.d(
                "MyTag",
                "Current shape is:\n " +
                        "Circle - $testCircle\n " +
                        "Ring - $testRing\n " +
                        "Reactangle - $testRect\n " +
                        "Triangle - $testTrinagle" +
                        "HalfCircle (TODO)- $testHalfCircle\n " +
                        "testHalfRing - $testHalfRing" +
                        "testHexagon (TODO) - $testHexagon\n" +
                        "testLeaf (TODO) - $testLeaf\n" +
                        "testMoon - $testMoon\n" +
                        "testArrow - $testArrow\n" +
                        "testRaindrop - $testRaindrop"
            )

            when (targetShape) {
                LevelConditions.CIRCLE -> testCircle
                LevelConditions.RING -> isRing(testCircle, goatPath, cellSize, cx, cy)
                LevelConditions.RECTANGLE -> isRect(goatBounds)
                LevelConditions.TRIANGLE -> isTriangle(goatBounds)

                LevelConditions.HEXAGON -> true

                LevelConditions.LEAF -> true

                LevelConditions.HALFCIRCLE -> isHalfCircle(goatBounds, cx, cy, r)
                LevelConditions.HALFRING -> true
                LevelConditions.MOON -> true

                LevelConditions.ARROW -> true
                LevelConditions.RAINDROP -> true

                else -> true
            }

        } catch (e: Exception) {
            Log.e("mytag", "Exception in SolutionService ${e.printStackTrace()}")
            false
        }


    private fun isCircle(
        bounds: List<Cell>,
        cx: Float,
        cy: Float,
        r: Float,
        cellSize: Float,
    ): Boolean {
        val delta = cellSize * 1.5
        return bounds.all { boundCell ->
            sqrt((boundCell.x - cx).pow(2) + (boundCell.y - cy).pow(2)) in (r - delta..r + delta)
        }
    }

    private fun isHalfCircle(bounds: List<Cell>, cx: Float, cy: Float, r: Float): Boolean {
        val isCircle = bounds.all { boundCell ->
            val dx = abs(boundCell.x - cx)
            val dy = abs(boundCell.y - cy)

            dx * dx + dy * dy <= r * r
        }

        return isCircle
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
        val minX = bounds.minOfOrNull { it.x } ?: 0f
        val minY = bounds.minOfOrNull { it.y } ?: 0f
        val maxX = bounds.maxOfOrNull { it.x } ?: 0f
        val maxY = bounds.maxOfOrNull { it.y } ?: 0f

        val rightTop = bounds.find { it.x == maxX && it.y == minY } ?: return false
        val rightBot = bounds.find { it.x == maxX && it.y == maxY } ?: return false
        val leftBot = bounds.find { it.x == minX && it.y == maxY } ?: return false
        val leftTop = bounds.find { it.x == minX && it.y == minY } ?: return false

        val physics = PhysicService()
        val a1 = physics.calcAngleBetweenInDeg(leftTop, rightTop, rightBot).roundToInt()
        val a2 = physics.calcAngleBetweenInDeg(rightTop, rightBot, leftBot).roundToInt()
        val a3 = physics.calcAngleBetweenInDeg(rightBot, leftBot, leftTop).roundToInt()
        val a4 = physics.calcAngleBetweenInDeg(leftBot, leftTop, rightTop).roundToInt()

        val delta = 1
        return (a1 - a2).absoluteValue <= delta &&
                (a2 - a3).absoluteValue <= delta &&
                (a3 - a4).absoluteValue <= delta &&
                (a4 - a1).absoluteValue <= delta
    }

    private fun isTriangle(bounds: List<Cell>): Boolean {
        val minX = bounds.minOfOrNull { it.x } ?: 0f
        val minY = bounds.minOfOrNull { it.y } ?: 0f
        val maxX = bounds.maxOfOrNull { it.x } ?: 0f
        val maxY = bounds.maxOfOrNull { it.y } ?: 0f

        val corners = mutableListOf<Cell>()
        val rightTop = bounds.find { it.x == maxX && it.y == minY }
        val rightBot = bounds.find { it.x == maxX && it.y == maxY }
        val leftBot = bounds.find { it.x == minX && it.y == maxY }
        val leftTop = bounds.find { it.x == minX && it.y == minY }

        if (rightTop != null) corners.add(rightTop)
        if (rightBot != null) corners.add(rightBot)
        if (leftBot != null) corners.add(leftBot)
        if (leftTop != null) corners.add(leftTop)

        if (corners.size != 3) return false

        val physics = PhysicService()
        val a1 = physics.calcAngleBetweenInDeg(corners[0], corners[1], corners[2]).roundToInt()
        val a2 = physics.calcAngleBetweenInDeg(corners[1], corners[2], corners[0]).roundToInt()
        val a3 = physics.calcAngleBetweenInDeg(corners[2], corners[0], corners[1]).roundToInt()

        val lenSide1 = physics.distBetween(corners[0].x, corners[0].y, corners[1].x, corners[1].y)
        val lenSide2 = physics.distBetween(corners[0].x, corners[0].y, corners[2].x, corners[2].y)
        val lenSide3 = physics.distBetween(corners[2].x, corners[2].y, corners[1].x, corners[1].y)

        Log.d(
            "mytag",
            " a1 = $a1\n a2 = $a2\n a3 = $a3\n len1 = $lenSide1\n len2 = $lenSide2\n len3 = $lenSide3 "
        )

        // a,b,c - side of a triangle => len(a) < len(b) + len(c)
        val condA =
            lenSide1 < lenSide2 + lenSide3 && lenSide2 < lenSide1 + lenSide3 && lenSide3 < lenSide1 + lenSide2

        // a1 + a2 + a3 == 180* +- delta
        val delta = 1 // TODO remove (?); it should always be 180*
        val condB = (a1 + a2 + a3) in (180 - delta..180 + delta)

        return condA && condB
    }
}
