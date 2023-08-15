package com.wikicoding.musicplayer.notifications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class MusicApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MusicNotificationService.CHANNEL_ID,
                "Music",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Used for music control"

            /** now we need to tell to our notificationManager to create the notification **/
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}