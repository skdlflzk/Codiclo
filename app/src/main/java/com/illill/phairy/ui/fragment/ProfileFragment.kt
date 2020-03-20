package com.illill.phairy.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.illill.phairy.R
import com.illill.phairy.databinding.FragmentProfileBinding
import com.illill.phairy.ui.activity.MainActivity
import com.illill.phairy.ui.activity.SettingActivity
import com.illill.phairy.viewmodel.ClientViewModel
import com.illill.phairy.viewmodel.ClothViewModel
import com.illill.phairy.viewmodel.CodiViewModel
import com.illill.phairy.viewmodel.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class ProfileFragment : BaseFragment(), View.OnClickListener {
    private var TAG = ProfileFragment::class.java.simpleName
    private lateinit var binding: FragmentProfileBinding
    private val TO_SETTING_ACTIVITY = 1004

    private val clientViewModel: ClientViewModel by inject()
    private val profileViewModel: ProfileViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)

        Log.e(TAG, "--ProfileFragment--")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

        binding.clientViewModel = clientViewModel
        binding.profileViewModel = profileViewModel
        binding.lifecycleOwner = this


        setButtons()

        return binding.root
    }

    fun setData(force: Boolean) {
//        Log.d(TAG, "setData: profileViewModel.targetUid  = ${profileViewModel.targetUid}")
        profileViewModel.syncMyInfo(clientViewModel)

    }

    private fun setButtons() {

//        binding.ivBack.setOnClickListener(this)
        binding.view2Data1.root.setOnClickListener(this)
        binding.view2Data2.root.setOnClickListener(this)
        binding.view2Data3.root.setOnClickListener(this)
        binding.tvSetting.setOnClickListener(this)
//
    }

    override fun refreshFragment() {

        binding.clientViewModel = clientViewModel
        binding.profileViewModel = profileViewModel
        setData(true)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.iv_back -> (activity as MainActivity).moveToProfile()
            R.id.view_2_data_1 -> (activity as MainActivity).moveToFollow(isFollower = true)
            R.id.view_2_data_2 -> (activity as MainActivity).moveToFollow(isFollower = false)
            R.id.view_2_data_3 -> (activity as MainActivity).moveToFavorite()
            R.id.tv_setting -> moveToSettingActivity()

        }
    }

    private fun moveToSettingActivity() {
        if(clientViewModel.isClient) {
            startActivityForResult(Intent(context, SettingActivity::class.java), TO_SETTING_ACTIVITY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == TO_SETTING_ACTIVITY && resultCode == Activity.RESULT_OK) {
            (activity as MainActivity).refreshData()
        }
    }
}
