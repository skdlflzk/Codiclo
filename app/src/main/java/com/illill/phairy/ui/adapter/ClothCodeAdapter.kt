package com.illill.phairy.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.illill.phairy.R
import com.illill.phairy.databinding.ItemClothCodeBinding
import com.illill.phairy.viewmodel.ClothViewModel
import com.illill.phairy.viewmodel.CodiViewModel
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class ClothCodeAdapter : RecyclerView.Adapter<ClothCodeAdapter.ItemClothViewHolder>(), KoinComponent {
    private val TAG = this@ClothCodeAdapter.javaClass.simpleName

//    private val codiViewModel by inject<CodiViewModel>()
    private val clothViewModel by inject<ClothViewModel>()

    var items: List<String>? = listOf()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ItemClothViewHolder {
        val bind = DataBindingUtil.inflate<ItemClothCodeBinding>(LayoutInflater.from(p0.context), R.layout.item_cloth_code, p0, false)

        return ItemClothViewHolder(bind, bind.root)

    }

    override fun getItemCount() = items?.size ?: 0

    override fun onBindViewHolder(p0: ItemClothViewHolder, p1: Int) {
        val binding = p0.binding
        val item = items?.get(p1)
        binding.code = item
        binding.clothViewModel = clothViewModel
    }

    inner class ItemClothViewHolder(var binding: ItemClothCodeBinding, itemView: View) : RecyclerView.ViewHolder(itemView)
}