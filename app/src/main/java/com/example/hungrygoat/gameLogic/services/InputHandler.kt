package com.example.hungrygoat.gameLogic.services

import com.example.hungrygoat.gameLogic.game.grid.GridHandler
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.EmptyObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.rope.Rope
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.rope.RopeSegment

class InputHandler {

    fun getClickedObject(gridHandler: GridHandler, objects: List<GameObject>, x: Float, y: Float) =
        objects.find { gridHandler.distBetween(it, EmptyObject(x, y, 0f)) <= it.r }

    fun getClickedRopeSegment(
        gridHandler: GridHandler,
        ropes: List<Rope>,
        x: Float,
        y: Float
    ): RopeSegment? {
        val clicked = EmptyObject(x, y, 0f)

        return ropes.asSequence()
            .flatMap { rope -> rope.ropeSegments.asSequence() }
            .find { seg -> gridHandler.distBetween(seg, clicked) <= seg.r }
    }
}