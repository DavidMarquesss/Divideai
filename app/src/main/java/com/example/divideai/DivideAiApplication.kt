package com.example.divideai

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Application class responsável por aplicar preferências persistentes
 * (atualmente: o modo de tema escolhido pelo usuário) antes que qualquer
 * Activity seja criada.
 */
class DivideAiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(getSavedThemeMode(this))
    }

    companion object {
        private const val PREFS_NAME = "divideai_prefs"
        private const val KEY_THEME_MODE = "theme_mode"

        /** Recupera o modo de tema salvo, ou o padrão do sistema. */
        fun getSavedThemeMode(context: Context): Int {
            return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        /**
         * Persiste o modo escolhido e aplica imediatamente.
         * As Activities visíveis serão recriadas automaticamente.
         */
        fun setThemeMode(context: Context, mode: Int) {
            context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putInt(KEY_THEME_MODE, mode)
                .apply()
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}
