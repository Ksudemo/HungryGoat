package com.example.hungrygoat.gameLogic.gameObjects

import android.util.Log
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.constants.PickedOptions
import com.example.hungrygoat.gameLogic.game.GameEngine
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.EmptyObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Peg
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Rope
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.RopeNode
import com.example.hungrygoat.gameLogic.services.GridHandler
import com.example.hungrygoat.gameLogic.services.InputHandler
import com.example.hungrygoat.gameLogic.services.PhysicService

class GameObjectFactory {
    private val physicService = PhysicService()
    private val inputHandler = InputHandler()

    fun createNewObject(
        clickedX: Float,
        clickedY: Float,
        tempObj: GameObject?,
        option: PickedOptions,
        gridHandler: GridHandler,
    ): GameObject? {
        try {
            val clickedCell =
                gridHandler.getObjectCell(EmptyObject(clickedX, clickedY, GameObjectTags.EMPTY))
                    ?: return null

            val centerX = clickedCell.x
            val centerY = clickedCell.y

            return when (option) {
                PickedOptions.ROPE -> {

                    val nodes =
                        inputHandler.getClickedRopeNodeObject(
                            GameEngine.getRopes(), clickedX, clickedY
                        )

                    if (tempObj?.gameObjectTag == GameObjectTags.RopeNode)
                        nodes.add(tempObj as RopeNode)

                    val clickedGameObject =
                        inputHandler.getClickedObject(GameEngine.getObjects(), clickedX, clickedY)
                            ?: nodes.firstOrNull()

                    if (tempObj == null)
                        clickedGameObject?.apply {
                            isTempOnRopeSet = true
                        }
                    else
                        setRope(gridHandler, tempObj, clickedGameObject, nodes)
                }

                PickedOptions.PEG -> Peg(
                    centerX,
                    centerY,
                    GameObjectTags.PEG
                )

                PickedOptions.GOAT -> Goat(
                    centerX,
                    centerY,
                    GameObjectTags.GOAT,
                ).apply {
                    movableAction {
                        calcReachedSet(gridHandler)
                        setBoundary(gridHandler)
                    }
                    invokeAction()
                }

                PickedOptions.DOG -> Dog(
                    centerX,
                    centerY,
                    GameObjectTags.DOG
                ).apply {
                    movableAction {
                        calcReachedSet(gridHandler)
                        setBoundary(gridHandler)
                    }
                    invokeAction()
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
        tempObj: GameObject?,
        clickedObj: GameObject?,
        nodes: List<RopeNode>,
    ): GameObject? {

        tempObj?.isTempOnRopeSet = false
        clickedObj?.isTempOnRopeSet = false

        return if (tempObj == null || clickedObj == null || !physicService.canTied(
                tempObj,
                clickedObj
            ) || tempObj == clickedObj
        )
            null
        else {
            val length = gridHandler.distBetween(
                tempObj, clickedObj,
                "GameObjectFactory"
            )
            val isTiedToRope = nodes.isNotEmpty()
            val rope = Rope(tempObj, clickedObj, isTiedToRope, length, GameObjectTags.ROPE).apply {
                setRopeNodes()
                setReachedSet(gridHandler)
            }
            if (isTiedToRope)
                nodes.first().baseRope.attachedRopes.add(rope)

            rope
        }
    }
}