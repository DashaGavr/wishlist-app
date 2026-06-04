package org.example.project

import com.russhwolf.settings.MapSettings
import data.SettingsRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsRepositoryTest {

    @Test
    fun `getApiKey returns empty string when not set`() {
        val repo = SettingsRepository(MapSettings())
        assertEquals("", repo.getApiKey())
    }

    @Test
    fun `setApiKey persists and getApiKey retrieves it`() {
        val settings = MapSettings()
        val repo = SettingsRepository(settings)
        repo.setApiKey("sk-ant-test-key")
        assertEquals("sk-ant-test-key", repo.getApiKey())
    }

    @Test
    fun `overwriting API key replaces previous value`() {
        val settings = MapSettings()
        val repo = SettingsRepository(settings)
        repo.setApiKey("first-key")
        repo.setApiKey("second-key")
        assertEquals("second-key", repo.getApiKey())
    }
}
