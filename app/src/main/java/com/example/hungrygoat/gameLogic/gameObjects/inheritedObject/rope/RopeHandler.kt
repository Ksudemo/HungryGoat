package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.rope

import com.example.hungrygoat.gameLogic.game.grid.GridHandler
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Cell
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.math.max
import kotlin.math.min

class RopeHandler {
    fun mergeBoundingBoxes(bBoxList: List<Pair<IntRange, IntRange>>): Pair<IntRange, IntRange> {
        fun trimRange(curRange: IntRange, otherRange: IntRange): IntRange {
            val curLeft = max(curRange.first, otherRange.first)
            val curRight = min(curRange.last, otherRange.last)
            return curLeft..curRight
        }

        if (bBoxList.isEmpty())
            return IntRange.EMPTY to IntRange.EMPTY

        var (curX, curY) = bBoxList.first()

        for (bbox in bBoxList.drop(1)) {
            curX = trimRange(curX, bbox.first)
            curY = trimRange(curY, bbox.second)
        }

        return curX to curY
    }

    suspend fun getRopeReachedSetIndecies(
        gridHandler: GridHandler,
        ranges: Pair<IntRange, IntRange>,
        rope: Rope
    ): Set<Pair<Int, Int>> =
        when (val connectedRope = rope.getRopeConnectedTo()) {
            null -> when (val anchorPoint = rope.getAnchorPoint()) {
                null -> setOf()
                else -> getReachedSetIndecies(
                    gridHandler,
                    setOf(anchorPoint),
                    ranges,
                    rope.ropeLength
                ).await().toSet()
            }

            else -> {
                getReachedSetIndecies(
                    gridHandler,
                    connectedRope.ropeSegments,
                    ranges, rope.ropeLength
                ).await().toSet()
            }
        }


    private suspend fun getReachedSetIndecies(
        gridHandler: GridHandler,
        anchorPoints: Set<GameObject>,
        ranges: Pair<IntRange, IntRange>,
        ropeLength: Float,
    ): Deferred<List<Pair<Int, Int>>> = coroutineScope {
        val grid = gridHandler.getGrid()

        return@coroutineScope async {
            ranges.first.flatMap { i ->
                ranges.second.filter { j ->
                    anchorPoints.any { anchor ->
                        canReachCell(
                            gridHandler,
                            anchor,
                            grid[i, j], ropeLength
                        )
                    }
                }.map { j -> i to j }
            }
        }
    }

    private fun canReachCell(
        gridHandler: GridHandler,
        from: GameObject,
        to: Cell,
        ropeLength: Float,
    ): Boolean {
        val distance = gridHandler.distBetween(to, from)
        return distance <= ropeLength + gridHandler.getGrid().cellSize / 2
    }
}