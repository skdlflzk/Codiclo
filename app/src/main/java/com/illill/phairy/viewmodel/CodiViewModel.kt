package com.illill.phairy.viewmodel

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.LeadingMarginSpan
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.illill.phairy.BR
import com.illill.phairy.BaseApplication
import com.illill.phairy.R
import com.illill.phairy.data.model.Cloth
import com.illill.phairy.data.model.Codi
import com.illill.phairy.databinding.ViewCodiInfo1Binding
import com.illill.phairy.databinding.ViewCodiInfo2Binding
import com.illill.phairy.ui.adapter.ClothCircleAdapter
import com.illill.phairy.ui.adapter.ClothCodeAdapter
import com.illill.phairy.ui.adapter.ClothHorizontalCircleAdapter
import com.illill.phairy.ui.adapter.CodiPagerAdapter
import com.illill.phairy.ui.common.UnswipableViewPager
import com.illill.phairy.util.RetrofitClient
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.w3c.dom.Text


class CodiViewModel(private val retrofitClient: RetrofitClient) : ViewModel() {
    private val TAG = this.javaClass.simpleName

    //GridAdapter에서 코디 창을 띄우는 이벤트
    var codiItemClickListener: ((Codi) -> Unit)? = null


    //옷을 선택하여 selected에 추가 / 제거 하는 함수
    var clothCircleItemClickListener: ((Cloth) -> Unit)? = {
        val i = selected.value?.indexOfFirst { s -> s.code == it.code } ?: -1
        if (i == -1) {  //못찾았다면 추가
            _selected.value = (selected.value ?: listOf()) + it
        } else {  //존재한다면 삭제
            _selected.value = (selected.value ?: listOf()) - it
        }
    }

    //수정 버튼 클릭
    var codiEditButtonClicked: ((View, Codi) -> Unit)? = null

    //태그 클릭! 검색
    var codiTagButtonClicked: ((String) -> Unit)? = null

    //삭제 버튼 클릭
    var deleteCodiButtonClicked: ((suspend () -> (Boolean)) -> Unit)? = null


    //현재 수정 중인지 생성 중인지 확인
    val onEdit = MutableLiveData<Boolean>()

    //현재 수정/생성 중인 코디
    private val _codi = MutableLiveData<Codi>()
    val codi: LiveData<Codi> get() = _codi

    //현재 선택된 옷들
    private val _selected = MutableLiveData<List<Cloth>>()
    val selected: LiveData<List<Cloth>> get() = _selected

    //검색어
    val searchInput = MutableLiveData<String>()

    val isSending = MutableLiveData<Boolean>().apply {
        value = false
    }

    //작업할 코디 세팅
    fun setCodi(c: Codi?) {
        _codi.value = c ?: Codi()
    }

    fun restoreSelected(matcher: ((List<String>?) -> (List<Cloth>?))) {
        _selected.value = matcher.invoke(codi.value?.clothes)
    }

