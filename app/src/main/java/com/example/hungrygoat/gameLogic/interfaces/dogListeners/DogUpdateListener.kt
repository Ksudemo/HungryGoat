package com.example.hungrygoat.gameLogic.interfaces.dogListeners

import com.example.hungrygoat.gameLogic.game.grid.GridHandler

fun interface DogUpdateListener {
    fun update(gridHandler: GridHandler)
}
