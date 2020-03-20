package com.illill.phairy.ui.activity

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.illill.phairy.R
import com.illill.phairy.databinding.ActivityPhotoBinding
import com.illill.phairy.ui.common.CropWindow
import com.illill.phairy.ui.common.CutView
import com.illill.phairy.viewmodel.PhotoViewModel
import org.koin.android.ext.android.inject
import java.io.FileNotFoundException
import java.io.IOException


/**
 * ClothActivity에 전달된 이미지를
 * 자유 도형 / 크롭 기능을 사용하여 비트맵을 수정합니다.
 */
class PhotoActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = PhotoActivity::class.java.simpleName

    private lateinit var binding: ActivityPhotoBinding


    val photoViewModel by inject<PhotoViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: --PhotoActivity--")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_photo)



        binding.photoViewModel = photoViewModel.apply {
            screen = resources.displayMetrics.widthPixels
        }
        binding.lifecycleOwner = this

        setButtons()

        loadBitmap()
    }

    private fun setButtons() {

        binding.ivDraw.setOnClickListener(this)
        binding.ivCrop.setOnClickListener(this)
        binding.ivRevert.setOnClickListener(this)
        binding.ivDone.setOnClickListener(this)

        binding.tvCancel.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.iv_revert -> {
                clearView()
                photoViewModel.reset()
            }

            R.id.iv_draw -> {
                clearView()
                if (photoViewModel.mode.value == "draw") adaptChanged()
                else draw()
            }

            R.id.iv_crop -> {
                if (photoViewModel.mode.value == "crop") adaptChanged()
                else crop()
            }

            R.id.iv_done -> {
                adaptChanged()
                finish()
            }

            R.id.tv_cancel -> {
                setResult(-4)
                finish()
            }
        }
    }

    //추가로 생성된 뷰를 지운다
    private fun clearView() {

        for (i in binding.llPreview.childCount - 1 downTo 0) {
            val v = binding.llPreview.getChildAt(i)
            if (v.id != R.id.iv_preview) binding.llPreview.removeView(v)
        }
    }

    private fun loadBitmap() {
        photoViewModel.clearData()
        val uri = Uri.parse(intent.getStringExtra("uri"))

        Log.d(TAG, "loadBitmap: ${uri.toString()}")
        Glide.with(applicationContext)
                .asBitmap()
                .load(uri)
//                    .transition(BitmapTransitionOptions.withCrossFade(500))
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        Log.d(TAG, "onLoadFailed: ")
                        Toast.makeText(applicationContext, getString(R.string.error_later), Toast.LENGTH_SHORT).show()
                    }


                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                        Log.d(TAG, "onResourceReady: ")
                        photoViewModel.loadOrigin(resource, applicationContext)

                    }
                })

    }

    private fun draw() {
        photoViewModel.setMode("draw")
        try {
            //edited 비트맵 값을 가져와서  글라이드로
            Glide.with(applicationContext)
                    .asBitmap()
                    .load(photoViewModel.edited.value)
//                    .transition(BitmapTransitionOptions.withCrossFade(500))
                    .into(object : CustomViewTarget<ImageView, Bitmap>(binding.ivPreview) {
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            Log.e(TAG, "onLoadFailed: $errorDrawable")
                            Log.e(TAG, "onLoadFailed: ${errorDrawable?.minimumWidth}")
                        }

                        override fun onResourceCleared(placeholder: Drawable?) {}

                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            Toast.makeText(applicationContext, getString(R.string.draw_outline), Toast.LENGTH_SHORT).show()

                            lateinit var mSomeView: CutView
                            mSomeView = CutView(applicationContext, resource, object : CutView.ImageCropListener {
                                override fun onClickDialogPositiveButton() {
                                    var cropBitmap = mSomeView.bitmapFromMemCache ?: return
                                    cropBitmap = getBitmapWithTransparentBG(cropBitmap, Color.WHITE)
//                                    val d = BitmapDrawable(resources, cropBitmap)

                                    photoViewModel.updateEdited(cropBitmap)
                                    binding.llPreview.removeView(mSomeView)
                                    photoViewModel.setMode("")
                                }

                                override fun onClickDialogNegativeButton() {
//                                    Log.d(TAG, "onClickDialogNegativeButton: no")
                                }
                            })

                            val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)

                            mSomeView.layoutParams = params
                            binding.llPreview.addView(mSomeView)
                        }
                    })

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    //변경사항을 저장한다
    private fun adaptChanged() {
        val mode = photoViewModel.mode.value

        when (mode) {
            "crop" -> {
                (binding.llPreview.getChildAt(1) as? CropWindow)?.cropImage()?.let {
                    photoViewModel.updateEdited(it)
                }
            }
            "draw" -> {
                //그리지 않고 취소
            }
        }
        clearView()
        photoViewModel.setMode("")
    }


    private fun crop() {
        photoViewModel.setMode("crop")
//        Log.d(TAG, "crop: ${photoViewModel.edited.value?.width} , ${photoViewModel.edited.value?.height}")

        Toast.makeText(applicationContext, getString(R.string.draw_outline), Toast.LENGTH_SHORT).show()

        val bitmap = if (photoViewModel.edited.value?.isRecycled == true) photoViewModel.origin.value else photoViewModel.edited.value
        val v = CropWindow(applicationContext, bitmap ?: return).apply {
            layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        }

        binding.llPreview.addView(v)
    }

    fun getBitmapWithTransparentBG(srcBitmap: Bitmap, bgColor: Int): Bitmap {
        val result = srcBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val nWidth = result.width
        val nHeight = result.height
        for (y in 0 until nHeight)
            for (x in 0 until nWidth) {
                val nPixelColor = result.getPixel(x, y)
                if (nPixelColor == bgColor)
                    result.setPixel(x, y, Color.TRANSPARENT)
            }
        return result
    }

    override fun onBackPressed() {
        super.onBackPressed()
        binding.tvCancel.performClick()
    }
}

