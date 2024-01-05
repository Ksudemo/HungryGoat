package com.example.hungrygoat.gameLogic.services

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.Log
import com.example.hungrygoat.constants.GameObjectTags
import com.example.hungrygoat.constants.GameSettings
import com.example.hungrygoat.gameLogic.game.Cell
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Rope
import kotlin.math.abs
import kotlin.math.max

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
        objects: MutableList<GameObject>, tempObject: GameObject?,
        cellSize: Float, numRows: Int, numCols: Int,
        ropes: MutableList<Rope>,
        ruller: MutableList<List<Any>>,
        goatVisited: MutableList<Cell>?,
        settings: GameSettings,
    ) {
        try {
            drawBackground(canvas)

            if (settings.drawCellIndex)
                drawCellIndex(grid, canvas)

            if (settings.drawDogBounds)
                drawDogBounds(canvas, objects)

            goatVisited?.forEach {
                drawCell(canvas, rectPaint.apply {
                    color = goatVisitedColor
                    style = Paint.Style.FILL_AND_STROKE
                }, it)
            }

            if (settings.drawRopeNodes)
                drawRopeNodes(ropes, canvas, paint)

            drawRuler(ruller, canvas)
            drawObjectAndRopes(objects, tempObject, ropes, canvas)

            if (settings.drawGoatBounds)
                drawGoatBounds(
                    canvas,
                    objects.find { it.gameObjectTag == GameObjectTags.GOAT } as Goat?,
                    cellSize, numRows, numCols
                )

        } catch (e: Exception) {
            Log.e("MyTag", e.toString())
        }
    }


    private fun drawRuler(
        ruller: MutableList<List<Any>>,
        canvas: Canvas,
    ) {
        val rulerPaint: Paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 10f
            pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }


        ruller.forEach {
            val a = it[0] as GameObject
            val b = it[1] as GameObject
            val angle = "%.2f".format(it[2] as Double)
            canvas.drawLine(a.x, a.y, b.x, b.y, rulerPaint)
            canvas.drawText("$angle°", (a.x + b.x) / 2, b.y + 10, paint.apply {
                textSize = 100f
                color = Color.RED
            })
        }
    }

    private fun drawBackground(canvas: Canvas) =
        canvas.drawColor(backgroundColor)

    private fun drawDogBounds(canvas: Canvas, objects: MutableList<GameObject>) {
        val dog = objects.find { it.gameObjectTag == GameObjectTags.DOG } as Dog?
        dog?.bounds?.forEach {
            drawCell(canvas, rectPaint.apply {
                color = Color.LTGRAY
                style = Paint.Style.FILL_AND_STROKE
            }, it)
        }
    }

    private fun drawGoatBounds(
        canvas: Canvas,
        goat: Goat?,
        cellSize: Float,
        numRows: Int,
        numCols: Int,
    ) {
        val linePaint = rectPaint.apply {
            color = Color.MAGENTA
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        val screenHeight = cellSize * numRows
        val screenWidth = cellSize * numCols
        val bounds = goat?.bounds// goat?.bounds2 // TODO Should be bounds not bounds2
        try {
            if (!bounds.isNullOrEmpty()) {

//                val cx = bounds.map { it.x }.average().toFloat()
//                val cy = bounds.map { it.y }.average().toFloat()
//                val r = bounds.map { sqrt((it.x - cx).pow(2) + (it.y - cy).pow(2)) }.average()
//                    .toFloat() + 10

                val minX = bounds.minOfOrNull { it.x } ?: 0f
                val minY = bounds.minOfOrNull { it.y } ?: 0f
                val maxX = bounds.maxOfOrNull { it.x } ?: 0f
                val maxY = bounds.maxOfOrNull { it.y } ?: 0f

                var cx = (minX + maxX) / 2
                var cy = (minY + maxY) / 2
                val r = max(abs((maxY - minY) / 2), abs((maxX - minX) / 2))

                cx = when {
                    cx - r < 0 -> r
                    cx + r > screenWidth -> screenWidth - r
                    else -> cx
                }
                cy = when {
                    cy - r < 0 -> r
                    cy + r > screenHeight -> screenHeight - r
                    else -> cy
                }

                canvas.drawCircle(cx, cy, r, linePaint.apply { color = Color.WHITE })

                canvas.drawRect(minX, maxY, maxX, minY, linePaint.apply { color = Color.RED })
                bounds.forEach {
                    drawCell(canvas, cell = it, paint = linePaint.apply { color = Color.BLUE })
                }

                //Draw the first cell
                drawCell(
                    canvas,
                    cell = bounds.first(),
                    paint = linePaint.apply { color = Color.RED }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun drawObjectAndRopes(
        objects: MutableList<GameObject>, tempObject: GameObject?,
        ropes: MutableList<Rope>, canvas: Canvas,
    ) {
        try {
            ropes.forEach { it.draw(canvas, paint) }

            objects.forEach { it.draw(canvas, paint) }
            tempObject?.draw(canvas, paint)

            objects.find { it.isTempOnRopeSet }?.drawBase(canvas, paint)

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