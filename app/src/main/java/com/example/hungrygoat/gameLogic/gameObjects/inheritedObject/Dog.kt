package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.MovableGameObject
import com.example.hungrygoat.gameLogic.interfaces.DogUpdate
import com.example.hungrygoat.gameLogic.interfaces.Draw
import com.example.hungrygoat.gameLogic.interfaces.Move
import com.example.hungrygoat.gameLogic.services.GridHandler


class Dog(x: Float, y: Float, tag: GameObjectTags) :
    MovableGameObject(x, y, tag), Draw, DogUpdate, Move {

    val color = Color.RED

    override fun draw(canvas: Canvas, paint: Paint) {
        paint.color = color
        canvas.drawCircle(x, y, circleRadius, paint)
    }

    override fun update(gridHandler: GridHandler, goatHaveAvailableCells: Boolean) {
        if (goatHaveAvailableCells) {
            val cellToMove = getNextCellToMove(gridHandler)
            move(cellToMove)
        }
    }

    private fun getNextCellToMove(gridHandler: GridHandler): Cell? {
        if (path.isEmpty())
            path = bounds.sortedBy {
                gridHandler.distBetween(
                    this,
                    it.x,
                    it.y
                )
            }
        Log.d("mytag", "dog path size - ${path.size}, visited - ${path.count { it.visited }}")
        return path.filter { !it.visited }.minByOrNull { gridHandler.distBetween(this, it.x, it.y) }
    }

    override fun move(cellToMove: Cell?) {
        if (cellToMove == null) return
        x = cellToMove.x
        y = cellToMove.y
        cellToMove.visited = true

        if (!path.any { !it.visited })
            path.forEach { it.visited = false }
    }
}