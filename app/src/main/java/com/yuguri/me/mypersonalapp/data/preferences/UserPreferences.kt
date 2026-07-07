package com.yuguri.me.mypersonalapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val USER_ID = intPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val NICKNAME = stringPreferencesKey("nickname")
        val TOKEN = stringPreferencesKey("token")
        val NEWS_VIEW_COUNT = intPreferencesKey("news_view_count")
        val NEWS_FAVORITE_COUNT = intPreferencesKey("news_favorite_count")
        val AVATAR_TIMESTAMP = longPreferencesKey("avatar_timestamp")
        val CITY_NAME = stringPreferencesKey("city_name")
        val DISTRICT = stringPreferencesKey("district")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
    }

    val userId: Flow<Int> = context.dataStore.data.map { it[USER_ID] ?: 0 }
    val userName: Flow<String> = context.dataStore.data.map { it[USER_NAME] ?: "User" }
    val nickname: Flow<String> = context.dataStore.data.map { it[NICKNAME] ?: "User" }
    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN] }
    val newsViewCount: Flow<Int> = context.dataStore.data.map { it[NEWS_VIEW_COUNT] ?: 0 }
    val newsFavoriteCount: Flow<Int> = context.dataStore.data.map { it[NEWS_FAVORITE_COUNT] ?: 0 }
    val cityName: Flow<String> = context.dataStore.data.map { it[CITY_NAME] ?: "" }
    val district: Flow<String> = context.dataStore.data.map { it[DISTRICT] ?: "" }
    val latitude: Flow<Double> = context.dataStore.data.map { it[LATITUDE] ?: 0.0 }
    val longitude: Flow<Double> = context.dataStore.data.map { it[LONGITUDE] ?: 0.0 }

    suspend fun setUserId(id: Int) {
        context.dataStore.edit { it[USER_ID] = id }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[USER_NAME] = name }
    }

    suspend fun setNickname(name: String) {
        context.dataStore.edit { it[NICKNAME] = name }
    }

    suspend fun setToken(token: String?) {
        context.dataStore.edit {
            if (token != null) {
                it[TOKEN] = token
            } else {
                it.remove(TOKEN)
            }
        }
    }

    suspend fun incrementNewsView() {
        context.dataStore.edit { prefs ->
            val current = prefs[NEWS_VIEW_COUNT] ?: 0
            prefs[NEWS_VIEW_COUNT] = current + 1
        }
    }

    suspend fun incrementNewsFavorite() {
        context.dataStore.edit { prefs ->
            val current = prefs[NEWS_FAVORITE_COUNT] ?: 0
            prefs[NEWS_FAVORITE_COUNT] = current + 1
        }
    }

    suspend fun setNewsViewCount(count: Int) {
        context.dataStore.edit { it[NEWS_VIEW_COUNT] = count }
    }

    suspend fun setNewsFavoriteCount(count: Int) {
        context.dataStore.edit { it[NEWS_FAVORITE_COUNT] = count }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun setCityName(name: String) {
        context.dataStore.edit { it[CITY_NAME] = name }
    }

    suspend fun setDistrict(name: String) {
        context.dataStore.edit { it[DISTRICT] = name }
    }

    suspend fun setLatitude(value: Double) {
        context.dataStore.edit { it[LATITUDE] = value }
    }

    suspend fun setLongitude(value: Double) {
        context.dataStore.edit { it[LONGITUDE] = value }
    }

    suspend fun setCityPreference(cityName: String, district: String, latitude: Double, longitude: Double) {
        context.dataStore.edit {
            it[CITY_NAME] = cityName
            it[DISTRICT] = district
            it[LATITUDE] = latitude
            it[LONGITUDE] = longitude
        }
    }
}

