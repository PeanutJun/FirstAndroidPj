package com.yuguri.me.mypersonalapp.data.api

import com.yuguri.me.mypersonalapp.data.model.*
import retrofit2.http.*

// ========== 天聚 API (MoodFeed) ==========
interface MoodFeedApi {
    @GET("networkhot/index")
    suspend fun fetchHotSearch(@Query("key") key: String = API_KEYS.MOOD_FEED): HotSearchResponse

    @GET("dgryl/index")
    suspend fun fetchWorkerQuote(@Query("key") key: String = API_KEYS.MOOD_FEED): ContentResponse

    @GET("qiaomen/index")
    suspend fun fetchLifeTip(@Query("key") key: String = API_KEYS.MOOD_FEED): ContentResponse

    @GET("dailytel/index")
    suspend fun searchPhone(
        @Query("key") key: String = API_KEYS.MOOD_FEED,
        @Query("word") word: String
    ): PhoneResponse
}

// ========== 天聚 API (Tianju) ==========
interface TianjuApi {
    @GET("pyqwenan/index")
    suspend fun fetchPyqWenan(@Query("key") key: String = API_KEYS.TIANJU): PyqWenanResponse

    @GET("dialogue/index")
    suspend fun fetchDialogue(@Query("key") key: String = API_KEYS.TIANJU): DialogueResponse

    @GET("lishi/index")
    suspend fun fetchHistoryToday(
        @Query("key") key: String = API_KEYS.TIANJU,
        @Query("date") date: String
    ): HistoryResponse
}

// ========== 天气 API (OpenMeteo) ==========
interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m,precipitation",
        @Query("timezone") timezone: String = "auto"
    ): CurrentWeatherResponse

    @GET("v1/forecast")
    suspend fun getHourlyPrecipitation(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = "precipitation",
        @Query("timezone") timezone: String = "auto",
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): PrecipitationResponse

    @GET("v1/forecast")
    suspend fun getSevenDayWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,temperature_2m_mean,precipitation_sum,precipitation_probability_max,wind_speed_10m_max",
        @Query("timezone") timezone: String = "auto"
    ): SevenDayWeatherResponse
}

// ========== 汇率 API (Frankfurter) ==========
interface ExchangeRateApi {
    @GET("v2/rates")
    suspend fun getRatesByBase(@Query("base") base: String): List<RateInfo>
}

// ========== 汽车 API (Tianapi) ==========
interface CarApi {
    @GET("auto/index")
    suspend fun fetchCarNews(
        @Query("key") key: String = API_KEYS.CAR,
        @Query("num") num: Int = 10,
        @Query("page") page: Int = 1
    ): CarNewsResponse

    @GET("oilprice/index")
    suspend fun fetchOilPrice(
        @Query("key") key: String = API_KEYS.CAR,
        @Query("prov") prov: String
    ): OilPriceResponse

    @GET("chepai/index")
    suspend fun fetchLicensePlate(
        @Query("key") key: String = API_KEYS.CAR,
        @Query("word") word: String
    ): LicensePlateResponse

    @GET("obdcode/index")
    suspend fun fetchObdCode(
        @Query("key") key: String = API_KEYS.CAR,
        @Query("code") code: String
    ): ObdCodeResponse
}

// ========== 新闻 API ==========
interface NewsApi {
    @GET("v2/everything")
    suspend fun fetchDomesticNews(
        @Query("q") q: String = "科技",
        @Query("language") language: String = "zh",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 10,
        @Query("page") page: Int = 1,
        @Query("apiKey") apiKey: String = API_KEYS.NEWS
    ): NewsArticleResponse

    @GET("v2/everything")
    suspend fun fetchInternationalNews(
        @Query("q") q: String = "news",
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 10,
        @Query("page") page: Int = 1,
        @Query("apiKey") apiKey: String = API_KEYS.NEWS
    ): NewsArticleResponse

    @GET("v2/top-headlines")
    suspend fun fetchTopHeadlines(
        @Query("country") country: String = "us",
        @Query("pageSize") pageSize: Int = 10,
        @Query("page") page: Int = 1,
        @Query("apiKey") apiKey: String = API_KEYS.NEWS
    ): NewsArticleResponse
}

