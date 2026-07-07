package com.yuguri.me.mypersonalapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.yuguri.me.mypersonalapp.data.network.ApiProvider
import com.yuguri.me.mypersonalapp.data.preferences.UserPreferences
import com.yuguri.me.mypersonalapp.ui.navigation.AppNavigation
import com.yuguri.me.mypersonalapp.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        userPreferences = UserPreferences(this)

        setContent {
            AppTheme {
                val token by userPreferences.token.collectAsState(initial = null)
                LaunchedEffect(token) {
                    if (token != null) {
                        ApiProvider.setToken(token)
                    }
                }
                AppNavigation()
            }
        }
    }
}