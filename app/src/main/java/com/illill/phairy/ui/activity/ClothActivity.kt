package com.illill.phairy.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.illill.phairy.BaseApplication.Companion.gson
import com.illill.phairy.R
import com.illill.phairy.data.model.Cloth
import com.illill.phairy.databinding.ActivityClothBinding
import com.illill.phairy.viewmodel.ClientViewModel
import com.illill.phairy.viewmodel.ClothViewModel
import com.illill.phairy.viewmodel.PhotoViewModel
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


/**
 *
 * 옷을 생성하는 액티비티.
 * TedImagePicker로 하나의 이미지를 "uri" 로 intent에 담아 호출됨
 *
 */
class ClothActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = ClothActivity::class.java.simpleName
    private val _photoActivity = 2000
    private val _requestCamera= 2000

    private lateinit var binding: ActivityClothBinding


    private val clothViewModel by inject<ClothViewModel>()
    private val clientViewModel by inject<ClientViewModel>()
    private val photoViewModel by inject<PhotoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: --ClothActivity--")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_cloth)


        val clothStr = intent.getStringExtra("cloth")


        binding.clientViewModel = clientViewModel
        binding.photoViewModel = photoViewModel
        binding.clothViewModel = clothViewModel.apply {
            setCloth(if (TextUtils.isEmpty(clothStr)) {
                onEdit.value = false
                Cloth().apply {
                    uid = clientViewModel.self.value?.uid ?: ""
                    nickname = clientViewModel.I.nickname
                    category = "TOP"
                    open = clientViewModel.I.open
                }
            } else {
                onEdit.value = true
                gson.fromJson(clothStr, Cloth::class.java).apply {
                    nickname = clientViewModel.I.nickname               //닉네임은 바뀌었을 수도 있으니까!
                }
            })
        }

        binding.lifecycleOwner = this
        setButtons()
        setData()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_add -> send()
            R.id.tv_back -> finish()
        }
    }

    private fun send() {
        //중복 업로드를 방지한다
        if (clothViewModel.isSending.value != false) {
            Log.d(TAG, "send: sendin!")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            if (clothViewModel.cloth.value?.season == 0) {
                Toast.makeText(applicationContext, getString(R.string.at_least_one_season), Toast.LENGTH_SHORT).show()
                return@launch
            }

            when (clothViewModel.onEdit.value) {
                true -> {
                    if (clothViewModel.update()) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.error_later), Toast.LENGTH_SHORT).show()
                    }
                }
                false -> {

                    Log.d(TAG, "upload: ?? ${clothViewModel.onEdit.value}")
                    val image = photoViewModel.multipartBody(applicationContext)
                    if (image == null) {
                        Toast.makeText(applicationContext, getString(R.string.error_later), Toast.LENGTH_SHORT).show()
                    } else {
                        if (clothViewModel.add(image)) {
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

    private fun setButtons() {
        binding.tvAdd.setOnClickListener(this)
        binding.tvBack.setOnClickListener(this)
    }


    private fun setData() {

        if (clothViewModel.onEdit.value == true) {      //수정모드일때는 데이터 수정 X
            return

        } else {

            if(ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){

                TedImagePicker.with(this)
                        .min(1,"이미지를 선택하세요")

                        .errorListener {
                            Log.d(TAG, "setData:errr")
                        }
                        .start {
                            val intent = Intent(applicationContext, PhotoActivity::class.java)
                            intent.putExtra("uri", it.toString())
                            startActivityForResult(intent, _photoActivity)
                        }

            }else{
                ActivityCompat.requestPermissions(this@ClothActivity,arrayOf(Manifest.permission.CAMERA),_requestCamera)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == _photoActivity) {
            if (resultCode == -1) finish()
        }
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

}