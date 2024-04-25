package com.ksudemo.hungrygoat.gameLogic.gameObjects

import android.content.res.Resources
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import com.ksudemo.hungrygoat.R
import com.ksudemo.hungrygoat.constants.appContants.SingletonAppConstantsInfo
import com.ksudemo.hungrygoat.constants.enums.GameObjectTags
import com.ksudemo.hungrygoat.constants.enums.PickedOptions
import com.ksudemo.hungrygoat.gameLogic.game.GameEngine
import com.ksudemo.hungrygoat.gameLogic.game.grid.GridHandler
import com.ksudemo.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.Peg
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.rope.Rope
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.rope.RopeSegment
import com.ksudemo.hungrygoat.gameLogic.services.InputHandler
import kotlin.time.measureTime

class GameObjectFactory {
    private val inputHandler = InputHandler()

    fun createNewObject(
        clickedX: Float,
        clickedY: Float,
        tempObj: GameObject?,
        option: PickedOptions,
        gridHandler: GridHandler,
        r: Float
    ): GameObject? {
        try {
            val appC = SingletonAppConstantsInfo.getAppConst()
            val res = appC.resources
            return when (option) {
                PickedOptions.ROPE -> createRope(gridHandler, clickedX, clickedY, tempObj, r)

                PickedOptions.PEG -> createPeg(res, clickedX, clickedY, r)

                PickedOptions.GOAT -> createGoat(res, clickedX, clickedY, r, gridHandler)

                PickedOptions.DOG -> createDog(res, clickedX, clickedY, r, gridHandler)

                else -> null
            }

        } catch (e: Exception) {
            Log.e(
                "mytag",
                "Exception in GameObjectFactory.createNewObject() ${e.printStackTrace()}"
            )
            return null
        }
    }

    private fun createDog(
        res: Resources,
        clickedX: Float,
        clickedY: Float,
        r: Float,
        gridHandler: GridHandler
    ): Dog {
        val dr = ResourcesCompat.getDrawable(res, R.mipmap.dog, null)
        val dog = Dog(clickedX, clickedY, dr, r)
        return dog.apply {
            movableAction(
                {
                    calcReachedSet(gridHandler)
                    bounds = getBoundary(gridHandler)
                }
            )
            invokeAction()
        }
    }

    private fun createGoat(
        res: Resources,
        clickedX: Float,
        clickedY: Float,
        r: Float,
        gridHandler: GridHandler
    ): Goat {
        val dr = ResourcesCompat.getDrawable(res, R.mipmap.goat, null)

        val goat = Goat(clickedX, clickedY, dr, r)
        return goat.apply {
            movableAction({
                val time1 = measureTime {
                    calcReachedSet(gridHandler)
                }
                val time2 = measureTime {
                    bounds = getBoundary(gridHandler)
                }
                Log.d("mytag", "movable.calcReachedSet time - $time1")
                Log.d("mytag", "movable.setBoundary time - $time2")
            })
            invokeAction()
        }
    }

    private fun createPeg(
        res: Resources,
        clickedX: Float,
        clickedY: Float,
        r: Float
    ): Peg {
        val dr = ResourcesCompat.getDrawable(res, R.mipmap.peg_object, null)
        return Peg(clickedX, clickedY, dr, r)
    }

    private fun createRope(
        gridHandler: GridHandler,
        clickedX: Float,
        clickedY: Float,
        tempObj: GameObject?,
        r: Float
    ): GameObject? {
        val clickedObject = inputHandler.getClickedObject(
            gridHandler, GameEngine.getObjects(), clickedX, clickedY
        )

        val clickedSegment = inputHandler.getClickedRopeSegment(
            gridHandler, GameEngine.getRopes(), clickedX, clickedY
        )

        val curClicked = clickedObject ?: clickedSegment
        // if click on the same object twice or missclick on empty spot
        if (curClicked == null || curClicked == tempObj)
            return null

        /*
                    if tempObj is null (click on the first object then rope set) => mark clicked as isTempOnRopeSet,
                    else set rope from tempObj to curClicked
                     */
        return when (tempObj) {
            null -> curClicked.apply { isTempOnRopeSet = true }
            else -> setRope(gridHandler, tempObj, curClicked, r)
        }
    }


    private fun setRope(
        gridHandler: GridHandler,
        tempObj: GameObject,
        clickedObj: GameObject,
        r: Float
    ): Rope {
        val len = gridHandler.distBetween(tempObj, clickedObj)

        val segments =
            listOf(tempObj, clickedObj).filter { it.gameObjectTag == GameObjectTags.RopeSegment }
        val isTiedToRope = segments.isNotEmpty()
        val rope = Rope(tempObj, clickedObj, isTiedToRope, len, GameObjectTags.ROPE, r)
        rope.setRopeSegments()

        segments.forEach { seg ->
            (seg as RopeSegment).baseRope.attachedRopesHashSet.add(rope)
        }

        return rope
    }
}