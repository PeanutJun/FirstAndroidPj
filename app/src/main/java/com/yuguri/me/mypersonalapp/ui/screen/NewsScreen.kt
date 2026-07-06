package com.yuguri.me.mypersonalapp.ui.screen

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.yuguri.me.mypersonalapp.data.model.*
import com.yuguri.me.mypersonalapp.data.network.ApiProvider
import com.yuguri.me.mypersonalapp.data.preferences.UserPreferences
import com.yuguri.me.mypersonalapp.ui.navigation.Screen
import kotlinx.coroutines.*

private val DarkBg = Color(0xFF0F172A)
private val Primary = Color(0xFFF97316)
private val Secondary = Color(0xFF38BDF8)
private val TxtMain = Color(0xFFF8FAFC)
private val TxtSub = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val vm: NewsViewModel = viewModel()
    val userId by prefs.userId.collectAsState(initial = 0)

    LaunchedEffect(vm.selectedIndex) {
        vm.loadNews(prefs)
    }

    Scaffold(bottomBar = { AppBottomBar(navController, Screen.News.route) }, containerColor = DarkBg) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("新闻", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TxtMain)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = vm.selectedIndex == 0, onClick = { vm.selectCategory(0, prefs) },
                        label = { Text("国内新闻", fontSize = 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary.copy(alpha = 0.2f), selectedLabelColor = Primary, containerColor = Color(0x0FFFFFFF), labelColor = TxtSub))
                    FilterChip(selected = vm.selectedIndex == 1, onClick = { vm.selectCategory(1, prefs) },
                        label = { Text("国际新闻", fontSize = 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary.copy(alpha = 0.2f), selectedLabelColor = Primary, containerColor = Color(0x0FFFFFFF), labelColor = TxtSub))
                    FilterChip(selected = vm.selectedIndex == 2, onClick = { vm.selectCategory(2, prefs) },
                        label = { Text("热门新闻", fontSize = 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary.copy(alpha = 0.2f), selectedLabelColor = Primary, containerColor = Color(0x0FFFFFFF), labelColor = TxtSub))
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (vm.loading && vm.newsList.isEmpty()) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary, modifier = Modifier.size(32.dp))
                        Text("加载中...", fontSize = 14.sp, color = TxtSub, modifier = Modifier.padding(top = 8.dp))
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(vm.newsList) { item ->
                            NewsCard(item,
                                onClick = {
                                    item.url?.let { vm.openWeb(it) }
                                    if (userId > 0) vm.incrementView(userId)
                                },
                                onTranslate = { vm.translateHeadline(item) }
                            )
                        }
                        if (vm.newsList.isNotEmpty() && !vm.loading) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    TextButton(onClick = { vm.loadMore(prefs) }) {
                                        Text("加载更多", color = Primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (vm.showWebView) {
                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                            }
                        },
                        update = { webView ->
                            if (webView.url != vm.webViewUrl) {
                                webView.loadUrl(vm.webViewUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    Row(Modifier.fillMaxWidth().background(Color(0xBF0B1220)).padding(8.dp)) {
                        IconButton(onClick = { vm.showWebView = false }) { 
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TxtMain) 
                        }
                        Text("文章详情", color = TxtMain, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally))
                        Spacer(Modifier.width(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsCard(item: NewsArticleItem, onClick: () -> Unit, onTranslate: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0x851E293B)), shape = RoundedCornerShape(12.dp),
        onClick = onClick) {
        Row(modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title ?: "", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TxtMain, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(item.description ?: "", fontSize = 13.sp, color = TxtSub, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.source?.name ?: "", fontSize = 12.sp, color = Secondary)
                    Text((item.publishedAt ?: "").substringBefore("T"), fontSize = 12.sp, color = TxtSub)
                }
            }
            if (!item.urlToImage.isNullOrEmpty()) {
                AsyncImage(model = item.urlToImage, contentDescription = "News Image",
                    modifier = Modifier.width(100.dp).height(80.dp).clip(RoundedCornerShape(8.dp)))
            }
        }
    }
}

class NewsViewModel : ViewModel() {
    var selectedIndex by mutableStateOf(0)
    var newsList by mutableStateOf(listOf<NewsArticleItem>())
    var loading by mutableStateOf(false)
    var currentPage by mutableStateOf(1)
    var searchQuery by mutableStateOf("")
    var showWebView by mutableStateOf(false)
    var webViewUrl by mutableStateOf("")
    var translateResult by mutableStateOf("")

    fun selectCategory(index: Int, prefs: UserPreferences?) {
        selectedIndex = index
        currentPage = 1
        newsList = emptyList()
        loadNews(prefs)
    }

    fun loadNews(prefs: UserPreferences?) {
        viewModelScope.launch {
            loading = true
            try {
                val result = withContext(Dispatchers.IO) {
                    when (selectedIndex) {
                        0 -> ApiProvider.newsApi.fetchDomesticNews(q = searchQuery.ifEmpty { "科技" }, page = currentPage)
                        1 -> ApiProvider.newsApi.fetchInternationalNews(q = searchQuery.ifEmpty { "news" }, page = currentPage)
                        else -> ApiProvider.newsApi.fetchTopHeadlines(page = currentPage)
                    }
                }
                if (result.status == "ok") {
                    newsList = if (currentPage == 1) result.articles else newsList + result.articles
                }
            } catch (e: Exception) { android.util.Log.e("NewsCrash", "loadNews: " + e.message.toString()) }
            loading = false
        }
    }

    fun loadMore(prefs: UserPreferences?) {
        currentPage++
        loadNews(prefs)
    }

    fun openWeb(url: String) {
        webViewUrl = url
        showWebView = true
    }

    fun translateHeadline(item: NewsArticleItem) {
        viewModelScope.launch {
            try {
                val body = BaiduTranslateRequest(appid = "20220327001144560", q = item.title ?: "", from = "en", to = "zh")
                val response = withContext(Dispatchers.IO) {
                    ApiProvider.baiduTranslateApi.translateText(body = body)
                }
                translateResult = response.trans_result.firstOrNull()?.dst ?: "翻译失败"
            } catch (e: Exception) { android.util.Log.e("NewsCrash", "loadNews: " + e.message.toString()) }
        }
    }

    fun incrementView(userId: Int) {
        viewModelScope.launch { 
            try { 
                withContext(Dispatchers.IO) {
                    ApiProvider.userApi.incrementNewsView(NewsViewRequest(userId))
                }
            } catch (e: Exception) { android.util.Log.e("NewsCrash", "incrementView: " + e.message.toString()) } 
        }
    }
}