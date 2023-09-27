package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.MovableGameObject
import com.example.hungrygoat.gameLogic.services.GridHandler

class Goat(vx: Float, vy: Float, tag: GameObjectTags) :
    MovableGameObject(vx, vy, tag) {

    val color = Color.YELLOW
    override fun draw(canvas: Canvas, paint: Paint) {
        paint.color = color
        canvas.drawCircle(x, y, circleRadius, paint)
    }

    override fun update(gridHandler: GridHandler, wolfObj: Wolf?) {
        if (hadAvailableCells) {
            val cellToMove = getNextCellToMove(gridHandler, wolfObj)
            move(cellToMove)
            visited.addAll(path)
            hadAvailableCells = false
        }
    }


    private fun getNextCellToMove(gridHandler: GridHandler, wolfObj: Wolf?): Cell? {
        if (path.isNotEmpty())
            return (path - visited.toSet()).firstOrNull()

        println("get nextCell to move")
        val wolfReachedSet = wolfObj?.reachedSet ?: emptySet()
        val availableCells = reachedSet - (wolfReachedSet + visited)

        val goatCell = gridHandler.getObjectCell(this)

        path = availableCells
//            availableCells.sortedBy { gridHandler.getDistanceBetweenCells(goatCell, it) }

        val cellToMove = path.firstOrNull()
        hadAvailableCells = cellToMove != null

        return cellToMove
    }

    private fun move(cellToMove: Cell?) {
        if (cellToMove == null) return
        x = cellToMove.x
        y = cellToMove.y

        visited.add(cellToMove)
    }
}