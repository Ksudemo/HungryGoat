package com.ksudemo.hungrygoat.gameLogic.game

import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import com.ksudemo.hungrygoat.constants.appContants.SingletonAppConstantsInfo
import com.ksudemo.hungrygoat.constants.enums.GameObjectTags
import com.ksudemo.hungrygoat.constants.enums.GameStates
import com.ksudemo.hungrygoat.constants.enums.LevelConditions
import com.ksudemo.hungrygoat.constants.enums.PickedOptions
import com.ksudemo.hungrygoat.gameLogic.game.grid.GridHandler
import com.ksudemo.hungrygoat.gameLogic.gameObjects.GameObjectFactory
import com.ksudemo.hungrygoat.gameLogic.gameObjects.RulerSet
import com.ksudemo.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.Peg
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.rope.Rope
import com.ksudemo.hungrygoat.gameLogic.interfaces.EducationStepDoneListener
import com.ksudemo.hungrygoat.gameLogic.interfaces.dogListeners.DogUnboundedListener
import com.ksudemo.hungrygoat.gameLogic.interfaces.enigneListeners.CheckSolutionListener
import com.ksudemo.hungrygoat.gameLogic.interfaces.enigneListeners.RopeDepthLevelBoundsListener
import com.ksudemo.hungrygoat.gameLogic.interfaces.goatListeners.GoatUnboundedListener
import com.ksudemo.hungrygoat.gameLogic.services.EducationHelperService
import com.ksudemo.hungrygoat.gameLogic.services.InputHandler
import com.ksudemo.hungrygoat.gameLogic.services.PhysicService
import com.ksudemo.hungrygoat.gameLogic.services.RenderService
import com.ksudemo.hungrygoat.gameLogic.services.solution.SolutionService
import java.util.Collections

class GameEngine {
    private var goatUnboundedListener: GoatUnboundedListener? = GoatUnboundedListener {}
    private var dogUnboundedListener: DogUnboundedListener? = DogUnboundedListener {}
    private var checkSolutionListener: CheckSolutionListener? =
        CheckSolutionListener { _: List<LevelConditions>, _: Int -> }

    private var ropeDepthLevelBoundsListener: RopeDepthLevelBoundsListener? =
        RopeDepthLevelBoundsListener {}
    private var educationStepDoneListener: EducationStepDoneListener? = EducationStepDoneListener {}

    fun registereEducationStepDoneListener(edu: EducationStepDoneListener) {
        educationStepDoneListener = edu
    }

    fun unregistereEducationStepDoneListener() {
        educationStepDoneListener = null
    }

    fun registerRopeDepthLevelBoundsListener(ropeDepthListener: RopeDepthLevelBoundsListener) {
        ropeDepthLevelBoundsListener = ropeDepthListener
    }

    fun unregisterRopeDepthLevelBoundsListener() {
        ropeDepthLevelBoundsListener = null
    }

    fun regesterCheckSolutionListener(checkSolListener: CheckSolutionListener) {
        checkSolutionListener = checkSolListener
    }

    fun unregesterCheckSolutionListener() {
        checkSolutionListener = null
    }

    fun registerMovableErrorsListener(
        dogUnbounded: DogUnboundedListener,
        goatUnbounded: GoatUnboundedListener? = null
    ) {
        goatUnboundedListener = goatUnbounded
        dogUnboundedListener = dogUnbounded
    }

    fun unregesterMovableErrorsListener() {
        dogUnboundedListener = null
        goatUnboundedListener = null
    }

    companion object {
        private val solutionService = SolutionService()
        private val renderService = RenderService()
        private val eduService = EducationHelperService()

        private val gridHandler = GridHandler()
        private val gameObjectFactory = GameObjectFactory()

        private var goat: Goat? = null
        private var dogs = mutableListOf<Dog>()

        private var ropes = Collections.synchronizedList(mutableListOf<Rope>())
        private var objects = Collections.synchronizedList(mutableListOf<GameObject>())
        private val objectsMutex = Any()
        private val history = mutableListOf<GameObject>()

        var ruler = mutableListOf<RulerSet>()

        private var tempWhileMove: GameObject? = null
        fun getObjects(): MutableList<GameObject> = objects
        fun getRopes(): MutableList<Rope> = ropes
    }

