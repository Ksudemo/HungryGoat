package com.ksudemo.hungrygoat.gameLogic.gameObjects.abstractObjects

import com.ksudemo.hungrygoat.constants.enums.GameObjectTags

abstract class CellGameObject(vx: Float, vy: Float, tg: GameObjectTags, radius: Float) :
    GameObject(vx, vy, tg, radius) {
}