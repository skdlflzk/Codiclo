package com.illill.phairy.ui.common

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.collection.LruCache
import java.util.*

class CutView(c: Context, bm: Bitmap,val  listener: ImageCropListener) : View(c), View.OnTouchListener {
    val CACHE_KEY = "bitmap"
    private var flgPathDraw = true
    private var bFirstPoint = false

    private var firstPoint: Point? = null
    private var lastPoint: Point? = null

    private val frameRect: Rect

    private val originalImageBitmap: Bitmap = bm
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0

    private var paint: Paint

    private var points: MutableList<Point> = ArrayList()
    private var mMemoryCache: LruCache<String, Bitmap>

    val bitmapFromMemCache: Bitmap?
        get() = mMemoryCache.get(CACHE_KEY)

    interface ImageCropListener {
        fun onClickDialogPositiveButton()
        fun onClickDialogNegativeButton()
    }

    init{

        isFocusable = true
        isFocusableInTouchMode = true
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        paint.pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0f)
        paint.strokeWidth = 5f
        paint.color = Color.WHITE

        val screenWidth = c.resources.displayMetrics.widthPixels

        val ratio = bm.height.toFloat() / bm.width.toFloat()

        frameRect = if (bm.width > bm.height) {
            Log.d("init", ": w>h")
            Rect(0, (screenWidth * (0.5f - ratio / 2f)).toInt(), screenWidth, (screenWidth * (0.5f + ratio / 2f)).toInt())
        } else {
            Log.d("init", ": w<=h")
            Rect((screenWidth/2f - bm.height.toFloat()/2f/ratio).toInt(), 0, (screenWidth/2f + bm.height.toFloat()/2f/ratio).toInt(), screenWidth)
        }