    fun getRenderSerivece() = renderService
    fun setGrid(w: Int, h: Int, cellSize: Float) {
        gridHandler.setGrid(w, h, cellSize)
    }

    fun clearObjects() {
        objects.clear()
        ropes.clear()
        ruler.clear()
        history.clear()

        goat = null
        dogs.clear()

        resetTempObj()
    }

    fun resetTempObj() {
        objects.find { it.isTempOnRopeSet }?.isTempOnRopeSet = false
    }

    fun revertLastMove() {
        val appC = SingletonAppConstantsInfo.getAppConst()
        if (appC.getState() != GameStates.STATE_PLAYER_PLACE_OBJECTS)
            return

        if (history.isNotEmpty()) {
            val last = history.removeLast()
            if (last.gameObjectTag == GameObjectTags.ROPE)
                removeFromRopes(listOf(last as Rope)) else removeFromObjects(last)
        }
    }

    private fun checkSolution(): List<LevelConditions> =
        when {
            goat?.attachedRopes?.isEmpty() == true -> {
                goatUnboundedListener?.onGoatUnbounded()
                emptyList()
            }

            dogs.any { it.attachedRopes.isEmpty() } -> {
                dogUnboundedListener?.onDogUnbounded()
                emptyList()
            }

            else -> solutionService.checkSolution(goat!!, dogs, gridHandler)
        }


    fun update() {
        when {
            goat == null -> return
            goat?.attachedRopes?.isEmpty() == true -> {
                goatUnboundedListener?.onGoatUnbounded()
                return
            }

            dogs.any { it.attachedRopes.isEmpty() } -> {
                dogUnboundedListener?.onDogUnbounded()
                return
            }
        }

        val appC = SingletonAppConstantsInfo.getAppConst()
        var updateSuccess = false

        repeat(appC.updatePerFrame) {
            updateSuccess = goat?.update(gridHandler, dogs) == true

            ropes.forEach { it.update() }
            dogs.forEach { it.update(gridHandler) }
        }

        val stateToCheck = GameStates.STATE_CHECK_SOLUTION
        if (!updateSuccess && appC.getState() != stateToCheck) {
            appC.changeState(stateToCheck)
            checkSolutionListener?.checkSolution(checkSolution(), dogs.size)
            return
        }
    }

    fun draw(canvas: Canvas) =
        renderService.render(
            canvas,
            gridHandler,
            objects,
            ropes,
            tempWhileMove,
            ruler,
            SingletonAppConstantsInfo.getAppConst().getSetttings()
        )

    fun handleTouch(
        event: MotionEvent,
        pickedOption: PickedOptions,
        state: GameStates,
        radius: Float
    ) {
        if (state == GameStates.STATE_PLAYER_PLACE_OBJECTS) {
            val x = event.x
            val y = event.y
            when (event.action) {
                MotionEvent.ACTION_UP -> createNewObject(x, y, pickedOption, radius)
                MotionEvent.ACTION_MOVE -> tempCreateNewObjectOnMove(x, y, pickedOption, radius)
            }
        }
    }

