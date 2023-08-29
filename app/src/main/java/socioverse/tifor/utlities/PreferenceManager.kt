package socioverse.tifor.utlities

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences =
            context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    fun putBoolean(key: String?, value: Boolean?) {
        try {
            val editor = sharedPreferences.edit()
            editor.putBoolean(key, value!!)
            editor.apply()
        } catch (e: Exception) {
        }
    }

    fun getBoolean(key: String?): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun putString(key: String?, value: String?) {
        try {
            val editor = sharedPreferences.edit()
            editor.putString(key, value)
            editor.apply()
        } catch (e: Exception) {
        }
    }

    fun getString(key: String?): String? {
        return sharedPreferences.getString(key, null)
    }

    fun clear() {
        try {
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
        } catch (e: Exception) {
        }
    }
}