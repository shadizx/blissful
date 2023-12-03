package com.cmpt362.blissful.db.util

import android.content.Context

fun getUserId(context: Context): String {
    val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    return sharedPref.getString("userId", "") ?: ""
}

fun getUserName(context: Context): String {
    val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    return sharedPref.getString("userName", "") ?: ""
}

fun signOut(context: Context) {
    val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("userId", "")
        apply()
    }
}
