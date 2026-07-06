package com.yuguri.me.mypersonalapp.ui.screen
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuguri.me.mypersonalapp.data.api.LoginRequest
import com.yuguri.me.mypersonalapp.data.network.ApiProvider
import com.yuguri.me.mypersonalapp.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// ========== Color Constants ==========
private val DarkBackground = Color(0xFF0B1220)
private val PrimaryColor = Color(0xFF22D3EE)
private val TextPrimary = Color(0xFFF8FAFC)
private val TextSecondary = Color(0x99F8FAFC)   // 0x99 ≈ 60% alpha
private val InputBackground = Color(0x1FFFFFFF)  // subtle white
private val InputBorder = Color(0x33F8FAFC)
private val CardBackground = Color(0xB8141623)   // ≈ 0.72 alpha on #141623
// ========== ViewModel ==========
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
    // ---- Login fields ----
    fun updateLoginUsername(value: String) {
        _loginState.value = _loginState.value.copy(username = value, message = "")
    }
    fun updateLoginPassword(value: String) {
        _loginState.value = _loginState.value.copy(password = value, message = "")
    }
    // ---- Register fields ----
    fun updateRegisterUsername(value: String) {
        _registerState.value = _registerState.value.copy(username = value, message = "")
    }
    fun updateRegisterPassword(value: String) {
        _registerState.value = _registerState.value.copy(password = value, message = "")
    }
    fun updateRegisterPhone(value: String) {
        _registerState.value = _registerState.value.copy(phone = value, message = "")
    }
    // ---- Login action ----
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
    // ---- Register action ----
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
                    // 清空表单并延迟切换
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
// ========== Composable ==========
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val vm: LoginViewModel = viewModel(factory = LoginViewModel.Factory(userPreferences))
    var isLogin by remember { mutableStateOf(true) }
    val loginState by vm.loginState.collectAsState()
    val registerState by vm.registerState.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            // ---- Logo + Title ----
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🛡",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "易生活",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "天气 & 新闻 & 汇率 & 汽车",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
            // ---- Card with Tab ----
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackground)
                    .border(1.dp, InputBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(top = 16.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
            ) {
                // ---- Tab Row ----
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(InputBackground)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabButton(
                        text = "登录",
                        selected = isLogin,
                        onClick = { isLogin = true },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        text = "注册",
                        selected = !isLogin,
                        onClick = { isLogin = false },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                if (isLogin) {
                    LoginForm(
                        state = loginState,
                        onUsernameChange = vm::updateLoginUsername,
                        onPasswordChange = vm::updateLoginPassword,
                        onLogin = { vm.handleLogin(onLoginSuccess) }
                    )
                } else {
                    RegisterForm(
                        state = registerState,
                        onUsernameChange = vm::updateRegisterUsername,
                        onPhoneChange = vm::updateRegisterPhone,
                        onPasswordChange = vm::updateRegisterPassword,
                        onRegister = { vm.handleRegister { isLogin = true } }
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
// ========== Reusable UI ==========
@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) PrimaryColor else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) DarkBackground else TextSecondary
        )
    }
}
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(placeholder, color = TextSecondary.copy(alpha = 0.5f))
            },
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = PrimaryColor,
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = InputBorder,
                focusedContainerColor = InputBackground,
                unfocusedContainerColor = InputBackground
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
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
            containerColor = PrimaryColor,
            contentColor = DarkBackground,
            disabledContainerColor = PrimaryColor.copy(alpha = 0.3f),
            disabledContentColor = DarkBackground.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(25.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = DarkBackground,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
@Composable
private fun LoginForm(
    state: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    AuthTextField(
        value = state.username,
        onValueChange = onUsernameChange,
        label = "用户名",
        placeholder = "请输入用户名"
    )
    Spacer(modifier = Modifier.height(16.dp))
    AuthTextField(
        value = state.password,
        onValueChange = onPasswordChange,
        label = "密码",
        placeholder = "请输入密码",
        isPassword = true
    )
    if (state.message.isNotBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = state.message,
            fontSize = 13.sp,
            color = if (state.isSuccess) Color(0xFF7DEFA1) else Color(0xFFFF6B6B)
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
    AuthButton(
        text = "登 录",
        loading = state.loading,
        enabled = state.username.isNotBlank() && state.password.isNotBlank(),
        onClick = onLogin
    )
}
@Composable
private fun RegisterForm(
    state: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegister: () -> Unit
) {
    AuthTextField(
        value = state.username,
        onValueChange = onUsernameChange,
        label = "用户名",
        placeholder = "请输入用户名"
    )
    Spacer(modifier = Modifier.height(16.dp))
    AuthTextField(
        value = state.phone,
        onValueChange = { if (it.length <= 11) onPhoneChange(it) },
        label = "手机号",
        placeholder = "请输入手机号",
        keyboardType = KeyboardType.Number
    )
    Spacer(modifier = Modifier.height(16.dp))
    AuthTextField(
        value = state.password,
        onValueChange = onPasswordChange,
        label = "密码",
        placeholder = "请设置密码",
        isPassword = true
    )
    if (state.message.isNotBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = state.message,
            fontSize = 13.sp,
            color = if (state.isSuccess || state.message.contains("成功")) Color(0xFF7DEFA1) else Color(0xFFFF6B6B)
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
    AuthButton(
        text = "注 册",
        loading = state.loading,
        enabled = state.username.isNotBlank() && state.phone.length == 11 && state.password.isNotBlank(),
        onClick = onRegister
    )
}