package com.gbros.tabslite

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import com.gbros.tabslite.utilities.ApiHelper
import com.gbros.tabslite.utilities.DARK_MODE_PREF_NAME
import com.gbros.tabslite.utilities.PREFS_NAME
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// thanks https://github.com/codepath/android_guides/wiki/Understanding-the-Android-Application-Class
class DefaultApplication : Application() {
    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    override fun onCreate() {
        super.onCreate()

        // set dark mode based on preferences
        AppCompatDelegate.setDefaultNightMode(getNightMode()) // thanks https://proandroiddev.com/android-dark-theme-implementation-recap-4fcffb0c4bff

        // initialize the API key.  Must be at the Application level, because the app has multiple entry points.
        GlobalScope.launch { ApiHelper.updateApiKey() }  // set the api key now before we need it
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    override fun onLowMemory() {
        super.onLowMemory()
        // todo: we can delete tabs from the database that aren't favorites
    }

    private fun getNightMode(): Int {
        val settings: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return settings.getInt(DARK_MODE_PREF_NAME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    private fun setNightMode(pref: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM){
        AppCompatDelegate.setDefaultNightMode(pref)
        val settings: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        settings.edit().putInt(DARK_MODE_PREF_NAME, pref).apply()
    }

    fun darkModeDialog(context: Context) {
        // get current setting
        val checkedItem = when (getNightMode()){
            AppCompatDelegate.MODE_NIGHT_NO  -> 0
            AppCompatDelegate.MODE_NIGHT_YES -> 1
            else -> 2
        }
        val options = arrayOf("Force Light Mode", "Force Dark Mode", "Use System Mode") //todo: make these into @string resources

        // get new setting
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Dark Mode setting")
        builder.setSingleChoiceItems(options, checkedItem) { dialogInterface: DialogInterface, item: Int ->
            setNightMode(  when(item) {
                0 -> AppCompatDelegate.MODE_NIGHT_NO
                1 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            })

            dialogInterface.dismiss()
        }

        // start
        builder.create()
        builder.show();
    }

    fun runningOnFirebaseTest(): Boolean {
        try {
            val testLabSetting: String = Settings.System.getString(contentResolver, "firebase.test.lab")
            return "true" == testLabSetting
        } catch (_: Throwable) {
            return false
        }
    }
}