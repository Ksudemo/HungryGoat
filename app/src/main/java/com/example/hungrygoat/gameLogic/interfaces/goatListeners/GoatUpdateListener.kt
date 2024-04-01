package com.example.hungrygoat.gameLogic.interfaces.goatListeners

import com.example.hungrygoat.gameLogic.game.grid.GridHandler
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog

fun interface GoatUpdateListener {
    fun update(
        gridHandler: GridHandler, dogs: List<Dog>,
    ): Boolean
}
