package com.example.hungrygoat.gameLogic.gameObjects.abstractObjects

import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Rope
import com.example.hungrygoat.gameLogic.services.grid.GridHandler

abstract class MovableGameObject(
    private val vx: Float,
    private val vy: Float,
    tg: GameObjectTags,
) :
    GameObject(vx, vy, tg) {

    private val attachedRopeActions = mutableListOf<() -> Unit>()
    val attachedRopes = mutableListOf<Rope>()

    protected val updatePerFrame = 100
    var hadAvailableCells = true

    var lastVisitedIndex = 0
    var path = listOf<Cell>()

    var reachedSet = setOf<Pair<Int, Int>>()  // Множество клеток, до которых может дотянутся
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

    fun movableAction(vararg actions: () -> Unit) {
        attachedRopeActions.addAll(actions)
    }

    fun moveToStart() {
        lastVisitedIndex = 0
        path = emptyList()
        hadAvailableCells = true

        x = vx
        y = vy
    }

    fun calcReachedSet(gridHandler: GridHandler) {
        reachedSet = if (attachedRopes.isEmpty())
            hashSetOf()
//                gridHandler.getGrid().getAll()
        else {
            attachedRopes.map { it.setReachedSet(gridHandler) }
                .reduce { acc, set -> acc.intersect(set) }
        }
    }

    fun setBoundary(gridHandler: GridHandler) {
        val grid = gridHandler.getGrid()
        bounds =
            gridHandler.getBoundaryCells(reachedSet)
    }

}