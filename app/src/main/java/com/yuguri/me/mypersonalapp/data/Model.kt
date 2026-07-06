package com.yuguri.me.mypersonalapp.data.model

import com.google.gson.annotations.SerializedName

// ========== 热搜 / 内容 / 心情 ==========
data class HotSearchResponse(
    val code: Int = 0,
    val msg: String = "",
    val result: HotSearchResult? = null
)

data class HotSearchResult(
    val list: List<HotSearchItem> = emptyList()
)

data class HotSearchItem(
    val title: String = "",
    val digest: String = "",
    val hotnum: Int = 0
)

data class ContentResponse(
    val code: Int = 0,
    val msg: String = "",
    val result: ContentData? = null
)

data class ContentData(
    val content: String = ""
)

data class PhoneResponse(
    val code: Int = 0,
    val msg: String = "",
    val result: PhoneResult? = null
)

data class PhoneResult(
    val list: List<PhoneItem> = emptyList()
)

data class PhoneItem(
    val tel: String = "",
    val cate: String = "",
    val name: String = ""
)

data class PyqWenanResponse(
    val code: Int = 0,
    val msg: String = "",
    val result: PyqWenanData? = null
)

data class PyqWenanData(
    val source: String = "",
    val content: String = ""
)

data class DialogueResponse(
    val code: Int = 0,
    val msg: String = "",
    val result: DialogueData? = null
)

data class DialogueData(
    val type: Int = 0,
    val source: String = "",
    val english: String = "",
    val dialogue: String = ""
)

data class HistoryResponse(
    val code: Int = 0,
    val msg: String = "",
    val result: HistoryResult? = null
)

data class HistoryResult(
    val list: List<HistoryItem> = emptyList()
)

data class HistoryItem(
    val title: String = "",
    val lsdate: String = ""
)

// ========== 天气 ==========
data class CurrentWeatherResponse(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezone: String = "",
    @SerializedName("timezone_abbreviation") val timezoneAbbreviation: String = "",
    val elevation: Double = 0.0,
    @SerializedName("current_units") val currentUnits: CurrentUnits = CurrentUnits(),
    val current: CurrentWeatherRaw = CurrentWeatherRaw()
)

data class CurrentUnits(
    val time: String = "",
    val interval: String = "",
    @SerializedName("temperature_2m") val temperature2m: String = "",
    @SerializedName("apparent_temperature") val apparentTemperature: String = "",
    @SerializedName("relative_humidity_2m") val relativeHumidity2m: String = "",
    @SerializedName("weather_code") val weatherCode: String = "",
    @SerializedName("wind_speed_10m") val windSpeed10m: String = "",
    @SerializedName("wind_direction_10m") val windDirection10m: String = "",
    val precipitation: String = ""
)

data class CurrentWeatherRaw(
    val time: String = "",
    val interval: Int = 0,
    @SerializedName("temperature_2m") val temperature2m: Double = 0.0,
    @SerializedName("apparent_temperature") val apparentTemperature: Double = 0.0,
    @SerializedName("relative_humidity_2m") val relativeHumidity2m: Double = 0.0,
    @SerializedName("weather_code") val weatherCode: Int = 0,
    @SerializedName("wind_speed_10m") val windSpeed10m: Double = 0.0,
    @SerializedName("wind_direction_10m") val windDirection10m: Double = 0.0,
    val precipitation: Double = 0.0
)

data class CurrentWeather(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var timezone: String = "",
    var timezoneAbbreviation: String = "",
    var elevation: Double = 0.0,
    var time: String = "",
    var interval: Int = 0,
    var temperature: Double = 0.0,
    var apparentTemperature: Double = 0.0,
    var relativeHumidity: Double = 0.0,
    var weatherCode: Int = 0,
    var weatherText: String = "",
    var windSpeed: Double = 0.0,
    var windDirection: Double = 0.0,
    var precipitation: Double = 0.0
)

fun CurrentWeatherResponse.toCurrentWeather(): CurrentWeather {
    val w = WeatherCode.getText(current.weatherCode)
    return CurrentWeather(
        latitude = latitude,
        longitude = longitude,
        timezone = timezone,
        timezoneAbbreviation = timezoneAbbreviation,
        elevation = elevation,
        time = current.time,
        interval = current.interval,
        temperature = current.temperature2m,
        apparentTemperature = current.apparentTemperature,
        relativeHumidity = current.relativeHumidity2m,
        weatherCode = current.weatherCode,
        weatherText = w,
        windSpeed = current.windSpeed10m,
        windDirection = current.windDirection10m,
        precipitation = current.precipitation
    )
}

data class PrecipitationResponse(
    val hourly: HourlyPrecipitation? = null
)

data class HourlyPrecipitation(
    val time: List<String> = emptyList(),
    val precipitation: List<Double> = emptyList()
)

// ========== 7天天气 ==========
data class SevenDayWeatherResponse(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezone: String = "",
    @SerializedName("timezone_abbreviation") val timezoneAbbreviation: String = "",
    val elevation: Double = 0.0,
    val dailyUnits: DailyUnits = DailyUnits(),
    val daily: DailyWeatherData = DailyWeatherData()
)

