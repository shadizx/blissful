package com.cmpt362.blissful.db.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri

fun getUserId(context: Context): String {
    val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    return sharedPref.getString("userId", "") ?: ""
}

fun signOut(context: Context) {
    val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("userId", "")
        apply()
    }
}

fun getBitmap(context: Context, imgUri: Uri): Bitmap {
    val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
    val matrix = Matrix()
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}