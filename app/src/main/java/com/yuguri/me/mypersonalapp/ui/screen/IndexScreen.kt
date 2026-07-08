package com.yuguri.me.mypersonalapp.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yuguri.me.mypersonalapp.data.model.*
import com.yuguri.me.mypersonalapp.data.network.ApiProvider
import com.yuguri.me.mypersonalapp.data.preferences.UserPreferences
import com.yuguri.me.mypersonalapp.ui.navigation.Screen
import java.util.Calendar
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

private val DarkBackground = Color(0xFF0B1220)
private val PrimaryColor = Color(0xFF22D3EE)
private val TextPrimary = Color(0xFFF8FAFC)
private val TextSecondary = Color(0xFF94A3B8)
private val CardBackground = Color(0x14FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val vm: IndexViewModel = viewModel()
    val userId by prefs.userId.collectAsState(initial = 0)
    val userName by prefs.userName.collectAsState(initial = "User")
    val nickname by prefs.nickname.collectAsState(initial = "User")
    val viewCount by prefs.newsViewCount.collectAsState(initial = 0)
    LaunchedEffect(Unit) { vm.userId = userId; vm.loadHotSearch(); vm.loadMood(0); vm.loadHistory() }
    LaunchedEffect(userId) { vm.userId = userId; vm.loadSentenceFavCount(userId) }
    Scaffold(bottomBar = { AppBottomBar(navController, Screen.Index.route) }, containerColor = DarkBackground) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { UserBanner(nickname.ifEmpty { userName }, viewCount, vm.sentenceFavCount) }
            item { MoodCard(vm) }
            item { HotSearchSection(vm) }
            item { HistorySection(vm) }
            item { PhoneSearchSection(vm) }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun UserBanner(name: String, views: Int, favs: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBackground), shape = RoundedCornerShape(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("\uD83D\uDC4B \u4F60\u597D\uFF0C" + name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Column(horizontalAlignment = Alignment.End) {
                Text("\u6D4F\u89C8 " + views, fontSize = 12.sp, color = TextSecondary)
                Text("\u6536\u85CF " + favs, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun MoodCard(vm: IndexViewModel) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBackground), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("心情卡片(点击小标签换一句)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                IconButton(onClick = { vm.favoriteCurrentMood() }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "收藏", tint = PrimaryColor)
                }
            }
            if (vm.favMessage.isNotBlank()) {
                Text(vm.favMessage, fontSize = 11.sp, color = Color(0xFF22C55E))
            }
            Spacer(Modifier.height(12.dp))
            if (vm.moodLoading) CircularProgressIndicator(color = PrimaryColor, modifier = Modifier.size(24.dp))
            else { Text(vm.moodContent, fontSize = 15.sp, color = TextPrimary); Text(vm.moodSubtext, fontSize = 12.sp, color = TextSecondary) }
            if (vm.favMessage.isNotBlank()) {
                Text(vm.favMessage, fontSize = 11.sp, color = Color(0xFF22C55E))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("\u6253\u5DE5\u4EBA" to 0, "\u5C0F\u7A8D\u95E8" to 1, "\u670B\u53CB\u5708" to 2, "\u53F0\u8BCD" to 3).forEach { (label, type) ->
                    FilterChip(selected = vm.moodType == type, onClick = { vm.loadMood(type) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryColor.copy(alpha = 0.2f), selectedLabelColor = PrimaryColor, containerColor = Color(0x0FFFFFFF), labelColor = TextSecondary))
                }
            }
        }
    }

    if (vm.showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { vm.showCategoryDialog = false },
            title = { Text("\u9009\u62E9\u6536\u85CF\u5217\u8868", color = TextPrimary) },
            text = {
                Column {
                    if (vm.categories.isEmpty()) {
                        Text("\u52A0\u8F7D\u4E2D...", color = TextSecondary)
                    } else {
                        vm.categories.forEach { cat ->
                            TextButton(
                                onClick = { vm.performFavorite(cat.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(cat.name, color = PrimaryColor, fontSize = 16.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.showCategoryDialog = false }) {
                    Text("\u53D6\u6D88", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun HotSearchSection(vm: IndexViewModel) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBackground), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("\uD83D\uDD25 \u70ED\u641C\u699C", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            if (vm.hotLoading) CircularProgressIndicator(color = PrimaryColor, modifier = Modifier.size(24.dp))
            else vm.hotSearchList.forEachIndexed { i, item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(modifier = Modifier.weight(1f)) {
                        Text((i + 1).toString(), color = if (i < 3) PrimaryColor else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.width(24.dp))
                        Text(item.title, color = TextPrimary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    }
                    Text("" + item.hotnum, color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun HistorySection(vm: IndexViewModel) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBackground), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("\uD83D\uDCDC \u5386\u53F2\u4E0A\u7684\u4ECA\u5929", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            if (vm.historyLoading) CircularProgressIndicator(color = PrimaryColor, modifier = Modifier.size(24.dp))
            else vm.historyList.forEach { item ->
                Text(item.lsdate + ": " + item.title, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 3.dp))
            }
        }
    }
}

@Composable
private fun PhoneSearchSection(vm: IndexViewModel) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBackground), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("全国常用电话查询", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = vm.phoneQuery, onValueChange = { vm.phoneQuery = it },
                label = { Text("请输入你要查询的电话(例如淘宝)...") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryColor, cursorColor = PrimaryColor),
                trailingIcon = { IconButton(onClick = { vm.searchPhone() }) { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryColor) } })
            if (vm.phoneLoading) CircularProgressIndicator(color = PrimaryColor, modifier = Modifier.padding(top = 8.dp).size(24.dp))
            vm.phoneResults.forEach { item ->
                Text(item.name + " - " + item.tel + " (" + item.cate + ")", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
fun AppBottomBar(navController: NavController, currentRoute: String) {
    NavigationBar(containerColor = Color(0xFF0F172A), contentColor = TextPrimary) {
        listOf(Triple("\u9996\u9875", Icons.Default.Home, Screen.Index.route),
            Triple("\u5929\u6C14", Icons.Default.Cloud, Screen.Weather.route),
            Triple("\u6C7D\u8F66", Icons.Default.DirectionsCar, Screen.Car.route),
            Triple("\u65B0\u95FB", Icons.Default.Article, Screen.News.route),
            Triple("\u6211\u7684", Icons.Default.Person, Screen.Profile.route)
        ).forEach { (label, icon, route) ->
            NavigationBarItem(selected = currentRoute == route,
                onClick = { if (currentRoute != route) navController.navigate(route) { popUpTo(Screen.Index.route) { saveState = true }; launchSingleTop = true; restoreState = true } },
                icon = { Icon(icon, contentDescription = label) }, label = { Text(label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryColor, selectedTextColor = PrimaryColor, unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary, indicatorColor = PrimaryColor.copy(alpha = 0.1f)))
        }
    }
}

class IndexViewModel : ViewModel() {
    var hotSearchList by mutableStateOf<List<HotSearchItem>>(emptyList())
    var hotLoading by mutableStateOf(false)
    var moodType by mutableStateOf(0)
    var moodContent by mutableStateOf("")
    var moodSubtext by mutableStateOf("")
    var moodLoading by mutableStateOf(false)
    var historyList by mutableStateOf<List<HistoryItem>>(emptyList())
    var historyLoading by mutableStateOf(false)
    var phoneQuery by mutableStateOf("")
    var phoneResults by mutableStateOf<List<PhoneItem>>(emptyList())
    var phoneLoading by mutableStateOf(false)
    var favMessage by mutableStateOf("")
    var userId by mutableStateOf(0)
    var sentenceFavCount by mutableStateOf(0)
    var categories by mutableStateOf<List<CategoryData>>(emptyList())
    var showCategoryDialog by mutableStateOf(false)

    fun loadHotSearch() = viewModelScope.launch {
        hotLoading = true
        try { val res = ApiProvider.moodFeedApi.fetchHotSearch(); if (res.code == 200) hotSearchList = res.result?.list?.take(10) ?: emptyList() } catch (_: Exception) {}
        hotLoading = false
    }

    fun loadMood(type: Int) {
        moodType = type
        viewModelScope.launch {
            moodLoading = true
            try {
                when (type) {
                    0 -> { val r = ApiProvider.moodFeedApi.fetchWorkerQuote(); if (r.code == 200) { moodContent = r.result?.content ?: ""; moodSubtext = "\u2014\u2014 \u6253\u5DE5\u4EBA\u8BED\u5F55" } }
                    1 -> { val r = ApiProvider.moodFeedApi.fetchLifeTip(); if (r.code == 200) { moodContent = r.result?.content ?: ""; moodSubtext = "\u2014\u2014 \u751F\u6D3B\u5C0F\u7A8D\u95E8" } }
                    2 -> { val r = ApiProvider.tianjuApi.fetchPyqWenan(); if (r.code == 200) { moodContent = r.result?.content ?: ""; moodSubtext = "\u2014\u2014 " + (r.result?.source ?: "") } }
                    3 -> { val r = ApiProvider.tianjuApi.fetchDialogue(); if (r.code == 200) { moodContent = r.result?.dialogue ?: ""; moodSubtext = "\u2014\u2014 " + (r.result?.source ?: "") } }
                }
            } catch (_: Exception) {}
            moodLoading = false
        }
    }

    fun loadHistory() = viewModelScope.launch {
        historyLoading = true
        try {
            val cal = Calendar.getInstance()
            val month = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
            val day = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
            val date = month + day
            val res = ApiProvider.tianjuApi.fetchHistoryToday(date = date)
            if (res.code == 200) historyList = res.result?.list ?: emptyList()
        } catch (_: Exception) {}
        historyLoading = false
    }

    fun searchPhone() {
        if (phoneQuery.isBlank()) return
        viewModelScope.launch {
            phoneLoading = true
            try { val res = ApiProvider.moodFeedApi.searchPhone(word = phoneQuery); if (res.code == 200) phoneResults = res.result?.list ?: emptyList() } catch (_: Exception) {}
            phoneLoading = false
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    ApiProvider.userApi.getCategories(userId)
                }
                if (res.code == 200 && res.data != null) {
                    categories = res.data
                }
            } catch (_: Exception) {}
        }
    }

    fun favoriteCurrentMood() {
        if (moodContent.isBlank()) return
        if (userId <= 0) {
            favMessage = "请先登录"
            return
        }
        loadCategories()
        showCategoryDialog = true
    }

    fun performFavorite(categoryId: Int) {
        showCategoryDialog = false
        viewModelScope.launch {
            try {
                val body = FavoriteSentenceRequest(
                    userId = userId,
                    content = moodContent,
                    source = moodSubtext.removePrefix("—— "),
                    categoryId = categoryId
                )
                val r = withContext(Dispatchers.IO) {
                    ApiProvider.userApi.favoriteSentence(body)
                }
                if (r.code == 200) {
                    favMessage = "已收藏"
                    loadSentenceFavCount(userId)
                } else {
                    favMessage = r.msg.ifEmpty { "收藏失败" }
                }
            } catch (e: Exception) {
                favMessage = "收藏失败: ${e.message}"
            }
            kotlinx.coroutines.delay(2000)
            favMessage = ""
        }
    }

    fun loadSentenceFavCount(userId: Int) {
        if (userId <= 0) {
            sentenceFavCount = 0
            return
        }
        viewModelScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    ApiProvider.userApi.getFavoritesCount(userId)
                }
                if (res.code == 200 && res.data != null) {
                    sentenceFavCount = res.data.count
                }
            } catch (_: Exception) {}
        }
    }
}
