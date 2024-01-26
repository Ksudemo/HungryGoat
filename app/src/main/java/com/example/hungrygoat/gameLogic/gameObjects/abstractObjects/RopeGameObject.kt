package com.example.hungrygoat.gameLogic.gameObjects.abstractObjects

import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Rope
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.RopeNode

abstract class RopeGameObject(
    objectFrom: GameObject,
    tg: GameObjectTags,
) : GameObject(objectFrom.x, objectFrom.y, tg) {

    var ropeReachedSet: Set<Cell> = mutableSetOf()
    val ropeNodes: HashSet<RopeNode> = hashSetOf()
    val attachedRopes: HashSet<Rope> = hashSetOf()
    fun remove() {
        ropeNodes.clear()
//        ropeReachedSet.clear()
    }
}