package com.ksudemo.hungrygoat.constants.appContants

data class GameSettings(
    var drawGoatBounds: Boolean = true,
    var drawDogBounds: Boolean = true,
    var drawGrahamScanLines: Boolean = false,
    var objectsSize: Float = 40f
)