package zeffect.cn.common.sp

import android.content.Context


/**
 * Created by Administrator on 2017/9/17.
 */
object SpUtils {
    var PREFERENCE_NAME = "sp"

    /**
     * put string preferences
     *
     * @param context
     * @param key     The name of the preference to modify
     * @param value   The new value for the preference
     * @return True if the new values were successfully written to persistent storage.
     */
    fun putString(context: Context, key: String, value: String): Boolean {
        val settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString(key, value)
        return editor.commit()
    }

    /**
     * get string preferences
     *
     * @param context
     * @param key          The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a preference with
     * this name that is not a string
     */
    fun getString(context: Context, key: String, defaultValue: String = ""): String {
        val settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return settings.getString(key, defaultValue)
    }

    /**
     * put int preferences
     *
     * @param context
     * @param key     The name of the preference to modify
     * @param value   The new value for the preference
     * @return True if the new values were successfully written to persistent storage.
     */
    fun putInt(context: Context, key: String, value: Int): Boolean {
        val settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putInt(key, value)
        return editor.commit()
    }


    /**
     * get int preferences
     *
     * @param context
     * @param key          The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a preference with
     * this name that is not a int
     */
    fun getInt(context: Context, key: String, defaultValue: Int = 0): Int {
        val settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return settings.getInt(key, defaultValue)
    }

    /**
     * put long preferences
     *
     * @param context
     * @param key     The name of the preference to modify
     * @param value   The new value for the preference
     * @return True if the new values were successfully written to persistent storage.
     */
    fun putLong(context: Context, key: String, value: Long): Boolean {
        val settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putLong(key, value)
        return editor.commit()
    }

    /**
     * get long preferences
     *
     * @param context
     * @param key          The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a preference with
     * this name that is not a long
     */
    fun getLong(context: Context, key: String, defaultValue: Long = 0L): Long {
        val settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return settings.getLong(key, defaultValue)
    }

    /**
     * put float preferences
     *
     * @param context
     * @param key     The name of the preference to modify
     * @param value   The new value for the preference
     * @return True if the new values were successfully written to persistent storage.
     */
    fun putFloat(context: Context, key: String, value: Float): Boolean {
        val settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putFloat(key, value)
        return editor.commit()
    }

    /**
     * get float preferences
     *
     * @param context
     * @param key          The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a preference with
     * this name that is not a float
     */
    fun getFloat(context: Context, key: String, defaultValue: Float = 0f): Float {
        val settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return settings.getFloat(key, defaultValue)
    }

    /**
     * put boolean preferences
     *
     * @param context
     * @param key     The name of the preference to modify
     * @param value   The new value for the preference
     * @return True if the new values were successfully written to persistent storage.
     */
    fun putBoolean(context: Context, key: String, value: Boolean): Boolean {
        val settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putBoolean(key, value)
        return editor.commit()
    }

    /**
     * get boolean preferences
     *
     * @param context
     * @param key          The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a preference with
     * this name that is not a boolean
     */
    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        val settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return settings.getBoolean(key, defaultValue)
    }
}