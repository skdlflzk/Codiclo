package com.illill.phairy

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.illill.phairy.data.model.Client
import com.illill.phairy.util.RetrofitClient
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RetrofitTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    val BASE_URL = "http://ec2-52-78-237-229.ap-northeast-2.compute.amazonaws.com:3000"
    val STORAGE_URL = "https://codiclo.s3.ap-northeast-2.amazonaws.com"
    val UID = "JbBNYlRRtdOi3zCfWizWcDa3xud2"

    //    lateinit var cvm: ClientViewModel
    lateinit var retrofit: RetrofitClient

    @Before
    @Throws(Exception::class)
    fun start() {
        val appContext = InstrumentationRegistry.getInstrumentation().context

        retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(RetrofitClient::class.java)

//        cvm = ClientViewModel(retrofit)
    }

    //입력 확인
    @Test
    fun testLoadUserData() {
        var result: Client? =null
        runBlocking {
            result = retrofit.loadSelfAsync(UID).await()

        }

        assertEquals(result?.uid, UID)
    }

    @After
    @Throws(IOException::class)
    fun end() {
        assertEquals("a", "")
    }

}