package com.example.hungrygoat.gameLogic.gameObjects.abstractObjects

import android.util.Log
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Rope
import com.example.hungrygoat.gameLogic.services.GridHandler
import kotlin.math.atan2

@Suppress("ConvertArgumentToSet")
abstract class MovableGameObject(
    private val vx: Float,
    private val vy: Float,
    tg: GameObjectTags,
) :
    GameObject(vx, vy, tg) {

    private val attachedRopeActions = mutableListOf<() -> Unit>()
    private val attachedRopes = mutableListOf<Rope>()

    var path = setOf<Cell>()
    val visited = mutableListOf<Cell>()
    var reachedSet = setOf<Cell>()
    var bounds = listOf<Cell>()

    var hadAvailableCells = true

    fun invokeAction() {
        attachedRopeActions.forEach { it.invoke() }
    }

    fun attachRope(rope: Rope) {
        attachedRopes.add(rope)
        invokeAction()
    }

    fun movableAction(action: () -> Unit) =
        attachedRopeActions.add(action)

    fun moveToStart() {
        path = emptySet()
        visited.clear()
        hadAvailableCells = true

        x = vx
        y = vy
    }

    fun calcReachedSet(gridHandler: GridHandler) {
        reachedSet = if (attachedRopes.isEmpty()) {
            gridHandler.getGrid().toSet()
        } else {
            var temp = attachedRopes.first().ropeReachedSet.toSet()

            attachedRopes.forEach { rope ->
                temp = temp.intersect(rope.ropeReachedSet)
            }

            temp
        }
    }

    fun setBoundary(gridHandler: GridHandler) {
        fun angleBetween(center: Pair<Float, Float>, point: Cell): Float {
            val dx = point.x - center.first
            val dy = point.y - center.second
            return atan2(dy, dx)
        }

        Log.v("MyTag", "Start setting cur movable boundarys")
        val temp = gridHandler.getBoundaryCells(reachedSet)

        val centerX = temp.map { it.x }.average().toFloat()
        val centerY = temp.map { it.y }.average().toFloat()
        val center = Pair(centerX, centerY)

        bounds = temp.sortedBy { angleBetween(center, it) }
        Log.v("MyTag", "Done setting cur movable boundarys")
    }

}