package com.ksudemo.hungrygoat.gameLogic.services.solution

import android.util.Log
import com.ksudemo.hungrygoat.constants.enums.LevelConditions
import com.ksudemo.hungrygoat.gameLogic.game.grid.GridHandler
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.Cell
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import com.ksudemo.hungrygoat.gameLogic.services.PhysicService
import com.ksudemo.hungrygoat.gameLogic.services.solution.SolutionUtility.MovableInfo
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

class SolutionService {
    private val utils = SolutionUtility()
    private val physics = PhysicService()

    fun checkSolution(
        goat: Goat,
        dogs: List<Dog>,
        gridHandler: GridHandler,
    ): List<LevelConditions> {
        try {
            if (!utils.isGridHandlerInitialized())
                utils.setGridHadler(gridHandler)
            val cellSize = gridHandler.getGrid().cellSize

            val corners = utils.getCorners(goat.bounds, cellSize)
            val angles = utils.getAngles(corners)
            val sideLengths = utils.getSideLengths(corners)
            Log.d("mytag", "corners size - ${corners.size}")
            Log.d("mytag", "angles - $angles , sum = ${angles.sum()}")
            Log.d("mytag", "lengths - $sideLengths")
            Log.d("mytag", "avg len - ${sideLengths.average()}")


            val goatCoords = utils.getCoords(goat, cellSize)
            val dogCoords = dogs.map { utils.getCoords(it, cellSize) }

            if (dogCoords.isNotEmpty()) {
                Log.d(
                    "mytag",
                    "is goat contains dog - ${
                        physics.isCircleContainsAnotherCircle(
                            goatCoords,
                            dogCoords.first()
                        )
                    }"
                )
                Log.d(
                    "mytag",
                    "is goat intersects dog - ${
                        physics.isTwoCircleIntersects(
                            goatCoords,
                            dogCoords.first()
                        )
                    }"
                )
            }

            val isCircle = checkCircle(goatCoords)
            val isRing = checkRing(isCircle, goat, dogs, goatCoords, dogCoords)
            val isMoon = checkMoon(gridHandler, goat, corners)
            val isOval = checkOval(goat)

            val isLeaf = checkLeaf(gridHandler, goat)

            val isParallelogramm = checkParallelogramm(angles, sideLengths)
            val isRect = checkRectangle(isParallelogramm, angles)
            val isTriangle = checkTriangle(goat, corners, angles)
            val isTriangleWithDogs = checkTriangleWithDogs(isTriangle, goat, dogs)
            val isRaindrop = checkRaindrop(goat)

            val isHalfCircleWithoutDogs =
                checkHalfcircle(goat, goat.bounds, corners, cellSize, isMoon)

            val isHalfcircleWithDogs = checkHalfcircleWithDogs(isHalfCircleWithoutDogs, goat, dogs)
            val isHalfring = checkHalfring(goat, dogs)

            val isHexagon = checkHexagon(goat, corners)
            val isArrow = checkArrow(corners, angles)

            Log.d(
                "MyTag",
                "Current shape is:\n " +
                        "Circle - $isCircle\n " +
                        "Ring - $isRing\n " +
                        "isOval - $isOval\n" +

                        "isLeaf - $isLeaf\n" +
                        "isMoon - $isMoon\n" +

                        "isRect - $isRect\n" +
                        "isParallelogramm - $isParallelogramm\n " +
                        "isTrianlge - $isTriangle\n" +
                        "isTriangleWithDogs - $isTriangleWithDogs\n" +
                        "isRaindrop - $isRaindrop\n" +
                        "isHexagon - $isHexagon\n" +
                        "isHalfcircle - $isHalfCircleWithoutDogs\n " +
                        "isHalfcircleWithDogs - $isHalfcircleWithDogs\n " +
                        "isHalfring - $isHalfring\n" +
                        "isArrow - $isArrow\n"
            )