        bFirstPoint = false
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        mMemoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.byteCount / 1024
            }
        }
        this.setOnTouchListener(this)
    }

    private fun addBitmapToMemoryCache(bitmap: Bitmap) {
        if (bitmapFromMemCache == null) {
            mMemoryCache.put(CACHE_KEY, bitmap)
        }
    }

    private fun calcBitmapScale(canvasWidth: Int, canvasHeight: Int, bmpWidth: Int, bmpHeight: Int): Float {

        var scale = canvasWidth.toFloat() / bmpWidth.toFloat()
        val tmp = bmpHeight * scale

        if (tmp < canvasHeight) {
            scale = canvasHeight.toFloat() / bmpHeight.toFloat()
            return scale
        }

        return scale
    }

    public override fun onDraw(canvas: Canvas) {
        canvasWidth = canvas.width
        canvasHeight = canvas.height

        /* int bmpWidth = this.originalImageBitmap.getWidth();
    int bmpHeight = this.originalImageBitmap.getHeight();


    float toCanvasScale = this.calcBitmapScale(canvasWidth, canvasHeight, bmpWidth, bmpHeight);


    float diffX = (bmpWidth * toCanvasScale - canvasWidth);
    float diffY = (bmpHeight * toCanvasScale - canvasHeight);


    float addX = (diffX / toCanvasScale) / 2;
    float addY = (diffY / toCanvasScale) / 2;


    Rect rSrc = new Rect((int)addX, (int)addY,
            (int)((canvasWidth / toCanvasScale) + addX), (int)((canvasHeight /
    toCanvasScale) + addY));
    RectF rDest = new RectF(0, 0, canvasWidth, canvasHeight);
    */

        canvas.drawBitmap(originalImageBitmap, null, frameRect, null)

        val cropAreaPath = Path()
        var isFirstPoint = true

        var i = 0
        while (i < points.size) {
            val point = points[i]
            if (isFirstPoint) {
                isFirstPoint = false
                // 最初の処理でPathのx,y座標をpointの座標に移動する
                cropAreaPath.moveTo(point.x, point.y)
            } else if (i < points.size - 1) {
                val next = points[i + 1]
                cropAreaPath.quadTo(point.x, point.y, next.x, next.y)
            } else {
                lastPoint = points[i]
                cropAreaPath.lineTo(point.x, point.y)
            }
            i += 2
        }
        canvas.drawPath(cropAreaPath, paint)
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val point = Point()
        point.x = event.x.toInt().toFloat()
        point.y = event.y.toInt().toFloat()

        if (flgPathDraw) {
            if (bFirstPoint) {
                if (comparePoint(firstPoint!!, point)) {
                    // points.add(point);
                    points.add(firstPoint!!)
                    flgPathDraw = false
                    showCropDialog()
                } else {
                    points.add(point)
                }
            } else {
                points.add(point)
            }

            if (!bFirstPoint) {
                firstPoint = point
                bFirstPoint = true
            }
        }

        invalidate()

        if (event.action == MotionEvent.ACTION_UP) {
            Log.d("Action up***>", "called")
            lastPoint = point
            if (flgPathDraw) {
                if (points.size > 12) {
                    if (!comparePoint(firstPoint!!, lastPoint!!)) {
                        flgPathDraw = false
                        points.add(firstPoint!!)
                        showCropDialog()
                    }
                }
            }
        }

        return true
    }

    private fun comparePoint(first: Point, current: Point): Boolean {
        val left_range_x = (current.x - 3).toInt()
        val left_range_y = (current.y - 3).toInt()

        val right_range_x = (current.x + 3).toInt()
        val right_range_y = (current.y + 3).toInt()

        return if (left_range_x < first.x && first.x < right_range_x && left_range_y < first.y && first.y < right_range_y) {
            points.size >= 10
        } else {
            false
        }

    }

    fun fillinPartofPath() {
        val point = Point()
        point.x = points[0].x
        point.y = points[0].y

        points.add(point)
        invalidate()
    }

    fun resetView() {
        points.clear()
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        flgPathDraw = true
        invalidate()
    }

    private fun showCropDialog() {
        val croppedImage = cropImage(this.originalImageBitmap)

        addBitmapToMemoryCache(croppedImage)
        listener.onClickDialogPositiveButton()
        //        DialogInterface.OnClickListener dialogClickListener = new
        //                DialogInterface.OnClickListener() {
        //                    @Override
        //                    public void onClick(DialogInterface dialog, int which) {
        //                        switch (which) {
        //                            case DialogInterface.BUTTON_POSITIVE:
        //                                addBitmapToMemoryCache(croppedImage);
        //                                imageCropListener.onClickDialogPositiveButton();
        //                                break;
        //
        //                            case DialogInterface.BUTTON_NEGATIVE:
        //                                bFirstPoint = false;
        //                                resetView();
        //                                imageCropListener.onClickDialogNegativeButton();
        //                                break;
        //                        }
        //                    }
        //                };
        //
        //        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Dialog_Alert);
        //        builder.setMessage("Do you Want to save Crop or Non-crop image?")
        //                .setPositiveButton("Crop", dialogClickListener)
        //                .setNegativeButton("Cancel", dialogClickListener).show()
        //                .setCancelable(false);
    }

    private fun cropImage(image: Bitmap): Bitmap {
        val cropImage = Bitmap.createBitmap(canvasWidth, canvasHeight,
                image.config)
        val canvas = Canvas(cropImage)
        val paint = Paint()
        paint.isAntiAlias = true

        val path = Path()
        for (i in points.indices) {
            path.lineTo(points[i].x, points[i].y)
        }
        canvas.drawPath(path, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
//        drawBitmap(originalImageBitmap, null, frameRect, null)
        canvas.drawBitmap(originalImageBitmap, null,frameRect, paint)

        return cropImage
    }

    inner class Point {
//        var dy: Float = 0.toFloat()
//        var dx: Float = 0.toFloat()
        var x: Float = 0.toFloat()
        var y: Float = 0.toFloat()

        override fun toString(): String {
            return "$x, $y"
        }
    }
}