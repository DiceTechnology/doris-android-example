package com.dice.doris.csai.util

import com.google.common.reflect.TypeToken
import com.google.gson.Gson

object GsonUtils {
    val gson = Gson()
    inline fun <reified T> fromJson(json: String): T? {
        return try {
            val type = object : TypeToken<T>() {}.type
            return gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}