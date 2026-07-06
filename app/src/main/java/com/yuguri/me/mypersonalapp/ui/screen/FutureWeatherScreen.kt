package com.yuguri.me.mypersonalapp.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yuguri.me.mypersonalapp.data.model.*
import com.yuguri.me.mypersonalapp.data.network.ApiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val DarkBg = Color(0xFF0F172A)
private val DarkBg2 = Color(0xFF1E293B)
private val Primary = Color(0xFFF97316)
private val Secondary = Color(0xFF38BDF8)
private val Success = Color(0xFF22C55E)
private val Purple = Color(0xFFA78BFA)
private val TxtMain = Color(0xFFF8FAFC)
private val TxtSub = Color(0xFF94A3B8)
private val CardBg = Color(0x851E293B)

@Composable
fun FutureWeatherScreen(navController: NavController, cityName: String, keywords: String, latitude: Double, longitude: Double) {
    val vm: FutureWeatherViewModel = viewModel(key = cityName, factory = FutureWeatherFactory(latitude, longitude))
    LaunchedEffect(Unit) { vm.loadForecast() }

    val density = LocalDensity.current
    Box(Modifier.fillMaxSize().background(DarkBg)) {
        val gradient = androidx.compose.ui.graphics.Brush.radialGradient(
            colors = listOf(Color(0xFF6366F1).copy(alpha = 0.12f), Color(0xFF8B5CF6).copy(alpha = 0.04f), Color.Transparent),
            center = androidx.compose.ui.geometry.Offset(0f, 0f),
            radius = with(density) { 500.dp.toPx() }
        )
        Box(modifier = Modifier.fillMaxSize().background(gradient))

        if (vm.loading) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Primary, modifier = Modifier.size(32.dp))
                Text("加载中...", fontSize = 14.sp, color = TxtSub.copy(alpha = 0.75f), modifier = Modifier.padding(top = 8.dp))
            }
        } else if (vm.forecast.isNotEmpty()) {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Row(Modifier.fillMaxWidth().padding(top = 24.dp, start = 8.dp, end = 16.dp)) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "back", tint = TxtMain)
                        }
                        Text(
                            cityName + " " + (if (keywords.isNotEmpty()) keywords + " " else "") + "未来7天天气",
                            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TxtMain,
                            modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.width(48.dp))
                    }
                }
                items(vm.forecast) { item -> ForecastCard(item) }
                item { Spacer(Modifier.height(32.dp)) }
            }
        } else if (vm.error.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth().padding(top = 24.dp, start = 8.dp, end = 16.dp)) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "back", tint = TxtMain)
                    }
                    Text(
                        cityName + " " + (if (keywords.isNotEmpty()) keywords + " " else "") + "未来7天天气",
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TxtMain,
                        modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.width(48.dp))
                }
                Text(vm.error, color = Color(0xFFF87171), modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}

@Composable
private fun ForecastCard(item: SevenDayWeatherItem) {
    Box(
        modifier = Modifier.fillMaxWidth().background(CardBg).padding(16.dp)
            .border(1.dp, Primary.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .shadow(elevation = 12.dp, spotColor = Color(0x25000000), ambientColor = Color(0x25000000))
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                val dayStr = if (item.date.isNotEmpty()) item.date.substringAfterLast("-") + "日" else ""
                Text(dayStr, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TxtMain, modifier = Modifier.padding(bottom = 4.dp))
                Text(item.weatherText, fontSize = 14.sp, color = Secondary)
            }

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${formatDouble(item.temperatureMax, 1)}°", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Primary)
                Spacer(Modifier.height(4.dp))
                Text("${formatDouble(item.temperatureMin, 1)}°", fontSize = 14.sp, color = Secondary)
            }

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("降水 ${formatDouble(item.precipitationSum, 1)}mm", fontSize = 12.sp, color = Success)
                Spacer(Modifier.height(4.dp))
                Text("风速 ${formatDouble(item.windSpeedMax, 0)}km/h", fontSize = 12.sp, color = Purple)
            }
        }
    }
}

private fun formatDouble(value: Double, digits: Int): String {
    return "%.${digits}f".format(value)
}

class FutureWeatherViewModel(lat: Double, lng: Double) : ViewModel() {
    var forecast by mutableStateOf<List<SevenDayWeatherItem>>(emptyList())
    var loading by mutableStateOf(false)
    var error by mutableStateOf("")
    private var mLat = 0.0
    private var mLng = 0.0
    init { mLat = lat; mLng = lng }

    fun loadForecast() = viewModelScope.launch {
        if (mLat == 0.0 || mLng == 0.0) {
            error = "缺少经纬度信息"
            return@launch
        }
        loading = true
        try {
            val response = withContext(Dispatchers.IO) {
                ApiProvider.weatherApi.getSevenDayWeather(mLat, mLng)
            }
            forecast = response.toSevenDayWeather()
        } catch (e: Exception) {
            error = e.message ?: "加载失败"
        }
        loading = false
    }
}

class FutureWeatherFactory(lat: Double, lng: Double) : ViewModelProvider.Factory {
    private var mLat = 0.0
    private var mLng = 0.0
    init { mLat = lat; mLng = lng }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FutureWeatherViewModel(mLat, mLng) as T
}