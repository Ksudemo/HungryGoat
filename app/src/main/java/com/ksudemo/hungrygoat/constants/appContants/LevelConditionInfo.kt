package com.ksudemo.hungrygoat.constants.appContants

import com.ksudemo.hungrygoat.constants.enums.LevelConditions

data class LevelConditionInfo(
    val levelCondition: LevelConditions,
    val levelConditionTranslated: String,
    var rating: Int,
    var time: Long = -1L
)
