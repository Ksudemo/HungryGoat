package com.ksudemo.hungrygoat.gameLogic.interfaces.dogListeners

import com.ksudemo.hungrygoat.gameLogic.game.grid.GridHandler

fun interface DogUpdateListener {
    fun update(gridHandler: GridHandler)
}
