package com.example.hungrygoat.gameLogic.services.solution

import android.util.Log
import com.example.hungrygoat.constants.LevelConditions
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import com.example.hungrygoat.gameLogic.services.GridHandler
import kotlin.math.abs
import kotlin.math.absoluteValue

class SolutionService {
    data class Coords(val x: Float, val y: Float, val r: Float)

    private val utils = SolutionUtility()

    fun checkSolution(
        goat: Goat,
        dog: Dog?,
        gridHandler: GridHandler,
        targetShape: LevelConditions,
    ) =
        try {
            val goatBounds = goat.bounds
            val cellSize = gridHandler.cellSize

            val goatCoords = utils.getCoords(goat, cellSize)
            val dogCoords = utils.getCoords(dog, cellSize)

            Log.d(
                "MyTag",
                "Current shape is:\n " +
                        "Circle - ${isCircle(goatBounds, goatCoords)}\n " +
                        "Ring - ${
                            isRing(
                                isCircle(goatBounds, goatCoords),
                                goatCoords,
                                dogCoords
                            )
                        }\n " +

                        "isLeaf -> ${isLeaf(gridHandler, goat)}\n" +
                        "isMoon -> ${isMoon(gridHandler, goat, dog, goatCoords)}\n" +

                        "Reactangle - ${isRect(goatBounds)}\n " +
                        "Triangle - ${isTriangle(goatBounds)}\n" +

                        "HalfCircle (TODO)- ${
                            isHalfCircle(
                                isCircle(goatBounds, goatCoords),
                                goatBounds,
                                goatCoords
                            )
                        }\n " +
                        ""
            )

            when (targetShape) {
                LevelConditions.CIRCLE -> isCircle(goatBounds, goatCoords)
                LevelConditions.RING -> isRing(
                    isCircle(goatBounds, goatCoords), goatCoords, dogCoords
                )

                LevelConditions.RECTANGLE -> isRect(goatBounds)
                LevelConditions.TRIANGLE -> isTriangle(goatBounds)

                LevelConditions.MOON -> isMoon(gridHandler, goat, dog, goatCoords)
                LevelConditions.LEAF -> isLeaf(gridHandler, goat)

                LevelConditions.HALFCIRCLE -> isHalfCircle(
                    isCircle(goatBounds, goatCoords),
                    goatBounds,
                    goatCoords
                )

                LevelConditions.HALFRING -> isHalfRing(
                    goatCoords, dogCoords
                )

                LevelConditions.HEXAGON -> isHexagon(goatBounds)
                LevelConditions.ARROW -> isArrow(goatBounds)
                LevelConditions.RAINDROP -> isRaindrop(goatBounds)
                else -> true
            }


        } catch (e: Exception) {
            Log.e("mytag", "Exception in SolutionService ${e.printStackTrace()}")
            false
        }


    private fun isCircle(bounds: List<Cell>, goatCoords: Coords): Boolean {
        return bounds.all { boundCell ->
            val dx = abs(boundCell.x - goatCoords.x)
            val dy = abs(boundCell.y - goatCoords.y)

            dx < goatCoords.r && dy < goatCoords.r
        }
    }

    private fun isRing(
        isCircle: Boolean,
        goatCoords: Coords,
        dogCoords: Coords,
    ): Boolean {
        if (isCircle)
            return dogCoords.r < goatCoords.r
        return false
    }

    private fun isRect(bounds: List<Cell>): Boolean {
        val corners = utils.getBoundgBox(bounds).filterNotNull() // rt rb lb lt
        if (corners.size != 4) return false

        val angles = utils.getAngles(corners)
        val maxDiff = 1
        return (angles[0] - angles[1]).absoluteValue <= maxDiff &&
                (angles[1] - angles[2]).absoluteValue <= maxDiff &&
                (angles[2] - angles[3]).absoluteValue <= maxDiff &&
                (angles[3] - angles[0]).absoluteValue <= maxDiff
    }

    private fun isTriangle(bounds: List<Cell>): Boolean {
        val corners = utils.getBoundgBox(bounds).filterNotNull() // rt rb lb lt
        if (corners.size != 3) return false

        val angles = utils.getAngles(corners)
        val lens = utils.getSideLengths(corners)


        // a,b,c - side of a triangle => len(a) < len(b) + len(c)
        val condA =
            lens[0] < lens[1] + lens[2] && lens[1] < lens[0] + lens[2] && lens[2] < lens[0] + lens[1]

        val delta = 1
        // a1 + a2 + a3 == 180* +- delta
        val conB = (angles[0] + angles[1] + angles[2]) in (180 - delta..180 + delta)

        return condA && conB
    }

    private fun isLeaf(gridHandler: GridHandler, goat: Goat): Boolean {
        val ropes = goat.attachedRopes
        if (ropes.size != 2)
            return false

        val ropeA = ropes[0]
        val ropeB = ropes[1]
        return utils.circleIntersects(gridHandler, ropeA, ropeB)
    }

    private fun isMoon(
        gridHandler: GridHandler,
        goat: Goat,
        dog: Dog?,
        goatCoords: Coords,
    ): Boolean {
        if (!isCircle(goat.path, goatCoords) && dog == null)
            return false
        if (goat.attachedRopes.size != 1 && dog!!.attachedRopes.size != 1)
            return false

        val ropeA = goat.attachedRopes[0]
        val ropeB = dog!!.attachedRopes[0]

        return utils.circleIntersects(gridHandler, ropeA, ropeB)
    }

    private fun isHexagon(bounds: List<Cell>): Boolean {

        return false
    }

    private fun isHalfRing(goatCoords: Coords, dogCoords: Coords): Boolean {

        return false
    }

    private fun isHalfCircle(isCircle: Boolean, bounds: List<Cell>, goatCoords: Coords): Boolean {


        return false
    }

    private fun isArrow(bounds: List<Cell>): Boolean {

        return false
    }

    private fun isRaindrop(bounds: List<Cell>): Boolean {


        return false
    }


}
