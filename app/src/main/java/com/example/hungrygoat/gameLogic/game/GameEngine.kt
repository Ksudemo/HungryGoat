package com.example.hungrygoat.gameLogic.game

import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.constants.GameStates
import com.example.hungrygoat.constants.LevelConditions
import com.example.hungrygoat.constants.PickedOptions
import com.example.hungrygoat.constants.SingletonAppConstantsInfo
import com.example.hungrygoat.gameLogic.gameObjects.GameObjectFactory
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Rope
import com.example.hungrygoat.gameLogic.services.InputHandler
import com.example.hungrygoat.gameLogic.services.PhysicService
import com.example.hungrygoat.gameLogic.services.RenderService
import com.example.hungrygoat.gameLogic.services.grid.GridHandler
import com.example.hungrygoat.gameLogic.services.solution.SolutionService

class GameEngine {

    companion object {
        private val solutionService = SolutionService()
        private val renderService = RenderService()

        private val gridHandler = GridHandler()
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

    fun setGrid(w: Int, h: Int, cellSize: Float) {
        gridHandler.setGrid(w, h, cellSize)
//        time = measureTimeMillis {
//            createNewObject(352.5f, 1102.5f, PickedOptions.PEG)
//            createNewObject(607.5f, 947.5f, PickedOptions.GOAT)
//            createNewObject(352.5f, 1102.5f, PickedOptions.ROPE)
//            createNewObject(607.5f, 947.5f, PickedOptions.ROPE)
//        }
//        Log.d("mytag", "time to setup circle - $time")
    }

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

    fun checkSolution(levelCondition: LevelConditions): Boolean =
        when {
            goat == null || goat?.bounds?.isEmpty() == true -> false
            else ->
                solutionService.checkSolution(goat!!, dog, gridHandler, levelCondition)
        }

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
            gridHandler,
            objects,
            ropes,
            tempWhileMove,
            ruler,
            SingletonAppConstantsInfo.appConstants.getSetttings()
        )


    fun handleTouch(
        event: MotionEvent,
        pickedOption: PickedOptions,
        state: GameStates,
    ) =
        if (state == GameStates.STATE_PLAYER_PLACE_OBJECTS) {
            val x = event.x
            val y = event.y
            when (event.action) {
                MotionEvent.ACTION_UP -> createNewObject(x, y, pickedOption)
                MotionEvent.ACTION_MOVE -> tempCreateNewObjectOnMove(x, y, pickedOption)
                else -> null
            }
        } else null

    private fun createNewObject(x: Float, y: Float, pickedOption: PickedOptions) {
        when (pickedOption) {
            PickedOptions.ERASER -> eraseObject(x, y)
            else -> {
                val createdObject =
                    gameObjectFactory.createNewObject(
                        x, y, objects.find { it.isTempOnRopeSet }, pickedOption, gridHandler
                    )
                handleCreatedObject(createdObject ?: return)
            }
        }
        tempWhileMove = null
        ruler.clear()
    }

    private fun tempCreateNewObjectOnMove(x: Float, y: Float, pickedOption: PickedOptions) {
        ruler.clear()

        if (tempWhileMove != null && tempWhileMove?.gameObjectTag.toString() == pickedOption.toString()) {
            tempWhileMove?.x = x
            tempWhileMove?.y = y

            objects.forEach {
                val angle = PhysicService().calcAngleBetweenInDeg(
                    tempWhileMove!!, it
                )
                ruler.add(listOf(tempWhileMove!!, it, angle))
            }
        } else
            tempWhileMove =
                gameObjectFactory.createNewObject(
                    x, y, objects.find { it.isTempOnRopeSet }, pickedOption, gridHandler
                ) ?: return
    }

    private fun eraseObject(x: Float, y: Float) {
        val clickedObject = InputHandler().getClickedObject(objects, x, y)

        if (clickedObject != null)
            removeFromObjects(clickedObject)
        else {
            val clickedRope =
                InputHandler().getClickedRopeNodeObject(
                    ropes, x, y
                ).firstOrNull()?.baseRope
            removeFromRopes(clickedRope)
        }
    }

    private fun handleCreatedObject(createdObject: GameObject) =
        if (createdObject.gameObjectTag == GameObjectTags.ROPE)
            addToRopes(createdObject as Rope)
        else
            checkAndSetMovable(createdObject)

    private fun addToRopes(obj: GameObject) {
        val rope = obj as Rope
        if (ropes.any { it.isTiedToThisRope(rope) }) return

        val isDog = isObjADog(rope.objectTo, rope.objectFrom)
        val isGoat = isObjAGoat(rope.objectTo, rope.objectFrom)

        if (isDog)
            dog?.attachRope(rope)
        else if (isGoat)
            goat?.attachRope(rope)

        Log.d(
            "mytag",
            "isTiedToRope = ${rope.isTiedToRope}\n from = ${rope.objectFrom}\n to = ${rope.objectTo}"
        )
        ropes.add(rope)
    }

    private fun checkAndSetMovable(obj: GameObject) {
        val isDog = isObjADog(obj)
        val isGoat = isObjAGoat(obj)

        if ((isDog || isGoat) && !obj.isTempOnRopeSet) {
            removeFromObjects(obj)

            when {
                isGoat -> {
                    goat = null
                    goat = obj as Goat
                }

                else -> {
                    dog = null
                    dog = obj as Dog
                    goat?.moveToStart()
                }
            }
        }

        objects.add(obj)
    }

    private fun removeFromObjects(obj: GameObject?) {
        if (obj == null) return

        val curObjTag = obj.gameObjectTag
        val curObjAGoat = isObjAGoat(obj)
        val curObjADog = isObjADog(obj)

        if (curObjADog || curObjAGoat)
            objects.removeIf { it.gameObjectTag == curObjTag }
        else
            objects.removeIf { it.gameObjectTag == curObjTag && it.x == obj.x && it.y == obj.y }

        if (curObjAGoat)
            goat = null
        else if (curObjADog)
            dog = null

        val ropeToRemove =
            ropes.find { it.objectTo.gameObjectTag == curObjTag || it.objectFrom.gameObjectTag == curObjTag }

        removeFromRopes(ropeToRemove)
    }

    private fun removeFromRopes(rope: Rope?) {
        if (rope == null) return

        if (rope.tiedToMovale) {
            goat?.deattachRope(rope)
            dog?.deattachRope(rope)
        }

        ropes.remove(rope)
        rope.attachedRopes.forEach { removeFromRopes(it) }
        rope.remove()
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