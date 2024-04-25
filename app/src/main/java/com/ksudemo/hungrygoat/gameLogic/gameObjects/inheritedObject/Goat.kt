package com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import com.ksudemo.hungrygoat.constants.enums.GameObjectTags
import com.ksudemo.hungrygoat.gameLogic.game.grid.GameGrid
import com.ksudemo.hungrygoat.gameLogic.game.grid.GridHandler
import com.ksudemo.hungrygoat.gameLogic.gameObjects.abstractObjects.MovableGameObject
import com.ksudemo.hungrygoat.gameLogic.interfaces.enigneListeners.DrawListener
import com.ksudemo.hungrygoat.gameLogic.interfaces.goatListeners.GoatUpdateListener

class Goat(
    vx: Float,
    vy: Float,
    private val drawable: Drawable?,
    private val radius: Float,
    tag: GameObjectTags = GameObjectTags.GOAT,
) :
    MovableGameObject(vx, vy, tag, radius), DrawListener, GoatUpdateListener {

    val color = Color.YELLOW
    override fun draw(canvas: Canvas, paint: Paint) {
        paint.color = color
        Rect(
            (x - radius * scaleFactor).toInt(),
            (y - radius * scaleFactor).toInt(),
            (x + radius * scaleFactor).toInt(),
            (y + radius * scaleFactor).toInt()
        ).also { drawable?.bounds = it }
        drawable?.draw(canvas)
    }

    override fun update(gridHandler: GridHandler, dogs: List<Dog>): Boolean {
        if (path.isEmpty()) preparePath(gridHandler, dogs)
        if (lastVisitedIndex !in path.indices) return false

        path[lastVisitedIndex++].apply {
            this@Goat.x = this.x
            this@Goat.y = this.y
        }

        return true
    }

    fun preparePath(gridHandler: GridHandler, dogs: List<Dog>) {
        Log.d("mytag", "start prepare path")
        val grid = gridHandler.getGrid()
        path = if (dogs.isNotEmpty()) {
            val goatCell = gridHandler.getObjectCell(this)
            countPath(gridHandler, goatCell, dogs)
        } else reachedSet.map { (i, j) -> grid[i, j] }

        bounds = getBoundary(gridHandler, path)
        intersectionPathWithGridEdges = grid.intersectsBounds(bounds)

        Log.d(
            "mytag",
            "goat bounds size - ${bounds.size}\n path size - ${path.size}\n intersectWithEdges - ${intersectionPathWithGridEdges.size}"
        )
    }

    private fun countPath(gridHandler: GridHandler, goatCell: Cell?, dogs: List<Dog>): List<Cell> {
        if (goatCell == null || dogs.any { it.attachedRopes.isEmpty() })
            return emptyList()

        val grid = gridHandler.getGrid()
        val mappedReachedSet = reachedSet.map { (i, j) -> grid[i, j] }.toSet()

        val dogMapped =
            dogs.flatMap { it.reachedSet }.map { (i, j) -> grid[i, j] }.distinct().toSet()

        if (goatCell in dogMapped)
            return emptyList()

        return try {
            val zones = findZonesWithObstacles(grid, mappedReachedSet, dogMapped)
            val reachableCells = zones.flatMap { zone ->
                findReachableCells(grid, goatCell, zone, dogMapped)
            }.toSet()
            Log.d("mytag", "zones size - ${zones.size}")
            reachableCells.toList()
        } catch (e: Exception) {
            Log.e("mytag", e.message.toString())
            emptyList()
        }
    }

    private fun findZonesWithObstacles(
        grid: GameGrid,
        reachedSet: Set<Cell>,
        obstacles: Set<Cell>
    ): List<Set<Cell>> {
        val zones = mutableListOf<Set<Cell>>()
        val unprocessed = reachedSet.toMutableSet()

        while (unprocessed.isNotEmpty()) {
            val current = unprocessed.first()
            val zone = mutableSetOf<Cell>()
            val queue = mutableListOf(current)

            while (queue.isNotEmpty()) {
                val currentCell = queue.removeAt(0)
                if (currentCell in unprocessed) {
                    zone.add(currentCell)
                    unprocessed.remove(currentCell)

                    val neighbors = currentCell.getNeighbours(grid)
                    queue.addAll(neighbors.filter { it !in obstacles && it in reachedSet })
                }
            }

            zones.add(zone)
        }

        return zones
    }

    private fun findReachableCells(
        grid: GameGrid,
        goatCell: Cell,
        zone: Set<Cell>,
        obstacles: Set<Cell>,
    ): Set<Cell> {
        val reachableCells: MutableSet<Cell> = mutableSetOf()
        val wave = mutableListOf(goatCell)

        while (wave.isNotEmpty()) {
            val current = wave.removeAt(0)
            if (current !in reachableCells && current !in obstacles && current in zone) {
                reachableCells.add(current)
                val neighbors = current.getNeighbours(grid)
                wave.addAll(neighbors.filter { it !in obstacles && it in zone })
            }
        }

        return reachableCells
    }
}