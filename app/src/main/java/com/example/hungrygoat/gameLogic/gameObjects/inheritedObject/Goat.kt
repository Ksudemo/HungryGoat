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
            val cellToMove = getNextCellToMove(gridHandler, dogObj)
            move(cellToMove)
//            visited.addAll(path)
//            hadAvailableCells = false
            true
        } else
            false

    private fun getNextCellToMove(gridHandler: GridHandler, dogObj: Dog?): Cell? {
        if (path.isNotEmpty())
            return (path - visited.toSet()).firstOrNull()

        val dogReachedSet = dogObj?.reachedSet ?: emptySet()
        val availableCells = reachedSet - (dogReachedSet + visited)

        gridHandler.getObjectCell(this)

        path = availableCells.sortedBy { gridHandler.distBetween(this, it.x, it.y) }.toSet()

        val cellToMove = path.firstOrNull()
        hadAvailableCells = cellToMove != null
        return cellToMove
    }

    override fun move(cellToMove: Cell?) {
        if (cellToMove == null) {
            hadAvailableCells = false
            return
        }

        x = cellToMove.x
        y = cellToMove.y

        visited.add(cellToMove)
    }
}