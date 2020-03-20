package com.illill.phairy.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.illill.phairy.R
import com.illill.phairy.data.model.Cloth
import com.illill.phairy.databinding.ItemHorizontalClothesBinding
import com.illill.phairy.viewmodel.ProfileViewModel
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class ClothHorizontalAdapter : RecyclerView.Adapter<ClothHorizontalAdapter.ItemHorizontalClothesViewHolder>(), KoinComponent {
    private val TAG = this@ClothHorizontalAdapter.javaClass.simpleName
    var items = listOf<String>()
    var clothes = listOf<Cloth>()
    var season = 0

    private val profileViewModel by inject<ProfileViewModel>()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ItemHorizontalClothesViewHolder {
        val bind = DataBindingUtil.inflate<ItemHorizontalClothesBinding>(LayoutInflater.from(p0.context), R.layout.item_horizontal_clothes, p0, false)

        return ItemHorizontalClothesViewHolder(bind, bind.root)

    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(p0: ItemHorizontalClothesViewHolder, p1: Int) {
        val binding = p0.binding
        val category = items[p1]
        binding.category = category
        binding.profileViewModel = profileViewModel
        binding.season = season
        binding.clothList = clothes

    }

    inner class ItemHorizontalClothesViewHolder(var binding: ItemHorizontalClothesBinding, itemView: View) : RecyclerView.ViewHolder(itemView)
}