// ========== 百度翻译 ==========
interface BaiduTranslateApi {
    @POST("ait/api/aiTextTranslate")
    @Headers("Content-Type: application/json")
    suspend fun translateText(
        @Header("Authorization") auth: String = "Bearer ${API_KEYS.BAIDU_AUTH_TOKEN}",
        @Body body: BaiduTranslateRequest
    ): BaiduTranslateResponse
}

// ========== 高德地图提示 ==========
interface AmapApi {
    @GET("assistant/inputtips")
    suspend fun getInputTips(
        @Query("key") key: String = API_KEYS.AMAP_KEY,
        @Query("keywords") keywords: String,
        @Query("city") city: String,
        @Query("datatype") datatype: String = "poi"
    ): AmapTipsResponse
}

// ========== 用户后端 API ==========
interface UserApi {
    @POST("register")
    suspend fun register(@Body body: Map<String, String>): UserResponse

    @POST("login")
    suspend fun login(@Body body: LoginRequest): UserResponse

    @POST("news/view")
    suspend fun incrementNewsView(@Body body: NewsViewRequest)

    @POST("news/favorite")
    suspend fun incrementNewsFavorite(@Body body: Map<String, Any>)

    @GET("user/city-preference")
    suspend fun getUserCityPreference(@Query("userId") userId: Int): CityPreferenceResponse

    @POST("user/city-preference")
    suspend fun saveUserCityPreference(@Body body: CityPreferenceRequest): ApiMsgResponse

    @GET("user/profile")
    suspend fun getUserProfile(@Query("userId") userId: Int): ProfileResponse

    @POST("user/profile")
    suspend fun saveUserProfile(@Body body: ProfileUpdateRequest): ApiMsgResponse

    @POST("sentence/favorite")
    suspend fun favoriteSentence(@Body body: FavoriteSentenceRequest): FavoriteSentenceResponse

    @GET("sentence/favorites")
    suspend fun getUserFavorites(
        @Query("userId") userId: Int,
        @Query("categoryId") categoryId: Int = 200
    ): FavoritesListResponse

    @GET("sentence/favorites/count")
    suspend fun getFavoritesCount(@Query("userId") userId: Int): FavoritesCountResponse

    @GET("oil-price")
    suspend fun getOilPrice(@Query("prov") prov: String): OilPriceResponse

    @POST("sentence/favorite/remove")
    suspend fun removeFavorite(@Body body: RemoveFavoriteRequest): FavoriteSentenceResponse

    @GET("sentence/categories")
    suspend fun getCategories(@Query("userId") userId: Int): CategoryListResponse

    @POST("sentence/category")
    suspend fun createCategory(@Body body: CreateCategoryRequest): CreateCategoryResponse
}

data class LoginRequest(val username: String, val password: String)
data class ProfileUpdateRequest(val userId: Int, val nickname: String, val phone: String)

data class CityPreferenceResponse(
    val code: Int = 0,
    val msg: String = "",
    val data: CityPreferenceData? = null
)

data class CityPreferenceData(
    val city_name: String = "",
    val district: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class ApiMsgResponse(
    val code: Int = 0,
    val msg: String = ""
)

// ========== API Keys ==========
object API_KEYS {
    const val MOOD_FEED = "84df859304c9f24d81428f5b5d4f5035"
    const val TIANJU = "c6e5aa8cb90829e23d3a9bd85174bf35"
    const val CAR = "84df859304c9f24d81428f5b5d4f5035"
    const val NEWS = "fdb836fb63884a4f8a21dcb03a21f815"
    const val BAIDU_APP_ID = "20220327001144560"
    const val BAIDU_AUTH_TOKEN = "7Yny_d8s9dvs3um9ef2qqh3d0"
    const val AMAP_KEY = "6ef14c625aebea4f12491adaac72b8f0"

    // 你自己的服务器地址（改成你的实际域名）
    const val USER_BASE_URL = "http://10.193.95.80:5000"
}


