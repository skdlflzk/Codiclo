package com.illill.phairy.viewmodel

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.illill.phairy.R
import com.illill.phairy.data.model.Client
import com.illill.phairy.ui.adapter.FollowAdapter
import com.illill.phairy.util.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FollowViewModel(val retrofit: RetrofitClient) : ViewModel() {
    private val TAG = this.javaClass.simpleName


    //followers를 보여줄지 followings를 보여줄지 선택
    private val _isFollower = MutableLiveData<Boolean>()
    val isFollower: LiveData<Boolean> get() = _isFollower


    //검색하고 싶은 닉네임 필터
//    private val _filter = MutableLiveData<String>()
    val filter = MutableLiveData<String>()


    //보여줄 사람 리스트
    private val _follow = MutableLiveData<List<Client>>()
    val follow: LiveData<List<Client>> get() = _follow

    fun setFollow(tagetList: List<String>, f: Boolean) = CoroutineScope(Dispatchers.Main).launch {

        _isFollower.value = f
        loadNicknamesAsync(tagetList)
    }

    private suspend fun loadNicknamesAsync(uidList: List<String>) {

        try {
            _follow.value = withContext(Dispatchers.Default) {
                retrofit.loadNicknamesAsync(uidList).await()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {


        //        app:follow="@{followViewModel.follow}"
//        app:filter="@{followViewModel.filter}"
        @BindingAdapter(value = ["follow", "filter"])
        @JvmStatic
        fun setFollowList(rv: RecyclerView, list: List<Client>?, filter: String?) {
            list ?: return


            val adapter: FollowAdapter
            if (rv.adapter == null) {
                rv.layoutManager = LinearLayoutManager(rv.context, RecyclerView.VERTICAL, false)
                adapter = FollowAdapter()
                rv.adapter = adapter
            } else {
                adapter = rv.adapter as FollowAdapter
            }

            adapter.items = list.filter {
                it.nickname.toLowerCase().contains(filter ?: "")
            }

            Log.d("setFollowList", "setFollowList: ${adapter.items}")
            adapter.notifyDataSetChanged()
        }

        //        app:isFollowing="@{clientViewModel.self.followers}"
//        app:uid="@{follow.uid}"
        @BindingAdapter(value = ["isFollowing", "uid"])
        @JvmStatic
        fun isFollowing(tv: TextView, self: Client?, uid: String?) {
            self ?: return
            uid ?: return


            if (self.uid == uid) tv.visibility = View.INVISIBLE

            if (self.followers.contains(uid)) {
                tv.text = tv.context.getString(R.string.following_upper)
                tv.setBackgroundResource(R.drawable.circle_solid_gray)
            } else {
                tv.text = tv.context.getString(R.string.follow_upper)
                tv.setBackgroundResource(R.drawable.circle_solid_red)
            }


        }
    }
}
