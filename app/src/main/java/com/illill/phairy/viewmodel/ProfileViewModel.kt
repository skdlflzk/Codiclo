package com.illill.phairy.viewmodel

import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.illill.phairy.BaseApplication
import com.illill.phairy.R
import com.illill.phairy.data.model.Client
import com.illill.phairy.data.model.Cloth
import com.illill.phairy.data.model.Codi
import com.illill.phairy.data.model.Item
import com.illill.phairy.databinding.LayoutClothBinding
import com.illill.phairy.databinding.LayoutCodiBinding
import com.illill.phairy.ui.adapter.GridAdapter
import com.illill.phairy.util.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log
import android.text.Spannable
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.R.string
import android.util.TypedValue
import androidx.core.content.ContextCompat


/**
 *
 * ######################프로필용 정보
 * @property targetUid 정보를 받아올 유저의 UID
 *
 * @property other 프로필에서 보여줄 유저
 * @property clothList  프로필 유저의 옷 리스트
 * @property codiList 프로필 유저의 코디  리스트
 */

class ProfileViewModel(val retrofit: RetrofitClient) : ViewModel() {
    private val TAG = this.javaClass.simpleName

    var targetUid: String? = null

    //프로필에서 보여줄 유저 정보.
    private val _other = MutableLiveData<Client>()
    val other: LiveData<Client> get() = _other

    //프로필 에서 보여줄 코디들
    private val _codiList = MutableLiveData<List<Codi>>()
    val codiList: LiveData<List<Codi>> get() = _codiList

    //프로필 에서 보여줄 옷
    private val _clothList = MutableLiveData<List<Cloth>>()
    val clothList: LiveData<List<Cloth>> get() = _clothList

    //내 정보인지 표시
    private val _me = MutableLiveData<Boolean>()
    val me: LiveData<Boolean> get() = _me


    //favorite item 가져오기
    private val _favCodiList = MutableLiveData<List<Codi>>()
    val favCodiList: LiveData<List<Codi>> get() = _favCodiList

    private val _favClothList = MutableLiveData<List<Cloth>>()
    val favClothList: LiveData<List<Cloth>> get() = _favClothList

    val favSearchInput = MutableLiveData<String>()


    var viewMoreButtonClicked: ((String) -> Unit)? = null


    suspend fun loadProfile(force: Boolean) {
        //이미 받아져온 상태라면 굳이 받아오지 말까? 갱신이 필요한가?
        val target = targetUid ?: return
        Log.d(TAG, "loadProfile: target = $target")
//        if (TextUtils.isEmpty(target) && target == other.value?.uid && !force) return@launch
        _me.value = false
        val result: Client
        result = withContext(Dispatchers.Default) {
            try {
                retrofit.loadClientAsync(target).await()
            } catch (e: Exception) {
                e.printStackTrace()
                Client()
            }
        }

        Log.d(TAG, "loadProfile: result = $result")
        _other.value = result

    }

    private fun sortChild(list: List<Any>): Pair<List<Codi>, List<Cloth>> {

        val fcodi = mutableListOf<Codi>()
        val fcloth = mutableListOf<Cloth>()
        list.forEach {
            val js = BaseApplication.gson.toJson(it)
            val cl = BaseApplication.gson.fromJson(js, Cloth::class.java)
            if (TextUtils.isEmpty(cl.category)) {
                val ci = BaseApplication.gson.fromJson(js, Codi::class.java)
                fcodi.add(ci)
            } else {
                fcloth.add(cl)
            }
        }
        return Pair(fcodi, fcloth)
    }

