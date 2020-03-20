package com.illill.phairy.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.illill.phairy.BaseApplication.Companion.gson
import com.illill.phairy.R
import com.illill.phairy.data.model.Codi
import com.illill.phairy.databinding.ActivityCodiBinding
import com.illill.phairy.ui.fragment.CodiMake1Fragment
import com.illill.phairy.ui.fragment.CodiMake2Fragment
import com.illill.phairy.viewmodel.ClientViewModel
import com.illill.phairy.viewmodel.CodiViewModel
import com.illill.phairy.viewmodel.PhotoViewModel
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class CodiActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = CodiActivity::class.java.simpleName
    private val _requestCamera= 2000

    private lateinit var binding: ActivityCodiBinding


    private val codiViewModel by inject<CodiViewModel>()
    private val clientViewModel by inject<ClientViewModel>()
    private val photoViewModel by inject<PhotoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: --ClothActivity--")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_codi)

        val codiStr = intent.getStringExtra("codi")

        //추가하거나 / 수정할 코디를 지정
        binding.codiViewModel = codiViewModel.apply {
            setCodi(if (TextUtils.isEmpty(codiStr)) {
                onEdit.value = false
                Codi().apply {
                    uid = clientViewModel.I.uid
                    nickname = clientViewModel.I.nickname
                    open = clientViewModel.I.open
                }
            } else {
                onEdit.value = true
                gson.fromJson(codiStr, Codi::class.java).apply {
                    nickname = clientViewModel.I.nickname               //닉네임은 바뀌었을 수도 있으니까!
                    open = clientViewModel.I.open
                }
            })
        }

        binding.photoViewModel = photoViewModel
        binding.lifecycleOwner = this

        Log.d(TAG, "onCreate: codiViewModel.isSending.value = ${codiViewModel.isSending.value}")


        setButtons()

        setData()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_back -> back()
        }
    }


    private fun back() {
        when (binding.vpInfo.currentItem) {
            1 -> binding.vpInfo.currentItem = 0
            0 -> finish()
        }
    }

    fun next() {
        when (binding.vpInfo.currentItem) {
            0 -> binding.vpInfo.currentItem = 1
            1 -> send()
        }
    }

    private fun send() {

        //중복 업로드를 방지한다
        if (codiViewModel.isSending.value != false) {
            Toast.makeText(applicationContext, "진행 중 입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {

            if (codiViewModel.codi.value?.season == 0) {
                Toast.makeText(applicationContext, getString(R.string.at_least_one_season), Toast.LENGTH_SHORT).show()
                return@launch
            }
            when (codiViewModel.onEdit.value) {
                true -> {
                    if (codiViewModel.update()) {

                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.error_later), Toast.LENGTH_SHORT).show()
                    }
                }
                false -> {
                    Log.d(TAG, "upload: ?? ${codiViewModel.onEdit.value}")
                    val parts = photoViewModel.multipartBodyArray(applicationContext, clientViewModel.I.uid)
                    if (parts == null) {
                        Toast.makeText(applicationContext, getString(R.string.error_later), Toast.LENGTH_SHORT).show()
                    } else {
                        if (codiViewModel.add(parts)) {
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            Toast.makeText(applicationContext, getString(R.string.error_later), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            photoViewModel.bitmapList.value?.forEach {
                it.recycle()
            }
            photoViewModel.bitmapList.value = null
        } catch (e: Exception) {

        } finally {
            photoViewModel.bitmapList.value = null
            codiViewModel.clearData()
        }
    }


    private fun setButtons() {
        binding.tvBack.setOnClickListener(this)


        val adapter = PagerAdapter(supportFragmentManager)
        binding.vpInfo.adapter = adapter

    }


    //새로 업로드할 사진을 고르거나 기존의 사진을 불러온다
    private fun setData() {

        if (codiViewModel.onEdit.value == true) {   //수정할 경우

            codiViewModel.restoreSelected { _list ->
                val list = _list ?: listOf()
                clientViewModel.myClothList.value?.filter { it.code in list }
            }

            codiViewModel.codi.value?.pictures?.forEach {

                Glide.with(applicationContext)
                        .asBitmap()
                        .load(it)
                        .override(1080, 1080)
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                Log.d(TAG, "onResourceReady: bitmap uri = $it")
                                Log.d(TAG, "onResourceReady: ${resource.width}x${resource.height}")
                                photoViewModel.bitmapList.value = (photoViewModel.bitmapList.value
                                        ?: listOf()) + resource
                            }
                        })
            }

        } else {    //새로 코디를 생성할 때
            if(ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){

                photoViewModel.bitmapList.value = null
                TedImagePicker.with(this)
                        .min(1, getString(R.string.image_min_1))
                        .max(4, getString(R.string.image_max_4))
                        .startMultiImage { list ->

                            list.forEach {
                                Glide.with(applicationContext)
                                        .asBitmap()
                                        .load(it)
                                        .override(1080, 1080)
                                        .into(object : SimpleTarget<Bitmap>() {
                                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                Log.d(TAG, "onResourceReady: bitmap uri = $it")
                                                Log.d(TAG, "onResourceReady: ${resource.width}x${resource.height}")
                                                photoViewModel.bitmapList.value = (photoViewModel.bitmapList.value
                                                        ?: listOf()) + resource
                                            }
                                        })

                            }
                        }
            }else{
                ActivityCompat.requestPermissions(this@CodiActivity,arrayOf(Manifest.permission.CAMERA),_requestCamera)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            if (resultCode == -1) finish()
        }

        Log.d(TAG, "onActivityResult: requestCode = ${requestCode},resultCode = $resultCode")

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == _requestCamera){
            if(grantResults.all { it==PackageManager.PERMISSION_GRANTED }){
                setData()
            }else{
                Toast.makeText(applicationContext, "카메라를 사용하시려면 설정에서 권한을 승인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    inner class PagerAdapter(private val fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val fragments: List<Fragment> = listOf(CodiMake1Fragment(), CodiMake2Fragment())
        override fun getItem(pos: Int) = fragments[pos]
        override fun getCount() = fragments.size
        override fun destroyItem(container: ViewGroup, position: Int, v: Any) {
            super.destroyItem(container, position, v)

            if (position <= count) {
                val manager = fm
                manager.beginTransaction()
                        .remove(v as Fragment)
                        .commit()
            }
        }
    }

}