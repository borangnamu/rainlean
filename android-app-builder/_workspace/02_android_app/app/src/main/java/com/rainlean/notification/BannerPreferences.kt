package com.rainlean.notification

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "banner_prefs")

@Singleton
class BannerPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_BANNER_ENABLED = booleanPreferencesKey("banner_enabled")
        private val KEY_LAST_HEADING_DEG = doublePreferencesKey("last_heading_deg")
    }

    val bannerEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_BANNER_ENABLED] ?: false
    }

    val lastHeadingDeg: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_HEADING_DEG] ?: 0.0
    }

    suspend fun setBannerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BANNER_ENABLED] = enabled }
    }

    suspend fun saveLastHeadingDeg(deg: Double) {
        context.dataStore.edit { it[KEY_LAST_HEADING_DEG] = deg }
    }
}
