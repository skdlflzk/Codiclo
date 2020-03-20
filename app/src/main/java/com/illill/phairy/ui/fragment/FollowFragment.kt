package com.illill.phairy.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.illill.phairy.R
import com.illill.phairy.databinding.FragmentFollowBinding
import com.illill.phairy.ui.activity.MainActivity
import com.illill.phairy.viewmodel.ClientViewModel
import com.illill.phairy.viewmodel.FollowViewModel
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel


class FollowFragment : BaseFragment(), View.OnClickListener {
    private var TAG = FollowFragment::class.java.simpleName
    private lateinit var binding: FragmentFollowBinding


    private val followViewModel: FollowViewModel by sharedViewModel()
//    private val clientViewModel: ClientViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)

        Log.e(TAG, "--ProfileFragment--")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_follow, container, false)

        binding.followViewModel = followViewModel
        binding.lifecycleOwner = this

        setView()

        return binding.root
    }

    private fun setView() {

        binding.ivBack.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_back-> (activity as MainActivity).moveToProfile()
        }
    }


    override fun refreshFragment() {

    }

}
