package com.yuguri.me.mypersonalapp.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuguri.me.mypersonalapp.R
import com.yuguri.me.mypersonalapp.data.api.LoginRequest
import com.yuguri.me.mypersonalapp.data.network.ApiProvider
import com.yuguri.me.mypersonalapp.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val DarkBackground = Color(0xFF000000)
private val InputBackground = Color(0x1FFFFFFF)
private val InputBorder = Color(0x33FFFFFF)
private val CardBackground = Color(0xB8141623)
private val CardBorder = Color(0x22FFFFFF)
private val OrangeButton = Color(0xFFE67E22)

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val phone: String = "",
    val loading: Boolean = false,
    val message: String = "",
    val isSuccess: Boolean = false
)

class LoginViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()
    private val _registerState = MutableStateFlow(LoginUiState())
    val registerState: StateFlow<LoginUiState> = _registerState.asStateFlow()
    private val userApi = ApiProvider.userApi

    fun updateLoginUsername(value: String) {
        _loginState.value = _loginState.value.copy(username = value, message = "")
    }

    fun updateLoginPassword(value: String) {
        _loginState.value = _loginState.value.copy(password = value, message = "")
    }

    fun updateRegisterUsername(value: String) {
        _registerState.value = _registerState.value.copy(username = value, message = "")
    }

    fun updateRegisterPassword(value: String) {
        _registerState.value = _registerState.value.copy(password = value, message = "")
    }

    fun updateRegisterPhone(value: String) {
        _registerState.value = _registerState.value.copy(phone = value, message = "")
    }

    fun handleLogin(onSuccess: () -> Unit) {
        val state = _loginState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _loginState.value = state.copy(message = "请填写完整信息")
            return
        }
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(loading = true, message = "")
            try {
                val res = userApi.login(LoginRequest(state.username, state.password))
                if (res.code == 200 && res.data != null) {
                    val user = res.data
                    userPreferences.setUserId(user.userId)
                    userPreferences.setUserName(user.username)
                    userPreferences.setNickname(user.nickname.ifBlank { user.username })
                    userPreferences.setToken(user.token)
                    userPreferences.setNewsViewCount(user.newsViewCount)
                    userPreferences.setNewsFavoriteCount(user.newsFavoriteCount)
                    ApiProvider.setToken(user.token)
                    try {
                        val cityRes = userApi.getUserCityPreference(user.userId)
                        if (cityRes.code == 200 && cityRes.data != null) {
                            val city = cityRes.data
                            userPreferences.setCityPreference(city.city_name, city.district, city.latitude, city.longitude)
                        }
                    } catch (_: Exception) {}
                    _loginState.value = _loginState.value.copy(loading = false, isSuccess = true)
                    onSuccess()
                } else {
                    _loginState.value = _loginState.value.copy(
                        loading = false,
                        message = res.msg.ifBlank { "登录失败" }
                    )
                }
            } catch (e: Exception) {
                _loginState.value = _loginState.value.copy(
                    loading = false,
                    message = "网络错误：${e.message}"
                )
            }
        }
    }

    fun handleRegister(switchToLogin: () -> Unit) {
        val state = _registerState.value
        if (state.username.isBlank() || state.password.isBlank() || state.phone.isBlank()) {
            _registerState.value = state.copy(message = "请填写完整信息")
            return
        }
        if (state.phone.length != 11) {
            _registerState.value = state.copy(message = "手机号应为11位")
            return
        }
        viewModelScope.launch {
            _registerState.value = _registerState.value.copy(loading = true, message = "")
            try {
                val body = mapOf(
                    "username" to state.username,
                    "password" to state.password,
                    "phone" to state.phone
                )
                val res = userApi.register(body)
                if (res.code == 200) {
                    _registerState.value = _registerState.value.copy(
                        loading = false,
                        message = "注册成功！",
                        isSuccess = true
                    )
                    kotlinx.coroutines.delay(1500)
                    _registerState.value = LoginUiState()
                    switchToLogin()
                } else {
                    _registerState.value = _registerState.value.copy(
                        loading = false,
                        message = res.msg.ifBlank { "注册失败" }
                    )
                }
            } catch (e: Exception) {
                _registerState.value = _registerState.value.copy(
                    loading = false,
                    message = "网络错误：${e.message}"
                )
            }
        }
    }

    class Factory(private val prefs: UserPreferences) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(prefs) as T
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val vm: LoginViewModel = viewModel(factory = LoginViewModel.Factory(userPreferences))
    var isLogin by remember { mutableStateOf(true) }
    val loginState by vm.loginState.collectAsState()
    val registerState by vm.registerState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Image(
            painter = painterResource(id = R.drawable.backgroundchatgpt),
            contentDescription = "Login Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(22.dp)),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "易生活",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 14.dp)
                )

                Text(
                    text = "天气 & 新闻 & 汇率 & 汽车",
                    fontSize = 16.sp,
                    color = Color(0xAAFFFFFF),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SocialButton(
                    iconRes = R.drawable.pingguo,
                    text = "Continue with Apple",
                    backgroundColor = Color.White,
                    textColor = Color.Black,
                    onClick = {
                        showToast = true
                        scope.launch {
                            kotlinx.coroutines.delay(2000)
                            showToast = false
                        }
                    }
                )

                SocialButton(
                    iconRes = R.drawable.google,
                    text = "Continue with Google",
                    backgroundColor = Color.White,
                    textColor = Color.Black,
                    onClick = {
                        showToast = true
                        scope.launch {
                            kotlinx.coroutines.delay(2000)
                            showToast = false
                        }
                    }
                )

                SocialButton(
                    iconRes = null,
                    text = "Continue with PhoneNumber",
                    backgroundColor = OrangeButton,
                    textColor = Color.White,
                    iconText = "📞",
                    onClick = { showSheet = true }
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                containerColor = Color(0xFF191C28),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                LoginBottomSheetContent(
                    isLogin = isLogin,
                    onSwitchTab = { isLogin = it },
                    loginState = loginState,
                    registerState = registerState,
                    onUsernameChange = if (isLogin) vm::updateLoginUsername else vm::updateRegisterUsername,
                    onPasswordChange = if (isLogin) vm::updateLoginPassword else vm::updateRegisterPassword,
                    onPhoneChange = vm::updateRegisterPhone,
                    onLogin = { vm.handleLogin(onLoginSuccess) },
                    onRegister = { vm.handleRegister { isLogin = true } },
                    onClose = { showSheet = false }
                )
            }
        }

        AnimatedVisibility(
            visible = showToast,
            enter = slideInHorizontally(initialOffsetX = { -300 }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { 300 }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    color = Color(0xFF2D3748),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 40.dp)
                ) {
                    Text(
                        text = "敬请期待",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginBottomSheetContent(
    isLogin: Boolean,
    onSwitchTab: (Boolean) -> Unit,
    loginState: LoginUiState,
    registerState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onClose: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val sheetHeight = remember { (configuration.screenHeightDp * 0.65f).dp }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(sheetHeight)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x33FFFFFF))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            TabItem(
                text = "登录",
                selected = isLogin,
                onClick = { onSwitchTab(true) }
            )
            TabItem(
                text = "注册",
                selected = !isLogin,
                onClick = { onSwitchTab(false) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLogin) {
            AuthCard {
                AuthTextField(
                    value = loginState.username,
                    onValueChange = onUsernameChange,
                    placeholder = "请输入用户名"
                )
                Spacer(modifier = Modifier.height(14.dp))
                AuthTextField(
                    value = loginState.password,
                    onValueChange = onPasswordChange,
                    placeholder = "请输入密码",
                    isPassword = true
                )
                if (loginState.message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = loginState.message,
                        fontSize = 14.sp,
                        color = if (loginState.message.contains("成功")) Color(0xFF7DEFA1) else Color(0xFFFF6B6B)
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
                AuthButton(
                    text = "登  录",
                    loading = loginState.loading,
                    enabled = loginState.username.isNotBlank() && loginState.password.isNotBlank(),
                    onClick = onLogin
                )
            }
        } else {
            AuthCard {
                Text(
                    text = "用户名",
                    fontSize = 14.sp,
                    color = Color(0x99FFFFFF),
                    modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
                )
                AuthTextField(
                    value = registerState.username,
                    onValueChange = onUsernameChange,
                    placeholder = "请设置用户名"
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "手机号",
                    fontSize = 14.sp,
                    color = Color(0x99FFFFFF),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                AuthTextField(
                    value = registerState.phone,
                    onValueChange = { if (it.length <= 11) onPhoneChange(it) },
                    placeholder = "请输入手机号",
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "密码",
                    fontSize = 14.sp,
                    color = Color(0x99FFFFFF),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                AuthTextField(
                    value = registerState.password,
                    onValueChange = onPasswordChange,
                    placeholder = "请设置密码",
                    isPassword = true
                )
                if (registerState.message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = registerState.message,
                        fontSize = 14.sp,
                        color = if (registerState.message.contains("成功")) Color(0xFF7DEFA1) else Color(0xFFFF6B6B)
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
                AuthButton(
                    text = "注  册",
                    loading = registerState.loading,
                    enabled = registerState.username.isNotBlank() && registerState.phone.length == 11 && registerState.password.isNotBlank(),
                    onClick = onRegister
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SocialButton(
    iconRes: Int?,
    text: String,
    backgroundColor: Color,
    textColor: Color,
    iconText: String = "",
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.Crop
                )
            } else if (iconText.isNotEmpty()) {
                Text(text = iconText, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else Color(0x80FFFFFF)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(3.dp)
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (selected) Color(0xE6FFFFFF) else Color.Transparent)
        )
    }
}

@Composable
private fun AuthCard(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
        ) {
            content()
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, color = Color(0x80FFFFFF))
        },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = InputBackground,
            unfocusedContainerColor = InputBackground,
            disabledContainerColor = InputBackground
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(1.dp, InputBorder, RoundedCornerShape(14.dp))
    )
}

@Composable
private fun AuthButton(
    text: String,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0x33FFFFFF),
            contentColor = Color.White,
            disabledContainerColor = Color(0x1AFFFFFF),
            disabledContentColor = Color(0x66FFFFFF)
        ),
        shape = RoundedCornerShape(25.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(25.dp))
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}