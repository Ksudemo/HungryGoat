package com.example.hungrygoat.gameLogic.game

import android.graphics.Canvas
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.constants.GameStates
import com.example.hungrygoat.constants.LevelConditions
import com.example.hungrygoat.constants.PickedOptions
import com.example.hungrygoat.constants.SingletonAppConstantsInfo
import com.example.hungrygoat.gameLogic.gameObjects.GameObjectFactory
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.EmptyObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Rope
import com.example.hungrygoat.gameLogic.services.GridHandler
import com.example.hungrygoat.gameLogic.services.PhysicService
import com.example.hungrygoat.gameLogic.services.RenderService
import com.example.hungrygoat.gameLogic.services.SolutionService

class GameEngine {

    companion object {

        private val solutionService = SolutionService()
        private val renderService = RenderService()

        private val gridHandler = GridHandler()
        private val physicService = PhysicService()
        private val gameObjectFactory = GameObjectFactory()

        private var goat: Goat? = null
        private var dog: Dog? = null

        private val objects = mutableListOf<GameObject>()
        private val ropes = mutableListOf<Rope>()

        var ruler = mutableListOf<List<Any>>()

        private var tempWhileMove: GameObject? = null

        fun getObjects() = objects
        fun getRopes() = ropes

    }

    fun setGrid(w: Int, h: Int, cellSize: Float) =
        gridHandler.setGrid(w, h, cellSize)

    fun clearObjects() {
        objects.clear()
        ropes.clear()
        ruler.clear()

        goat = null
        dog = null

        resetTempObj()
    }

    fun resetTempObj() {
        objects.find { it.isTempOnRopeSet }?.isTempOnRopeSet = false
    }

    fun checkSolution(levelCondition: LevelConditions) =
        if (goat == null || goat?.bounds?.isEmpty() == true)
            false
        else
            solutionService.checkSolution(
                goat!!, dog,
                gridHandler.cellSize,
                levelCondition
            )

    fun update() {
        if (goat != null) {
            val updateSuccess = goat?.update(gridHandler, dog)

            val appC = SingletonAppConstantsInfo.getAppConst()
            if (updateSuccess == false && appC.getState() != GameStates.STATE_CHECK_SOLUTION)
                appC.changeState(GameStates.STATE_CHECK_SOLUTION)
        }
        dog?.update(gridHandler, goat?.hadAvailableCells ?: false)
    }

    fun draw(canvas: Canvas) =
        renderService.render(
            canvas,
            gridHandler.getGrid(), objects, tempWhileMove,
            gridHandler.cellSize, gridHandler.numRows, gridHandler.numColumns,
            ropes,
            ruler,
            goat?.path,
            SingletonAppConstantsInfo.appConstants.getSetttings()
        )

    fun createNewObject(x: Float, y: Float, pickedOption: PickedOptions) {
        val currentState = SingletonAppConstantsInfo.getAppConst().getState()

        if (currentState == GameStates.STATE_PLAYER_PLACE_OBJECTS) {

            if (tempWhileMove != null) {
                handleCreatedObject(tempWhileMove!!)
                tempWhileMove = null
                ruler.clear()
                return
            }

            val createdObject =
                gameObjectFactory.createNewObject(
                    x, y, objects.find { it.isTempOnRopeSet }, pickedOption, gridHandler
                )

            handleCreatedObject(createdObject ?: return)
        }
    }

    fun tempCreateNewObjectOnMove(x: Float, y: Float, pickedOption: PickedOptions) {
        fun isInRange(angle: Double, targetAngle: Double, eps: Double): Boolean {
            val remainder = (angle - targetAngle) % 360.0
            return remainder <= eps || (360.0 - remainder) <= eps
        }

        fun isValidAngle(angle: Double, eps: Double) =
            isInRange(angle, 30.0, eps) || isInRange(angle, 45.0, eps)

        if (SingletonAppConstantsInfo.getAppConst().getState()
            != GameStates.STATE_PLAYER_PLACE_OBJECTS
        ) return
        ruler.clear()

        if (tempWhileMove != null && tempWhileMove?.gameObjectTag.toString() == pickedOption.toString()) {
            val closestToTemp = gridHandler.getClosestObject(tempWhileMove!!, objects)

            val testPoint = EmptyObject(x, y, GameObjectTags.EMPTY)

//            var angle = physicService.calcAngleBetweenInDeg(
//                testPoint, closestToTemp
//            )

//            if (isValidAngle(angle, eps = 0.0) || closestToTemp == null) {
            tempWhileMove?.x = x
            tempWhileMove?.y = y
//            }

            objects.forEach {
                val angle = physicService.calcAngleBetweenInDeg(
                    it, tempWhileMove!!
                )
//                val isValid = isValidAngle(angle, eps = 5.0)
//                if (isValid)
                ruler.add(listOf(tempWhileMove!!, it, angle))
            }

        } else
            tempWhileMove =
                gameObjectFactory.createNewObject(
                    x, y, objects.find { it.isTempOnRopeSet }, pickedOption, gridHandler
                ) ?: return
    }

    private fun handleCreatedObject(createdObject: GameObject) =
        if (createdObject.gameObjectTag == GameObjectTags.ROPE)
            addToRopes(createdObject as Rope)
        else
            checkAndSetMovable(createdObject)

    private fun addToRopes(obj: GameObject) {
        val rope = obj as Rope

        val isDog = isObjADog(rope.objectTo, rope.objectFrom)
        val isGoat = isObjAGoat(rope.objectTo, rope.objectFrom)

        if (isDog)
            dog?.attachRope(rope)
        else if (isGoat)
            goat?.attachRope(rope)

        ropes.add(rope)
    }

    private fun checkAndSetMovable(obj: GameObject) {
        val isDog = isObjADog(obj)
        val isGoat = isObjAGoat(obj)

        if ((isDog || isGoat) && !obj.isTempOnRopeSet) {
            removeFromObjects(obj)

            if (isGoat)
                goat = obj as Goat
            else
                dog = obj as Dog
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

    private fun isObjADog(vararg objs: GameObject): Boolean =
        objs.find { it.gameObjectTag == GameObjectTags.DOG } != null

    private fun isObjAGoat(vararg objs: GameObject): Boolean =
        objs.find { it.gameObjectTag == GameObjectTags.GOAT } != null


    fun restoreInitialState() {
        goat?.moveToStart()
        dog?.moveToStart()
    }


    fun killEngine() {
//        clearObjects()
//        gridHandler.freeGrid()
    }

}