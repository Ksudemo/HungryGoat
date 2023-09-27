package com.example.hungrygoat.gameLogic.services

import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject

class PhysicService {
    fun canTied(objectA: GameObject, objectB: GameObject): Boolean {
        val isAaGoat = objectA.gameObjectTag == GameObjectTags.GOAT
        val isBaWolf = objectB.gameObjectTag == GameObjectTags.WOLF

        val isAaWolf = objectA.gameObjectTag == GameObjectTags.WOLF
        val isBaGoat = objectB.gameObjectTag == GameObjectTags.GOAT

        return !((isAaGoat && isBaWolf) || (isAaWolf && isBaGoat))
    }
}