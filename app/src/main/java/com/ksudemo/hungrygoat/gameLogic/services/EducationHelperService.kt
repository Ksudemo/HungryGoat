package com.ksudemo.hungrygoat.gameLogic.services

import android.util.Log
import com.ksudemo.hungrygoat.constants.enums.EducationStepTags
import com.ksudemo.hungrygoat.constants.enums.GameObjectTags
import com.ksudemo.hungrygoat.constants.enums.PickedOptions
import com.ksudemo.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.rope.Rope

class EducationHelperService {
    private fun checkRopeType(
        curEduStepTag: EducationStepTags,
        gameObject: GameObject?,
        targetTag: GameObjectTags?
    ): Boolean {
        Log.d(
            "edu stuff",
            "checkTempRopeType - curEduStepTag - $curEduStepTag \n targetTag - $targetTag\n temp obj tag - ${gameObject?.gameObjectTag}"
        )
        return gameObject?.gameObjectTag == targetTag
    }

    private fun checkPlaceType(
        curEduStepTag: EducationStepTags,
        pickedOption: PickedOptions,
        targetOption: PickedOptions
    ): Boolean {
        return curEduStepTag == EducationStepTags.PLACE_GOAT && pickedOption == targetOption
                || curEduStepTag == EducationStepTags.PLACE_DOG && pickedOption == targetOption
                || curEduStepTag == EducationStepTags.PLACE_PEG && pickedOption == targetOption
    }

    private fun checkEducationStep(
        curEduStepTag: EducationStepTags,
        pickedOption: PickedOptions,
        createdObject: GameObject?
    ): Boolean {
        Log.d("mytag", "curEduStepTag- $curEduStepTag, pickedOption - $pickedOption")
        val canPlace = when (curEduStepTag) {
            EducationStepTags.PLACE_GOAT -> checkPlaceType(
                curEduStepTag,
                pickedOption,
                PickedOptions.GOAT
            )

            EducationStepTags.PLACE_DOG -> checkPlaceType(
                curEduStepTag,
                pickedOption,
                PickedOptions.DOG
            )

            EducationStepTags.PLACE_PEG -> checkPlaceType(
                curEduStepTag,
                pickedOption,
                PickedOptions.PEG
            )

            else -> null
        }

        val targetTag = when (curEduStepTag) {
            EducationStepTags.TIE_ROPE -> GameObjectTags.RopeSegment
            EducationStepTags.TIE_GOAT -> GameObjectTags.GOAT
            EducationStepTags.TIE_DOG -> GameObjectTags.DOG
            EducationStepTags.TIE_PEG -> GameObjectTags.PEG
            else -> null
        }

        val ropeCheck = if (createdObject?.isTempOnRopeSet == true)
            checkRopeType(curEduStepTag, createdObject, targetTag)
        else
            if (createdObject?.gameObjectTag == GameObjectTags.ROPE)
                checkRopeType(curEduStepTag, (createdObject as Rope).objectTo, targetTag)
            else false

        return (canPlace == true) || ropeCheck
    }

    fun canCreate(
        curEduStepTag: EducationStepTags?,
        pickedOption: PickedOptions,
        createdObject: GameObject?
    ) = curEduStepTag == null || checkEducationStep(curEduStepTag, pickedOption, createdObject)
}