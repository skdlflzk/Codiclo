package com.illill.phairy.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.illill.phairy.R
import com.illill.phairy.data.model.Client
import com.illill.phairy.databinding.ItemFollowBinding
import com.illill.phairy.viewmodel.ClientViewModel
import com.illill.phairy.viewmodel.ProfileViewModel
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class FollowAdapter : RecyclerView.Adapter<FollowAdapter.ItemFollowHoloer>(), KoinComponent {
    private val TAG = this@FollowAdapter.javaClass.simpleName
    var items = listOf<Client>()


    private val clientViewModel by inject<ClientViewModel>()
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ItemFollowHoloer {
        val bind = DataBindingUtil.inflate<ItemFollowBinding>(LayoutInflater.from(p0.context), R.layout.item_follow, p0, false)

        return ItemFollowHoloer(bind, bind.root)

    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(p0: ItemFollowHoloer, p1: Int) {
        val binding = p0.binding
        val client = items[p1]
        binding.clientViewModel = clientViewModel
        binding.follow = client
    }

    inner class ItemFollowHoloer(var binding: ItemFollowBinding, itemView: View) : RecyclerView.ViewHolder(itemView)
}