package com.illill.phairy.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.illill.phairy.R
import com.illill.phairy.data.model.Cloth
import com.illill.phairy.databinding.ItemClothCircleBinding
import com.illill.phairy.viewmodel.ClientViewModel
import com.illill.phairy.viewmodel.ClothViewModel
import com.illill.phairy.viewmodel.CodiViewModel
import com.illill.phairy.viewmodel.ProfileViewModel
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class ClothCircleAdapter : RecyclerView.Adapter<ClothCircleAdapter.ItemClothViewHolder>(), KoinComponent {
    private val TAG = this@ClothCircleAdapter.javaClass.simpleName

    private val codiViewModel by inject<CodiViewModel>()

    var items: List<Cloth>? = listOf()
    var selected: List<Cloth>? = listOf()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ItemClothViewHolder {
        val bind = DataBindingUtil.inflate<ItemClothCircleBinding>(LayoutInflater.from(p0.context), R.layout.item_cloth_circle, p0, false)

        return ItemClothViewHolder(bind, bind.root)
    }

    override fun getItemCount() = items?.size?:0

    override fun onBindViewHolder(p0: ItemClothViewHolder, p1: Int) {
        val binding = p0.binding
        val item = items?.get(p1)
        binding.cloth = item
        binding.selected = selected?.contains(item) == true
        binding.codiViewModel = codiViewModel
        Log.d(TAG, "onBindViewHolder: CLOTH CIRCLE item = $item , selected = ${selected?.contains(item)}")
    }

    inner class ItemClothViewHolder(var binding: ItemClothCircleBinding, itemView: View) : RecyclerView.ViewHolder(itemView)
}