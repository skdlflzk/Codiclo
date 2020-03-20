package com.illill.phairy.viewmodel

import android.text.TextUtils
import android.util.Log
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.illill.phairy.R
import com.illill.phairy.data.model.Cloth
import com.illill.phairy.ui.adapter.GridAdapter

class ClosetViewModel : ViewModel() {
    private val TAG = this.javaClass.simpleName

    //현재 ViewMore 상태
    private val _category = MutableLiveData<String>()
    val category: LiveData<String> get() = _category

    fun setCategory(cat: String) {
        _category.value = cat
    }

    //현재 ViewMore 상태
    val searchInput = MutableLiveData<String>()


    companion object {

        /**
         *
         * ViewMore에서 보며줄 그리드 형식 뷰
        app:tabSeason="@{tabSeason}"
        app:searchInput="@{closetViewModel.searchInput}"
        app:clothList="@{profileViewModel.clothList}"
        app:category="@{closetViewModel.category}"
         */
        @BindingAdapter(value = ["tabSeason", "searchInput", "clothList", "category"], requireAll = true)
        @JvmStatic
        fun setViewMoreClothGridRecyclerView(rv: RecyclerView, tabLayout: TabLayout?, searchInput: String?, clothList: List<Cloth>?, category: String?) {
            tabLayout ?: return
            clothList ?: return

            val iToSMap = mapOf(0 to 0b0001, 1 to 0b0010, 2 to 0b0100, 3 to 0b1000)

            val adapter: GridAdapter
            if (rv.adapter == null) {
                adapter = GridAdapter()
                rv.adapter = adapter
                rv.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)


                val list = rv.context.resources.getStringArray(R.array.season_array).toList()
                Log.d("setViewMoreCloth", "setViewMoreClothGridRecyclerView: list = ${list}")

                list.forEach {
                    val tab = tabLayout.newTab().apply {
                        text = it
                    }
                    tabLayout.addTab(tab)
                }

                tabLayout.selectTab(tabLayout.getTabAt(0))
            } else {
                adapter = rv.adapter as GridAdapter



                val f = clothList.filter {
                    it.season and (iToSMap[tabLayout.selectedTabPosition] ?: 1) != 0  //시즌 필터링
                            && it.category == category  //top,bottom 등 필터링
                }
                adapter.items = f.filter {
                    if (TextUtils.isEmpty(searchInput)) true
                    else {
                        it.tags.any { t->t.contains(searchInput!!) }|| it.memo.contains(searchInput!!)
                    }
                }

            }

            Log.d("setViewMoreCloth", "setViewMoreClothGridRecyclerView: adapter.items = ${adapter.items}")
            val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val pos = tab?.position ?: return

                    val f = clothList.filter {
                        it.season and (iToSMap[pos] ?: 1) != 0  //시즌 필터링
                                && it.category == category  //top,bottom 등 필터링
                    }
                    adapter.items = f.filter {
                        if (TextUtils.isEmpty(searchInput)) true
                        else {
                            it.tags.any { t->t.contains(searchInput!!) }|| it.memo.contains(searchInput!!)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
            tabLayout.clearOnTabSelectedListeners()
            tabLayout.addOnTabSelectedListener(tabSelectedListener)

            adapter.notifyDataSetChanged()
        }
    }
}