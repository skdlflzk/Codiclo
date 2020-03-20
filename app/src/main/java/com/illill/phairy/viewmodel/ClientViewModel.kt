package com.illill.phairy.viewmodel

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.illill.phairy.BaseApplication
import com.illill.phairy.BaseApplication.Companion.gson
import com.illill.phairy.BaseApplication.Companion.sharedPreferences
import com.illill.phairy.R
import com.illill.phairy.data.model.Client
import com.illill.phairy.data.model.Cloth
import com.illill.phairy.data.model.Codi
import com.illill.phairy.data.model.Item
import com.illill.phairy.ui.activity.StartActivity
import com.illill.phairy.util.RetrofitClient
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 사용자의 정보를 폭넓게 담당한다.
 * 회원가입 / 수정
 * 프로필 불러오기
 *loadMySelf
 *
 * ######################내 정보
 * @property isClient 사용자 (나)가 로그인 유저인지 확인
 * @property self 현재 저장된 내 정보
 * @property myClothList 내 옷 리스트
 * @property myCodiList 내 코디 리스트
 * @property myFavList 내가 좋아요한 리스트
 *
 *
 *
 */
class ClientViewModel(private val retrofit: RetrofitClient) : ViewModel() {
    private val TAG = this.javaClass.simpleName


    /**
     * 로그인 유저인지 확인
     */
    val isClient: Boolean
        get() = _self.value?.uid ?: "guest" != "guest"

    //나의 정보
    private val _self = MutableLiveData<Client>()
    val self: LiveData<Client> get() = _self
    val I: Client get() = self.value ?: Client()

    //나의 옷
    private val _myCodiList = MutableLiveData<List<Codi>>()
    val myCodiList: LiveData<List<Codi>> get() = _myCodiList

    //나의 코디
    private val _myClothList = MutableLiveData<List<Cloth>>()
    val myClothList: LiveData<List<Cloth>> get() = _myClothList

    val myFavList = MutableLiveData<List<String>>()

    //내가 좋아요한 코디 리스트
    private val _myFavCodiList = MutableLiveData<List<Codi>>()
    val myFavCodiList: LiveData<List<Codi>> get() = _myFavCodiList

    //내가 좋아요한 옷 리스트
    private val _myFavClothList = MutableLiveData<List<Cloth>>()
    val myFavClothList: LiveData<List<Cloth>> get() = _myFavClothList

    //고객의 프로필 보기
    var showProfileButtonClicked: ((String) -> Unit)? = null

    private fun sortChild(list: List<Any>): Pair<List<Codi>, List<Cloth>> {

        val fcodi = mutableListOf<Codi>()
        val fcloth = mutableListOf<Cloth>()
        list.forEach {
            val js = gson.toJson(it)
            val cl = gson.fromJson(js, Cloth::class.java)
            if (TextUtils.isEmpty(cl.category)) {
                val ci = gson.fromJson(js, Codi::class.java)
                fcodi.add(ci)
            } else {
                fcloth.add(cl)
            }
        }
        return Pair(fcodi, fcloth)
    }

