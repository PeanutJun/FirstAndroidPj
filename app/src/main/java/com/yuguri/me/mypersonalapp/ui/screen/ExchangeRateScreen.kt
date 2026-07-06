package com.yuguri.me.mypersonalapp.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yuguri.me.mypersonalapp.data.model.*
import com.yuguri.me.mypersonalapp.data.network.ApiProvider
import kotlinx.coroutines.launch

private val DarkBg = Color(0xFF0B1220)
private val Primary = Color(0xFF22D3EE)
private val TxtMain = Color(0xFFF8FAFC)
private val TxtSub = Color(0xFF94A3B8)
private val CardBg = Color(0x14FFFFFF)

data class CurrencyOption(val code: String, val name: String, val flag: String)

val CURRENCIES = listOf(
    CurrencyOption("CNY", "\u4EBA\u6C11\u5E01", "CN"),
    CurrencyOption("USD", "\u7F8E\u5143", "US"),
    CurrencyOption("EUR", "\u6B27\u5143", "EU"),
    CurrencyOption("JPY", "\u65E5\u5143", "JP"),
    CurrencyOption("KRW", "\u97E9\u5143", "KR"),
    CurrencyOption("GBP", "\u82F1\u9551", "UK"),
    CurrencyOption("HKD", "\u6E2F\u5E01", "HK"),
    CurrencyOption("TWD", "\u65B0\u53F0\u5E63", "TW")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRateScreen(navController: NavController) {
    val vm: ExchangeRateViewModel = viewModel()
    LaunchedEffect(Unit) { vm.loadRates() }
    Box(Modifier.fillMaxSize().background(DarkBg)) {
        Column(Modifier.fillMaxSize()) {
            Text("\u6C47\u7387\u67E5\u8BE2", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TxtMain, modifier = Modifier.padding(top = 24.dp, start = 16.dp, bottom = 8.dp))

            Card(Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("\u57FA\u7840\u8D27\u5E01:", color = TxtSub, fontSize = 14.sp, modifier = Modifier.width(70.dp))
                        ExposedDropdownMenuBox(expanded = vm.showPicker, onExpandedChange = { vm.showPicker = !vm.showPicker }) {
                            OutlinedTextField(value = vm.baseCurrency, onValueChange = {}, readOnly = true,
                                modifier = Modifier.menuAnchor().weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TxtMain, unfocusedTextColor = TxtMain, focusedBorderColor = Primary, cursorColor = Primary))
                            ExposedDropdownMenu(expanded = vm.showPicker, onDismissRequest = { vm.showPicker = false }) {
                                CURRENCIES.forEach { c ->
                                    DropdownMenuItem(text = { Text(c.flag + " " + c.code + " " + c.name) }, onClick = { vm.selectBase(c.code) })
                                }
                            }
                        }
                    }
                    if (!vm.loading && vm.error.isEmpty()) Text("\u66F4\u65B0\u65E5\u671F: " + (vm.rates.firstOrNull()?.date ?: ""), color = TxtSub, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }

            if (vm.loading) CircularProgressIndicator(color = Primary, modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp))
            else if (vm.error.isNotEmpty()) {
                Text(vm.error, color = Color(0xFFF87171), modifier = Modifier.padding(16.dp))
                Button(onClick = { vm.loadRates() }, modifier = Modifier.padding(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("\u91CD\u8BD5") }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(vm.rates) { rate ->
                        Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(12.dp)) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    val cur = CURRENCIES.find { it.code == rate.quote }
                                    Text((cur?.flag ?: "") + " " + rate.quote, color = TxtMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(cur?.name ?: "", color = TxtSub, fontSize = 12.sp)
                                }
                                Text("" + rate.rate, color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

class ExchangeRateViewModel : ViewModel() {
    var rates by mutableStateOf<List<RateInfo>>(emptyList())
    var loading by mutableStateOf(false)
    var error by mutableStateOf("")
    var baseCurrency by mutableStateOf("CNY")
    var showPicker by mutableStateOf(false)

    fun selectBase(code: String) { if (code != baseCurrency) { baseCurrency = code; showPicker = false; loadRates() } else showPicker = false }

    fun loadRates() = viewModelScope.launch {
        loading = true; error = ""
        try { rates = ApiProvider.exchangeRateApi.getRatesByBase(baseCurrency) } catch (e: Exception) { error = "\u83B7\u53D6\u5931\u8D25: " + e.message }
        loading = false
    }
}
