package com.illill.phairy.ui.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.illill.phairy.BaseApplication.Companion.sharedPreferences
import com.illill.phairy.R
import com.illill.phairy.data.model.Codi
import com.illill.phairy.databinding.ActivitySettingBinding
import com.illill.phairy.databinding.Dialog1lineBinding
import com.illill.phairy.databinding.DialogCodiBinding
import com.illill.phairy.viewmodel.ClientViewModel
import com.illill.phairy.viewmodel.SettingViewModel
import org.koin.android.ext.android.inject



class SettingActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = SettingActivity::class.java.simpleName

    private lateinit var binding: ActivitySettingBinding

    private val clientViewModel by inject<ClientViewModel>()
    private val settingViewModel by inject<SettingViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)
        Log.d(TAG, "onCreate: --SettingActivity--")

        binding.settingViewModel = settingViewModel
        binding.clientViewModel = clientViewModel
        binding.lifecycleOwner = this



        setButtons()

    }

    private fun setButtons() {

        binding.tvLogout.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.tvContact.setOnClickListener(this)
        binding.tvReview.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_logout->logout()
            R.id.iv_back -> finishActivity()
            R.id.tv_contact -> popUpContactUs()
            R.id.tv_review -> moveToStore()
        }
    }

    private fun moveToStore() {
        val appPackageName = packageName // getPackageName() from Context or Activity object
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (anfe: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }

    }

    private fun popUpContactUs() {

        val bind = DataBindingUtil.inflate<Dialog1lineBinding>(layoutInflater, R.layout.dialog_1line, null, false)
        val inputDialog = AlertDialog.Builder(this@SettingActivity)
                .setView(bind.root)
                .create().apply {

                    window?.requestFeature(Window.FEATURE_NO_TITLE)
                    window?.setBackgroundDrawableResource(android.R.color.transparent)

                }


        bind.content = getString(R.string.contact_us_description)
        bind.btnOk.setOnClickListener {
            inputDialog.dismiss()
        }

        inputDialog.show()
        val width = resources.displayMetrics.widthPixels * 0.725F
        val height = width * 0.725F
        inputDialog.window?.setLayout(width.toInt(), height.toInt())
    }


    override fun onBackPressed() {
        finishActivity()
    }

    private fun finishActivity() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun logout(){

        clientViewModel.clear()

        sharedPreferences.edit().putString("uid","").apply()

        FirebaseAuth.getInstance().signOut()
        val intent = Intent(applicationContext, StartActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        finishAndRemoveTask()

        startActivity(intent)
    }

}

