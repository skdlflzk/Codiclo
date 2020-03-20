package com.illill.phairy.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import com.illill.phairy.R
import com.illill.phairy.data.model.Cloth
import com.illill.phairy.data.model.Codi
import com.illill.phairy.databinding.ItemGridBinding
import com.illill.phairy.viewmodel.ClientViewModel
import com.illill.phairy.viewmodel.ClothViewModel
import com.illill.phairy.viewmodel.CodiViewModel
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class GridAdapter : RecyclerView.Adapter<GridAdapter.ItemGridViewHolder>(), KoinComponent, LifecycleObserver {
    private val TAG = this@GridAdapter.javaClass.simpleName
    var items = listOf<Any>()
    var heights = mutableMapOf<Int, Int>()


    private val clientViewModel by inject<ClientViewModel>()
    private val codiViewModel by inject<CodiViewModel>()
    private val clothViewModel by inject<ClothViewModel>()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ItemGridViewHolder {
        val bind = DataBindingUtil.inflate<ItemGridBinding>(LayoutInflater.from(p0.context), R.layout.item_grid, p0, false)

        return ItemGridViewHolder(bind, bind.root)

    }

    override fun getItemCount() = items.size
//    override fun getItemId(position: Int): Long {
//        return items[position].toString().hashCode().toLong()
//    }

    override fun onBindViewHolder(p0: ItemGridViewHolder, p1: Int) {
        val binding = p0.binding
        val item = items[p1]
        binding.clientViewModel = clientViewModel
        binding.item = item
        binding.ivItem.setOnClickListener {
            when (item) {
                is Codi -> {
                    codiViewModel.codiItemClickListener?.invoke(item)
                }
                is Cloth -> {
                    clothViewModel.clothItemClickListener?.invoke(item)
                }
            }
        }
//        val h = heights[p1]
//        if (h == null) {
//            var observer: ViewTreeObserver.OnGlobalLayoutListener? = null
//            observer = object : ViewTreeObserver.OnGlobalLayoutListener {
//                override fun onGlobalLayout() {
//                    if (binding.ivItem.height == 0) return
//                    else {
//                        Log.d(TAG, "onGlobalLayout: height = ${binding.ivItem.height}")
//                        binding.ivItem.viewTreeObserver.removeOnGlobalLayoutListener(observer)
//                        heights[p1] = binding.ivItem.height
//                    }
//                }
//            }
//            binding.ivItem.viewTreeObserver.addOnGlobalLayoutListener(observer)
//        } else {
//            binding.rlContent.layoutParams.height = h
//            Log.d(TAG, "onBindViewHolder: rebind h = ${h}")
//        }
    }

    inner class ItemGridViewHolder(var binding: ItemGridBinding, itemView: View) : RecyclerView.ViewHolder(itemView)
}