package com.example.hungrygoat.gameLogic.services

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.constants.GameSettings
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Rope
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Wolf
import kotlin.math.pow
import kotlin.math.sqrt

class RenderService {

    private val backgroundColor = Color.rgb(0, 100, 0)
    private val goatVisitedColor = Color.rgb(0, 255, 0)

    private val STROKE_WIDTH = 12f
    private val paint = setPaint()
    private val rectPaint = setPaint().apply {
        color = Color.BLACK
        strokeWidth = .5f
        style = Paint.Style.STROKE
    }

    fun render(
        canvas: Canvas,
        grid: List<Cell>,
        objects: MutableList<GameObject>,
        ropes: MutableList<Rope>,
        goatVisited: MutableList<Cell>?,
        setttings: GameSettings,

        ) {
        drawBackground(canvas)

        if (setttings.drawCellIndex)
            drawCellIndex(grid, canvas)

        if (setttings.drawWolfBounds)
            drawWolfBounds(canvas, objects)


        try {
            goatVisited?.forEach {
                drawCell(canvas, rectPaint.apply {
                    color = goatVisitedColor
                    style = Paint.Style.FILL_AND_STROKE
                }, it)
            }
        } catch (e: Exception) {
            Log.e("MyTag", e.toString())
        }

        if (setttings.drawRopeNodes)
            drawRopeNodes(ropes, canvas, paint)

        drawObjectAndRopes(objects, ropes, canvas)

        if (setttings.drawGoatBounds)
            drawGoatBounds(canvas, objects)
    }


    private fun drawBackground(canvas: Canvas) =
        canvas.drawColor(backgroundColor)

    private fun drawWolfBounds(canvas: Canvas, objects: MutableList<GameObject>) {
        val wolf = objects.find { it.gameObjectTag == GameObjectTags.WOLF } as Wolf?
        wolf?.bounds?.forEach {
            drawCell(canvas, rectPaint.apply {
                color = Color.LTGRAY
                style = Paint.Style.FILL_AND_STROKE
            }, it)
        }
    }

    private fun drawGoatBounds(canvas: Canvas, objects: MutableList<GameObject>) {
        val goat = objects.find { it.gameObjectTag == GameObjectTags.GOAT } as Goat?

        val linePaint = rectPaint.apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        try {
            if (goat?.bounds != null) {
//                for (i in 0 until goat.bounds.size - 1) {
//                    val cur = goat.bounds[i]
//                    val next = goat.bounds[i + 1]
//
//                    canvas.drawLine(cur.x, cur.y, next.x, next.y, linePaint)
//                }
//                canvas.drawLine(
//                    goat.bounds.last().x,
//                    goat.bounds.last().y,
//                    goat.bounds.first().x,
//                    goat.bounds.first().y, linePaint
//                )
                val cx = goat.bounds.map { it.x }.average().toFloat()
                val cy = goat.bounds.map { it.y }.average().toFloat()

                val first = goat.bounds.first()
                val r = sqrt((first.x - cx).pow(2) + (first.y - cy).pow(2))

                canvas.drawCircle(cx, cy, r, linePaint)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun drawObjectAndRopes(
        objects: MutableList<GameObject>,
        ropes: MutableList<Rope>, canvas: Canvas,
    ) {
        try {
            ropes.forEach { it.draw(canvas, paint) }

            objects.forEach { it.drawBase(canvas, paint) }
            objects.find { it.isSelected }?.drawBase(canvas, paint)
        } catch (e: Exception) {
            Log.e("MyTag", e.toString())
        }
    }


    private fun drawRopeNodes(ropes: MutableList<Rope>, canvas: Canvas, paint: Paint) =
        try {

            ropes.forEach { rope ->
                rope.ropePath.forEach {
                    it.draw(canvas, paint)
                }
            }
        } catch (e: Exception) {
            Log.e("MyTag", e.toString())
        }


    private fun drawCellIndex(grid: List<Cell>, canvas: Canvas) {
        for (i in grid.indices) {
            drawCell(
                canvas,
                rectPaint.apply { style = Paint.Style.STROKE },
                grid[i]
            )

//            canvas.drawText(
//                "$i",
//                grid[i].x,
//                grid[i].y,
//                paint.apply {
//                    color = Color.BLACK
//                    textSize = 80f
//                    style = Paint.Style.STROKE
//                })
        }
    }

    private fun drawCell(canvas: Canvas, paint: Paint, cell: Cell) {
        canvas.drawRect(cell.rect, paint)
//        canvas.drawCircle(cell.x, cell.y, 20f, paint)
    }

    private fun setPaint() = Paint().apply {
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.STROKE
    }
}