    /**
     * uid를 받아 내 정보를 로드
     * 에러 발생시 게스트로 로그인
     */
    suspend fun loadMySelf(uid: String) {
        withContext(Dispatchers.Default) {
            try {
                val result = retrofit.loadSelfAsync(uid).await()
                Log.d(TAG, "loadMySelf: result = $result")
                _self.postValue(result)

                if (result.uid != "guest") {    // 결과가 손님이 아닐 때

                    val cloths = retrofit.loadClothByUidAsync(uid).await()
                    val codis = retrofit.loadCodiByUidAsync(uid).await()
                    val favs = retrofit.loadFavByUidAsync(uid).await()

                    withContext(Dispatchers.Main) {
                        _myClothList.value = cloths.asReversed()
                        _myCodiList.value = codis.asReversed()

                        val (r1, r2) = sortChild(favs)
                        _myFavCodiList.value = r1
                        _myFavClothList.value = r2
                        Log.d(TAG, "loadMySelf: favCodi = ${_myFavCodiList.value}")
                        Log.d(TAG, "loadMySelf: favCloth = ${_myFavClothList.value}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "loadMySelf: ERROR = ")
                _self.postValue(Client())
            }
        }
    }

    fun refreshAsync() = CoroutineScope(Dispatchers.Main).launch {
        loadMySelf(I.uid)
    }

    suspend fun refresh() = loadMySelf(I.uid)


    /**
     * 회원가입할때 임시 닉네임 생성
     */
    fun getTempNickname(resources: Resources): String {
        fun loadFile(res: Int) = resources.openRawResource(res).bufferedReader().use { it.readLine() }
        return gson.fromJson(loadFile(R.raw.pro), Array<String>::class.java).toList().random() +
                gson.fromJson(loadFile(R.raw.post), Array<String>::class.java).toList().random()
    }

    suspend fun joinOrUpdate(client: Client): Boolean {

        return withContext(Dispatchers.Default) {
            try {
                retrofit.joinClientAsync(gson.toJson(client)).await()
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun favButtonClicked(v: View, item: Any) = CoroutineScope(Dispatchers.Main).launch {
        if (item !is Item) return@launch

        if (!isClient) {
            Toast.makeText(v.context, v.context.getString(R.string.error_only_client), Toast.LENGTH_SHORT).show()
            return@launch
        }

        val code = when (item) {
            is Cloth -> item.code
            is Codi -> item.code
            else -> return@launch
        }
        if (myFavCodiList.value?.any { it.code == code } == true || myFavClothList.value?.any { it.code == code } == true) { // 만약 가지고 있으므로 fav에서 제거

            val result = withContext(Dispatchers.Default) {
                try {
                    when (item) {
                        is Cloth -> retrofit.subFavClothAsync(I.uid, code).await()
                        is Codi -> retrofit.subFavCodiAsync(I.uid, code).await()
                        else -> false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            if (result) {
                when (item) {
                    is Cloth ->
                        _myFavClothList.value = (_myFavClothList.value ?: listOf()).filter {
                            it.code != item.code
                        }
                    is Codi ->
                        _myFavCodiList.value = (_myFavCodiList.value ?: listOf()).filter {
                            it.code != item.code
                        }
                    else -> false
                }
                (v as ImageView).setImageResource(R.drawable.ic_favorites_off)
            } else {
                Toast.makeText(v.context, v.context.getString(R.string.error_server), Toast.LENGTH_SHORT).show()
            }
        } else { //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ가지고 있지 않으므로 fav에 추가

            val result = withContext(Dispatchers.Default) {
                try {
                    when (item) {
                        is Cloth -> retrofit.addFavClothAsync(I.uid, code).await()
                        is Codi -> retrofit.addFavCodiAsync(I.uid, code).await()
                        else -> false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            if (result) {
                when (item) {
                    is Cloth -> _myFavClothList.value = (_myFavClothList.value ?: listOf()) + item
                    is Codi -> _myFavCodiList.value = (_myFavCodiList.value ?: listOf()) + item
                }
                (v as ImageView).setImageResource(R.drawable.ic_favorites_on)
            } else {
                Toast.makeText(v.context, v.context.getString(R.string.error_server), Toast.LENGTH_SHORT).show()
            }

        }
    }

    fun followButtonClicked(v: View, dst: String?) = CoroutineScope(Dispatchers.Main).launch {
        dst ?: return@launch

        if (!isClient) {
            Toast.makeText(v.context, v.context.getString(R.string.error_only_client), Toast.LENGTH_SHORT).show()
            return@launch
        }

        if (I.followers.any { it == dst }) { //ㅡㅡㅡㅡ팔로우 중인 상태

            val result = withContext(Dispatchers.Default) {
                try {
                    retrofit.unfollowAsync(I.uid, dst).await()
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            if (result) {
                _self.value?.followers = (_self.value?.followers ?: listOf()).filter { it != dst }

                v.setBackgroundResource(R.drawable.circle_solid_red)
                (v as TextView).text = v.context.getString(R.string.follow_upper)
            } else {
                Toast.makeText(v.context, v.context.getString(R.string.error_server), Toast.LENGTH_SHORT).show()
            }
        } else { //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ팔로우 중이 아님

            val result = withContext(Dispatchers.Default) {
                try {
                    retrofit.followAsync(I.uid, dst).await()
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            if (result) {
                _self.value?.followers = (_self.value?.followers ?: listOf()) + dst
                v.setBackgroundResource(R.drawable.circle_solid_gray)
                (v as TextView).text = v.context.getString(R.string.following)
            } else {
                Toast.makeText(v.context, v.context.getString(R.string.error_server), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun clear() {

        _self.value = null
        _myCodiList.value = null
        _myClothList.value = null
        _myFavClothList.value = null
        _myFavCodiList.value = null

    }

    /**
     *선택한 프사를 Uri->Bitmap 변형 후 멀티파트 이미지 리턴
     */
    private fun getProfileMultipartBody(context: Context, uri: Uri): MultipartBody.Part? {
        var file: File? = null
        try {
            val filePath = context.externalCacheDir?.absolutePath + "/Codiclo"
            val dir = File(filePath)
            if (!dir.exists())
                dir.mkdirs()
            file = File(dir, self.value?.uid)

            var input = context.contentResolver.openInputStream(uri)

            val bitmapOption = BitmapFactory.Options()
            bitmapOption.inJustDecodeBounds = true
            BitmapFactory.decodeStream(input, null, bitmapOption)
            val ratio = max(bitmapOption.outWidth, bitmapOption.outHeight).toFloat() / 600f


            bitmapOption.inSampleSize = ratio.roundToInt()
            bitmapOption.inJustDecodeBounds = false

            //inputStream은 한번 사용 후 소멸됨
            input = context.contentResolver.openInputStream(uri)
            val profile = BitmapFactory.decodeStream(input, null, bitmapOption)

            Log.d(TAG, "getProfileMultipartBody: ratio = $ratio, resultBitmap =${profile?.height} x ${profile?.width}")

            val stream: OutputStream = FileOutputStream(file)
            profile?.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file
                ?: return null)
        return MultipartBody.Part.createFormData("upload", file.name, requestFile)
    }

    private suspend fun updateProfile(context: Context, uri: Uri): Boolean {
        val imagePart = getProfileMultipartBody(context, uri) ?: return false
        return withContext(Dispatchers.Default) {
            try {
                val result = retrofit.updateProfileAsync(imagePart).await()
                withContext(Dispatchers.Main) {
                    if (result) {
                        _self.value = self.value
                        Toast.makeText(context, context.getString(R.string.success_profile), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, context.getString(R.string.error_server), Toast.LENGTH_SHORT).show()
                    }
                }
                result
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }


    fun onPropertyChanged(v: View?, actionId: Int?, e: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            CoroutineScope(Dispatchers.Main).launch {

                val tv = (v as? TextView) ?: return@launch

                if (!updateProperty(tv.id, tv.text.toString())) {
                    if (tv.id == R.id.et_nickname) {
                        Toast.makeText(v.context, v.context.getString(R.string.error_nickname), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(v.context, v.context.getString(R.string.error_server), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(v.context, v.context.getString(R.string.success_property), Toast.LENGTH_SHORT).show()
                }
            }
        }
        return true
    }

    fun onOpenChanged(v: View) {

        if (!isClient) {
            Toast.makeText(v.context, v.context.getString(R.string.error_only_client), Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "onPropertyChanged: to !I.open")
            val result = withContext(Dispatchers.Default) {
                try {
                    retrofit.updateOpenAsync(self.value?.uid
                            ?: "", "open", if (I.open) "0" else "1").await()
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
            if (!result) {
                Toast.makeText(v.context, v.context.getString(R.string.error_server), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(v.context, v.context.getString(R.string.success_property), Toast.LENGTH_SHORT).show()
                _self.value = _self.value?.apply {
                    open = !open
                }
            }
        }
    }

    private suspend fun updateProperty(id: Int, value: String): Boolean {
        val property = when (id) {
            R.id.et_nickname -> "nickname"
            R.id.tv_color -> "color"
            R.id.tv_brand -> "brand"
            R.id.tv_style -> "style"
            R.id.sw_open_account -> "open"
            else -> return false
        }
        return withContext(Dispatchers.Default) {
            try {
                val result = retrofit.updatePropertyAsync(self.value?.uid
                        ?: "", property, value).await()
                if (result) _self.postValue(self.value)
                result
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun selectProfile(v: View) {
        TedImagePicker.with(v.context)
                .start {
                    CoroutineScope(Dispatchers.Main).launch {
                        if (!updateProfile(v.context, it)) {
                            Toast.makeText(v.context, v.context.getString(R.string.error_server), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
    }


    companion object {
        @BindingAdapter("setProfileUrl")
        @JvmStatic
        fun setProfileUrl(iv: ImageView, _url: Any?) {
            var url = _url
            val b = BitmapFactory.decodeResource(iv.resources, R.drawable.img_default)

            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(iv.resources, b)

            when (_url) {
                //uid인지 검사
                is String -> {
                    url = if (_url.contains("http")) _url
                    else "${BaseApplication.STORAGE_URL}/profiles/$_url"
                }
            }

            Glide.with(iv.context)
                    .asBitmap()
                    .load(url)
                    .apply(RequestOptions.circleCropTransform())
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            Log.d("onLoadFailed", "onLoadFailed: failed to load $url")
                            Glide.with(iv.context)
                                    .load(circularBitmapDrawable)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(iv)
                        }

                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            iv.setImageBitmap(resource)
                        }
                    })
        }
    }
}
