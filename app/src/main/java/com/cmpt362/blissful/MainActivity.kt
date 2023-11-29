package com.cmpt362.blissful

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cmpt362.blissful.databinding.ActivityMainBinding
import com.cmpt362.blissful.notification.AlarmReceiver
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_add, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Check if daily notification is already enabled
        // Reference: https://stackoverflow.com/questions/29058179/android-app-with-daily-notification
        // Set the daily notification
        val prefs = getSharedPreferences("notification", Context.MODE_PRIVATE)

        if (prefs.getBoolean("is_daily_notification_enabled", true)) {

            // Set the daily notification if it is not set yet.
            setupDailyNotification()

            // Update shared pref on notification status
            prefs.edit().putBoolean("is_daily_notification_enabled", true).apply()
        } else {

            // Cancel daily notification if user turned it off
            turnOffDailyNotification()
        }
    }

    // Reference: https://stackoverflow.com/questions/23440251/how-to-repeat-notification-daily-on-specific-time-in-android-through-background
    private fun setupDailyNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Set time when the notification shows everyday
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 1)
        }

        // Repeat alarm
        // Reference: https://developer.android.com/training/scheduling/alarms
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun turnOffDailyNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.cancel(pendingIntent)
    }

    override fun onResume() {
        super.onResume()

        // This will trigger when user navigate back from the settings
        // Check if daily notification is enabled
        val prefs = getSharedPreferences("notification", Context.MODE_PRIVATE)
        val isNotificationEnabled = prefs.getBoolean("is_daily_notification_enabled", true)

        // If enabled, turn on notification alarm
        if (isNotificationEnabled) {
            setupDailyNotification()
        }
        // Else turn it off
        else {
            turnOffDailyNotification()
        }
    }
}