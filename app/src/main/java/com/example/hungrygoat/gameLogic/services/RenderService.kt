package com.example.hungrygoat.gameLogic.services

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import com.example.hungrygoat.constants.appContants.GameSettings
import com.example.hungrygoat.constants.enums.GameObjectTags
import com.example.hungrygoat.gameLogic.game.grid.GridHandler
import com.example.hungrygoat.gameLogic.gameObjects.RulerSet
import com.example.hungrygoat.gameLogic.gameObjects.abstractObjects.GameObject
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Cell
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Dog
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.Goat
import com.example.hungrygoat.gameLogic.gameObjects.inheritedObject.rope.Rope
import com.example.hungrygoat.gameLogic.interfaces.goatListeners.GoatBoundsTouchEdgesListener
import com.example.hungrygoat.gameLogic.services.solution.SolutionUtility

class RenderService {

    private var goatBoundsTouchEdgesListener: GoatBoundsTouchEdgesListener? =
        GoatBoundsTouchEdgesListener {}

    fun registerGoatBoundsTouchEdgesListener(goatTouchEdges: GoatBoundsTouchEdgesListener) {
        goatBoundsTouchEdgesListener = goatTouchEdges
    }

    fun unregisterGoatBoundTouchEdgesListener() {
        goatBoundsTouchEdgesListener = null
    }

    private val backgroundColor = Color.rgb(34, 177, 76) // 0 100 0
    private val goatVisitedColor = Color.rgb(181, 230, 29) // 0 255 0

    private val STROKE_WIDTH = 12f
    private val paint = setPaint()
    private val rulerPaint: Paint = setPaint().apply {
        color = Color.BLACK
        strokeWidth = 10f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }
    private val rulerTextPaint = setPaint().apply {
        textSize = 95f
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = STROKE_WIDTH / 2
        typeface = Typeface.MONOSPACE
        color = Color.RED
    }
    private val dogBoundsPaint = setPaint().apply {
        strokeWidth = 4f
        color = Color.LTGRAY
        style = Paint.Style.STROKE
    }
    private val goatBoundsPaint = setPaint().apply {
        strokeWidth = 4f
        color = Color.BLUE
        style = Paint.Style.STROKE
    }
    private val goatPathPaint = Paint().apply {
        strokeWidth = .5f
        color = goatVisitedColor
        style = Paint.Style.FILL_AND_STROKE
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 80f
        style = Paint.Style.FILL_AND_STROKE
    }

    fun render(
        canvas: Canvas,
        gridHandler: GridHandler,
        objects: List<GameObject>,
        ropes: List<Rope>,
        tempWhileMove: GameObject?,
        ruller: List<RulerSet>,
        settings: GameSettings,
    ) {
        try {
            drawBackground(canvas)

            val dogs = objects.filter { it.gameObjectTag == GameObjectTags.DOG }.map { it as Dog }
            val goat = objects.find { it.gameObjectTag == GameObjectTags.GOAT } as Goat?

            if (settings.drawDogBounds)
                drawDogBounds(canvas, dogs)

            drawGoatPath(canvas, goat)
            if (settings.drawGoatBounds)
                drawGoatBounds(canvas, goat)

            if (settings.drawGrahamScanLines)
                goat?.bounds?.let { testGrahamScanStuff(canvas, it, gridHandler) }

            drawRuler(canvas, ruller)
            drawObjectAndRopes(canvas, objects, ropes, tempWhileMove)
        } catch (e: Exception) {
            Log.e("mytag", "RenderService.render() ${e.printStackTrace()}")
        }
    }


    private fun drawRuler(canvas: Canvas, ruller: List<RulerSet>) {
        ruller.forEach {
            val a = it.from
            val b = it.to
            val angle = "%.2f".format(it.angle)
            canvas.drawLine(a.x, a.y, b.x, b.y, rulerPaint)
            canvas.drawText("$angle°", (a.x + b.x) / 2, b.y + 10, rulerTextPaint)
        }
    }

    private fun drawBackground(canvas: Canvas) =
        canvas.drawColor(backgroundColor)

    private fun drawDogBounds(canvas: Canvas, dogs: List<Dog>) {
        dogs.forEach { dog ->
            dog.bounds.forEach {
                drawCell(canvas, dogBoundsPaint, it)
            }
        }
    }

    private fun drawGoatPath(canvas: Canvas, goat: Goat?) {
        if (goat == null || goat.path.isEmpty()) return
        val intersectionGoatPathWithGridEdges = goat.intersectionPathWithGridEdges

        for (i in 0 until goat.lastVisitedIndex) {
            val c = goat.path[i]
            drawCell(canvas, goatPathPaint, c)

            if (intersectionGoatPathWithGridEdges.contains(c))
                goatBoundsTouchEdgesListener?.onGoatBoundsTouchEdges()

        }
    }

    private fun drawGoatBounds(canvas: Canvas, goat: Goat?) {
        if (goat == null) return
        try {
            val bounds = goat.bounds
            if (bounds.isNotEmpty())
                for (b in bounds)
                    drawCell(canvas, goatBoundsPaint, b)
        } catch (e: Exception) {
            Log.e("mytag", "RenderService.drawGoatBounds() ${e.printStackTrace()}")
        }
    }


    private fun testGrahamScanStuff(canvas: Canvas, bounds: List<Cell>, gridHandler: GridHandler) {
        val utility = SolutionUtility()
        utility.setGridHadler(gridHandler)
        val cellSize = gridHandler.getGrid().cellSize

        val grah = utility.grahamScan(bounds, cellSize)
        val filtered = utility.filteredGrahamScan(grah)

        drawGrahamScan(canvas, grah, Color.CYAN, Color.YELLOW, Color.BLACK, null)
        drawGrahamScan(canvas, filtered, Color.RED, Color.YELLOW, Color.BLACK, Color.RED)
    }

    private fun drawGrahamScan(
        canvas: Canvas,
        cells: List<Cell>,
        lineColor: Int,
        pointsColor: Int,
        firstPointColor: Int,
        textColor: Int?
    ) {
        if (cells.isEmpty()) return
        var prev = cells.first()
        val p = Paint().apply {
            color = lineColor
            strokeWidth = 8f
        }
        for (i in 1 until cells.size) {
            canvas.drawLine(
                prev.x,
                prev.y,
                cells[i].x,
                cells[i].y,
                p
            )
            if (textColor != null)
                canvas.drawText(
                    "$i",
                    cells[i].x,
                    cells[i].y,
                    textPaint.apply { color = textColor }
                )
            prev = cells[i]
        }
        canvas.drawLine(
            prev.x,
            prev.y,
            cells.first().x,
            cells.first().y,
            p
        )
        if (textColor != null)
            canvas.drawText(
                "0",
                cells[0].x,
                cells[0].y,
                textPaint.apply { color = textColor }
            )

        cells.forEach {
            drawCell(canvas, p.apply { color = pointsColor }, it)
        }
        drawCell(canvas, p.apply { color = firstPointColor }, cells.first())
    }

    private fun drawObjectAndRopes(
        canvas: Canvas, objects: List<GameObject>, ropes: List<Rope>, tempWhileMove: GameObject?
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

    private fun drawCell(canvas: Canvas, paint: Paint, cell: Cell?) {
        if (cell == null) return
        cell.draw(canvas, paint)
    }

    private fun setPaint() = Paint().apply {
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.STROKE
    }
}