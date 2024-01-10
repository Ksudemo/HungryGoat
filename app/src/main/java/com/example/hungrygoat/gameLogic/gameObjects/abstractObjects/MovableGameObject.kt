package com.example.hungrygoat.gameLogic.gameObjects.abstractObjects

import android.util.Log
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Rope
import com.example.hungrygoat.gameLogic.services.GridHandler
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

    fun deattachRope(rope: Rope) {
        val removed = attachedRopes.remove(rope)
        if (removed)
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
        val time = measureTimeMillis {
            bounds =
                gridHandler.getBoundaryCells(reachedSet) // temp.sortedBy { angleBetween(center, it) })
        }
        Log.d(
            "mytag",
            "bouds size = ${bounds.size}\nreached set size = ${reachedSet.size}, \n time for calc bounds = $time"
        )
    }

}