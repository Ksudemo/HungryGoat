package com.example.hungrygoat.gameLogic.gameObjects.abstractObjects

import android.util.Log
import com.example.hungrygoat.constants.enums.GameObjectTags
import com.example.hungrygoat.gameLogic.game.grid.GridHandler
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Cell
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.rope.Rope
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.rope.RopeHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTime

abstract class MovableGameObject(
    private val vx: Float,
    private val vy: Float,
    tg: GameObjectTags,
    rad: Float
) : GameObject(vx, vy, tg, rad) {

    private val attachedRopeActions = mutableListOf<() -> Unit>()

    var lastVisitedIndex = 0
    var path = listOf<Cell>()

    var reachedSet = setOf<Pair<Int, Int>>()  // Множество клеток, до которых может дотянутся
    var bounds = listOf<Cell>()  // границы
    var intersectionPathWithGridEdges = setOf<Cell>()
    fun invokeAction() {
        val time = measureTime {
            attachedRopeActions.forEach { it.invoke() }
        }
        Log.d("mytag", "All movable actions done in $time\n ")
    }

    fun attachRope(rope: Rope) {
        attachedRopes.add(rope)
        invokeAction()
    }

    fun deattachRope(rope: Rope) {
        val removed = attachedRopes.remove(rope)
        if (removed)
            invokeAction()

    }

    fun movableAction(vararg actions: () -> Unit) {
        attachedRopeActions.addAll(actions)
    }

    fun moveToStart() {
        lastVisitedIndex = 0
        x = vx
        y = vy
        path = emptyList()
    }


    private fun calculateBoundingBox(
        gridHandler: GridHandler,
        ropeHandler: RopeHandler
    ): Pair<IntRange, IntRange> {
        val defferedBoundgBox = attachedRopes.map { rope ->
            rope.getRopeBoundinBoxIndecies(gridHandler.getGrid())
        }

        return ropeHandler.mergeBoundingBoxes(defferedBoundgBox)
    }

    private fun calculateAsyncReachedSet(
        gridHandler: GridHandler,
        ropeHandler: RopeHandler,
        bBox: Pair<IntRange, IntRange>
    ): Set<Pair<Int, Int>> {
        val cols = bBox.first.first..bBox.first.last
        val rows = bBox.second.first..bBox.second.last
        val ranges = cols to rows

        val scope = CoroutineScope(Dispatchers.Default)
        val defferedRopeReachedSets = attachedRopes.map { rope ->
            scope.async {
                ropeHandler.getRopeReachedSetIndecies(gridHandler, ranges, rope)
            }
        }

        return runBlocking {
            defferedRopeReachedSets.awaitAll().reduce { acc, set -> acc.intersect(set) }
        } + gridHandler.getObjectGridIndecies(this)
    }

    fun calcReachedSet(gridHandler: GridHandler) {
        reachedSet = if (attachedRopes.isEmpty())
            hashSetOf()
        else {
            val ropeHandler = RopeHandler()

            val bBox = calculateBoundingBox(gridHandler, ropeHandler)
            calculateAsyncReachedSet(gridHandler, ropeHandler, bBox)
        }
    }

    fun getBoundary(gridHandler: GridHandler, cells: List<Cell>? = null): List<Cell> =
        if (cells != null) {
            val mappedToGridIndecies = cells.map { gridHandler.getObjectGridIndecies(it) }.toSet()
            gridHandler.getBoundaryCells(mappedToGridIndecies)
        } else gridHandler.getBoundaryCells(reachedSet)
}