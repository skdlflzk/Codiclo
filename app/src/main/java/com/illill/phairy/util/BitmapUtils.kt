package com.illill.phairy.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log

class BitmapUtils {

    val TAG = this@BitmapUtils::class.java.simpleName
    /**
     * ByteArray를 받아서 500x500의 비트맵을 리턴.
     */
    fun makeSquareFromByteArray(bitmapByte: ByteArray?): Bitmap {
        if (bitmapByte == null || bitmapByte.isEmpty()) return Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.size, options)
        val wPer500 = options.outWidth.toFloat() / 500.0F
        val hPer500 = options.outHeight.toFloat() / 500.0F
        val ratio = hPer500 / wPer500  // 종횡비
        Log.d(TAG, "makeSquareFromByteArray: w = ${options.outWidth}, h = ${options.outHeight}")
        Log.d(TAG, "makeSquareFromByteArray: ratio = ${ratio}}")
        options.inSampleSize = Math.min(wPer500, hPer500).toInt()    //둘중 더 짧은 비율로 실제 비트맵으로 출력
        options.inJustDecodeBounds = false
        val output = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.size, options)
        val scaled = if (wPer500 > hPer500) Bitmap.createScaledBitmap(output, (500 * (1.0F / ratio)).toInt(), 500, true)    //가로가 길다면  w = 500 * h/w, h=500
        else Bitmap.createScaledBitmap(output, 500, (500 * ratio).toInt(), true)      //세로가 길다면 w= 500, h = 500*h/w

//        output.recycle()
//        Log.d(TAG, "makeSquareFromByteArray: cutting under = ${(500 * ratio).toInt()/2 - 250}}")
//        Log.d(TAG, "makeSquareFromByteArray: cutting under = ${(500 * (1.0F/ratio)).toInt()/2 - 250}}")

        val croped = if (wPer500 > hPer500) Bitmap.createBitmap(scaled, (500 * (1.0F / ratio)).toInt() / 2 - 250, 0, 500, 500)    //가로가 길다면  w = 500 * h/w, h=500
                                  else Bitmap.createBitmap(scaled, 0, (500 * ratio).toInt() / 2 - 250, 500, 500)        //세로가 길다면 w= 500, h = 500*h/w
//        scaled.recycle()

        return if (wPer500 > hPer500)    //가로가 길면? == 기기가 옆으로 누웠다고 가정.시계 방향으로 90도 회전시키자
            rotateBitmap(croped, 90)
        else
            croped
    }

    fun rotateBitmap(bitmap: Bitmap, angle: Int): Bitmap {

        val rotationMatrix = Matrix()
        rotationMatrix.postRotate(angle.toFloat())

        val rotated = Bitmap.createBitmap(bitmap, 0, 0, 500, 500, rotationMatrix, true)
//        bitmap.recycle()
        return rotated
    }
}