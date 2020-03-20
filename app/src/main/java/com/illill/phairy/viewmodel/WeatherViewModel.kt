package com.illill.phairy.viewmodel

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.illill.phairy.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class WeatherViewModel : ViewModel() {
    init {
        loadWeather()
    }

    private val TAG = this.javaClass.simpleName

    private var _weather = MutableLiveData<String>()
    val weather: LiveData<String> get() = _weather

    private var _minDegree = MutableLiveData<String>()
    val minDegree: LiveData<String> get() = _minDegree

    private var _maxDegree = MutableLiveData<String>()
    val maxDegree: LiveData<String> get() = _maxDegree


    fun loadWeather() = CoroutineScope(Dispatchers.Default).launch {
        //http://api.openweathermap.org/data/2.5/weather?q=Seoul&appid=4151da97e35f5b8359de62fd4980b7ca
        val region = "Seoul"
        val WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?q=$region&appid=4151da97e35f5b8359de62fd4980b7ca"
        val client = OkHttpClient()
        client.newCall(
                Request.Builder()
                        .url(WEATHER_URL)
                        .build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "loadWeather: error while getting weather")
                
            }
        

            override fun onResponse(call: Call, response: Response) {
                val d = Log.d(TAG, "loadWeather: $response")
                parseWeather(response.body()?.string()?:"no_data")
            }
        })
    }

    private fun parseWeather(str: String) {
        try {
            val jsonObject = JSONObject(str)
            if (jsonObject.has("weather")) {
                val weatherObj = jsonObject.getJSONArray("weather")
                val main1 = weatherObj.getJSONObject(0).getString("main")
                val main2 = weatherObj.getJSONObject(0).getString("main")
                _weather.postValue(main1)
            }
            if (jsonObject.has("main")) {
                val tempMain = jsonObject.getJSONObject("main")
                val max = tempMain.getInt("temp_max") - 273
                val min = tempMain.getInt("temp_min") - 273
                _maxDegree.postValue(max.toString())
                _minDegree.postValue(min.toString())
            }
        } catch (e: Exception) {
            Log.d(TAG, "parseWeather: $str")
            e.printStackTrace()
        }
    }

    companion object {
        //날씨의 문자열에 따라 날씨 아이콘을 지정
        @BindingAdapter("weather")
        @JvmStatic
        fun setWeatherIcon(v: ImageView, weather: String?) {
            var id = 0
            when (weather) {
                "Clear" -> id = R.drawable.ic_sunny
                "Mist", "Smoke", "Clouds" -> id = R.drawable.ic_cloudy
                "Rain", "Drizzle", "Thunderstorm" -> id = R.drawable.ic_rainy
                "Snow" -> id = R.drawable.ic_snowy
                else-> Log.d("setWeatherIcon", "setWeatherIcon: no icon of $weather")
        }
            v.setImageResource(id)
        }

    }
}