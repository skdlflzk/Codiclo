package com.illill.phairy.ui.activity

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.illill.phairy.BaseApplication
import com.illill.phairy.BuildConfig
import com.illill.phairy.R
import com.illill.phairy.data.model.Cloth
import com.illill.phairy.data.model.Codi
import com.illill.phairy.databinding.*
import com.illill.phairy.ui.fragment.*
import com.illill.phairy.viewmodel.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = MainActivity::class.java.simpleName

    private lateinit var binding: ActivityMainBinding


    private val codiViewModel by inject<CodiViewModel>()
    private val clothViewModel by inject<ClothViewModel>()
    private val clientViewModel by inject<ClientViewModel>()
    private val profileViewModel by inject<ProfileViewModel>()


    private val _moveToCodiActivity = 1001
    private val _moveToClothActivity = 1002


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        Log.d(TAG, "onCreate: --MainActivity--")

        checkVersion()

        setListener()
        setButtons()
        setFragments()

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_add -> {
                if (clientViewModel.isClient) selectAddItem()
                else Toast.makeText(applicationContext, getString(R.string.error_only_client), Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: backpressed")

        if (binding.vpFragment.currentItem == 0 || binding.vpFragment.currentItem == 1) {
            backbuttonSubject.onNext(System.currentTimeMillis())
        } else {

            binding.vpFragment.currentItem = 1
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == _moveToCodiActivity && resultCode == Activity.RESULT_OK) {

            dialogs.toList().forEach {
                it.second.dismiss() //모두 종료
            }
            refreshData()
        } else if (requestCode == _moveToClothActivity && resultCode == Activity.RESULT_OK) {

            dialogs.toList().forEach {
                it.second.dismiss() //모두 종료
            }
            refreshData()
        }
    }


    fun refreshData() {
        Log.d(TAG, "refreshData: ")
        CoroutineScope(Dispatchers.Main).launch {

            withContext(Dispatchers.Default) {
                clientViewModel.refresh()
            }
            (binding.vpFragment.adapter as PagerAdapter).refresh()
        }
    }


    private fun setListener() {
        codiViewModel.codiItemClickListener = { popUpCodiDetail(it) }
        codiViewModel.deleteCodiButtonClicked = { deleteButtonClicked(it) }
        codiViewModel.codiEditButtonClicked = { v, codi -> moveToCodiActivity(v, codi) }

        clothViewModel.deleteClothButtonClicked = { deleteButtonClicked(it) }
        clothViewModel.clothItemClickListener = { popUpClothDetail(it) }
        clothViewModel.clothEditButtonClicked = { v, cloth -> moveToClothActivity(v, cloth) }

        clientViewModel.showProfileButtonClicked = { moveToOtherProfile(it) }


        profileViewModel.viewMoreButtonClicked = { moveToShowMore(it) }

    }


    private fun moveToLogin() {
        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
    }


    private fun setButtons() {
        binding.ivAdd.setOnClickListener(this)
    }


    private fun selectAddItem() {

        if (!clientViewModel.isClient) {
            Toast.makeText(applicationContext, applicationContext.getString(R.string.error_only_client), Toast.LENGTH_SHORT).show()
            return
        }

        val bottomDialog = BottomSheetDialog(this@MainActivity, R.style.CustomBottomSheetDialogTheme)
        bottomDialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
        }


        val bind = DataBindingUtil.inflate<DialogBottomSelectBinding>(layoutInflater, R.layout.dialog_bottom_select, null, false)
        bottomDialog.setContentView(bind.root)

//        bind.tvAddCloth.setOnClick
        bind.clCancel.setOnClickListener { bottomDialog.dismiss() }
        bind.clAddCloth.setOnClickListener {
            moveToClothActivity(it)

            bottomDialog.dismiss()
        }
        bind.clAddCodi.setOnClickListener {
            moveToCodiActivity(it)
            bottomDialog.dismiss()
        }
        bottomDialog.show()
    }


    private fun moveToClothActivity(v: View, cloth: Cloth? = null) {
        val intent = Intent(applicationContext, ClothActivity::class.java)

        when (v.id) {
            R.id.tv_edit -> {
                intent.putExtra("cloth", BaseApplication.gson.toJson(cloth))
            }
            R.id.cl_add_cloth -> Unit
        }
        startActivityForResult(intent, _moveToClothActivity)

    }


    //코디 생성 / 수정 화면으로 이동한다
    private fun moveToCodiActivity(v: View, codi: Codi? = null) {

        val intent = Intent(applicationContext, CodiActivity::class.java)
        when (v.id) {
            R.id.tv_edit -> {
                intent.putExtra("codi", BaseApplication.gson.toJson(codi))
            }
            R.id.cl_add_cloth -> Unit
        }
        startActivityForResult(intent, _moveToCodiActivity)
    }


    private val dialogs = mutableMapOf<String, Dialog>()
    private fun popUpCodiDetail(codi: Codi) {

        if(!codi.open && codi.uid !=clientViewModel.I.uid){
            Toast.makeText(this, getString(R.string.is_closed_post), Toast.LENGTH_SHORT).show()
            return
        }
        val bind = DataBindingUtil.inflate<DialogCodiBinding>(layoutInflater, R.layout.dialog_codi, null, false)
        val codiDialog = AlertDialog.Builder(this@MainActivity)
                .setView(bind.root)
                .create()
        codiDialog.setCanceledOnTouchOutside(true)
        codiDialog.setCancelable(true)
        codiDialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

        }

        bind.codi = codi
        bind.clientViewModel = clientViewModel
        bind.codiViewModel = codiViewModel
        bind.lifecycleOwner = this

        Log.d(TAG, "popUpCodi: codiViewModel.isSending.value = ${codiViewModel.isSending.value}")
        codiDialog.setOnDismissListener {
            dialogs.toList().forEach {
                dialogs[it.first]?.dismiss()
            }
            dialogs.clear()
        }
        codiDialog.show()
        val width = 0.86f * resources.displayMetrics.widthPixels
