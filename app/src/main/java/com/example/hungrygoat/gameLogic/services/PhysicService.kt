package com.example.hungrygoat.gameLogic.services

import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.EmptyObject
import kotlin.math.acos
import kotlin.math.sqrt

class PhysicService {
    fun canTied(objectA: GameObject, objectB: GameObject): Boolean {
        val isAaGoat = objectA.gameObjectTag == GameObjectTags.GOAT
        val isBaDog = objectB.gameObjectTag == GameObjectTags.DOG

        val isAaDog = objectA.gameObjectTag == GameObjectTags.DOG
        val isBaGoat = objectB.gameObjectTag == GameObjectTags.GOAT

        return !((isAaGoat && isBaDog) || (isAaDog && isBaGoat))
    }

    fun calcAngleBetweenInDeg(a: GameObject?, b: GameObject?): Double {
        if (a == null || b == null) return -1.0
        val c = EmptyObject(b.x + 1, b.y, GameObjectTags.EMPTY)
        return calcAngleBetweenInDeg(a, b, c)
    }

    fun calcAngleBetweenInDeg(a: GameObject?, b: GameObject?, c: GameObject?): Double {
        if (a == null || b == null || c == null)
            return -1.0

        val x1 = a.x - b.x
        val x2 = c.x - b.x
        val y1 = a.y - b.y
        val y2 = c.y - b.y

        val d1 = sqrt(x1 * x1 + y1 * y1)
        val d2 = sqrt(x2 * x2 + y2 * y2)

        val fraction = (x1 * x2 + y1 * y2) / (d1 * d2).toDouble()

        return Math.toDegrees(acos(fraction))
    }

    fun distBetween(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

}