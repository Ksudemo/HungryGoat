package com.ksudemo.hungrygoat.gameLogic.interfaces.goatListeners

import com.ksudemo.hungrygoat.gameLogic.game.grid.GridHandler
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog

fun interface GoatUpdateListener {
    fun update(
        gridHandler: GridHandler, dogs: List<Dog>,
    ): Boolean
}
