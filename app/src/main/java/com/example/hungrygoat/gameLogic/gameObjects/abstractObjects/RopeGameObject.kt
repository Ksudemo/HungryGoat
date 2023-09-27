package com.example.hungrygoat.gameLogic.gameObjects.abstractObjects

import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.RopeNode

abstract class RopeGameObject(
    objectFrom: GameObject,
    tg: GameObjectTags,
) :
    GameObject(objectFrom.x, objectFrom.y, tg) {
    lateinit var ropeReachedSet: List<Cell>
    val ropePath = mutableListOf<RopeNode>()
}