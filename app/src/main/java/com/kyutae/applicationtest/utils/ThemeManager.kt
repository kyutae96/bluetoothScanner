package com.kyutae.applicationtest.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * 테마 관리 클래스
 */
object ThemeManager {

    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    /**
     * 테마 모드
     */
    enum class ThemeMode(val value: Int) {
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        DARK(AppCompatDelegate.MODE_NIGHT_YES),
        SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        companion object {
            fun fromValue(value: Int): ThemeMode {
                return values().find { it.value == value } ?: SYSTEM
            }
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 테마 모드 설정
     */
    fun setThemeMode(context: Context, mode: ThemeMode) {
        getPrefs(context).edit().putInt(KEY_THEME_MODE, mode.value).apply()
        AppCompatDelegate.setDefaultNightMode(mode.value)
    }

    /**
     * 현재 테마 모드 가져오기
     */
    fun getThemeMode(context: Context): ThemeMode {
        val value = getPrefs(context).getInt(KEY_THEME_MODE, ThemeMode.SYSTEM.value)
        return ThemeMode.fromValue(value)
    }

    /**
     * 저장된 테마 적용
     */
    fun applyTheme(context: Context) {
        val mode = getThemeMode(context)
        AppCompatDelegate.setDefaultNightMode(mode.value)
    }

    /**
     * 다크 모드 활성화 여부 확인
     */
    fun isDarkMode(context: Context): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}