    private fun createNewObject(x: Float, y: Float, pickedOption: PickedOptions, radius: Float) {
        synchronized(objectsMutex) {
            Log.d("mytag", "x = $x \n y = $y \n")
            when (pickedOption) {
                PickedOptions.ERASER -> eraseObject(x, y)
                else -> {
                    val createdObject = gameObjectFactory.createNewObject(
                        x, y, objects.find { it.isTempOnRopeSet }, pickedOption, gridHandler, radius
                    )

                    val curEduStepTag = SingletonAppConstantsInfo.getAppConst().getCurEduStepTag()
                    val flag = eduService.canCreate(curEduStepTag, pickedOption, createdObject)

                    Log.d("mytag", "created - $createdObject")
                    Log.d("mytag", "isTempOnRopeSet - ${objects.find { it.isTempOnRopeSet }}")

                    when (flag) {
                        true -> {
                            if (curEduStepTag != null) educationStepDoneListener?.onStepDone()
                            handleCreatedObject(createdObject ?: return)
                        }

                        else -> createdObject?.isTempOnRopeSet = false
                    }
                    Log.d("mytag", "objs size - ${objects.size}")
                }
            }

            tempWhileMove = null
            ruler.clear()
        }
    }

    private fun tempCreateNewObjectOnMove(
        x: Float,
        y: Float,
        pickedOption: PickedOptions,
        radius: Float
    ) {
        if (pickedOption == PickedOptions.ROPE) return
        ruler.clear()

        if (tempWhileMove != null && tempWhileMove?.gameObjectTag.toString() == pickedOption.toString()) {

            tempWhileMove?.x = x
            tempWhileMove?.y = y

            objects.forEach {
                val angle = PhysicService().calcAngleBetween(
                    tempWhileMove!!, it
                )
                val rulerSet = RulerSet(tempWhileMove!!, it, angle)
                ruler.add(rulerSet)
            }
        } else
            tempWhileMove = gameObjectFactory.createNewObject(
                x, y, objects.find { it.isTempOnRopeSet }, pickedOption, gridHandler, radius
            ) ?: return
    }


    private fun eraseObject(x: Float, y: Float) {
        val clickedObject = InputHandler().getClickedObject(gridHandler, objects, x, y)
        val rope = InputHandler().getClickedRopeSegment(gridHandler, ropes, x, y)?.baseRope

        if (clickedObject != null)
            removeFromObjects(clickedObject)
        else removeFromRopes(listOfNotNull(rope))
    }

    @Synchronized
    private fun handleCreatedObject(createdObject: GameObject) {
        if (createdObject.gameObjectTag == GameObjectTags.ROPE)
            addToRopes(createdObject as Rope)
        else
            checkAndSetMovable(createdObject)
    }

    private fun canAttach(isDog: Boolean, isGoat: Boolean, rope: Rope): Boolean {
        if (isDog && isGoat) return false

        val anchor =
            if (rope.objectTo.gameObjectTag != GameObjectTags.RopeSegment) rope.objectTo else
                if (rope.objectFrom.gameObjectTag != GameObjectTags.RopeSegment) rope.objectFrom else return true

        val ropeConnectedToAttached =
            anchor.attachedRopes.any { attached -> rope.getRopeConnectedTo() == attached }

        val ropeAndAnyAttachedConnectedToSameRope =
            anchor.attachedRopes.any { attached -> rope.getRopeConnectedTo() == attached.getRopeConnectedTo() && rope.getRopeConnectedTo() != null }

        return !ropeConnectedToAttached && !ropeAndAnyAttachedConnectedToSameRope
    }

    @Synchronized
    private fun addToRopes(obj: GameObject) {
        val rope = obj as Rope
        objects.find { it.isTempOnRopeSet }?.isTempOnRopeSet = false
        objects.removeIf { it.gameObjectTag == GameObjectTags.RopeSegment }

        val isDog = isObjADog(rope.objectTo, rope.objectFrom)
        val isGoat = isObjAGoat(rope.objectTo, rope.objectFrom)

        Log.d("mytag", "can attach - ${canAttach(isDog, isGoat, rope)}")
        if (!canAttach(isDog, isGoat, rope)) return

        if (rope.depthLevel > 2) {
            ropeDepthLevelBoundsListener?.onRopeToHighDepth()
            return
        }
        when {
            isGoat -> {
                goat?.attachRope(rope)
                goat?.preparePath(gridHandler, dogs)
            }

            isDog -> {
                dogs.find { it == rope.objectTo || it == rope.objectFrom }?.attachRope(rope)
                goat?.preparePath(gridHandler, dogs)
            }
        }

        if (rope.objectFrom.gameObjectTag == GameObjectTags.PEG)
            (rope.objectFrom as Peg).attachedRopes.add(rope)

        if (rope.objectTo.gameObjectTag == GameObjectTags.PEG)
            (rope.objectTo as Peg).attachedRopes.add(rope)


        rope.id = if (ropes.isEmpty()) 0 else ropes.last().id + 1
        ropes.add(rope)
        history.add(rope)
    }

