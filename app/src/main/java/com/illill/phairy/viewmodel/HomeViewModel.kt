package com.illill.phairy.viewmodel

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.chip.ChipGroup
import com.illill.phairy.R
import com.illill.phairy.data.model.Client
import com.illill.phairy.data.model.Cloth
import com.illill.phairy.data.model.Codi
import com.illill.phairy.data.model.Item
import com.illill.phairy.ui.adapter.GridAdapter
import com.illill.phairy.util.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * @property items 검색 결과 코디와 옷을 보여주기 위해 Any 타입으로 설정.
 */
class HomeViewModel(private val retrofitClient: RetrofitClient) : ViewModel() {
    init {
        loadData()
    }

    private val TAG = this.javaClass.simpleName

    //  검색 결과
    private val _items = MutableLiveData<Any>()
    val items: LiveData<Any> get() = _items

    //    인기 키워드
    private val _keywords = MutableLiveData<List<String>>()
    val keywords: LiveData<List<String>> get() = _keywords


    var inputText: String = ""

    var hint: String = "검색어를 입력해주세요"


    fun loadData() = CoroutineScope(Dispatchers.Main).launch {

        val list = withContext(Dispatchers.Default) {
            try {
                retrofitClient.loadCodiByTagAsync(inputText).await()
            } catch (e: Exception) {

                e.printStackTrace()
                listOf<Codi>()
            }
        }
        _items.value = list.asReversed()
        Log.d(TAG, "loadData: $list")

    }


    fun onEditorActionEvent(actionId: Int?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            loadData()
            return true
        }
        return false
    }

    companion object {

        /**
         * 썸네일 지정
         */
        @BindingAdapter(value = ["item", "progress", "self"])
        @JvmStatic
        fun setThumbnail(iv: ImageView, obj: Any?, pb: ProgressBar?, self: Client?) {
            obj ?: return

            pb?.visibility = View.VISIBLE

            val url = when (obj) {
                is Cloth -> {
                    if (self?.uid == obj.uid || obj.open) obj.iconUrl else R.drawable.preview1
                }
                is Codi -> {
                    if (!obj.open && self?.uid != obj.uid) R.drawable.preview1
                    else if (obj.pictures.isNotEmpty()) obj.pictures[0]
                    else R.drawable.preview2
                }
                is Int -> obj
                else -> return
            }


            Glide.with(iv.context)
                    .asBitmap()
                    .load(url)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            pb?.visibility = View.GONE
                        }

                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            iv.setImageBitmap(resource)
                            pb?.visibility = View.GONE
                        }
                    })
        }


        /**
         * 하트 표시할지 말지
         *
         */
        @BindingAdapter(value = ["item", "favCodiList", "favClothList"], requireAll = true)
        @JvmStatic
        fun setLikedOrNot(iv: ImageView, item: Any?, favCodiList: List<Codi>?, favClothList: List<Cloth>?) {
            item ?: return

            val code = when (item) {
                is Cloth -> item.code
                is Codi -> item.code
                else -> {
                    iv.visibility = View.GONE
                    return
                }
            }
//            Log.d("setLikedOrNot", "setLikedOrNot: $code of $item")
            iv.visibility = View.VISIBLE
            val codes = (favClothList?.map { it.code }
                    ?: listOf<String>()) + (favCodiList?.map { it.code } ?: listOf<String>())

            if (code in codes) iv.setImageResource(R.drawable.ic_favorites_on)
            else iv.setImageResource(R.drawable.ic_favorites_off)
        }

        /**
         * 검색 결과 표시
         */
        @BindingAdapter(value = ["searchResult"])
        @JvmStatic
        fun setSearchResult(tv: TextView, item: List<Codi>?) {
            if (item?.size == 0) tv.visibility = View.VISIBLE
            else tv.visibility = View.GONE
        }


        @BindingAdapter(value = ["gridItem"])
        @JvmStatic
        fun setGridRecyclerView(rv: RecyclerView, list: List<Any>?) {
            val adapter: GridAdapter
            if (rv.adapter == null) {

                val manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                manager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
                rv.layoutManager = manager

                adapter = GridAdapter()
//                adapter.setHasStableIds(true)
                rv.adapter = adapter

            } else {
                adapter = rv.adapter as GridAdapter
            }

            adapter.items = list
                    ?: listOf(R.drawable.preview2, R.drawable.preview2, R.drawable.preview1, R.drawable.preview2)
            adapter.notifyDataSetChanged()

        }

        //
//        app:recentKeyword="@{homeViewModel.keywords}"

        @BindingAdapter(value = ["recentKeyword", "homeViewModel"], requireAll = true)
        @JvmStatic
        fun setRecentKeyword(group: ChipGroup, recentKeyword: List<String>?, homeViewModel: HomeViewModel?) {
            homeViewModel ?: return
            recentKeyword ?: return

        }

    }

}