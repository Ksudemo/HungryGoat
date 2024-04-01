package com.example.hungrygoat.gameLogic.interfaces.enigneListeners

import com.example.hungrygoat.constants.enums.LevelConditions

fun interface CheckSolutionListener {
    fun checkSolution(result: List<LevelConditions>, dogsSize: Int)
}