package com.example.hungrygoat.gameLogic.services

import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Rope
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.RopeNode
import kotlin.math.pow
import kotlin.math.sqrt

class InputHandler {

    fun getClickedObject(objects: List<GameObject>, x: Float, y: Float) =
        objects.find { distanceBetween(it.x, it.y, x, y) <= it.circleRadius }

    fun getClickedRopeNodeObject(ropes: List<Rope>, x: Float, y: Float): MutableList<RopeNode> {
        val nodes = mutableListOf<RopeNode>()
        ropes.forEach { rope ->
            val filtered = rope.ropeNodes.filter {
                distanceBetween(it.x, it.y, x, y) <= it.circleRadius
            }
            nodes.addAll(filtered)
        }
        return nodes
    }

    private fun distanceBetween(xFrom: Float, yFrom: Float, x: Float, y: Float): Float {
        return sqrt((xFrom - x).pow(2) + (yFrom - y).pow(2))
    }
}