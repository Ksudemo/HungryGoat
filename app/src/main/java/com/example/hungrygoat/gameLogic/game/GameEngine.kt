package com.example.hungrygoat.gameLogic.game

import android.graphics.Canvas
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.constants.GameSettings
import com.example.hungrygoat.constants.GameStates
import com.example.hungrygoat.constants.PickedOptions
import com.example.hungrygoat.constants.SingletonAppConstantsInfo
import com.example.hungrygoat.gameLogic.gameObjects.GameObjectFactory
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Rope
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Wolf
import com.example.hungrygoat.gameLogic.services.GridHandler
import com.example.hungrygoat.gameLogic.services.RenderService

class GameEngine {

    companion object {
        private val renderService = RenderService()

        private val gridHandler = GridHandler()

        private val gameObjectFactory = GameObjectFactory()

        private var goat: Goat? = null
        private var wolf: Wolf? = null

        private val objects = mutableListOf<GameObject>()
        private val ropes = mutableListOf<Rope>()

        fun getObjects() = objects
        fun getRopes() = ropes

    }

    fun setGrid(w: Int, h: Int, cellSize: Float) =
        gridHandler.setGrid(w, h, cellSize)


    fun clearObjects() {
        objects.clear()
        ropes.clear()

        goat = null
        wolf = null

        resetTempObj()
    }

    fun resetTempObj() {
        objects.find { it.isSelected }?.isSelected = false
    }

    fun update() {
        goat?.update(gridHandler, wolf)
        wolf?.update(gridHandler, goat?.hadAvailableCells ?: false)
    }

    fun draw(canvas: Canvas, setttings: GameSettings) =
        renderService.render(
            canvas,
            gridHandler.getGrid(),
            objects,
            ropes,
            goat?.visited,
            setttings
        )

    fun createNewObject(x: Float, y: Float, pickedOption: PickedOptions) {
        val currentState = SingletonAppConstantsInfo.getAppConst().getState()

        if (currentState == GameStates.STATE_PAUSED) {
            val createdObject =
                gameObjectFactory.createNewObject(
                    x, y, objects.find { it.isSelected }, pickedOption, gridHandler
                )

            handleCreatedObject(createdObject ?: return)
        }
    }

    private fun handleCreatedObject(createdObject: GameObject) {
        if (createdObject.gameObjectTag == GameObjectTags.ROPE) {
            addToRopes(createdObject as Rope)
        } else {
            checkAndSetMovable(createdObject)
        }
    }

    private fun addToRopes(obj: GameObject) {
        val rope = obj as Rope

        val isWolf = isObjAWolf(rope.objectTo, rope.objectFrom)
        val isGoat = isObjAGoat(rope.objectTo, rope.objectFrom)

        if (isWolf)
            wolf?.attachRope(rope)
        else if (isGoat)
            goat?.attachRope(rope)

        ropes.add(rope)
    }


    private fun checkAndSetMovable(obj: GameObject) {
        val isWolf = isObjAWolf(obj)
        val isGoat = isObjAGoat(obj)

        if ((isWolf || isGoat) && !obj.isSelected) {
            removeFromObjects(obj)

            if (isGoat)
                goat = obj as Goat
            else
                wolf = obj as Wolf
        }

        objects.add(obj)
    }

    private fun removeFromObjects(obj: GameObject?) {
        val curObjTag = obj?.gameObjectTag

        objects.removeIf { it.gameObjectTag == curObjTag }

        val ropeToRemove =
            ropes.find { it.objectTo.gameObjectTag == curObjTag || it.objectFrom.gameObjectTag == curObjTag }

        ropes.remove(ropeToRemove)
    }

    private fun isObjAWolf(vararg objs: GameObject): Boolean =
        objs.find { it.gameObjectTag == GameObjectTags.WOLF } != null

    private fun isObjAGoat(vararg objs: GameObject): Boolean =
        objs.find { it.gameObjectTag == GameObjectTags.GOAT } != null


    fun restoreInitialState() {
        goat?.moveToStart()
        wolf?.moveToStart()
    }


    fun killEngine() {
        clearObjects()
        gridHandler.freeGrid()
    }

}