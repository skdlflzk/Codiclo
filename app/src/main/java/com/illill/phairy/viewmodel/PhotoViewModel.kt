package com.illill.phairy.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.illill.phairy.BaseApplication
import com.illill.phairy.R
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.math.max


class PhotoViewModel() : ViewModel() {
    private val TAG = this.javaClass.simpleName

    private val _edited = MutableLiveData<Bitmap>()
    val edited: LiveData<Bitmap> get() = _edited

    private val _origin = MutableLiveData<Bitmap>()
    val origin: LiveData<Bitmap> get() = _origin

    private val _mode = MutableLiveData<String>().apply { value = "" }
    val mode: LiveData<String> get() = _mode

    //Codi에 업데이트할 비트맵 모음
    val bitmapList = MutableLiveData<List<Bitmap>>()

    var screen = 0

    fun setMode(m: String) {
        _mode.value = m

    }

    fun clearData(){
        _origin.value = null
    }

    fun loadOrigin(bitmap: Bitmap, context: Context) {
        _origin.value = bitmap
        val ratio = screen.toFloat() / max(bitmap.height, bitmap.width).toFloat()
        Log.d(TAG, "updateEdited: screen = $screen")
        _edited.value = Bitmap.createScaledBitmap(bitmap, (ratio * bitmap.width).toInt(), (ratio * bitmap.height).toInt(), true)
    }


    //TODO recycle
    fun reset() {
        _edited.value = origin.value ?: return
        setMode("")
    }


    //TODO recycle
    fun updateEdited(bitmap: Bitmap?) {
        if(bitmap ==null){

//            Log.e(TAG, "updateEdited: screen null!! $screen")
            reset()
            return
        }

        val ratio = screen.toFloat() / max(bitmap.height, bitmap.width).toFloat()
//        Log.d(TAG, "updateEdited: screen = $screen")
        _edited.value = Bitmap.createScaledBitmap(bitmap, (ratio * bitmap.width).toInt(), (ratio * bitmap.height).toInt(), true)
    }


    /**
     *가지고 있는 edited 비트맵을 캐시 파일로 만들어 리턴
     */
    fun multipartBody(context: Context): MultipartBody.Part? {
        var file: File? = null
        try {
            val file_path = context.externalCacheDir?.absolutePath + "/Codiclo"
            val dir = File(file_path)
            if (!dir.exists())
                dir.mkdirs()
//            val file = File(dir, "sketchpad" + pad.t_id + ".png")
//            file = File(context.externalCacheDir?.path + File.pathSeparator + System.currentTimeMillis()+".jpg")
            file = File(dir, "${System.currentTimeMillis()}.png")

            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            edited.value?.compress(Bitmap.CompressFormat.PNG, 80, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file
                ?: return null)
        return MultipartBody.Part.createFormData("upload", file.name, requestFile)
    }

    /**
     *가지고 있는 uriList들을 캐시 파일들로 만들어 리턴
     */
    fun multipartBodyArray(context: Context, uid: String): Array<MultipartBody.Part>? {
        val partList = mutableListOf<MultipartBody.Part>()

        val time = System.currentTimeMillis()
        bitmapList.value?.forEachIndexed { index, bit ->
            var file: File? = null
            try {
                val file_path = context.externalCacheDir?.absolutePath + "/Codiclo"
                val dir = File(file_path)
                if (!dir.exists())
                    dir.mkdirs()
                file = File(dir, "${uid}_${time}_$index.jpg")


//                var input = context.contentResolver.openInputStream(uri)
//
//                val bitmapOption = BitmapFactory.Options()
//                bitmapOption.inJustDecodeBounds = true
//                BitmapFactory.decodeStream(input, null, bitmapOption)
//                val ratio = max(bitmapOption.outWidth, bitmapOption.outHeight).toFloat() / 1080f
//
//
//                bitmapOption.inSampleSize = ratio.roundToInt()
//                bitmapOption.inJustDecodeBounds = false
//
//                //inputStream은 한번 사용 후 소멸됨
//                input = context.contentResolver.openInputStream(uri)
//                val profile = BitmapFactory.decodeStream(input, null, bitmapOption)

//                Log.d(TAG, "getProfileMultipartBody: ratio = $ratio, resultBitmap =${profile?.height} x ${profile?.width}")

                // Compress the bitmap and save in jpg format
                val stream: OutputStream = FileOutputStream(file)
                bit.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                stream.flush()
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file
                    ?: return null)

            partList.add(MultipartBody.Part.createFormData("upload", file.name, requestFile))
        }
        return partList.toTypedArray()
    }

    companion object {

        @BindingAdapter("edited")
        @JvmStatic
        fun setBitmap(iv: ImageView, _data: Any?) {
            _data ?: return
//            Log.d("setBitmap", "setBitmap: ${bitmap.width}")

            val data = when (_data) {
                //uid인지 검사
                is String -> {
                    if (_data.contains("http")) _data
                    else "${BaseApplication.STORAGE_URL}/clothes/$_data"
                }
                else -> _data
            }
            Glide.with(iv.context)
                    .load(data)
                    .into(iv)

        }

        @BindingAdapter("color")
        @JvmStatic
        fun setSelectedBackgroundColor(iv: ImageView, c: Int?) {
            iv.setColorFilter(c?:Color.BLACK, android.graphics.PorterDuff.Mode.SRC_IN)

        }
    }

}