    @Synchronized
    private fun checkAndSetMovable(obj: GameObject) {
        if (objects.find { it.x == obj.x && it.y == obj.y && it.gameObjectTag == obj.gameObjectTag } != null)
            return

        val isDog = isObjADog(obj)
        val isGoat = isObjAGoat(obj)

        if (isGoat && !obj.isTempOnRopeSet)
            removeFromObjects(goat)

        when {
            isGoat -> goat = (obj as Goat)

            isDog -> {
                dogs.add(obj as Dog)
                goat?.apply {
                    moveToStart()
                    preparePath(gridHandler, dogs)
                }
            }
        }

        if (!obj.isTempOnRopeSet)
            history.add(obj)
        objects.add(obj)
    }


    @Synchronized
    private fun removeFromObjects(obj: GameObject?) {
        try {
            if (obj == null) return

            val curObjTag = obj.gameObjectTag
            val curObjAGoat = isObjAGoat(obj)
            val curObjADog = isObjADog(obj)

            if (curObjAGoat) {
                val ropes = goat?.attachedRopes ?: emptyList()
                goat?.attachedRopes = mutableListOf()

                objects.remove(goat)
                goat = null

                removeFromRopes(ropes)

                return
            } else if (curObjADog) {
                val dog = obj as Dog
                objects.remove(dog)
                dogs.remove(dog)
                goat?.preparePath(gridHandler, dogs)

                removeFromRopes(dog.attachedRopes)
                dog.attachedRopes = mutableListOf()

                return
            }

            objects.removeIf { it.gameObjectTag == curObjTag && it.x == obj.x && it.y == obj.y }
            removeFromRopes(obj.attachedRopes)
        } catch (e: Exception) {
            Log.e("mytag", "exception in removeFromObjects ${e.printStackTrace()}")
        }
    }

    @Synchronized
    private fun removeFromRopes(ropesToRemove: List<Rope>) {
        Log.d("mytag", "ropes to remove - $ropesToRemove")
        if (ropesToRemove.isEmpty()) return
        val ropesToRemoveAsSet = ropesToRemove.toSet()

        (goat?.attachedRopes?.intersect(ropesToRemoveAsSet) ?: emptySet())
            .forEach { goat?.deattachRope(it) }

        for (dog in dogs) {
            dog.attachedRopes.intersect(ropesToRemoveAsSet)
                .forEach { dog.deattachRope(it) }
        }
        goat?.preparePath(gridHandler, dogs)
        ropes.removeAll(ropesToRemoveAsSet)
        for (r in ropesToRemove) {
            Log.d("mytag", "rope to remove - $r")
            Log.d("mytag", "attachedRopes - ${r.attachedRopesHashSet}")
            removeFromRopes(r.attachedRopesHashSet.toList())
        }
    }

    private fun isObjADog(vararg objs: GameObject): Boolean =
        objs.any { it.gameObjectTag == GameObjectTags.DOG }

    private fun isObjAGoat(vararg objs: GameObject): Boolean =
        objs.any { it.gameObjectTag == GameObjectTags.GOAT }

    fun restoreInitialState() {
        goat?.apply {
            moveToStart()
        }
        dogs.forEach {
            it.moveToStart()
            it.path = emptyList()
        }
    }

    fun goatAvaliable() = goat != null
    fun killEngine() {
        clearObjects()
        gridHandler.freeGrid()
    }
}