data class DailyUnits(
    val time: String = "",
    @SerializedName("weather_code") val weatherCode: String = "",
    @SerializedName("temperature_2m_max") val temperature2mMax: String = "",
    @SerializedName("temperature_2m_min") val temperature2mMin: String = "",
    @SerializedName("temperature_2m_mean") val temperature2mMean: String = "",
    @SerializedName("precipitation_sum") val precipitationSum: String = "",
    @SerializedName("precipitation_probability_max") val precipitationProbabilityMax: String = "",
    @SerializedName("wind_speed_10m_max") val windSpeed10mMax: String = ""
)

data class DailyWeatherData(
    val time: List<String> = emptyList(),
    @SerializedName("weather_code") val weatherCode: List<Int> = emptyList(),
    @SerializedName("temperature_2m_max") val temperature2mMax: List<Double> = emptyList(),
    @SerializedName("temperature_2m_min") val temperature2mMin: List<Double> = emptyList(),
    @SerializedName("temperature_2m_mean") val temperature2mMean: List<Double> = emptyList(),
    @SerializedName("precipitation_sum") val precipitationSum: List<Double> = emptyList(),
    @SerializedName("precipitation_probability_max") val precipitationProbabilityMax: List<Double> = emptyList(),
    @SerializedName("wind_speed_10m_max") val windSpeed10mMax: List<Double> = emptyList()
)

data class SevenDayWeatherItem(
    val date: String = "",
    val weatherCode: Int = 0,
    val weatherText: String = "",
    val temperatureMax: Double = 0.0,
    val temperatureMin: Double = 0.0,
    val temperatureMean: Double = 0.0,
    val precipitationSum: Double = 0.0,
    val precipitationProbabilityMax: Double = 0.0,
    val windSpeedMax: Double = 0.0
)

fun SevenDayWeatherResponse.toSevenDayWeather(): List<SevenDayWeatherItem> {
    return daily.time.mapIndexed { i, date ->
        SevenDayWeatherItem(
            date = date,
            weatherCode = daily.weatherCode.getOrElse(i) { 0 },
            weatherText = WeatherCode.getText(daily.weatherCode.getOrElse(i) { 0 }),
            temperatureMax = daily.temperature2mMax.getOrElse(i) { 0.0 },
            temperatureMin = daily.temperature2mMin.getOrElse(i) { 0.0 },
            temperatureMean = daily.temperature2mMean.getOrElse(i) { 0.0 },
            precipitationSum = daily.precipitationSum.getOrElse(i) { 0.0 },
            precipitationProbabilityMax = daily.precipitationProbabilityMax.getOrElse(i) { 0.0 },
            windSpeedMax = daily.windSpeed10mMax.getOrElse(i) { 0.0 }
        )
    }
}

// ========== 汇率 ==========
data class RateInfo(
    val date: String = "",
    val base: String = "",
    val quote: String = "",
    val rate: Double = 0.0
)

// ========== 汽车 ==========
data class CarNewsResponse(
    val code: Int = 0,
    val msg: String = "",
    val result: CarNewsListResult? = null
)

data class CarNewsListResult(
    val newslist: List<CarNewsItem> = emptyList(),
    val allnum: Int = 0,
    val curpage: Int = 0
)

data class CarNewsItem(
    val id: String = "",
    val url: String = "",
    val ctime: String = "",
    val title: String = "",
    val picUrl: String = "",
    val source: String = "",
    val description: String = ""
)

data class OilPriceResponse(
    val code: Int = 0,
    val msg: String = "",
    val result: OilPriceResult? = null
)

data class OilPriceResult(
    val prov: String = "",
    val p0: String = "",
    val p89: String = "",
    val p92: String = "",
    val p95: String = "",
    val p98: String = "",
    val time: String = ""
)

data class LicensePlateResponse(
    val code: Int = 0,
    val msg: String = "",
    val result: LicensePlateResult? = null
)

data class LicensePlateResult(
    val number: String = "",
    val province: String = "",
    val city: String = "",
    val district: String = ""
)

data class ObdCodeResponse(
    val code: Int = 0,
    val msg: String = "",
    val result: ObdCodeResult? = null
)

data class ObdCodeResult(
    val list: List<ObdCodeItem> = emptyList()
)

data class ObdCodeItem(
    val code: String = "",
    val category: String = "",
    val zhnote: String = "",
    val ennote: String = "",
    val descr: String = "",
    val carmodel: String = ""
)

// ========== 新闻 ==========
data class NewsArticleResponse(
    val status: String = "",
    val totalResults: Int = 0,
    val articles: List<NewsArticleItem> = emptyList()
)

data class NewsArticleItem(
    val source: NewsArticleSource? = null,
    val author: String? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val urlToImage: String? = null,
    val publishedAt: String? = null,
    val content: String? = null
)

data class NewsArticleSource(
    val id: String = "",
    val name: String = ""
)

// ========== 翻译 ==========
data class BaiduTranslateRequest(
    val appid: String,
    val from: String,
    val to: String,
    val q: String
)

