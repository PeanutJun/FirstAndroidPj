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
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yuguri.me.mypersonalapp.data.api.ProfileUpdateRequest
import com.yuguri.me.mypersonalapp.data.model.*
import com.yuguri.me.mypersonalapp.data.network.ApiProvider
import com.yuguri.me.mypersonalapp.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val DarkBackground = Color(0xFF0B1220)
private val PrimaryColor = Color(0xFF22D3EE)
private val TextPrimary = Color(0xFFF8FAFC)
private val TextSecondary = Color(0xFF94A3B8)
private val CardBackground = Color(0x14FFFFFF)

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val viewModel: ProfileViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(prefs) as T
        }
    })

    val userId by prefs.userId.collectAsState(initial = 0)
    val userName by prefs.userName.collectAsState(initial = "User")
    val nickname by prefs.nickname.collectAsState(initial = "User")

    LaunchedEffect(userId) {
        if (userId > 0) {
            viewModel.loadProfile(userId)
            viewModel.loadCategories(userId)
        }
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var showAddCatDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "👤",
            fontSize = 48.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        Text(
            text = nickname.ifEmpty { userName },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        if (viewModel.profilePhone.isNotEmpty()) {
            Text(
                text = viewModel.profilePhone,
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.editNickname = nickname
                viewModel.editPhone = viewModel.profilePhone
                showEditDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("编辑资料", fontSize = 16.sp)
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "我的收藏",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (viewModel.catLoading) {
            CircularProgressIndicator(color = PrimaryColor)
        } else {
            LazyColumn {
                items(viewModel.catList) { cat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.selectCategory(userId, cat.id) },
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cat.name, color = TextPrimary, fontSize = 15.sp)
                            Icon(
                                if (viewModel.selectedCatId == cat.id) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    }

                    if (viewModel.selectedCatId == cat.id) {
                        if (viewModel.favsLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(16.dp),
                                color = PrimaryColor
                            )
                        } else {
                            viewModel.selectedCatFavs.forEach { fav ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0x0AFFFFFF))
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                                            Text(
                                                fav.content,
                                                color = TextPrimary,
                                                fontSize = 14.sp
                                            )
                                            if (fav.source.isNotEmpty()) {
                                                Text(
                                                    "—— ${fav.source}",
                                                    color = TextSecondary,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { viewModel.requestDeleteFavorite(fav.id) },
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color(0xFFF87171))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedButton(
                        onClick = { showAddCatDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "新增", modifier = Modifier.size(18.dp))
                        Text("新增列表", fontSize = 14.sp)
                    }
                }
            }
        }
    }

    if (showAddCatDialog) {
        var newCatName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCatDialog = false },
            title = { Text("新增收藏列表", color = TextPrimary) },
            text = {
                TextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    placeholder = { Text("请输入列表名称", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B),
                        focusedIndicatorColor = PrimaryColor,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCatName.isNotBlank()) {
                            viewModel.createCategory(userId, newCatName) { success ->
                                if (success) showAddCatDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCatDialog = false }) {
                    Text("取消", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("编辑资料", color = TextPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = viewModel.editNickname,
                        onValueChange = { viewModel.editNickname = it },
                        label = { Text("昵称") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = viewModel.editPhone,
                        onValueChange = { viewModel.editPhone = it },
                        label = { Text("手机号") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )
                    if (viewModel.message.isNotEmpty()) {
                        Text(
                            viewModel.message,
                            color = if (viewModel.message.contains("成功")) Color(0xFF22C55E)
                                    else Color(0xFFF87171),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveProfile(userId) { success ->
                            if (success) showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    enabled = !viewModel.saving
                ) {
                    Text(if (viewModel.saving) "保存中..." else "保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("取消", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // 删除确认对话框
    if (viewModel.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showDeleteDialog = false },
            title = { Text("确认删除", color = TextPrimary) },
            text = { Text("确定要删除这条收藏吗？", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeleteFavorite() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171))
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDeleteDialog = false }) {
                    Text("取消", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // 空列表 Snackbar 提示
    if (viewModel.snackbarText.isNotEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Snackbar(
                modifier = Modifier.padding(top = 16.dp),
                containerColor = Color(0xFF334155),
                contentColor = TextPrimary
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(viewModel.snackbarText, fontSize = 14.sp)
                }
            }
        }
    }
}

class ProfileViewModel(private val prefs: UserPreferences) : ViewModel() {
    var profilePhone by mutableStateOf("")
    var catList by mutableStateOf<List<CategoryData>>(emptyList())
    var catLoading by mutableStateOf(false)
    var selectedCatId by mutableStateOf(-1)
    var selectedCatFavs by mutableStateOf<List<FavoriteSentenceData>>(emptyList())
    var favsLoading by mutableStateOf(false)
    var editNickname by mutableStateOf("")
    var editPhone by mutableStateOf("")
    var saving by mutableStateOf(false)
    var message by mutableStateOf("")
    var showDeleteDialog by mutableStateOf(false)
    var pendingDeleteId by mutableStateOf(0)
    var snackbarText by mutableStateOf("")

    fun loadProfile(userId: Int) {
        viewModelScope.launch {
            try {
                val res = ApiProvider.userApi.getUserProfile(userId)
                if (res.code == 200 && res.data != null) {
                    profilePhone = res.data.phone
                }
            } catch (_: Exception) {}
        }
    }

    fun loadCategories(userId: Int) {
        viewModelScope.launch {
            catLoading = true
            try {
                val res = ApiProvider.userApi.getCategories(userId)
                if (res.code == 200 && res.data != null) {
                    catList = res.data
                }
            } catch (_: Exception) {}
            catLoading = false
        }
    }

    fun selectCategory(userId: Int, catId: Int) {
        if (selectedCatId == catId) {
            selectedCatId = -1
            selectedCatFavs = emptyList()
            return
        }
        selectedCatId = catId
        viewModelScope.launch {
            favsLoading = true
            try {
                val res = ApiProvider.userApi.getUserFavorites(userId, catId)
                if (res.code == 200 && res.data != null) {
                    selectedCatFavs = res.data
                    if (res.data.isEmpty()) {
                        snackbarText = "该列表为空"
                        kotlinx.coroutines.delay(2000)
                        snackbarText = ""
                    }
                }
            } catch (_: Exception) {}
            favsLoading = false
        }
    }

    fun saveProfile(userId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            saving = true
            message = ""
            try {
                val body = ProfileUpdateRequest(userId, editNickname, editPhone)
                val res = ApiProvider.userApi.saveUserProfile(body)
                if (res.code == 200) {
                    prefs.setNickname(editNickname)
                    profilePhone = editPhone
                    message = "保存成功"
                    onResult(true)
                } else {
                    message = "保存失败: ${res.msg}"
                    onResult(false)
                }
            } catch (e: Exception) {
                message = "错误: ${e.message}"
                onResult(false)
            }
            saving = false
        }
    }

    fun requestDeleteFavorite(favId: Int) {
        pendingDeleteId = favId
        showDeleteDialog = true
    }

    fun confirmDeleteFavorite() {
        showDeleteDialog = false
        viewModelScope.launch {
            try {
                val body = RemoveFavoriteRequest(pendingDeleteId)
                val res = ApiProvider.userApi.removeFavorite(body)
                if (res.code == 200) {
                    selectedCatFavs = selectedCatFavs.filter { it.id != pendingDeleteId }
                    snackbarText = "已删除"
                    kotlinx.coroutines.delay(2000)
                    snackbarText = ""
                }
            } catch (_: Exception) {}
        }
    }

    fun createCategory(userId: Int, name: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val body = CreateCategoryRequest(userId, name)
                val res = ApiProvider.userApi.createCategory(body)
                if (res.code == 200) {
                    message = "创建成功"
                    loadCategories(userId)
                    onResult(true)
                } else {
                    message = res.msg.ifEmpty { "创建失败" }
                    onResult(false)
                }
            } catch (e: Exception) {
                message = "创建失败: ${e.message}"
                onResult(false)
            }
        }
    }
}

