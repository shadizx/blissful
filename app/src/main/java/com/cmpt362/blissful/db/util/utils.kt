package com.cmpt362.blissful.db.util

import android.content.Context

fun getUserId(context: Context): Int {
    val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    return sharedPref.getInt("userId", -1)
}

fun signOut(context: Context) {
    val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putInt("userId", -1)
        apply()
    }
}