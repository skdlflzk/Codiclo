package com.illill.phairy.ui.common

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.illill.phairy.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class CropWindow(c: Context, bm: Bitmap) : View(c), View.OnTouchListener {
    private var firstPoint: Point = Point(0, 0)

    private val originalImageBitmap: Bitmap = bm
    private val drawable: BitmapDrawable

    private val paintW: Paint
    private val paintC: Paint
    private val paintCA: Paint

    private val cropRect: Rect
    private val frameRect: Rect
//    private val bitmapRect: Rect

    private val screenWidth: Int
    private val handleRadius = 30f
    private var outerColor: Int = 0
    private var outerColorOn: Int = 0


    private var drawHelper = false
    private var drawMode = -1

    init {
        isFocusable = true
        isFocusableInTouchMode = true

        drawable = BitmapDrawable(c.resources, bm)
//        adjustViewBounds = true
//        setImageBitmap(bm)

        paintW = Paint(Paint.ANTI_ALIAS_FLAG)
        paintW.style = Paint.Style.STROKE
        paintW.strokeWidth = 5f
        paintW.color = Color.WHITE
        paintC = Paint(Paint.ANTI_ALIAS_FLAG)
        paintC.color = ContextCompat.getColor(c, R.color.colorAccent)
        paintCA = Paint(Paint.ANTI_ALIAS_FLAG)
        paintCA.color = ContextCompat.getColor(c, R.color.colorAccentOn)

        screenWidth = c.resources.displayMetrics.widthPixels

        val ratio = bm.height.toFloat() / bm.width.toFloat()
        frameRect = if (bm.width > bm.height) {
            Log.d("init", ": w>h")
            Rect(0, (screenWidth * (0.5f - ratio / 2f)).toInt(), screenWidth, (screenWidth * (0.5f + ratio / 2f)).toInt())
        } else {
            Log.d("init", ": w<=h")
            Rect((screenWidth/2f - bm.height.toFloat()/2f/ratio).toInt(), 0, (screenWidth/2f + bm.height.toFloat()/2f/ratio).toInt(), screenWidth)
        }
        cropRect = Rect(frameRect)
        Log.d("init", "init:cropRect = $cropRect ")
        Log.d("init", "init:frameRect = $frameRect ")
//        bitmapRect = Rect(0, 0, bm.width, bm.height)

        outerColor = ContextCompat.getColor(c, R.color.outside)
        outerColorOn = ContextCompat.getColor(c, R.color.outsideOn)

        this.setOnTouchListener(this)

    }

    public override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(originalImageBitmap, null, frameRect, null)

        //draw Rect, 살짝 두껍게
        paintW.strokeWidth = 5f

        canvas.drawLine(cropRect.left.toFloat(), cropRect.top.toFloat(), cropRect.right.toFloat(), cropRect.top.toFloat(), paintW)
        canvas.drawLine(cropRect.right.toFloat(), cropRect.top.toFloat(), cropRect.right.toFloat(), cropRect.bottom.toFloat(), paintW)
        canvas.drawLine(cropRect.right.toFloat(), cropRect.bottom.toFloat(), cropRect.left.toFloat(), cropRect.bottom.toFloat(), paintW)
        canvas.drawLine(cropRect.left.toFloat(), cropRect.bottom.toFloat(), cropRect.left.toFloat(), cropRect.top.toFloat(), paintW)

        //보조선 긋기, 살짝 얇게
        if (drawHelper) {
            paintW.strokeWidth = 2f

            //세로 2줄
            canvas.drawLine((2 * cropRect.left + cropRect.right) / 3f, cropRect.top.toFloat(), (2 * cropRect.left + cropRect.right) / 3f, cropRect.bottom.toFloat(), paintW)
            canvas.drawLine((cropRect.left + 2 * cropRect.right) / 3f, cropRect.top.toFloat(), (cropRect.left + 2 * cropRect.right) / 3f, cropRect.bottom.toFloat(), paintW)

            //가로 2줄
            canvas.drawLine(cropRect.left.toFloat(), (2 * cropRect.top + cropRect.bottom) / 3f, cropRect.right.toFloat(), (2 * cropRect.top + cropRect.bottom) / 3f, paintW)
            canvas.drawLine(cropRect.left.toFloat(), (cropRect.top + 2 * cropRect.bottom) / 3f, cropRect.right.toFloat(), (cropRect.top + 2 * cropRect.bottom) / 3f, paintW)
        }

        canvas.clipRect(cropRect, Region.Op.DIFFERENCE)
        canvas.drawColor(if (drawMode in 0..3) outerColorOn else outerColor)


        //draw Handle
        canvas.drawCircle(cropRect.left.toFloat(), cropRect.top.toFloat(), handleRadius, paintC)
        canvas.drawCircle(cropRect.right.toFloat(), cropRect.top.toFloat(), handleRadius, paintC)
        canvas.drawCircle(cropRect.right.toFloat(), cropRect.bottom.toFloat(), handleRadius, paintC)
        canvas.drawCircle(cropRect.left.toFloat(), cropRect.bottom.toFloat(), handleRadius, paintC)

        when (drawMode) {
            0 -> canvas.drawCircle(cropRect.left.toFloat(), cropRect.top.toFloat(), handleRadius * 1.5f, paintCA)
            1 -> canvas.drawCircle(cropRect.right.toFloat(), cropRect.top.toFloat(), handleRadius * 1.5f, paintCA)
            2 -> canvas.drawCircle(cropRect.right.toFloat(), cropRect.bottom.toFloat(), handleRadius * 1.5f, paintCA)
            3 -> canvas.drawCircle(cropRect.left.toFloat(), cropRect.bottom.toFloat(), handleRadius * 1.5f, paintCA)
        }


    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val point = Point()
        point.x = event.x.toInt()
        point.y = event.y.toInt()

        if (event.action == MotionEvent.ACTION_DOWN) {
            drawMode = inJoint(point)
            firstPoint = point
            drawHelper = true
        } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            drawHelper = false
            drawMode = -1
        }
        val dx = point.x - firstPoint.x
        val dy = point.y - firstPoint.y
        when (drawMode) {
            0 -> {
                cropRect.left = max(cropRect.left + dx, frameRect.left)
                cropRect.top = max(cropRect.top + dy, frameRect.top)
                firstPoint = point
            }
            1 -> {
                cropRect.right = min(cropRect.right + dx, frameRect.right)
                cropRect.top = max(cropRect.top + dy, frameRect.top)
                firstPoint = point
            }
            2 -> {
                cropRect.right = min(cropRect.right + dx, frameRect.right)
                cropRect.bottom = min(cropRect.bottom + dy, frameRect.bottom)
                firstPoint = point
            }
            3 -> {
                cropRect.left = max(cropRect.left + dx, frameRect.left)
                cropRect.bottom = min(cropRect.bottom + dy, frameRect.bottom)
                firstPoint = point
            }
            else -> {



                //SEE XXX FIXME constains 벽에 붙었을때 안움직이는 문제

                Log.d("onTouch", "onTouch: dx = $dx, dy = $dy avalable =cropRect.getPoints().size = ${cropRect.getPoints().size} ${cropRect.getPoints().all {
                    Log.d("in", "onTouch: ${it.x}, ${it.y} frameRect.contains(it.x + dx, it.y + dx) = ${frameRect.contains(it.x + dx, it.y + dx)}") 
                    frameRect.contains(it.x + dx, it.y + dx) }
                },cropRect = $cropRect => frameRect = $frameRect")
                if (cropRect.getPoints().all { frameRect.contains(it.x + dx, it.y + dx) }) {
                    cropRect.offset((dx), (dy))
                    firstPoint = point
                }

            }
        }

        invalidate()

