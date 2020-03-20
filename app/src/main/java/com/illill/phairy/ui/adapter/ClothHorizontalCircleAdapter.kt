package com.illill.phairy.ui.adapter

import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.illill.phairy.R
import com.illill.phairy.databinding.ItemHorizontalClothesCircleBinding
import com.illill.phairy.viewmodel.ClientViewModel
import com.illill.phairy.viewmodel.CodiViewModel
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class ClothHorizontalCircleAdapter : RecyclerView.Adapter<ClothHorizontalCircleAdapter.ItemHorizontalClothesViewHolder>(), KoinComponent {
    private val TAG = this@ClothHorizontalCircleAdapter.javaClass.simpleName
    var items = listOf<String>()
    var searchInput = ""
    private val clientViewModel by inject<ClientViewModel>()
    private val codiViewModel by inject<CodiViewModel>()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ItemHorizontalClothesViewHolder {
        val bind = DataBindingUtil.inflate<ItemHorizontalClothesCircleBinding>(LayoutInflater.from(p0.context), R.layout.item_horizontal_clothes_circle, p0, false)

        return ItemHorizontalClothesViewHolder(bind, bind.root)

    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(p0: ItemHorizontalClothesViewHolder, p1: Int) {
        val binding = p0.binding
        val category = items[p1]

        binding.category = category
        binding.codiViewModel = codiViewModel
        binding.circleItems = clientViewModel.myClothList.value?.filter {
            it.category == category
                    && if (TextUtils.isEmpty(searchInput)) {
                true
            } else {
                it.tags.any { t->t.contains(searchInput) }|| it.memo.contains(searchInput)
            }
        }
        Log.d(TAG, "onBindViewHolder: $category size = ${binding.circleItems?.size}")

    }

    inner class ItemHorizontalClothesViewHolder(var binding: ItemHorizontalClothesCircleBinding, itemView: View) : RecyclerView.ViewHolder(itemView)
}