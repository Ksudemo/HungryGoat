package com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Paint
import com.ksudemo.hungrygoat.constants.enums.GameObjectTags
import com.ksudemo.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject

class EmptyObject(
    x: Float,
    y: Float,
    radius: Float,
    tag: GameObjectTags = GameObjectTags.EMPTY,
) :
    GameObject(x, y, tag, radius) {
    override fun draw(canvas: Canvas, paint: Paint) = Unit
}