//        Log.d("onTouch", "onTouch: $cropRect, p $point")
//        Log.d("onTouch", "mode = $drawMode")
        return true
    }

    private fun inJoint(p: Point): Int {
        return when {
            sqrt((p.x - cropRect.left).pow() + (p.y - cropRect.top).pow()) < handleRadius * 3 -> 0
            sqrt((p.x - cropRect.right).pow() + (p.y - cropRect.top).pow()) < handleRadius * 3 -> 1
            sqrt((p.x - cropRect.right).pow() + (p.y - cropRect.bottom).pow()) < handleRadius * 3 -> 2
            sqrt((p.x - cropRect.left).pow() + (p.y - cropRect.bottom).pow()) < handleRadius * 3 -> 3
            else -> -1
        }
    }

    private fun Int.pow() = this * this
    private fun sqrt(i: Int) = sqrt(i.toFloat())


    private fun Rect.getPoints(): List<Point> {
        return listOf(Point(this.left, this.top), Point(this.right-1, this.top), Point(this.right-1, this.bottom-1), Point(this.left, this.bottom-1))
    }

    fun cropImage(): Bitmap {


        val xP = (cropRect.left - frameRect.left).toFloat() / frameRect.width().toFloat()
        val yP = (cropRect.top - frameRect.top).toFloat() / frameRect.height().toFloat()
        val widthP = cropRect.width().toFloat() / frameRect.width().toFloat()
        val heightP = cropRect.height().toFloat() / frameRect.height().toFloat()


//    val rate = (if (originalImageBitmap.width < originalImageBitmap.height) originalImageBitmap.width.toFloat()
//    else originalImageBitmap.height.toFloat())/ cropRect.width().toFloat()

        Log.d("cropImage", "cropImage:  ${originalImageBitmap.width} x ${originalImageBitmap.height} ")
        Log.d("cropImage", "cropImage: $cropRect , w = ${cropRect.width()} h== ${cropRect.height()}")
        return Bitmap.createBitmap(originalImageBitmap, (originalImageBitmap.width.toFloat() * xP).toInt(), (originalImageBitmap.height.toFloat() * yP).toInt(), min(originalImageBitmap.width, (originalImageBitmap.width.toFloat() * widthP).toInt()), min(originalImageBitmap.height, (originalImageBitmap.height.toFloat() * heightP).toInt()))
    }
}