    fun deleteCodi(v: View, target: Codi) =
            deleteCodiButtonClicked?.invoke {
                val result: Boolean
                result = withContext(Dispatchers.Default) {
                    try {
                        retrofitClient.deleteCodiAsync(BaseApplication.gson.toJson(target)).await()
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


    fun editorAction(view: View?, actionId: Int?, e: KeyEvent?): Boolean {
        view ?: return true
        Log.d(TAG, "editorAction: $actionId")

        val c = codi.value ?: return true
        val t = (view as TextView).text.toString()

        if (t.isEmpty()) return true
        if (c.tags.contains(t)) return true
        else c.tags += t
        _codi.value = c

        (view as? TextView?)?.text = ""

        return true
    }

    fun search(view: View?, actionId: Int?, e: KeyEvent?): Boolean {
        view ?: return true
        searchInput.value = (view as TextView).text.toString()
        Log.d(TAG, "search: ${searchInput.value}")

        return true
    }

    fun seasonClick(view: View?) {
        val b = when (view?.id) {
            R.id.tv_spring -> 0b0001
            R.id.tv_summer -> 0b0010
            R.id.tv_fall -> 0b0100
            R.id.tv_winter -> 0b1000
            else -> return
        }
        val c = codi.value ?: return

        c.season = c.season xor b
//        Log.d(TAG, "seasonClick: ${c.season }")
        _codi.value = c
    }

    fun clearData() {
        _codi.value = null
        _selected.value = null
        searchInput.value = null
        onEdit.value = null
    }

    private fun getJsonMultipartBody(): RequestBody {

        val clothes = selected.value?.map { it.code } ?: listOf()
        _codi.value?.clothes = clothes

        return RequestBody.create(MediaType.parse("multipart/form-data"), BaseApplication.gson.toJson(codi.value))
    }

    suspend fun add(imageMultiparts: Array<MultipartBody.Part>): Boolean {

        isSending.value = true
        try {
            return retrofitClient.addCodiAsync(imageMultiparts, getJsonMultipartBody()).await()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isSending.postValue(false)
        }
        return false
    }

    suspend fun update(): Boolean {
        isSending.value = true
        try {

            val clothes = selected.value?.map { it.code } ?: listOf()
            _codi.value?.clothes = clothes
            return retrofitClient.updateCodiAsync(BaseApplication.gson.toJson(codi.value)).await()
        } catch (e: Exception) {
            e.printStackTrace()

        } finally {
            isSending.postValue(false)
        }

        return false
    }


    companion object {


        /**
         * 코디 자세히 보기
         * 메모가 차지할 왼쪽 공간을 남겨둔다
         */
        @BindingAdapter(value = ["marginFirstLine", "leftView"])
        @JvmStatic
        fun setMarginFirstLine(tv: TextView, memo: String?, leftView: TextView?) {
            leftView ?: return

            val spannableString = SpannableString(memo)

            val span = object : LeadingMarginSpan.LeadingMarginSpan2 {
                override fun getLeadingMargin(first: Boolean) = if (first) {
                    leftView.width + leftView.left
                } else {
                    0
                }

                override fun drawLeadingMargin(c: Canvas?, p: Paint?, x: Int, dir: Int, top: Int, baseline: Int, bottom: Int, text: CharSequence?, start: Int, end: Int, first: Boolean, layout: Layout?) {}
                override fun getLeadingMarginLineCount() = 1
            }
//

            spannableString.setSpan(span, 0, spannableString.length, 0)

            tv.text = spannableString

        }

        /**
         * * 코디 자세히 보기에서 삭제 불가한 태그 추가
         */
        @BindingAdapter(value = ["readOnlyTags", "tagClickViewModel"])
        @JvmStatic
        fun setReadOnlyTags(chipGroup: ChipGroup, tags: List<String>?, tagClickViewModel: ViewModel?) {
            tags ?: return

            Log.d("readOnlyTags", "setReadOnlyTags: tags  $tags")
            val context = chipGroup.context

            chipGroup.visibility = if (tags.isEmpty()) View.GONE
            else View.VISIBLE
            tags.forEach { tag ->
                val chip = Chip(context)
                chip.text = tag
                chip.setTextColor(ContextCompat.getColor(chipGroup.context, R.color.colorAccent))
                chip.isCheckable = false
                chip.isClickable = true


                // Defined in colors.xml - #ca8eed
                chip.setTextAppearanceResource(R.style.ChipTextStyle)
                chipGroup.addView(chip)

                when (tagClickViewModel) {
                    is CodiViewModel -> {
                        chip.setOnClickListener { tagClickViewModel.codiTagButtonClicked?.invoke(tag) }
                    }
                    is ClothViewModel -> {
                        chip.setOnClickListener { tagClickViewModel.clothTagButtonClicked?.invoke(tag) }
                    }
                }
            }
        }

        /**
         * * 코디 추가에서 태그 삭제/제거
         */
        @BindingAdapter(value = ["rwTags", "codiViewModel"], requireAll = true)
        @JvmStatic
        fun setRWTags(chipGroup: ChipGroup, tags: List<String>?, codiViewModel: CodiViewModel?) {
            tags ?: return
            codiViewModel ?: return

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
                    codiViewModel.codi.value?.let {
                        it.tags = it.tags - tag
                        chipGroup.removeView(chip)
                    }
                }
                chipGroup.addView(chip)
            }
        }

        /**
         * 코디 자세히 보기
         * 해당 계절 표시
         */
        @BindingAdapter("season")
        @JvmStatic
        fun setSeason(chip: Chip, season: Int?) {
            season ?: return

            chip.setChipBackgroundColorResource(R.color.lightGray)

            when (chip.id) {
                R.id.tv_spring -> if (season and 0b0001 == 0b0001) chip.setChipBackgroundColorResource(R.color.colorAccent)
                R.id.tv_summer -> if (season and 0b0010 == 0b0010) chip.setChipBackgroundColorResource(R.color.colorAccent)
                R.id.tv_fall -> if (season and 0b0100 == 0b0100) chip.setChipBackgroundColorResource(R.color.colorAccent)
                R.id.tv_winter -> if (season and 0b1000 == 0b1000) chip.setChipBackgroundColorResource(R.color.colorAccent)
            }
        }

        /**
         * 코디 자세히 보기
         * 코디 사진 리스트 ViewPager에 추가
         */
        @BindingAdapter(value = ["pictures", "indicator"], requireAll = true)
        @JvmStatic
        fun setPictures(vp: ViewPager, pictures: List<Any>?, indicator: WormDotsIndicator?) {
            indicator ?: return
            pictures ?: return

            val adapter: CodiPagerAdapter

            if (vp.adapter == null) {
                adapter = CodiPagerAdapter()
                adapter.items = pictures
                vp.adapter = adapter
            } else {

                adapter = vp.adapter as CodiPagerAdapter
                adapter.items = listOf()    //클리어 후
                adapter.notifyDataSetChanged()
            }

            adapter.items = pictures as List<Any>
            adapter.notifyDataSetChanged()

            indicator.setViewPager(vp)

        }

        /**
         * 코디 만들기
         * app:clothCircle="@{cloth}"
         */
        @BindingAdapter("clothCircle")
        @JvmStatic
        fun setClothCircleImage(iv: ImageView, cloth: Any?) {
            cloth ?: return
            val link = when (cloth) {
                is Cloth -> cloth.iconUrl
                is String -> "${BaseApplication.STORAGE_URL}/clothes/$cloth"
                else -> return
            }

            Log.d("setClothCircleImage", "setClothCircleImage: link = $link")
            Glide.with(iv.context)
                    .load(link)
                    .thumbnail(0.1f)
                    .apply(RequestOptions.circleCropTransform())
                    .into(iv)
        }

        /**
         * 코디 생성 중 가로형 옷 리스트
        app:circleItems="@{circleItems}"
        app:selectedList="@{codiViewModel.selected}"
         */
        @BindingAdapter(value = ["circleItems", "selectedList"], requireAll = true)
        @JvmStatic
        fun setClothCircleRecyclerView(rv: RecyclerView, circleItems: List<Cloth>?, selected: List<Cloth>?) {
            Log.d("d", "setClothCircleRecyclerView: items = ${circleItems}, selected = ${selected}")
            val adapter: ClothCircleAdapter

            if (rv.adapter == null) {
                adapter = ClothCircleAdapter()
                rv.layoutManager = LinearLayoutManager(rv.context, RecyclerView.HORIZONTAL, false)
                rv.adapter = adapter
            } else {
                adapter = rv.adapter as ClothCircleAdapter
            }
            adapter.items = circleItems
            adapter.selected = selected
            adapter.notifyDataSetChanged()
        }


        /**
         * Dialog_codi_info1 에 사용되는 가로 recyclerView
        app:codeList="@{codeList}"
         */
        @BindingAdapter(value = ["codeList"], requireAll = true)
        @JvmStatic
        fun setCircleRecyclerView(rv: RecyclerView, codeItems: List<String>?) {

            Log.d("ClothCodeAdapter", "setCircleRecyclerView: $codeItems")
            codeItems ?: return
            val adapter: ClothCodeAdapter

            if (rv.adapter == null) {
                adapter = ClothCodeAdapter()
                rv.layoutManager = LinearLayoutManager(rv.context, RecyclerView.HORIZONTAL, false)
                rv.adapter = adapter
            } else {
                adapter = rv.adapter as ClothCodeAdapter
            }

            adapter.items = codeItems
            adapter.notifyDataSetChanged()
        }


        /**
         * 코디 생성 중 보여줄 카테고리 별 세로 리스트
        app:myClothList="@{clientViewModel.myClothList}"
        app:searchInput="@{codiViewModel.searchInput}"
         */
        @BindingAdapter(value = ["searchInput", "myClothList"], requireAll = true)
        @JvmStatic
        fun setHorizontalCircleRecyclerView(rv: RecyclerView, searchInput: String?, myClothList: List<Cloth>?) {
            myClothList ?: return

            val adapter: ClothHorizontalCircleAdapter
            if (rv.adapter == null) {
                adapter = ClothHorizontalCircleAdapter()
                rv.layoutManager = LinearLayoutManager(rv.context, RecyclerView.VERTICAL, false)
                rv.adapter = adapter
            } else {
                adapter = rv.adapter as ClothHorizontalCircleAdapter
            }

            Log.d("dd", "setHorizontalCircleRecyclerView: 검색어 = $searchInput")
            adapter.items = myClothList.filter {
                if (TextUtils.isEmpty(searchInput)) {
                    true
                } else {
                    it.tags.any { t -> t.contains(searchInput!!) } || it.memo.contains(searchInput!!)
                }
            }.groupBy { it.category }.map { it.key }
            Log.d("ㅇㅇ", "setHorizontalCircleRecyclerView: 보여줄 카테고리 수 = ${adapter.items}")
            adapter.searchInput = searchInput ?: ""

            adapter.notifyDataSetChanged()
        }

        /**
         * 코디 생성 중 보여줄 카테고리 별 세로 리스트
        app:codi1="@{viewCodi1}"
        app:codi2="@{viewCodi2}"
         */
        @BindingAdapter(value = ["codi1", "codi2"], requireAll = true)
        @JvmStatic
        fun setDialogView(tv: TextView, bind1: ViewCodiInfo1Binding?, bind2: ViewCodiInfo2Binding?) {
            bind1 ?: return
            bind2 ?: return

            bind1.root.visibility = View.VISIBLE
            bind2.root.visibility = View.GONE
            tv.setOnClickListener {
                if (bind1.root.visibility == View.VISIBLE) {

                    bind1.root.visibility = View.GONE
                    bind2.root.visibility = View.VISIBLE

                    it.setBackgroundResource(R.drawable.circle_solid_gray)

                } else {

                    bind1.root.visibility = View.VISIBLE
                    bind2.root.visibility = View.GONE
                    it.setBackgroundResource(R.drawable.circle_solid_red)
                }


            }
        }
    }
}