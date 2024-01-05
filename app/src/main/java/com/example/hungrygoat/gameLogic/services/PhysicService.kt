package com.example.hungrygoat.gameLogic.services

import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.EmptyObject
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

class PhysicService {
    fun canTied(objectA: GameObject, objectB: GameObject): Boolean {
        val isAaGoat = objectA.gameObjectTag == GameObjectTags.GOAT
        val isBaDog = objectB.gameObjectTag == GameObjectTags.DOG

        val isAaDog = objectA.gameObjectTag == GameObjectTags.DOG
        val isBaGoat = objectB.gameObjectTag == GameObjectTags.GOAT

        return !((isAaGoat && isBaDog) || (isAaDog && isBaGoat))
    }

    fun calcAngleBetweenInDeg(a: GameObject, b: GameObject?): Double {
        if (b == null)
            return -1.0

        val c = EmptyObject(a.x + 1, a.y, GameObjectTags.EMPTY)
        val dotProduct =
            (b.x - a.x) * (c.x - a.x) + (b.y - a.y) * (c.y - a.y)
        val magnitudeA =
            sqrt((b.x - a.x).pow(2) + (b.y - a.y).pow(2))
        val magnitudeC =
            sqrt((c.x - a.x).pow(2) + (c.y - a.y).pow(2))
        return Math.toDegrees(acos(dotProduct / (magnitudeA * magnitudeC)).toDouble())
    }
}