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
        gridHandler: GridHandler,
        objects: List<GameObject>,
        ropes: List<Rope>,
        tempWhileMove: GameObject?,
        ruller: List<List<Any>>,
        settings: GameSettings,
    ) {
        try {
            drawBackground(canvas)

            val dog = objects.find { it.gameObjectTag == GameObjectTags.DOG } as Dog?
            val goat = objects.find { it.gameObjectTag == GameObjectTags.GOAT } as Goat?

            if (settings.drawCellIndex)
                drawCellIndex(canvas, gridHandler.getGrid())

            if (settings.drawDogBounds)
                drawDogBounds(canvas, dog)

            drawGoatPath(canvas, goat)
            if (settings.drawGoatBounds)
                drawGoatBounds(canvas, gridHandler, goat)

            if (settings.drawRopeNodes)
                drawRopeNodes(canvas, ropes, paint)

            drawRuler(canvas, ruller)
            drawObjectAndRopes(canvas, objects, ropes, tempWhileMove)

        } catch (e: Exception) {
            Log.e("mytag", "RenderService.render() ${e.printStackTrace()}")
        }
    }


    private fun drawRuler(canvas: Canvas, ruller: List<List<Any>>) {
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

    private fun drawDogBounds(canvas: Canvas, dog: Dog?) {
        if (dog == null) return
        dog.bounds.forEach {
            drawCell(canvas, rectPaint.apply {
                color = Color.LTGRAY
                style = Paint.Style.FILL_AND_STROKE
            }, it)
        }
    }

    private fun drawGoatPath(canvas: Canvas, goat: Goat?) {
        if (goat != null && goat.path.isNotEmpty())
            for (i in 0 until goat.lastVisitedIndex) {
                drawCell(canvas, rectPaint.apply {
                    color = goatVisitedColor
                    style = Paint.Style.FILL_AND_STROKE
                }, goat.path[i])
            }
    }

    private fun drawGoatBounds(canvas: Canvas, gridHandler: GridHandler, goat: Goat?) {
        if (goat == null) return
        val linePaint = rectPaint.apply {
            color = Color.MAGENTA
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

//        TODO remove screenwidth and height variables before th release
        val cellSize = gridHandler.cellSize
        val numRows = gridHandler.numRows
        val numCols = gridHandler.numColumns

        val screenHeight = cellSize * numRows
        val screenWidth = cellSize * numCols

        try {
            if (goat.bounds.isNotEmpty()) {
//                TODO Remove all that stuff from here
//                val cx = bounds.map { it.x }.average().toFloat()
//                val cy = bounds.map { it.y }.average().toFloat()
//                val r = bounds.map { sqrt((it.x - cx).pow(2) + (it.y - cy).pow(2)) }.average()
//                    .toFloat() + 10

                val minX = goat.bounds.minOfOrNull { it.x } ?: 0f
                val minY = goat.bounds.minOfOrNull { it.y } ?: 0f
                val maxX = goat.bounds.maxOfOrNull { it.x } ?: 0f
                val maxY = goat.bounds.maxOfOrNull { it.y } ?: 0f

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
//                TODO To here

                goat.bounds.forEach {
                    drawCell(canvas, cell = it, paint = linePaint.apply { color = Color.BLUE })
                }
                //Draw the first cell
                drawCell(
                    canvas,
                    cell = goat.bounds.first(),
                    paint = linePaint.apply { color = Color.RED }
                )
            }
        } catch (e: Exception) {
            Log.e("mytag", "RenderService.drawGoatBounds() ${e.printStackTrace()}")
        }
    }

    private fun drawObjectAndRopes(
        canvas: Canvas, objects: List<GameObject>, ropes: List<Rope>, tempWhileMove: GameObject?,
    ) {
        try {
            ropes.forEach { it.draw(canvas, paint) }

            objects.forEach { it.draw(canvas, paint) }

            tempWhileMove?.draw(canvas, paint)
            objects.find { it.isTempOnRopeSet }?.drawBase(canvas, paint)

        } catch (e: Exception) {
            Log.e("mytag", "RenderService.drawObjectAndRopes() ${e.printStackTrace()}")
        }
    }

    private fun drawRopeNodes(canvas: Canvas, ropes: List<Rope>, paint: Paint) =
        try {
            ropes.forEach { rope ->
                rope.ropePath.forEach {
                    it.draw(canvas, paint)
                }
            }
        } catch (e: Exception) {
            Log.e("mytag", "RenderService.drawRopeNodes() ${e.printStackTrace()}")
        }

    private fun drawCellIndex(canvas: Canvas, grid: Array<Array<Cell>>) {
        for (i in grid.indices)
            for (j in grid[i].indices) {
                drawCell(
                    canvas,
                    rectPaint.apply { style = Paint.Style.STROKE },
                    grid[i][j]
                )

                canvas.drawText(
                    "$i ; $j",
                    grid[i][j].x - 60,
                    grid[i][j].y + 10,
                    paint.apply {
                        color = Color.BLACK
                        textSize = 80f
                        style = Paint.Style.STROKE
                    })

            }
    }

    private fun drawCell(canvas: Canvas, paint: Paint, cell: Cell) =
        canvas.drawRect(cell.rect, paint)


    private fun setPaint() = Paint().apply {
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.STROKE
    }
}