//        val height = 0.84f * resources.displayMetrics.heightPixels
        codiDialog.window?.setLayout((width).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)

        dialogs["Codi"] = codiDialog
    }


    private fun popUpClothDetail(cloth: Cloth) {

        if(!cloth.open && cloth.uid !=clientViewModel.I.uid){
            Toast.makeText(this, getString(R.string.is_closed_post), Toast.LENGTH_SHORT).show()
            return
        }

        val bind = DataBindingUtil.inflate<DialogClothBinding>(layoutInflater, R.layout.dialog_cloth, null, false)
        val clothDialog = AlertDialog.Builder(this@MainActivity)
                .setView(bind.root)
                .create()
        clothDialog.setCancelable(true)
        clothDialog.setCanceledOnTouchOutside(true)
        clothDialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
        }

        Log.d(TAG, "popUpCloth: clothViewModel.isSending.value = ${clothViewModel.isSending.value}")
        bind.cloth = cloth
        bind.clientViewModel = clientViewModel
        bind.clothViewModel = clothViewModel

        bind.lifecycleOwner = this

        clothDialog.setOnDismissListener {
            dialogs.toList().forEach {
                if (it.first != "Codi") dialogs[it.first]?.dismiss()
            }
            dialogs.clear()
        }

        clothDialog.show()

        val width = 0.86f * resources.displayMetrics.widthPixels
