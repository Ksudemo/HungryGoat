package com.example.hungrygoat.gameLogic.gameObjects.abstractObjects

import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Rope
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Wolf
import com.example.hungrygoat.gameLogic.services.GridHandler

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
        Log.v("MyTag", "Start setting cur movable reached set")

        reachedSet = if (attachedRopes.isEmpty()) {
            gridHandler.getGrid().toSet()
        } else {
            var temp = attachedRopes.first().ropeReachedSet.toSet()

            attachedRopes.forEach { rope ->
                temp = temp.intersect(rope.ropeReachedSet)
            }

            temp
        }
        Log.v("MyTag", "Done setting cur movable reached set")
    }


    fun setBoundary(gridHandler: GridHandler) {
        Log.v("MyTag", "Start setting cur movable boundarys")
        bounds = gridHandler.getBoundaryCells(reachedSet)
        Log.v("MyTag", "Done setting cur movable boundarys")
    }

    override fun draw(canvas: Canvas, paint: Paint) {}
    open fun update(gridHandler: GridHandler, goatHaveAvailableCells: Boolean) {}
    open fun update(
        gridHandler: GridHandler, wolfObj: Wolf?,
    ) {
    }
}