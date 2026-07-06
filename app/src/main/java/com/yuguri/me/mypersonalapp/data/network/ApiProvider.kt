package com.yuguri.me.mypersonalapp.data.network

import com.yuguri.me.mypersonalapp.data.api.*
import com.yuguri.me.mypersonalapp.data.model.*

object ApiProvider {
    private val tianapiRetrofit = RetrofitClient.create(
        "https://apis.tianapi.com/",
        MoodFeedApi::class.java
    )

    val moodFeedApi: MoodFeedApi by lazy {
        RetrofitClient.create("https://apis.tianapi.com/", MoodFeedApi::class.java)
    }

    val tianjuApi: TianjuApi by lazy {
        RetrofitClient.create("https://apis.tianapi.com/", TianjuApi::class.java)
    }

    val weatherApi: WeatherApi by lazy {
        RetrofitClient.create("https://api.open-meteo.com/", WeatherApi::class.java)
    }

    val exchangeRateApi: ExchangeRateApi by lazy {
        RetrofitClient.create("https://api.frankfurter.dev/", ExchangeRateApi::class.java)
    }

    val carApi: CarApi by lazy {
        RetrofitClient.create("https://apis.tianapi.com/", CarApi::class.java)
    }

    val newsApi: NewsApi by lazy {
        RetrofitClient.create("https://newsapi.org/", NewsApi::class.java)
    }

    val baiduTranslateApi: BaiduTranslateApi by lazy {
        RetrofitClient.create("https://fanyi-api.baidu.com/", BaiduTranslateApi::class.java)
    }

    val userApi: UserApi by lazy {
        RetrofitClient.create("${API_KEYS.USER_BASE_URL}/", UserApi::class.java)
    }

    val amapApi: AmapApi by lazy {
        RetrofitClient.create("https://restapi.amap.com/v3/", AmapApi::class.java)
    }
}