data class BaiduTranslateResponse(
    val trans_result: List<TransResultItem> = emptyList()
)

data class TransResultItem(
    val src: String = "",
    val dst: String = ""
)

// ========== 用户 ==========
data class UserResponse(
    val code: Int = 0,
    val msg: String = "",
    val data: UserData? = null
)

data class UserData(
    @SerializedName("id") val userId: Int = 0,
    val username: String = "",
    val nickname: String = "",
    val phone: String = ""
)

data class ProfileResponse(
    val code: Int = 0,
    val msg: String = "",
    val data: ProfileData? = null
)

data class ProfileData(
    val userId: Int = 0,
    val username: String = "",
    val nickname: String = "",
    val phone: String = ""
)

data class FavoriteSentenceResponse(
    val code: Int = 0,
    val msg: String = ""
)

data class FavoriteSentenceRequest(
    val userId: Int,
    val content: String,
    val source: String,
    val categoryId: Int
)

data class CityPreferenceRequest(
    val userId: Int,
    val cityName: String,
    val district: String,
    val latitude: Double,
    val longitude: Double
)

data class RemoveFavoriteRequest(
    val id: Int
)

data class NewsViewRequest(
    val userId: Int
)

data class FavoritesCountResponse(
    val code: Int = 0,
    val msg: String = "",
    val data: FavoritesCountData? = null
)

data class FavoritesCountData(
    val count: Int = 0
)

data class CategoryListResponse(
    val code: Int = 0,
    val msg: String = "",
    val data: List<CategoryData>? = null
)

data class CategoryData(
    val id: Int = 0,
    val name: String = "",
    val userId: Int = 0
)

data class CreateCategoryRequest(
    val userId: Int,
    val name: String
)

data class CreateCategoryResponse(
    val code: Int = 0,
    val msg: String = "",
    val data: CategoryData? = null
)

data class FavoritesListResponse(
    val code: Int = 0,
    val msg: String = "",
    val data: List<FavoriteSentenceData>? = null
)

data class FavoriteSentenceData(
    val id: Int = 0,
    val userId: Int = 0,
    val content: String = "",
    val source: String = "",
    val categoryId: Int = 0
)

// ========== Amap ==========
data class AmapTipsResponse(
    val tips: List<AmapTip> = emptyList()
)

data class AmapTip(
    val id: String = "",
    val name: String = "",
    val district: String = "",
    val location: String = ""
)

// ========== WeatherCode ==========
object WeatherCode {
    fun getText(code: Int): String {
        return when (code) {
            0 -> "\u2600\uFE0F \u6674"
            1 -> "\uD83C\uDF24\uFE0F \u6674\u95F4\u591A\u4E91"
            2 -> "\u26C5 \u591A\u4E91"
            3 -> "\u2601\uFE0F \u9634"
            45 -> "\uD83C\uDF2B\uFE0F \u96FE"
            48 -> "\uD83C\uDF2B\uFE0F \u96FE\u51D1"
            51 -> "\uD83C\uDF26\uFE0F \u5C0F\u6BDB\u6BDB\u96E8"
            53 -> "\uD83C\uDF26\uFE0F \u6BDB\u6BDB\u96E8"
            55 -> "\uD83C\uDF27\uFE0F \u5927\u6BDB\u6BDB\u96E8"
            56 -> "\uD83C\uDF27\uFE0F \u51BB\u6BDB\u6BDB\u96E8"
            57 -> "\uD83C\uDF27\uFE0F \u51BB\u5927\u96E8"
            61 -> "\uD83C\uDF26\uFE0F \u5C0F\u96E8"
            63 -> "\uD83C\uDF27\uFE0F \u4E2D\u96E8"
            65 -> "\u26C8\uFE0F \u5927\u96E8"
            66 -> "\uD83C\uDF27\uFE0F \u51BB\u96E8"
            67 -> "\u26C8\uFE0F \u5927\u51BB\u96E8"
            71 -> "\uD83C\uDF28\uFE0F \u5C0F\u96EA"
            73 -> "\uD83C\uDF28\uFE0F \u4E2D\u96EA"
            75 -> "\u2744\uFE0F \u5927\u96EA"
            77 -> "\u2744\uFE0F \u96EA\u7C92"
            80 -> "\uD83C\uDF26\uFE0F \u5C0F\u9635\u96E8"
            81 -> "\uD83C\uDF27\uFE0F \u4E2D\u9635\u96E8"
            82 -> "\u26C8\uFE0F \u5927\u9635\u96E8"
            85 -> "\uD83C\uDF28\uFE0F \u5C0F\u9635\u96EA"
            86 -> "\uD83C\uDF28\uFE0F \u5927\u9635\u96EA"
            95 -> "\u26C8\uFE0F \u96F7\u66B4"
            96 -> "\u26C8\uFE0F \u96F7\u66B4\u52A0\u5C0F\u51B0\u5DDD"
            99 -> "\u26C8\uFE0F \u96F7\u66B4\u52A0\u5927\u51B0\u5DDD"
            else -> "\u2601\uFE0F \u672A\u77E5($code)"
        }
    }
}