            return listOf(
                isCircle to LevelConditions.CIRCLE,
                isOval to LevelConditions.OVAL,
                isRing to LevelConditions.RING,
                isLeaf to LevelConditions.LEAF,
                isMoon to LevelConditions.MOON,
                isRect to LevelConditions.RECTANGLE,
                isParallelogramm to LevelConditions.PARALLELOGRAM,
                isTriangle to LevelConditions.TRIANGLE_WITHOUT_DOGS,
                isTriangleWithDogs to LevelConditions.TRIANGLE_WITH_DOGS,
                isRaindrop to LevelConditions.RAINDROP,
                isHalfCircleWithoutDogs to LevelConditions.HALFCIRCLE_WITHOUT_DOGS,
                isHalfcircleWithDogs to LevelConditions.HALFCIRCLE_WITH_DOGS,
                isHalfring to LevelConditions.HALFRING,
                isHexagon to LevelConditions.HEXAGON,
                isArrow to LevelConditions.ARROW
            ).filter { it.first }.map { it.second }

        } catch (e: Exception) {
            Log.e("mytag", "Exception in SolutionService ${e.printStackTrace()}")
            return emptyList()
        }
    }

    private fun checkCircle(coords: MovableInfo): Boolean {
        val mapped = coords.bounds.map { boundCell ->
            val dx = abs(boundCell.x - coords.x)
            val dy = abs(boundCell.y - coords.y)

            sqrt(dx * dx + dy * dy)
        }
        val avgDist = mapped.average()
        val maxDist = mapped.max()
        val minDist = mapped.min()

        return avgDist <= coords.r && (maxDist - minDist < 15f)
    }

    private fun checkOval(goat: Goat): Boolean {
        val attachedRope = goat.attachedRopes.firstOrNull()

        return (goat.attachedRopes.size == 1 && attachedRope?.getRopeConnectedTo()?.isTiedToRope == false)
    }

    private fun checkRing(
        isCircle: Boolean,
        goat: Goat,
        dogs: List<Dog>,
        goatCoords: MovableInfo,
        dogsCoords: List<MovableInfo>,
    ): Boolean {
        if (dogsCoords.isEmpty())
            return false

        val dogRopesAttachedObjs = dogs.map { dog ->
            dog.attachedRopes.map { listOf(it.objectFrom, it.objectTo) }.flatten()
        }.flatten()

        val circlishDog = dogsCoords.filter { checkCircle(it) }
        return isCircle && circlishDog.all {
            physics.isCircleContainsAnotherCircle(
                goatCoords,
                it
            )
        } && goat.attachedRopes.any { it.objectTo in dogRopesAttachedObjs || it.objectFrom in dogRopesAttachedObjs }
    }

    private fun checkParallelogramm(angles: List<Int>, sideLengths: List<Float>): Boolean {
        fun isOppositeCornersEqual(maxDiff: Int) =
            angles[0] - angles[2] in -maxDiff..maxDiff && angles[1] - angles[3] in -maxDiff..maxDiff

        fun isOppositeLensEqual(maxDiff: Int): Boolean {
            val isOppSidesAEqual =
                (sideLengths[0] - sideLengths[2]).roundToInt() in -maxDiff..maxDiff
            val isOppSidesBEqual =
                (sideLengths[1] - sideLengths[3]).roundToInt() in -maxDiff..maxDiff

            return isOppSidesAEqual && isOppSidesBEqual
        }

        if (angles.size != 4) return false

        val maxAngleDiff = 2
        val maxLensDiff = 20
        val isOppositeAnglesEquals = isOppositeCornersEqual(maxAngleDiff * 3)
        val isOppositeLensEqual = isOppositeLensEqual(maxLensDiff)

        return isOppositeAnglesEquals && isOppositeLensEqual
    }

    private fun checkRectangle(isParal: Boolean, angles: List<Int>): Boolean {
        if (!isParal) return false

        val delta = 3
        return angles.all { it in 90 - delta..90 + delta }
    }

    private fun checkTriangle(goat: Goat, corners: List<Cell>, angles: List<Int>): Boolean {
        if (corners.size < 3) return false

        val lens = utils.getSideLengths(corners)
        val delta = 1

        // a,b,c - side of a triangle => len(a) < len(b) + len(c)
        val condA =
            lens[0] < lens[1] + lens[2] && lens[1] < lens[0] + lens[2] && lens[2] < lens[0] + lens[1]

        // a1 + a2 + a3 == 180* +- delta
        val conB = angles.sum() in (180 - delta..180 + delta)
        val a = physics.isLineInsideBounds(corners[0], corners[1], goat.bounds, 8f)
        val b = physics.isLineInsideBounds(corners[1], corners[2], goat.bounds, 8f)
        val c = physics.isLineInsideBounds(corners[2], corners[0], goat.bounds, 8f)

        return condA && conB && a && b && c
    }


    private fun checkTriangleWithDogs(isTriangle: Boolean, goat: Goat, dogs: List<Dog>): Boolean =
        isTriangle && (goat.reachedSet - dogs.map { it.reachedSet }.flatten()
            .toSet()).size != goat.reachedSet.size


    private fun checkLeaf(gridHandler: GridHandler, goat: Goat): Boolean {
        val ropes = goat.attachedRopes
        if (ropes.size != 2)
            return false

        val ropeA = ropes[0]
        val ropeB = ropes[1]
        return utils.circleIntersects(gridHandler, ropeA, ropeB)
    }

    private fun checkMoon(
        gridHandler: GridHandler,
        goat: Goat,
        corners: List<Cell>
    ): Boolean {
        if (corners.isEmpty())
            return false

        val windowed = corners.windowed(2, 1).toMutableList()
        windowed.add(listOf(corners.last(), corners.first()))

        val farest = windowed.maxBy { lst ->
            val x = lst.map { it.x }.average().toFloat()
            val y = lst.map { it.y }.average().toFloat()

            val closestBoundCell =
                goat.bounds.minBy { PhysicService().distBetween(x, y, it.x, it.y) }
            PhysicService().distBetween(x, y, closestBoundCell.x, closestBoundCell.y)
        }
        val farestX = farest.map { it.x }.average().toFloat()
        val farestY = farest.map { it.y }.average().toFloat()
        val closestBoundCell =
            goat.bounds.minBy { PhysicService().distBetween(farestX, farestY, it.x, it.y) }
        val dist =
            PhysicService().distBetween(farestX, farestY, closestBoundCell.x, closestBoundCell.y)

        return dist > gridHandler.getGrid().cellSize * 5
    }


    private fun checkRaindrop(goat: Goat): Boolean {
        val ropes = goat.attachedRopes
        if (ropes.size != 2 || ropes.any { !it.isTiedToRope })
            return false

        val baseRopes = listOf(
            ropes[0].getRopeConnectedTo()!!,
            ropes[1].getRopeConnectedTo()!!,
        )

        val isRopeTheSame = baseRopes.distinct().size != baseRopes.size
        val baseRopeTiedToARope = baseRopes.any { it.isTiedToRope }
        if (isRopeTheSame || baseRopeTiedToARope)
            return false
        val uniquePegs = baseRopes.flatMap { listOf(it.objectTo, it.objectFrom) }.distinct()

        return uniquePegs.size == 3
    }

    private fun checkHexagon(goat: Goat, corners: List<Cell>): Boolean {
        if (corners.size != 6)
            return false

        val line1 = physics.isLineInsideBounds(corners[0], corners[1], goat.bounds, 8f)
        val line2 = physics.isLineInsideBounds(corners[1], corners[2], goat.bounds, 8f)
        val line3 = physics.isLineInsideBounds(corners[2], corners[3], goat.bounds, 8f)
        val line4 = physics.isLineInsideBounds(corners[3], corners[4], goat.bounds, 8f)
        val line5 = physics.isLineInsideBounds(corners[4], corners[5], goat.bounds, 8f)
        val line6 = physics.isLineInsideBounds(corners[5], corners[0], goat.bounds, 8f)

        return line1 && line2 && line3 && line4 && line5 && line6
    }

    private fun checkHalfcircle(
        goat: Goat,
        goatBounds: List<Cell>,
        corners: List<Cell>,
        cellSize: Float,
        isMoon: Boolean
    ): Boolean {
        if (corners.size < 2 || isMoon) return false

        val avgX = goatBounds.map { it.x }.average().toFloat()
        val avgY = goatBounds.map { it.y }.average().toFloat()
        val sort = goatBounds.sortedByDescending { physics.distBetween(it.x, it.y, avgX, avgY) }

        val first = sort[0]
        val second =
            sort.first { physics.distBetween(first.x, first.y, it.x, it.y) > cellSize * 10 }

        val isLineInsideBounds = physics.isLineInsideBounds(first, second, goatBounds, cellSize)
        Log.d(
            "mytag",
            "half circle: isLineInsideBounds - $isLineInsideBounds , secondCond - ${goat.attachedRopes.any { !it.isTiedToRope }}"
        )
        return isLineInsideBounds && goat.attachedRopes.any { !it.isTiedToRope } &&
                !checkCircle(utils.getCoords(goat, cellSize))
    }

    private fun checkHalfcircleWithDogs(isHalfcircle: Boolean, goat: Goat, dogs: List<Dog>) =
        isHalfcircle && (goat.reachedSet - dogs.map { it.reachedSet }.flatten()
            .toSet()).size != goat.reachedSet.size

    private fun checkHalfring(
        goat: Goat,
        dogs: List<Dog?>
    ): Boolean {
        Log.d(
            "mytag",
            "dogs.all { it?.attachedRopes?.size != 1 } -${dogs.all { it?.attachedRopes?.size != 1 }}"
        )
        if (dogs.isEmpty() || dogs.all { it?.attachedRopes?.size != 1 })
            return false

        val dogRopes = dogs.mapNotNull { it?.attachedRopes?.getOrNull(0) }
        val goatSameRopeWithAnyDog = goat.attachedRopes.find { goatRope ->
            !goatRope.isTiedToRope && dogRopes.any { dogRope ->
                (goatRope.objectTo == dogRope.objectTo) ||
                        (goatRope.objectTo == dogRope.objectFrom) ||
                        (goatRope.objectFrom == dogRope.objectTo) ||
                        (goatRope.objectFrom == dogRope.objectFrom)
            }
        }

        Log.d("mytag", "goatSameRopeWithAnyDog - $goatSameRopeWithAnyDog")
        if (goatSameRopeWithAnyDog == null) return false

        return goatSameRopeWithAnyDog.ropeLength > dogRopes[0].ropeLength
    }

    private fun checkArrow(corners: List<Cell>, angles: List<Int>): Boolean {
        if (corners.size != 5) return false

        val delta = 2
        val countRightAngles = angles.count { it in 90 - delta..90 + delta }

        Log.d("mytag", "checkArrow - countRightAngles - $countRightAngles")
        return countRightAngles in 2..3
    }
}
