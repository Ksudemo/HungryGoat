package com.example.hungrygoat.gameLogic.gameObjects

import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.constants.PickedOptions
import com.example.hungrygoat.gameLogic.game.GameEngine
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.EmptyObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Peg
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Rope
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.RopeNode
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Wolf
import com.example.hungrygoat.gameLogic.services.GridHandler
import com.example.hungrygoat.gameLogic.services.InputHandler
import com.example.hungrygoat.gameLogic.services.PhysicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

                    if (tempObj == null) {
                        clickedGameObject?.apply {
                            isSelected = true
                        }
                    } else
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
                        CoroutineScope(Dispatchers.Main).launch {
                            calcReachedSet(gridHandler)
                            setBoundary(gridHandler)
                        }
                    }
                    invokeAction()
                }

                PickedOptions.WOLF -> Wolf(
                    centerX,
                    centerY,
                    GameObjectTags.WOLF
                ).apply {
                    movableAction {
                        CoroutineScope(Dispatchers.Main).launch {
                            calcReachedSet(gridHandler)
                            setBoundary(gridHandler)
                        }
                    }
                    invokeAction()
                }

                PickedOptions.CLEAR -> null
            }

        } catch (e: Exception) {
            return null
        }
    }


    private fun setRope(
        gridHandler: GridHandler,
        tempObj: GameObject?,
        clickedObj: GameObject?,
        nodes: List<RopeNode>,
    ): GameObject? {
        return if (tempObj == null || clickedObj == null || !physicService.canTied(
                tempObj,
                clickedObj
            )
        )
            null
        else {
            tempObj.isSelected = false
            clickedObj.isSelected = false

            val length = gridHandler.distBetween(tempObj, clickedObj)
            val isTiedToRope = nodes.isNotEmpty()

            val rope = Rope(tempObj, clickedObj, isTiedToRope, length, GameObjectTags.ROPE).apply {
                setRopeNodes()
                setReachedSet(gridHandler)
            }

            rope
        }
    }
}