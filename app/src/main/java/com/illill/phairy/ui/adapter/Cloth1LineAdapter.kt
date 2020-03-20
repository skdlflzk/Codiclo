package com.illill.phairy.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.illill.phairy.R
import com.illill.phairy.data.model.Cloth
import com.illill.phairy.databinding.ItemClothBinding
import com.illill.phairy.viewmodel.ClientViewModel
import com.illill.phairy.viewmodel.ClothViewModel
import com.illill.phairy.viewmodel.ProfileViewModel
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class Cloth1LineAdapter : RecyclerView.Adapter<Cloth1LineAdapter.ItemClothViewHolder>(), KoinComponent {
    private val TAG = this@Cloth1LineAdapter.javaClass.simpleName

    private val clothViewModel by inject<ClothViewModel>()
    private val clientViewModel by inject<ClientViewModel>()
    var items = listOf<Cloth>()


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ItemClothViewHolder {
        val bind = DataBindingUtil.inflate<ItemClothBinding>(LayoutInflater.from(p0.context), R.layout.item_cloth, p0, false)
        return ItemClothViewHolder(bind, bind.root)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(p0: ItemClothViewHolder, p1: Int) {
        val binding = p0.binding
        val item = items[p1]
        binding.cloth = item
        binding.clientViewModel = clientViewModel
        binding.clothViewModel = clothViewModel

    }

    inner class ItemClothViewHolder(var binding: ItemClothBinding, itemView: View) : RecyclerView.ViewHolder(itemView)
}