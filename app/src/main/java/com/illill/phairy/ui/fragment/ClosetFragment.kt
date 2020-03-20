package com.illill.phairy.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.Api
import com.illill.phairy.R
import com.illill.phairy.databinding.FragmentClosetBinding
import com.illill.phairy.ui.activity.MainActivity
import com.illill.phairy.viewmodel.ClientViewModel
import com.illill.phairy.viewmodel.ClosetViewModel
import com.illill.phairy.viewmodel.ProfileViewModel
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel


class ClosetFragment : BaseFragment(), View.OnClickListener {
    private var TAG = ClosetFragment::class.java.simpleName
    private lateinit var binding: FragmentClosetBinding

    private val closetViewModel: ClosetViewModel by sharedViewModel()
    private val profileViewModel: ProfileViewModel by inject()
    private val clientViewModel: ClientViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)

        Log.e(TAG, "--ProfileFragment--")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_closet, container, false)

        binding.closetViewModel = closetViewModel
        binding.profileViewModel = profileViewModel
        binding.clientViewModel = clientViewModel
        binding.lifecycleOwner = this

        setButtons()

        return binding.root
    }


    private fun setButtons() {

        binding.ivBack.setOnClickListener(this)
//
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_back -> (activity as MainActivity).moveToProfile()
        }
    }


    override fun refreshFragment() {
        binding.closetViewModel = closetViewModel
        binding.profileViewModel = profileViewModel
        binding.clientViewModel = clientViewModel
        binding.executePendingBindings()
    }
}
