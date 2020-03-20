package com.illill.phairy.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.illill.phairy.R
import com.illill.phairy.databinding.FragmentHomeBinding
import com.illill.phairy.viewmodel.ClientViewModel
import com.illill.phairy.viewmodel.HomeViewModel
import com.illill.phairy.viewmodel.WeatherViewModel
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel


class HomeFragment : BaseFragment(), View.OnClickListener {

    private var TAG = HomeFragment::class.java.simpleName
    private lateinit var binding: FragmentHomeBinding


    private val weatherViewModel: WeatherViewModel by viewModel()
    private val homeViewModel: HomeViewModel by viewModel()
    private val clientViewModel: ClientViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)

        Log.e(TAG, "--HomeFragment--")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        binding.weatherViewModel = weatherViewModel
        binding.clientViewModel = clientViewModel
        binding.homeViewModel = homeViewModel
        binding.lifecycleOwner = this

        setView()

        return binding.root
    }

    private fun setView() {

        binding.etSearch.setOnEditorActionListener { _, id, _ -> homeViewModel.onEditorActionEvent(id) }
//
    }

    override fun refreshFragment() {

        Log.d(TAG, "refreshFragment: refreshing HomeFragment")
        binding.weatherViewModel = weatherViewModel
        binding.clientViewModel = clientViewModel
        binding.homeViewModel = homeViewModel

        homeViewModel.loadData()

        binding.lifecycleOwner = this

    }

    override fun onClick(v: View?) {
        when (v?.id) {
//            R.id.tv_donate -> donate()
        }
    }

}