//        val height = 0.84f * resources.displayMetrics.heightPixels
        clothDialog.window?.setLayout((width).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)

        dialogs["Cloth"] = clothDialog
    }


    private fun deleteButtonClicked(delete: suspend (() -> Boolean)) {

        val bind = DataBindingUtil.inflate<DialogDeleteBinding>(layoutInflater, R.layout.dialog_delete, null, false)
        val deleteDialog = AlertDialog.Builder(this@MainActivity)
                .setView(bind.root)
                .create()

        deleteDialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        bind.btnCancle.setOnClickListener { deleteDialog.dismiss() }
        bind.btnOk.setOnClickListener {
            dialogs.toList().forEach {
                dialogs[it.first]?.dismiss()
            }
            dialogs.clear()

            CoroutineScope(Dispatchers.Main).launch {
                val refresh = delete.invoke()
                Log.d(TAG, "deleteButtonClicked: $refresh")
                if (refresh) {
                    refreshData()
                }
            }
        }
        deleteDialog.show()

        val width = resources.displayMetrics.widthPixels * 0.725F
        val height = width * 0.5F
        deleteDialog.window?.setLayout(width.toInt(), height.toInt())

        dialogs["delete"] = deleteDialog
    }

    fun moveToProfile() {

        if (profileViewModel.me.value == false) {
            binding.vpFragment.currentItem = 5
        } else {
            binding.vpFragment.currentItem = 1
        }

    }

    private fun moveToOtherProfile(uid: String) {
        Log.d(TAG, "moveToOtherProfile: uid = $uid")
        if (uid == clientViewModel.I.uid) {
            profileViewModel.targetUid = null
            binding.vpFragment.currentItem = 1
            return
        }
        profileViewModel.targetUid = uid
        binding.vpFragment.currentItem = 5
    }

    fun moveToFollow(isFollower: Boolean) {

        val followViewModel by viewModel<FollowViewModel>()
        Log.d(TAG, "moveToFollow: me? ${profileViewModel.me.value}")
        val list = when (profileViewModel.me.value) {
            true -> {
                when (isFollower) {
                    true -> clientViewModel.I.followers
                    false -> clientViewModel.I.followings
                }
            }
            false -> {
                when (isFollower) {
                    true -> profileViewModel.other.value?.followers ?: listOf()
                    false -> profileViewModel.other.value?.followings ?: listOf()
                }
            }
            else -> return
        }
        followViewModel.setFollow(list, isFollower)
        binding.vpFragment.currentItem = 3
    }


    fun moveToFavorite() {
        binding.vpFragment.currentItem = 4
    }

    fun moveToHome() {
        binding.vpFragment.currentItem = 0
    }

    private fun moveToShowMore(category: String) {

        val closetViewModel by viewModel<ClosetViewModel>()
        closetViewModel.setCategory(category)
        binding.vpFragment.currentItem = 2
    }

    private fun setFragments() {
        val adapter = PagerAdapter(supportFragmentManager)
        binding.vpFragment.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.vpFragment)
        binding.tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_home_on)
        binding.tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_profile_off)

        (binding.tabLayout.getTabAt(5)?.view as? LinearLayout)?.visibility = View.GONE
        (binding.tabLayout.getTabAt(4)?.view as? LinearLayout)?.visibility = View.GONE
        (binding.tabLayout.getTabAt(3)?.view as? LinearLayout)?.visibility = View.GONE
        (binding.tabLayout.getTabAt(2)?.view as? LinearLayout)?.visibility = View.GONE


    }


    private val backbuttonSubject = BehaviorSubject.createDefault(0L)
    private val disposable = backbuttonSubject.toFlowable(BackpressureStrategy.BUFFER)
            .buffer(2, 1)
            .map { it[1] - it[0] < 2000 }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ b ->
                if (b) {
                    val d = Maybe.just(true)
                            .subscribeOn(Schedulers.io())
                            .subscribe {
                                Glide.get(this@MainActivity).clearDiskCache()
                            }
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
                }

            }, { it.printStackTrace() }, { })


    private fun checkVersion() {

        val version: String

        try {

            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                    // Debug일 때 Developer Mode를 enable 하여 캐쉬 설정을 변경한다.
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build()

            remoteConfig.setConfigSettings(configSettings)
            // 기본 캐쉬 만료시간은 12시간이다. Developer Mode 여부에 따라 fetch()에 적설한 캐시 만료시간을 넘긴다.
            remoteConfig.fetch(0).addOnCompleteListener { task ->
                if (task.isSuccessful) remoteConfig.activateFetched()
            }

            val i = packageManager.getPackageInfo(packageName, 0)
            version = i.versionName
            Log.d(TAG, "**phairy version = $version")

            val version_s = remoteConfig.getString("version")

            Log.d(TAG, "checkVersion: server version = $version_s")

            val must = remoteConfig.getBoolean("must_update")
            Log.d(TAG, "checkVersion: must = $must")
            if (version < version_s) {
                if (must) {
                    Toast.makeText(applicationContext, "새 버전이 출시되었습니다", Toast.LENGTH_LONG).show()
                    val uri = Uri.parse("market://details?id=$packageName")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                } else {
                    AlertDialog.Builder(this@MainActivity)
                            .setTitle("업데이트 알림")
                            .setMessage("티클 최신버전이 등록되었습니다.\n업데이트할까요?")
                            .setPositiveButton("업데이트") { d: DialogInterface, _: Int ->
                                val uri = Uri.parse("market://details?id=$packageName")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                d.dismiss()
                                startActivity(intent)
                            }
                            .setNegativeButton("취소") { d: DialogInterface, _: Int -> d.dismiss() }
                            .show()

                }
                return
            }

        } catch (e: Exception) {
            Log.d(TAG, "checkVersion: error ${e.message}")
        }
    }


    inner class PagerAdapter(private val fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val fragments: List<BaseFragment> = listOf(HomeFragment(), ProfileFragment(), ClosetFragment(), FollowFragment(), FavoriteFragment(), OtherFragment())

        fun refresh() {
            val cur = binding.vpFragment.currentItem
            fragments[cur].refreshFragment()
        }

        init {
            binding.vpFragment.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {

                    Log.d(TAG, "onPageSelected: $position")
                    if (position == 1 && !clientViewModel.isClient) {
                        moveToLogin()
                    }

                    listOf(R.drawable.ic_home_off, R.drawable.ic_profile_off).forEachIndexed { index, i ->
                        binding.tabLayout.getTabAt(index)?.setIcon(i)
                    }
                    when (position) {
                        0 -> binding.tabLayout.getTabAt(position)?.setIcon(R.drawable.ic_home_on)
                        1 -> {
                            (fragments[position] as ProfileFragment).setData(true)
                            binding.tabLayout.getTabAt(position)?.setIcon(R.drawable.ic_profile_on)
                        }
                        5 -> {
                            (fragments[position] as OtherFragment).setData(true)
                            binding.tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_profile_on)
                        }
                        else -> {
                            binding.tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_profile_on)
                        }
                    }
                }
            })

        }

        override fun getItem(pos: Int) = fragments[pos]
        override fun getCount() = fragments.size
        override fun destroyItem(container: ViewGroup, position: Int, v: Any) {
            super.destroyItem(container, position, v)
            if (position <= count) {
                val manager = fm
                manager.beginTransaction()
                        .remove(v as Fragment)
                        .commit()
            }
        }
    }

}

