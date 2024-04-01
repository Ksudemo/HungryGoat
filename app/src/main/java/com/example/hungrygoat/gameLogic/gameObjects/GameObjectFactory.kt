package com.example.hungrygoat.gameLogic.gameObjects

import android.util.Log
import androidx.core.content.res.ResourcesCompat
import com.example.hungrygoat.R
import com.example.hungrygoat.constants.appContants.SingletonAppConstantsInfo
import com.example.hungrygoat.constants.enums.GameObjectTags
import com.example.hungrygoat.constants.enums.PickedOptions
import com.example.hungrygoat.gameLogic.game.GameEngine
import com.example.hungrygoat.gameLogic.game.grid.GridHandler
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Peg
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.rope.Rope
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.rope.RopeSegment
import com.example.hungrygoat.gameLogic.services.InputHandler
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
                PickedOptions.ROPE -> {
                    val clickedObject = inputHandler.getClickedObject(
                        gridHandler,
                        GameEngine.getObjects(),
                        clickedX,
                        clickedY
                    )

                    val clickedSegment = inputHandler.getClickedRopeSegment(
                        gridHandler,
                        GameEngine.getRopes(),
                        clickedX,
                        clickedY
                    )

                    val curClicked = clickedObject ?: clickedSegment
                    // if click on the same object twice or missclick on empty spot
                    if (curClicked == null || curClicked == tempObj)
                        return null

                    /*
                    if tempObj is null (click on the first object then rope set) => mark clicked as isTempOnRopeSet,
                    else set rope from tempObj to curClicked
                     */
                    when (tempObj) {
                        null -> curClicked.apply { isTempOnRopeSet = true }
                        else -> setRope(gridHandler, tempObj, curClicked, r)
                    }
                }

                PickedOptions.PEG -> {
                    val dr = ResourcesCompat.getDrawable(res, R.mipmap.peg_object, null)
                    Peg(
                        clickedX,
                        clickedY,
                        dr,
                        r
                    )
                }

                PickedOptions.GOAT -> {
                    val dr = ResourcesCompat.getDrawable(res, R.mipmap.goat, null)
                    Goat(
                        clickedX,
                        clickedY,
                        dr, r
                    ).apply {
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

                PickedOptions.DOG -> {
                    val dr = ResourcesCompat.getDrawable(res, R.mipmap.dog, null)
                    Dog(
                        clickedX,
                        clickedY,
                        dr,
                        r
                    ).apply {
                        movableAction(
                            {
                                calcReachedSet(gridHandler)
                                bounds = getBoundary(gridHandler)
                            }
                        )
                        invokeAction()
                    }
                }

                PickedOptions.ERASER -> null
                PickedOptions.NULL -> null
            }

        } catch (e: Exception) {
            Log.e(
                "mytag",
                "Exception in GameObjectFactory.createNewObject() ${e.printStackTrace()}"
            )
            return null
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