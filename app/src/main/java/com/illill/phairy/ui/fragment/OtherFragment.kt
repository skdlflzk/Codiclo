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
import com.illill.phairy.databinding.FragmentOtherBinding
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


class OtherFragment : BaseFragment(), View.OnClickListener {
    private var TAG = OtherFragment::class.java.simpleName
    private lateinit var binding: FragmentOtherBinding
    private val TO_SETTING_ACTIVITY = 1004

    private val clientViewModel: ClientViewModel by inject()
    //    private val codiViewModel: CodiViewModel by inject()
//    private val clothViewModel: ClothViewModel by inject()
    private val profileViewModel: ProfileViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)

        Log.e(TAG, "--OtherFragment--")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_other, container, false)

        binding.clientViewModel = clientViewModel
        binding.profileViewModel = profileViewModel
        binding.lifecycleOwner = this

        setButtons()

        return binding.root
    }

    fun setData(force: Boolean) {

        Log.d(TAG, "setData: otherViewModel.targetUid  = ${profileViewModel.targetUid}")
        //타겟 옵션이 없거나, 대상이 나라면

        CoroutineScope(Dispatchers.Main).launch {
            profileViewModel.loadProfile(force)
            profileViewModel.loadInventory(force)
        }
    }

    private fun setButtons() {

        binding.ivBack.setOnClickListener(this)
        binding.view2Data1.root.setOnClickListener(this)
        binding.view2Data2.root.setOnClickListener(this)
        binding.view2Data3.root.setOnClickListener(this)
//
    }

    override fun refreshFragment() {

        binding.clientViewModel = clientViewModel
        binding.profileViewModel = profileViewModel
        setData(true)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_back -> (activity as MainActivity).moveToHome()
            R.id.view_2_data_1 -> (activity as MainActivity).moveToFollow(isFollower = true)
            R.id.view_2_data_2 -> (activity as MainActivity).moveToFollow(isFollower = false)
            R.id.view_2_data_3 -> (activity as MainActivity).moveToFavorite()
        }
    }
}
