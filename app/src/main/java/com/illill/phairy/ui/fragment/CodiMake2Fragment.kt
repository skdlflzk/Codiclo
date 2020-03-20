package com.illill.phairy.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.illill.phairy.R
import com.illill.phairy.databinding.FragmentClosetBinding
import com.illill.phairy.databinding.FragmentCodiMake1Binding
import com.illill.phairy.databinding.FragmentCodiMake2Binding
import com.illill.phairy.ui.activity.CodiActivity
import com.illill.phairy.ui.activity.MainActivity
import com.illill.phairy.viewmodel.ClientViewModel
import com.illill.phairy.viewmodel.ClosetViewModel
import com.illill.phairy.viewmodel.CodiViewModel
import com.illill.phairy.viewmodel.ProfileViewModel
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel


class CodiMake2Fragment : Fragment(), View.OnClickListener {
    private var TAG = CodiMake2Fragment::class.java.simpleName
    private lateinit var binding: FragmentCodiMake2Binding

    private val codiViewModel: CodiViewModel by inject()
    private val clientViewModel: ClientViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)

        Log.e(TAG, "--CodiMake2Fragment--")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_codi_make_2, container, false)

        binding.codiViewModel = codiViewModel
        binding.clientViewModel = clientViewModel
        binding.lifecycleOwner = this

        setButtons()

        return binding.root
    }


    private fun setButtons() {

        binding.tvAdd.setOnClickListener(this)
//
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_add-> (activity as CodiActivity).next()
        }
    }
}
