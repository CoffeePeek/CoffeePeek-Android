package com.coffeepeek.admin.theme

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.coffeepeek.room.model.Setting
import com.coffeepeek.room.repository.SettingRepository

enum class ThemeMode { SYSTEM, LIGHT, DARK }

object ThemeManager {
    private const val THEME_MODE_KEY = "theme_mode"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    private var settingRepository: SettingRepository? = null
    private var isInitialized = false

    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun initialize(repository: SettingRepository) {
        if (isInitialized) return
        isInitialized = true
        settingRepository = repository

        scope.launch {
            repository.readFlow(THEME_MODE_KEY).collect { setting ->
                val mode = setting?.value
                    ?.let { value -> runCatching { ThemeMode.valueOf(value) }.getOrNull() }
                    ?: ThemeMode.SYSTEM
                if (_themeMode.value != mode) {
                    _themeMode.value = mode
                }
            }
        }
    }

    fun setTheme(mode: ThemeMode) {
        _themeMode.value = mode
        settingRepository?.let { repository ->
            scope.launch {
                repository.save(Setting(THEME_MODE_KEY, mode.name))
            }
        }
    }
}
