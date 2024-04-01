package com.example.hungrygoat.constants.appContants

import com.example.hungrygoat.constants.enums.GameStates
import com.example.hungrygoat.constants.enums.PickedOptions

object SingletonAppConstantsInfo {
    lateinit var appConstants: AppConstants

    fun getAppConst(): AppConstants {
        if (!SingletonAppConstantsInfo::appConstants.isInitialized) {
            appConstants = AppConstants()
            appConstants.changeOption(PickedOptions.NULL)
            appConstants.changeState(GameStates.STATE_PLAYER_PLACE_OBJECTS)
        }

        return appConstants
    }
}