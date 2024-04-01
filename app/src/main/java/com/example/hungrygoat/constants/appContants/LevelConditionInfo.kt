package com.example.hungrygoat.constants.appContants

import com.example.hungrygoat.constants.enums.LevelConditions

data class LevelConditionInfo(
    val levelCondition: LevelConditions,
    val levelConditionTranslated: String,
    var rating: Int
)
