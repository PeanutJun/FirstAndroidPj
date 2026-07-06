package com.yuguri.me.mypersonalapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yuguri.me.mypersonalapp.ui.screen.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Index.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Index.route) {
            IndexScreen(navController = navController)
        }
        composable(Screen.Car.route) {
            CarScreen(navController = navController)
        }
        composable(Screen.Weather.route) {
            WeatherScreen(navController = navController)
        }
        composable(Screen.FutureWeather.route) { backStackEntry ->
            val cityName = backStackEntry.arguments?.getString("cityName") ?: ""
            val keywords = backStackEntry.arguments?.getString("keywords") ?: ""
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
            FutureWeatherScreen(
                navController = navController,
                cityName = cityName,
                keywords = keywords,
                latitude = lat,
                longitude = lng
            )
        }
        composable(Screen.ExchangeRate.route) {
            ExchangeRateScreen(navController = navController)
        }
        composable(Screen.News.route) {
            NewsScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
    }
}

