package com.yuguri.me.mypersonalapp.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Index : Screen("index")
    object Car : Screen("car")
    object Weather : Screen("weather")
    object FutureWeather : Screen("future_weather/{cityName}/{keywords}/{lat}/{lng}") {
        fun createRoute(cityName: String, keywords: String, lat: Double, lng: Double): String {
            return "future_weather/$cityName/$keywords/$lat/$lng"
        }
    }
    object ExchangeRate : Screen("exchange_rate")
    object News : Screen("news")
    object Profile : Screen("profile")
}

