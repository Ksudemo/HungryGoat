package com.ksudemo.hungrygoat.gameLogic.interfaces.enigneListeners

import com.ksudemo.hungrygoat.constants.enums.LevelConditions

fun interface CheckSolutionListener {
    fun checkSolution(result: List<LevelConditions>, dogsSize: Int)
}