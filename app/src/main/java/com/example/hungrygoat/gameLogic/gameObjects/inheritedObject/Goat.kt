package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.MovableGameObject
import com.example.hungrygoat.gameLogic.interfaces.Draw
import com.example.hungrygoat.gameLogic.interfaces.GoatUpdate
import com.example.hungrygoat.gameLogic.interfaces.Move
import com.example.hungrygoat.gameLogic.services.GridHandler

class Goat(vx: Float, vy: Float, tag: GameObjectTags) :
    MovableGameObject(vx, vy, tag), Draw, GoatUpdate, Move {

    val color = Color.YELLOW
    override fun draw(canvas: Canvas, paint: Paint) {
        paint.color = color
        canvas.drawCircle(x, y, circleRadius, paint)
    }

    override fun update(gridHandler: GridHandler, dogObj: Dog?): Boolean =
        if (hadAvailableCells) {
            repeat(2) { // TODO repeat 1/2 ?
                val cellToMove = getNextCellToMove(gridHandler, dogObj)
                move(cellToMove)
            }
//            path.forEach { it.visited = true }
//            hadAvailableCells = false
            true
        } else
            false

    private fun getNextCellToMove(gridHandler: GridHandler, dogObj: Dog?): Cell? {
        if (path.isEmpty()) {
            val dogReachedSet = dogObj?.reachedSet ?: emptySet()
            path = if (reachedSet.isEmpty()) gridHandler.getGrid() - dogReachedSet.toSet() else
                (reachedSet - dogReachedSet.toSet())
        }
        return path.filter { !it.visited }.minByOrNull { gridHandler.distBetween(this, it.x, it.y) }
    }

    override fun move(cellToMove: Cell?) {
        if (cellToMove == null) {
            hadAvailableCells = false
            return
        }
        cellToMove.visited = true
        x = cellToMove.x
        y = cellToMove.y
    }
}