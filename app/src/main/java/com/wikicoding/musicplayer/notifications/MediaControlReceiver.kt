package com.wikicoding.musicplayer.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wikicoding.musicplayer.MainActivity

class MediaControlReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        println(intent.action)
        when (intent.action) {
            "Play" -> {
                println("play clicked")
            }
            "Pause" -> {

            }
            "ACTION_SKIP" -> {

            }
            "ACTION_PREVIOUS" -> {

            }
        }
        //val service = MusicNotificationService(context)
        //service.showNotification("Music1", "03:50")
    }
}