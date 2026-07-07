package presentation

import androidx.lifecycle.ViewModel
import data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private val _apiKey = MutableStateFlow(settingsRepository.getApiKey())
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    fun saveApiKey(key: String) {
        settingsRepository.setApiKey(key)
        _apiKey.value = key
    }
}