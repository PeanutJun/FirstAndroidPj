package com.yuguri.me.mypersonalapp.ui.screen

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
private val AccentGreen = Color(0xFF4ADE80)

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
                        itemsIndexed(vm.newsList) { index, item ->
                            NewsCard(
                                item = item,
                                expandedDesc = vm.expandedItems.contains(index),
                                onToggleExpand = { vm.toggleExpand(index) },
                                translating = vm.translatingIndex == index,
                                translateResult = if (vm.translateIndex == index) vm.translateResult else null,
                                onTranslate = { vm.translateHeadline(item, index) },
                                onClick = {
                                    item.url?.let { vm.openWeb(it) }
                                    if (userId > 0) vm.incrementView(userId, prefs)
                                }
                            )
                        }
                        if (vm.newsList.isNotEmpty() && !vm.loading) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    TextButton(onClick = { vm.loadMore(prefs) }) {
                                        Text("加载更多", fontSize = 14.sp, color = Primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (vm.showWebView) {
        Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
            AndroidView(factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = object : WebViewClient() {}
                    settings.javaScriptEnabled = true
                    settings.setSupportZoom(true)
                }.also { webView ->
                    if (webView.url != vm.webViewUrl) {
                        webView.loadUrl(vm.webViewUrl)
                    }
                }
            }, modifier = Modifier.fillMaxSize())
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

@Composable
private fun NewsCard(
    item: NewsArticleItem,
    expandedDesc: Boolean,
    onToggleExpand: () -> Unit,
    translating: Boolean,
    translateResult: String?,
    onTranslate: () -> Unit,
    onClick: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0x851E293B)), shape = RoundedCornerShape(12.dp),
        onClick = onClick) {
        Row(modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title ?: "", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TxtMain, maxLines = 2, overflow = TextOverflow.Ellipsis)

                // 描述区域：可展开/收起
                if (!item.description.isNullOrBlank()) {
                    if (expandedDesc) {
                        Text(item.description, fontSize = 13.sp, color = TxtSub, modifier = Modifier.padding(top = 4.dp))
                    } else {
                        Text(item.description, fontSize = 13.sp, color = TxtSub, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                // 展开/收起按钮
                if (!item.description.isNullOrBlank() && item.description.length > 60) {
                    TextButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.height(28.dp).padding(0.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            if (expandedDesc) "收起" else "展开",
                            fontSize = 12.sp,
                            color = Primary
                        )
                    }
                }

                // 翻译结果
                AnimatedVisibility(
                    visible = translateResult != null,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    if (translateResult != null) {
                        Text(
                            translateResult,
                            fontSize = 13.sp,
                            color = AccentGreen,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                    }
                }

                // 底部：来源 + 日期 + 翻译按钮
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(item.source?.name ?: "", fontSize = 12.sp, color = Secondary)
                        Text((item.publishedAt ?: "").substringBefore("T"), fontSize = 12.sp, color = TxtSub)
                    }
                    // 翻译按钮
                    if (translating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Primary)
                    } else {
                        TextButton(
                            onClick = onTranslate,
                            modifier = Modifier.height(28.dp).padding(0.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("翻译", fontSize = 12.sp, color = AccentGreen)
                        }
                    }
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

    // 展开状态：记录每个展开的 item 索引
    var expandedItems by mutableStateOf(setOf<Int>())

    // 翻译状态
    var translateResult by mutableStateOf("")
    var translateIndex by mutableStateOf(-1)
    var translatingIndex by mutableStateOf(-1)

    fun toggleExpand(index: Int) {
        expandedItems = if (expandedItems.contains(index)) {
            expandedItems - index
        } else {
            expandedItems + index
        }
    }

    fun selectCategory(index: Int, prefs: UserPreferences?) {
        selectedIndex = index
        currentPage = 1
        newsList = emptyList()
        expandedItems = emptySet()
        translateResult = ""
        translateIndex = -1
        translatingIndex = -1
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

    fun translateHeadline(item: NewsArticleItem, index: Int) {
        viewModelScope.launch {
            translatingIndex = index
            translateIndex = -1
            translateResult = ""
            try {
                val textToTranslate = listOfNotNull(item.title?.takeIf { it.isNotBlank() }, item.description?.takeIf { it.isNotBlank() }).joinToString("\n\n")
                if (textToTranslate.isBlank()) {
                    translateResult = "没有可翻译的内容"
                    translateIndex = index
                    translatingIndex = -1
                    return@launch
                }
                val response = withContext(Dispatchers.IO) {
                    ApiProvider.baiduTranslateApi.translateText(
                        body = BaiduTranslateRequest(
                            appid = "20220327001144560",
                            q = textToTranslate,
                            from = "auto",
                            to = "zh"
                        )
                    )
                }
                translateResult = response.trans_result.firstOrNull()?.dst ?: "翻译失败"
                translateIndex = index
            } catch (e: Exception) {
                translateResult = "翻译失败: ${e.message}"
                translateIndex = index
                android.util.Log.e("NewsCrash", "translateHeadline: " + e.message.toString())
            }
            translatingIndex = -1
        }
    }

    fun incrementView(userId: Int, prefs: UserPreferences) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ApiProvider.userApi.incrementNewsView(NewsViewRequest(userId))
                }
                prefs.incrementNewsView()
            } catch (e: Exception) { android.util.Log.e("NewsCrash", "incrementView: " + e.message.toString()) }
        }
    }
}
