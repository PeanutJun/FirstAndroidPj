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
import kotlinx.coroutines.launch

private val DarkBg = Color(0xFF0B1220)
private val Primary = Color(0xFF22D3EE)
private val TxtMain = Color(0xFFF8FAFC)
private val TxtSub = Color(0xFF94A3B8)
private val CardBg = Color(0x14FFFFFF)

@Composable
fun CarScreen(navController: NavController) {
    val vm: CarViewModel = viewModel()
    LaunchedEffect(Unit) { vm.loadNews() }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(DarkBg)) {
            Text("Car \u6C7D\u8F66\u4E13\u533A", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TxtMain, modifier = Modifier.padding(top = 24.dp, start = 16.dp))
            Text("Real-time vehicle intelligence", fontSize = 12.sp, color = TxtSub.copy(alpha = 0.6f), modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { NewsSection(vm) }
                item { OilSection(vm) }
                item { PlateSection(vm) }
                item { ObdSection(vm) }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
        if (vm.showWeb) WebViewOverlay(vm.webUrl) { vm.showWeb = false }
    }
}

@Composable
private fun NewsSection(vm: CarViewModel) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text("News \u6C7D\u8F66\u8D44\u8BAF", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TxtMain)
            Spacer(Modifier.height(8.dp))
            if (vm.newsLoading) CircularProgressIndicator(color = Primary)
            else vm.newsList.forEach { item ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { vm.openWeb(item.url) }) {
                    AsyncImage(model = item.picUrl, contentDescription = null, modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.title, color = TxtMain, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(item.source, color = TxtSub, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
            if (vm.newsList.isNotEmpty() && !vm.newsLoading) {
                TextButton(onClick = { vm.loadMoreNews() }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("\u52A0\u8F7D\u66F4\u591A", color = Primary) }
            }
        }
    }
}

@Composable
private fun OilSection(vm: CarViewModel) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text("\u26FD \u6CB9\u4EF7\u67E5\u8BE2", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TxtMain)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = vm.oilProvince, onValueChange = { vm.oilProvince = it }, label = { Text("\u7701\u4EFD") }, modifier = Modifier.weight(1f), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TxtMain, unfocusedTextColor = TxtMain, focusedBorderColor = Primary, cursorColor = Primary))
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.loadOil() }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("\u67E5\u8BE2") }
            }
            if (vm.oilLoading) CircularProgressIndicator(color = Primary, modifier = Modifier.padding(top = 8.dp))
            vm.oilResult?.let {
                Column(Modifier.padding(top = 8.dp)) {
                    Text(it.prov, color = TxtMain, fontWeight = FontWeight.Bold)
                    listOf("92\u53F7" to it.p92, "95\u53F7" to it.p95, "98\u53F7" to it.p98, "89\u53F7" to it.p89, "\u67F4\u6CB9" to it.p0).forEach { (label, price) -> if (price.isNotEmpty()) Text(label + ": \u00A5" + price, color = TxtSub, fontSize = 13.sp) }
                }
            }
        }
    }
}

@Composable
private fun PlateSection(vm: CarViewModel) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text("\u8F66\u724C\u5F52\u5C5E\u5730\u67E5\u8BE2", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TxtMain)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = vm.plateWord, onValueChange = { vm.plateWord = it }, label = { Text("川A/粤A") }, modifier = Modifier.weight(1f), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TxtMain, unfocusedTextColor = TxtMain, focusedBorderColor = Primary, cursorColor = Primary))
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.loadPlate() }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("\u67E5\u8BE2") }
            }
            if (vm.plateLoading) CircularProgressIndicator(color = Primary, modifier = Modifier.padding(top = 8.dp))
            vm.plateResult?.let { Column(Modifier.padding(top = 8.dp)) { Text(it.number + " -> " + it.province + " " + it.city, color = TxtMain, fontSize = 14.sp) } }
        }
    }
}

@Composable
private fun ObdSection(vm: CarViewModel) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text("OBD \u6545\u969C\u7801", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TxtMain)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = vm.obdCode, onValueChange = { vm.obdCode = it }, label = { Text("P267B") }, modifier = Modifier.weight(1f), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TxtMain, unfocusedTextColor = TxtMain, focusedBorderColor = Primary, cursorColor = Primary))
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.loadObd() }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("\u67E5\u8BE2") }
            }
            if (vm.obdLoading) CircularProgressIndicator(color = Primary, modifier = Modifier.padding(top = 8.dp))
            vm.obdResult.forEach { item ->
                Card(Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0x0AFFFFFF))) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.code, color = Color(0xFFF87171), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("\u4E2D\u6587: " + item.zhnote, color = TxtMain, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                        Text("\u82F1\u6587: " + item.ennote, color = TxtSub, fontSize = 12.sp)
                        Text(item.descr, color = TxtSub, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                        Text(item.carmodel, color = TxtSub, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun WebViewOverlay(url: String, onClose: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(factory = { WebView(it).apply { webViewClient = WebViewClient(); settings.javaScriptEnabled = true; loadUrl(url) } }, modifier = Modifier.fillMaxSize())
        Row(Modifier.fillMaxWidth().background(Color(0xBF0B1220)).padding(8.dp)) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Close", tint = TxtMain) }
            Text("\u6587\u7AE0\u8BE6\u60C5", color = TxtMain, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally))
            Spacer(Modifier.width(48.dp))
        }
    }
}

class CarViewModel : ViewModel() {
    var newsList by mutableStateOf<List<CarNewsItem>>(emptyList())
    var newsLoading by mutableStateOf(false)
    var currentPage by mutableStateOf(1)
    var oilProvince by mutableStateOf("\u56DB\u5DDD")
    var oilResult by mutableStateOf<OilPriceResult?>(null)
    var oilLoading by mutableStateOf(false)
    var plateWord by mutableStateOf("川A")
    var plateResult by mutableStateOf<LicensePlateResult?>(null)
    var plateLoading by mutableStateOf(false)
    var obdCode by mutableStateOf("P267B")
    var obdResult by mutableStateOf<List<ObdCodeItem>>(emptyList())
    var obdLoading by mutableStateOf(false)
    var showWeb by mutableStateOf(false)
    var webUrl by mutableStateOf("")

    fun openWeb(url: String) { webUrl = url; showWeb = true }
    fun loadNews() = viewModelScope.launch {
        newsLoading = true
        try { val r = ApiProvider.carApi.fetchCarNews(page = currentPage); if (r.code == 200 && r.result != null) newsList = r.result.newslist } catch (_: Exception) {}
        newsLoading = false
    }
    fun loadMoreNews() { currentPage++; loadNews() }
    fun loadOil() = viewModelScope.launch {
        if (oilProvince.isBlank()) return@launch
        oilLoading = true; oilResult = null
        try { val r = ApiProvider.carApi.fetchOilPrice(prov = oilProvince); if (r.code == 200) oilResult = r.result } catch (_: Exception) {}
        oilLoading = false
    }
    fun loadPlate() = viewModelScope.launch {
        if (plateWord.isBlank()) return@launch
        plateLoading = true; plateResult = null
        try { val r = ApiProvider.carApi.fetchLicensePlate(word = plateWord); if (r.code == 200) plateResult = r.result } catch (_: Exception) {}
        plateLoading = false
    }
    fun loadObd() = viewModelScope.launch {
        if (obdCode.isBlank()) return@launch
        obdLoading = true; obdResult = emptyList()
        try { val r = ApiProvider.carApi.fetchObdCode(code = obdCode); if (r.code == 200 && r.result != null) obdResult = r.result.list } catch (_: Exception) {}
        obdLoading = false
    }
}
