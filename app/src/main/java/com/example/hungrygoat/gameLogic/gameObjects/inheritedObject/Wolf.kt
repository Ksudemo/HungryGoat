package com.example.hungrygoat.gameLogic.gameObjects.inheritedObject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.MovableGameObject
import com.example.hungrygoat.gameLogic.services.GridHandler


class Wolf(x: Float, y: Float, tag: GameObjectTags) :
    MovableGameObject(x, y, tag) {

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
        val availableCells = bounds - visited.toSet()

        gridHandler.getObjectCell(this)

        return availableCells.minByOrNull {
            gridHandler.distBetween(
                this,
                it.x,
                it.y
            )
        }
    }

    private fun move(cellToMove: Cell?) {
        if (cellToMove == null) return
        x = cellToMove.x
        y = cellToMove.y

        visited.add(cellToMove)

        if (visited.size == bounds.size)
            visited.clear()
    }
}