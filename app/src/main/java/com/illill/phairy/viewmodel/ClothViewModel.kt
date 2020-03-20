package com.illill.phairy.viewmodel

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import com.illill.phairy.BaseApplication
import com.illill.phairy.BaseApplication.Companion.gson
import com.illill.phairy.R
import com.illill.phairy.data.model.Client
import com.illill.phairy.data.model.Cloth
import com.illill.phairy.ui.adapter.Cloth1LineAdapter
import com.illill.phairy.ui.adapter.ClothHorizontalAdapter
import com.illill.phairy.ui.adapter.GridAdapter
import com.illill.phairy.util.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ClothViewModel(private val retrofitClient: RetrofitClient) : ViewModel() {
    private val TAG = this.javaClass.simpleName

    //현재 수정 중인지 생성 중인지 확인
    val onEdit = MutableLiveData<Boolean>()

    //현재 수정/생성 중인 옷
    private val _cloth = MutableLiveData<Cloth>()
    val cloth: LiveData<Cloth> get() = _cloth

    //태그 클릭! 검색
    var clothTagButtonClicked: ((String) -> Unit)? = null

    //삭제 버튼 클릭
    var deleteClothButtonClicked: ((suspend () -> (Boolean)) -> Unit)? = null

    //수정 버튼 클릭
    var clothEditButtonClicked: ((View, Cloth) -> Unit)? = null

    //옷 아이템 클릭 시 이벤트
    var clothItemClickListener: ((Cloth) -> Unit)? = null

    //Dialog_codi에서 code로 Cloth 열람
    var clothCodiItemClickListener: ((String) -> Unit)? = { code ->
        if (clothItemClickListener != null) {

            CoroutineScope(Dispatchers.Main).launch {

                val cloth = withContext(Dispatchers.Default) {
                    try {
                        retrofitClient.loadClothByCodeAsync(code).await()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }

                }
                clothItemClickListener?.invoke(cloth ?: return@launch)
            }
        }
    }


    val isSending = MutableLiveData<Boolean>()?.apply {
        value = false
    }


    //삭제 버튼 클릭
    fun deleteCloth(v: View, target: Cloth) =
            deleteClothButtonClicked?.invoke {
                val result: Boolean
                result = withContext(Dispatchers.Default) {
                    try {
                        retrofitClient.deleteClothAsync(gson.toJson(target)).await()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                }

                if (result) {
                    Toast.makeText(v.context, v.context.getString(R.string.deleted), Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(v.context, v.context.getString(R.string.error_server), Toast.LENGTH_SHORT).show()
                }
                return@invoke result
            }

    //작업할 옷 세팅
    fun setCloth(c: Cloth?) {
        _cloth.value = c ?: Cloth()
    }


    //태그 EditText에서 엔터버튼 클릭시
    fun editorAction(view: View?, actionId: Int?, e: KeyEvent?): Boolean {
        view ?: return true
        Log.d(TAG, "editorAction: $actionId")

        val c = cloth.value ?: return true
        val t = (view as TextView).text.toString()

        if (t.isEmpty()) return true
        if (c.tags.contains(t)) return true
        else c.tags += t
        _cloth.value = c

        (view as? TextView?)?.text = ""

        return true
    }


    //옷 생성 창에서 계절 클릭시
    fun seasonClick(view: View?) {
        val b = when (view?.id) {
            R.id.tv_spring -> 0b0001
            R.id.tv_summer -> 0b0010
            R.id.tv_fall -> 0b0100
            R.id.tv_winter -> 0b1000
            else -> return
        }
        val c = cloth.value ?: return

        c.season = c.season xor b
//        Log.d(TAG, "seasonClick: ${c.season }")
        _cloth.value = c
    }


    //옷 생성 창에서    <   TOP   >  카테고리 이동
    fun arrowButtonClicked(view: View) {
        val c = cloth.value ?: return
        val clothList = view.context.resources.getStringArray(R.array.cloth_array)
        val i = clothList.indexOfFirst { c.category == it } + when (view.id) {
            R.id.iv_left -> -1
            R.id.iv_right -> 1
            else -> return
        }

        when (i) {
            in -100..-1 -> c.category = clothList.last()
            in 0 until clothList.size -> c.category = clothList[i]
            in clothList.size..100 -> c.category = clothList.first()
        }

        _cloth.value = c
    }


    //옷을 업로드할때 Json 형식의 post 용 데이터로 만든다.
    private fun getJsonMultipartBody(): RequestBody {
        return RequestBody.create(MediaType.parse("multipart/form-data"), gson.toJson(cloth.value))
//        return MultipartBody.Part.createFormData(MediaType.parse("text/plain"), gson.toJson(cloth.value))
    }


    //Cloth 게시물을 새로 생성하기 위해 업로드한다.
    suspend fun add(imageMultipart: MultipartBody.Part): Boolean {

        isSending.value = true
        try {
            val result = retrofitClient.addClothAsync(arrayOf(imageMultipart), getJsonMultipartBody()).await()

            isSending.value = false
            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isSending.value = false
        Log.d(TAG, "add: ${isSending.value}")
        return false
    }

    suspend fun update(): Boolean {
        isSending.value = true
        try {
            val result = retrofitClient.updateClothAsync(gson.toJson(cloth.value)).await()

            isSending.value = false
            return result
        } catch (e: Exception) {
            e.printStackTrace()

        }
        isSending.value = false
        Log.d(TAG, "update: ${isSending.value}")

        return false
    }

    companion object {


        /**
         * * 코디 추가에서 태그 삭제/제거
         */
        @BindingAdapter(value = ["rwTags", "clothViewModel"], requireAll = true)
        @JvmStatic
        fun setRWTags(chipGroup: ChipGroup, tags: List<String>?, clothViewModel: ClothViewModel?) {
            tags ?: return
            clothViewModel ?: return


            val context = chipGroup.context


            chipGroup.removeAllViews()

            tags.forEach { tag ->
                val chip = Chip(context)
                chip.text = tag
                chip.setTextColor(ContextCompat.getColor(chipGroup.context, R.color.colorAccent))
                chip.isCheckable = false
                chip.isClickable = true

                // Defined in colors.xml - #ca8eed
                chip.setTextAppearanceResource(R.style.ChipTextStyle)
//                chip.setCloseIconResource(R.drawable.ic_action_navigation_close)
                chip.isCloseIconVisible = true

                //Added click listener on close icon to remove tag from ChipGroup
                chip.setOnCloseIconClickListener {
                    clothViewModel.cloth.value?.let {
                        it.tags = it.tags - tag
                        chipGroup.removeView(chip)
                    }
                }
                chipGroup.addView(chip)
            }
        }

        /**
         * * 코디 추가에서 태그 삭제/제거
         */
        @BindingAdapter(value = ["pickCloth", "currentCategory"])
        @JvmStatic
        fun setCarousel(vp: ViewPager, clothViewModel: ClothViewModel?, currentCategory: String?) {
            clothViewModel ?: return
            currentCategory ?: return

            val clothList = vp.context.resources.getStringArray(R.array.cloth_array)

            if (vp.adapter == null) {
                val textAdapter = object : PagerAdapter() {
                    override fun instantiateItem(container: ViewGroup, position: Int): Any {
                        val tv = TextView(vp.context)
                        tv.setTextAppearance(vp.context, R.style.UbuntuTextViewStyle)
                        tv.text = clothList[position]
                        tv.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9f, vp.context.resources.displayMetrics)
                        tv.gravity = Gravity.CENTER
                        container.addView(tv)
                        return tv
                    }

                    override fun isViewFromObject(view: View, `object`: Any) = view == `object`
                    override fun getCount() = clothList.size
                    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                        container.removeView(`object` as View)
                    }
                }
                vp.adapter = textAdapter
            }
            vp.currentItem = clothList.indexOfFirst { it == currentCategory }

        }

        /**
         * dialog_cloth item_cloth 옷 로드
         * app:cloth="@{cloth}"
         * app:self="@{self}"
         */
        @BindingAdapter(value = ["cloth", "self"])
        @JvmStatic
        fun setClothImage(iv: ImageView, cloth: Cloth?, self: Client?) {
            cloth ?: return
            val url = if (self?.uid == cloth.uid || cloth.open) cloth.iconUrl else R.drawable.preview2
            Glide.with(iv.context)
                    .load(url)
                    .thumbnail(0.1f)
                    .into(iv)

        }

        /**
         * dialog_cloth item_cloth 옷 로드
         * app:cloth="@{cloth}"
         * app:self="@{self}"
         */
        @BindingAdapter(value = ["buyLink"])
        @JvmStatic
        fun setLinkButton(tv: TextView, link: String?) {
            if(TextUtils.isEmpty(link)) return

            tv.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW,Uri.parse(link))
                tv.context.startActivity(intent)
            }
        }

        /**
         *
         * item_horizontal_cloth에서
         * 옷 리스트를 가로로 하나씩 나열
         * app:category="@{category}"
         * app:season="@{season}"
         * app:clothes="@{clothes}"
         */
        @BindingAdapter(value = ["category", "season", "clothes"], requireAll = true)
        @JvmStatic
        fun setHorizontalClothRecylerView(rv: RecyclerView, category: String?, season: Int?, clothes: List<Cloth>) {
//            clothList ?: return
            category ?: return
            season ?: return
            val adapter: Cloth1LineAdapter
            if (rv.adapter == null) {
                adapter = Cloth1LineAdapter()
                rv.adapter = adapter
                rv.layoutManager = LinearLayoutManager(rv.context, RecyclerView.HORIZONTAL, false)
            } else {
                adapter = rv.adapter as Cloth1LineAdapter
            }
            adapter.items = clothes.filter {
                it.season and season != 0
                        && it.category == category
            }

        }

        /**
         * layout_cloth에서
         * 1. 계절선택하기
         * 2/ 옷 가로 리스트를 세로로 쌓기
        app:allClothList="@{clientViewModel.clothList}"
        app:tabInventory="@{tabInventory}"
         */
        @BindingAdapter(value = ["allClothList", "tabInventory"], requireAll = true)
        @JvmStatic
        fun setHorizontalClothRecylerView(rv: RecyclerView, allClothList: List<Cloth>?, tabLayout: TabLayout?) {
            tabLayout ?: return
            allClothList ?: return
            Log.d("allClothList", "setHorizontalClothRecylerView: allClothList = ${allClothList}")
            val adapter: ClothHorizontalAdapter
            if (rv.adapter == null) {
                adapter = ClothHorizontalAdapter()
                rv.adapter = adapter
                rv.layoutManager = LinearLayoutManager(rv.context, RecyclerView.VERTICAL, false)

                val iToSMap = mapOf(0 to 0b0001, 1 to 0b0010, 2 to 0b0100, 3 to 0b1000)

                tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabReselected(tab: TabLayout.Tab?) {}

                    override fun onTabUnselected(tab: TabLayout.Tab?) {}

                    override fun onTabSelected(tab: TabLayout.Tab?) {

                        adapter.items = allClothList.filter {
                            it.season and (iToSMap[tab?.position] ?: 1) != 0  //시즌을 포함하는 코디를 필터링
                        }.groupBy { it.category }.map { it.key }

                        adapter.season = iToSMap[tab?.position] ?: 1
                        adapter.notifyDataSetChanged()
                    }
                })
                val list = rv.context.resources.getStringArray(R.array.season_array).toList()

                list.forEach {
                    val tab = tabLayout.newTab().apply {
                        text = it
                    }
                    tabLayout.addTab(tab)
                }

            } else {
                adapter = rv.adapter as ClothHorizontalAdapter
            }

            adapter.season = 0b0001
            adapter.items = allClothList.filter {
                it.season and 0b0001 != 0  //시즌을 포함하는 코디를 필터링
            }.groupBy { it.category }.map { it.key }
            adapter.clothes = allClothList

            Log.d("setHorizontalCloth", "setHorizontalClothRecylerView: ${adapter.items}")
            adapter.notifyDataSetChanged()
        }

    }
}