    suspend fun loadInventory(force: Boolean) {
//        if (TextUtils.isEmpty(targetUid) && targetUid == other.value?.uid && !force) return@launch

        withContext(Dispatchers.Default) {
            try {

                val uid = other.value?.uid ?: return@withContext
                Log.d(TAG, "loadInventory: $uid")
                _clothList.postValue(retrofit.loadClothByUidAsync(uid).await())
                _codiList.postValue(retrofit.loadCodiByUidAsync(uid).await())

                val favs = retrofit.loadFavByUidAsync(uid).await()

                val (r1, r2) = sortChild(favs)
                _favCodiList.postValue(r1)
                _favClothList.postValue(r2)

                Log.d(TAG, "loadInventory: loaded Cloth  = ${clothList.value}")
                Log.d(TAG, "loadInventory: loaded Clothes =  = ${codiList.value}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "loadProfile: ERROR = ")
                _other.postValue(Client())
            }
        }
    }


    fun syncMyInfo(clientViewModel: ClientViewModel) {
        _me.value = true
//        _other.value = clientViewModel.self.value
    }


    companion object {

        /**
         * 코디 / 옷을 선택하는 어댑터
         * Codi, Cloth 두 개 탭을 가진 tabLayout을 추가
         * viewPager에 layout_codi,layout_cloth 두 페이지를 생성
         */
        @BindingAdapter(value = ["otherCodiList", "otherClothList", "tabInventory"], requireAll = true)
        @JvmStatic
        fun setInventoryViewPager(vp: ViewPager, otherCodiList: List<Codi>?, otherClothList: List<Cloth>?, tabInventory: TabLayout?) {
            tabInventory ?: return
            Log.d("otherCodiList", "instantiateItem: size = $otherCodiList")
            Log.d("otherClothList", "instantiateItem: size = $otherClothList")

            val layouts = listOf(R.layout.layout_codi, R.layout.layout_cloth)

            vp.adapter = object : PagerAdapter() {
                override fun instantiateItem(container: ViewGroup, position: Int): Any {
                    when (position) {
                        0 -> {
                            val binding = DataBindingUtil.inflate<LayoutCodiBinding>(LayoutInflater.from(container.context), layouts[position], null, false)
                            binding.codiList = otherCodiList
                            container.addView(binding.root)
                            binding.executePendingBindings()
                            return binding.root
                        }
                        1 -> {
                            val binding = DataBindingUtil.inflate<LayoutClothBinding>(LayoutInflater.from(container.context), layouts[position], null, false)
                            binding.clothList = otherClothList
                            container.addView(binding.root)
                            binding.executePendingBindings()
                            return binding.root

                        }
                    }
                    return super.instantiateItem(container, position)
                }

                override fun isViewFromObject(view: View, `object`: Any) = view == `object`
                override fun getCount() = 2
                override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                    container.removeView(`object` as View)
                }
            }



            tabInventory.setupWithViewPager(vp)



            val counts = listOf(otherCodiList?.size, otherClothList?.size)
            val darkGray = ContextCompat.getColor(vp.context, R.color.iconSelect)
            val size_l = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, vp.context.resources.displayMetrics)
            val size_s = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, vp.context.resources.displayMetrics)
            val arr = vp.context.resources.getStringArray(R.array.profile_tab)

