package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Paint
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject

class EmptyObject(x: Float, y: Float, tag: GameObjectTags = GameObjectTags.EMPTY) :
    GameObject(x, y, tag) {
    override fun draw(canvas: Canvas, paint: Paint) {}
}