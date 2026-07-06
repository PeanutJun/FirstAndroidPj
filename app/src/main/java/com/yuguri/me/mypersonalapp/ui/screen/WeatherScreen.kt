package com.yuguri.me.mypersonalapp.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yuguri.me.mypersonalapp.data.model.*
import com.yuguri.me.mypersonalapp.data.network.ApiProvider
import com.yuguri.me.mypersonalapp.data.preferences.UserPreferences
import com.yuguri.me.mypersonalapp.ui.navigation.Screen
import kotlinx.coroutines.*

private val DarkBg = Color(0xFF0F172A)
private val DarkBg2 = Color(0xFF1E293B)
private val Primary = Color(0xFFF97316)
private val Secondary = Color(0xFF38BDF8)
private val Success = Color(0xFF22C55E)
private val Purple = Color(0xFFA78BFA)
private val TxtMain = Color(0xFFF8FAFC)
private val TxtSub = Color(0xFF94A3B8)
private val CardBg = Color(0x851E293B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val vm: WeatherViewModel = viewModel()
    val userId by prefs.userId.collectAsState(initial = 0)

    LaunchedEffect(Unit) {
        vm.userId = userId
        vm.loadDefaultCity()
    }

    Scaffold(bottomBar = { AppBottomBar(navController, Screen.Weather.route) }, containerColor = DarkBg) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(DarkBg)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { Text("天气", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TxtMain, modifier = Modifier.padding(top = 24.dp)) }

                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().background(Color(0x85334155)).padding(12.dp)
                            .border(1.dp, Color(0x1094A3B8), RoundedCornerShape(16.dp))
                            .shadow(elevation = 12.dp, spotColor = Color(0x25000000), ambientColor = Color(0x25000000))
                            .clickable { vm.showSheet = true }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TxtSub, modifier = Modifier.size(20.dp))
                            Text(
                                if (vm.selectedName.isNotEmpty()) "${vm.selectedName} ${vm.district}" else "选择城市",
                                fontSize = 16.sp, color = TxtMain, modifier = Modifier.padding(start = 8.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = TxtSub)
                        }
                    }
                }

                if (vm.loading) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Primary, modifier = Modifier.size(32.dp))
                            Text("加载中...", fontSize = 14.sp, color = TxtSub.copy(alpha = 0.75f), modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                } else if (vm.errorMessage.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFF87171), modifier = Modifier.size(48.dp))
                            Text(vm.errorMessage, fontSize = 16.sp, color = Color(0xFFF87171), modifier = Modifier.padding(top = 16.dp))
                            Button(onClick = { vm.loadDefaultCity() }, modifier = Modifier.padding(top = 16.dp)) {
                                Text("重试")
                            }
                        }
                    }
                } else {
                    vm.currentWeather?.let { weather ->
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().background(CardBg).padding(20.dp)
                                    .border(1.dp, Primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                    .shadow(elevation = 20.dp, spotColor = Primary.copy(alpha = 0.08f), ambientColor = Primary.copy(alpha = 0.08f))
                                    .padding(20.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(vm.selectedName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TxtMain)
                                            Text(vm.district, fontSize = 14.sp, color = TxtSub)
                                        }
                                        Icon(Icons.Default.Place, contentDescription = "Location", tint = Secondary)
                                    }
                                    Text("${formatDouble(weather.temperature, 1)}°C", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.padding(top = 20.dp))
                                    Text(weather.weatherText, fontSize = 18.sp, color = Secondary, modifier = Modifier.padding(top = 4.dp))
                                    Text(vm.rainAlert, fontSize = 14.sp, color = Success, modifier = Modifier.padding(top = 8.dp))

                                    Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.SpaceAround) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("湿度", fontSize = 12.sp, color = TxtSub)
                                            Text("${formatDouble(weather.relativeHumidity, 0)}%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Success)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("风速", fontSize = 12.sp, color = TxtSub)
                                            Text("${formatDouble(weather.windSpeed, 0)} km/h", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Purple)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("降水", fontSize = 12.sp, color = TxtSub)
                                            Text("${formatDouble(weather.precipitation, 1)} mm", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Secondary)
                                        }
                                    }

                                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Button(
                                            onClick = { navController.navigate(Screen.FutureWeather.createRoute(vm.selectedName, vm.district, vm.latitude, vm.longitude)) },
                                            modifier = Modifier.height(44.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Secondary.copy(alpha = 0.12f)),
                                            shape = RoundedCornerShape(22.dp),
                                            border = BorderStroke(1.dp, Secondary.copy(alpha = 0.15f))
                                        ) {
                                            Text("未来天气 ›", fontSize = 16.sp, color = Secondary)
                                        }
                                        Button(
                                            onClick = { vm.saveDefaultCity(userId) },
                                            modifier = Modifier.height(44.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Primary.copy(alpha = 0.12f)),
                                            shape = RoundedCornerShape(22.dp),
                                            border = BorderStroke(1.dp, Primary.copy(alpha = 0.15f))
                                        ) {
                                            Text("设为默认地区", fontSize = 16.sp, color = Primary)
                                        }
                                    }
                                    if (vm.saveMessage.isNotEmpty()) {
                                        Text(vm.saveMessage, fontSize = 12.sp, color = if (vm.saveMessage.contains("成功")) Success else Color(0xFFF87171), modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("热门城市", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TxtMain, modifier = Modifier.padding(top = 12.dp))
                            Text("长按可替换当前城市", fontSize = 12.sp, color = TxtSub.copy(alpha = 0.75f), modifier = Modifier.padding(top = 4.dp))
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth().height(260.dp)
                            ) {
                                items(vm.gridCities) { city ->
                                    GridCityCard(city,
                                        onClick = {
                                            navController.navigate(Screen.FutureWeather.createRoute(city.cityName, city.cityName, city.latitude, city.longitude))
                                        },
                                        onLongPress = {
                                            vm.updateCurrentCity(city.cityName, city.latitude, city.longitude)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }

            if (vm.showSheet) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { vm.showSheet = false },
                    sheetState = sheetState,
                    containerColor = DarkBg2,
                    contentColor = TxtMain
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("选择城市", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TxtMain)
                            IconButton(onClick = { vm.showSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TxtSub)
                            }
                        }

                        TextField(
                            value = vm.searchKeyword,
                            onValueChange = { vm.searchKeyword = it; vm.debounceSearch() },
                            placeholder = { Text("输入城市名", color = TxtSub) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TxtSub) },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B),
                                focusedIndicatorColor = Primary,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (vm.showTips && vm.tipsList.isNotEmpty()) {
                            LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp).padding(top = 12.dp)) {
                                items(vm.tipsList) { tip ->
                                    TextButton(onClick = {
                                        vm.selectTip(tip)
                                        vm.showSheet = false
                                    }, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                            Text(tip.name, fontSize = 16.sp, color = TxtMain, modifier = Modifier.align(Alignment.Start))
                                            if (tip.district.isNotEmpty()) {
                                                Text(tip.district, fontSize = 13.sp, color = TxtSub, modifier = Modifier.align(Alignment.Start))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridCityCard(city: GridCityWeather, onClick: () -> Unit, onLongPress: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().fillMaxHeight()
            .background(Color(0x650F172A))
            .border(1.dp, Color(0x0822D3EE), RoundedCornerShape(14.dp))
            .padding(8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            if (city.loaded) {
                Text(city.cityName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TxtMain)
                Text("${formatDouble(city.temperature, 1)}°C", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.padding(top = 2.dp))
                Text(city.weatherText, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Secondary, modifier = Modifier.padding(top = 4.dp))
            } else {
                Text(city.cityName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TxtMain)
                Text("加载中...", fontSize = 12.sp, color = TxtSub.copy(alpha = 0.75f), modifier = Modifier.padding(top = 10.dp))
            }
        }
    }
}

private fun formatDouble(value: Double, digits: Int): String {
    return "%.${digits}f".format(value)
}

data class GridCityWeather(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    var temperature: Double = 0.0,
    var weatherText: String = "",
    var humidity: Double = 0.0,
    var loaded: Boolean = false
)

class WeatherViewModel : ViewModel() {
    var userId = 0
    var cityName by mutableStateOf("北京")
    var district by mutableStateOf("")
    var selectedName by mutableStateOf("北京")
    var keywords by mutableStateOf("")
    var latitude by mutableStateOf(39.9042)
    var longitude by mutableStateOf(116.4074)
    var currentWeather by mutableStateOf<CurrentWeather?>(null)
    var loading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var rainAlert by mutableStateOf("")
    var saveMessage by mutableStateOf("")
    var showSheet by mutableStateOf(false)
    var searchKeyword by mutableStateOf("")
    var searchCity by mutableStateOf("")
    var tipsList by mutableStateOf(listOf<AmapTip>())
    var showTips by mutableStateOf(false)
    private var lastSearchKeyword = ""
    private var searchTimer: Job? = null

    val gridCities = mutableStateListOf(
        GridCityWeather("北京", 39.9042, 116.4074),
        GridCityWeather("上海", 31.2304, 121.4737),
        GridCityWeather("广州", 23.1291, 113.2644),
        GridCityWeather("成都", 30.5728, 104.0668),
        GridCityWeather("深圳", 22.5431, 114.0579),
        GridCityWeather("杭州", 30.2741, 120.1551)
    )

    fun loadDefaultCity() = viewModelScope.launch {
        loading = true
        errorMessage = ""

        try {
            if (userId > 0) {
                val res = withContext(Dispatchers.IO) {
                    ApiProvider.userApi.getUserCityPreference(userId)
                }
                if (res.code == 200 && res.data != null) {
                    val lat = res.data!!.latitude
                    val lng = res.data!!.longitude
                    if (lat != 0.0 && lng != 0.0) {
                        cityName = res.data!!.city_name ?: "北京"
                        district = res.data!!.district ?: ""
                        latitude = lat
                        longitude = lng
                        selectedName = res.data!!.city_name ?: "北京"
                        fetchWeather()
                        loadGridWeather()
                        return@launch
                    }
                }
            }
        } catch (_: Exception) {}

        fetchWeather()
        loadGridWeather()
    }

    fun updateCurrentCity(name: String, lat: Double, lng: Double) = viewModelScope.launch {
        cityName = name
        latitude = lat
        longitude = lng
        selectedName = name
        district = name
        fetchWeather()
    }

    fun selectTip(tip: AmapTip) {
        val location = tip.location
        if (location.isNotEmpty() && location.contains(",")) {
            val parts = location.split(",")
            val lng = parts[0].toDoubleOrNull() ?: 0.0
            val lat = parts[1].toDoubleOrNull() ?: 0.0
            if (lat != 0.0 && lng != 0.0) {
                latitude = lat
                longitude = lng
                selectedName = tip.name
                district = tip.district
                cityName = tip.name
                fetchWeather()
            }
        }
    }

    fun saveDefaultCity(userId: Int) = viewModelScope.launch {
        if (userId <= 0) {
            saveMessage = "请先登录"
            return@launch
        }
        try {
            val body = CityPreferenceRequest(
                userId = userId,
                cityName = selectedName,
                district = district,
                latitude = latitude,
                longitude = longitude
            )
            val res = withContext(Dispatchers.IO) {
                ApiProvider.userApi.saveUserCityPreference(body)
            }
            saveMessage = if (res.code == 200) "保存成功" else res.msg.ifEmpty { "保存失败" }
        } catch (e: Exception) {
            saveMessage = "保存失败: ${e.message}"
        }
        delay(2000)
        saveMessage = ""
    }

    fun debounceSearch() {
        searchTimer?.cancel()
        searchTimer = viewModelScope.launch {
            delay(300)
            if (searchKeyword.isNotEmpty()) {
                searchTips()
            } else {
                tipsList = emptyList()
                showTips = false
            }
        }
    }

    fun searchTips() = viewModelScope.launch {
        val keyword = searchKeyword
        lastSearchKeyword = keyword
        val city = if (searchCity.isNotEmpty()) searchCity else cityName
        try {
            val response = withContext(Dispatchers.IO) {
                ApiProvider.amapApi.getInputTips(keywords = keyword, city = city)
            }
            if (lastSearchKeyword != keyword) return@launch
            if (response.tips.isNotEmpty()) {
                tipsList = response.tips
                showTips = true
            } else {
                tipsList = emptyList()
                showTips = false
            }
        } catch (_: Exception) {
            if (lastSearchKeyword == keyword) {
                tipsList = emptyList()
                showTips = false
            }
        }
    }

    fun fetchWeather() = viewModelScope.launch {
        loading = true
        errorMessage = ""

        try {
            val response = withContext(Dispatchers.IO) {
                ApiProvider.weatherApi.getCurrentWeather(latitude, longitude)
            }
            currentWeather = response.toCurrentWeather()
            fetchRainPrediction()
        } catch (e: Exception) {
            errorMessage = "天气获取失败"
        } finally {
            loading = false
        }
    }

    fun fetchRainPrediction() = viewModelScope.launch {
        try {
            val now = java.util.Date()
            val year = java.util.Calendar.getInstance().apply { time = now }.get(java.util.Calendar.YEAR)
            val month = java.util.Calendar.getInstance().apply { time = now }.get(java.util.Calendar.MONTH) + 1
            val day = java.util.Calendar.getInstance().apply { time = now }.get(java.util.Calendar.DAY_OF_MONTH)
            val dateStr = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

            val precipRes = withContext(Dispatchers.IO) {
                ApiProvider.weatherApi.getHourlyPrecipitation(latitude, longitude, startDate = dateStr, endDate = dateStr)
            }
            val currentHour = java.util.Calendar.getInstance().apply { time = now }.get(java.util.Calendar.HOUR_OF_DAY)
            val hourly = precipRes.hourly ?: return@launch

            if (hourly.time.isEmpty()) return@launch

            var startIndex = -1
            for (i in hourly.time.indices) {
                val timeStr = hourly.time.getOrNull(i) ?: continue
                val hour = timeStr.substring(11, 13).toIntOrNull() ?: -1
                if (hour == currentHour) {
                    startIndex = i
                    break
                }
            }

            if (startIndex == -1) return@launch

            var rainWithin2h = false
            var rainLater = false
            for (i in startIndex until hourly.time.size) {
                val precip = hourly.precipitation.getOrNull(i) ?: 0.0
                if (precip > 0) {
                    if (i <= startIndex + 2) {
                        rainWithin2h = true
                    } else {
                        rainLater = true
                    }
                }
            }

            rainAlert = when {
                rainWithin2h -> "2小时内有降雨"
                rainLater -> "今天可能有降雨"
                else -> "天气阳光明媚"
            }
        } catch (_: Exception) {
            rainAlert = ""
        }
    }

    fun loadGridWeather() = viewModelScope.launch {
        gridCities.forEach { city ->
            viewModelScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        ApiProvider.weatherApi.getCurrentWeather(city.latitude, city.longitude)
                    }
                    val weather = response.toCurrentWeather()
                    city.temperature = weather.temperature
                    city.weatherText = weather.weatherText
                    city.humidity = weather.relativeHumidity
                    city.loaded = true
                } catch (_: Exception) {}
            }
        }
    }
}