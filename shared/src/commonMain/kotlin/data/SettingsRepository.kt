package data

import com.russhwolf.settings.Settings

class SettingsRepository(private val settings: Settings) {
    fun getApiKey(): String = settings.getString("anthropic_api_key", "")
    fun setApiKey(key: String) { settings.putString("anthropic_api_key", key) }
}
