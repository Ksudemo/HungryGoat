package com.example.hungrygoat.gameLogic.gameObjects.abstractObjects

import com.example.hungrygoat.constants.enums.GameObjectTags

abstract class CellGameObject(vx: Float, vy: Float, tg: GameObjectTags, radius: Float) :
    GameObject(vx, vy, tg, radius) {
}