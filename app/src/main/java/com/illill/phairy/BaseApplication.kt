package com.illill.phairy

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.illill.phairy.util.BitmapUtils
import com.illill.phairy.util.RetrofitClient
import com.illill.phairy.viewmodel.*
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import org.koin.android.ext.android.startKoin
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.Module
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class BaseApplication : Application() {
    internal var TAG = BaseApplication::class.java.simpleName

    override fun onCreate() {
        super.onCreate()
        gson = Gson()
        sharedPreferences = getSharedPreferences(resources.getString(R.string.app_name), Context.MODE_PRIVATE)

        loadModule()
    }


    companion object {
        lateinit var gson: Gson
        lateinit var sharedPreferences: SharedPreferences
        const val BASE_URL = "http://ec2-52-78-237-229.ap-northeast-2.compute.amazonaws.com:3000"
        const val STORAGE_URL = "https://codiclo.s3.ap-northeast-2.amazonaws.com"
        lateinit var mFirebaseAnalytics: FirebaseAnalytics
    }


    private fun loadModule() {
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(RetrofitClient::class.java)
        val modules: Module = module {
            single { ClientViewModel(retrofit) }
            single { ClothViewModel(retrofit) }
            single { CodiViewModel(retrofit) }
            single { PhotoViewModel() }
            single { ProfileViewModel(retrofit) }
            single { SettingViewModel() }

            viewModel { HomeViewModel(retrofit) }
            viewModel { WeatherViewModel() }
            viewModel { FollowViewModel(retrofit) }
            viewModel { ClosetViewModel() }

            single {
                BitmapUtils()
            }

        }
        val appModules = listOf(modules)
        startKoin(this, appModules)
    }


}