            fun setTabText(tv: TextView, position: Int) {

                val count = "${counts[position] ?: 0}\n"
                val sp1 = SpannableStringBuilder(count)


                sp1.setSpan(ForegroundColorSpan(darkGray), 0, count.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                sp1.setSpan(AbsoluteSizeSpan(size_l.toInt()), 0, count.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                tv.append(sp1)
                val sp2 = SpannableStringBuilder(arr[position])


                val color = if (vp.currentItem == position) ContextCompat.getColor(vp.context, R.color.colorAccent) else ContextCompat.getColor(vp.context, R.color.darkGray)
//                val lightGray = ContextCompat.getColor(vp.context, R.color.darkGray)

                sp2.setSpan(ForegroundColorSpan(color), 0, arr[position].length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                sp2.setSpan(AbsoluteSizeSpan(size_s.toInt()), 0, arr[position].length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                tv.append(sp2)
            }
            vp.context.resources.getStringArray(R.array.profile_tab).forEachIndexed { index, s ->
                val tv = TextView(vp.context)
                tv.setLines(2)
                tv.maxLines = 2
                tv.gravity = Gravity.CENTER
                setTabText(tv,index)
                tabInventory.getTabAt(index)?.customView = tv
            }

            vp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {



                override fun onPageScrollStateChanged(state: Int) {}
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageSelected(position: Int) {
                    (0..1).forEach {
                        val tv = tabInventory.getTabAt(it)?.customView  as? TextView ?: return
                        tv.text = ""
                        setTabText(tv, it)
                    }
                }
            })
            vp.currentItem = 0
        }


        /**
         *
         * layout_codi 내부의 그리드 RecyclerView
         *
        //        app:gridCodi="@{profileViewModel.codiList}"
        //        app:tabSeason="@{tabSeason}"
         */
        @BindingAdapter(value = ["gridCodi", "tabSeason"], requireAll = true)
        @JvmStatic
        fun setSeasonedRecyclerView(rv: RecyclerView, gridItem: List<Codi>?, tabLayout: TabLayout?) {
            tabLayout ?: return
            gridItem ?: return

            val iToSMap = mapOf(0 to 0b0001, 1 to 0b0010, 2 to 0b0100, 3 to 0b1000)

            val adapter: GridAdapter
            if (rv.adapter == null) {
                adapter = GridAdapter()
                rv.adapter = adapter
                rv.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

                tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabReselected(tab: TabLayout.Tab?) {}

                    override fun onTabUnselected(tab: TabLayout.Tab?) {}

                    override fun onTabSelected(tab: TabLayout.Tab?) {

                        adapter.items = gridItem.filter {
                            it.season and (iToSMap[tab?.position] ?: 0) != 0  //시즌을 포함하는 코디를 필터링
                        }

                        adapter.notifyDataSetChanged()
                    }
                })
                rv.context.resources.getStringArray(R.array.season_array).forEach {
                    val tab = tabLayout.newTab().apply {
                        text = it
                    }
                    tabLayout.addTab(tab)
                }
            } else {
                adapter = rv.adapter as GridAdapter

            }
            adapter.items = gridItem.filter {
                it.season and 0b0001 != 0  //시즌을 포함하는 코디를 필터링
            }

            Log.d("setSeasonedRecycler", "setSeasonedRecyclerView: full $gridItem")
            Log.d("setSeasonedRecycler", "setSeasonedRecyclerView: filter = ${adapter.items}")
            adapter.notifyDataSetChanged()

        }


        // Fragment_favorite 에서 검색어에따라 옷 +코디를 보여준다
//        app:gridCodi="@{profileViewModel.me?clientViewModel.myCodiList:profileViewModel.codiList}"
//        app:gridCloth="@{profileViewModel.me?clientViewModel.myClothList:profileViewModel.clothList}"
//        app:favSearchInput="@{profileViewModel.favSearchInput}"
        @BindingAdapter(value = ["gridCodi", "gridCloth", "favSearchInput"], requireAll = true)
        @JvmStatic
        fun setSeasonedRecyclerView(rv: RecyclerView, gridCodi: List<Codi>?, gridCloth: List<Cloth>?, favSearchInput: String?) {
            gridCodi ?: return
            gridCloth ?: return

            val adapter: GridAdapter
            if (rv.adapter == null) {

                val manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                manager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
                rv.layoutManager = manager

                adapter = GridAdapter()
                adapter.setHasStableIds(true)
                rv.adapter = adapter

            } else {
                adapter = rv.adapter as GridAdapter
            }

            adapter.items = gridCloth.filter {
                it.tags.toString().contains(favSearchInput ?: "")
                        || it.memo.contains(favSearchInput ?: "")
            } + gridCodi.filter {
                it.tags.toString().contains(favSearchInput ?: "")
                        || it.memo.contains(favSearchInput ?: "")
            }
            adapter.notifyDataSetChanged()

        }


    }
}
