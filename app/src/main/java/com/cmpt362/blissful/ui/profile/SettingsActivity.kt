package com.cmpt362.blissful.ui.profile

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.cmpt362.blissful.R

class SettingsActivity : AppCompatActivity() {
    private lateinit var switchNotification: SwitchCompat
    private lateinit var confirmButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        confirmButton = findViewById(R.id.buttonBack)
        confirmButton.setOnClickListener {
            finish()
        }

        switchNotification = findViewById(R.id.switch_daily_notification)
        val prefs = getSharedPreferences("notification", Context.MODE_PRIVATE)
        switchNotification.isChecked = prefs.getBoolean("is_daily_notification_enabled", true)

        switchNotification.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("is_daily_notification_enabled", isChecked).apply()
        }
    }
}
