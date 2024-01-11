package com.example.hungrygoat.gameLogic.interfaces

fun interface LevelCompleteListener {
    fun onLevelComplete()
}

fun interface LevelFailedListener {
    fun onLevelFailed()
}