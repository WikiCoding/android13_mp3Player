package com.wikicoding.musicplayer.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.wikicoding.musicplayer.MainActivity
import com.wikicoding.musicplayer.R

class MusicNotificationService(private val context: Context) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(name: String, duration: String) {
        val activityIntent = Intent(context, MainActivity::class.java)

        val activityPendingIntent = PendingIntent.getActivity(
            context, 1, activityIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val playIntent = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, MediaControlReceiver::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val pauseIntent = PendingIntent.getBroadcast(context, 3, Intent("Pause"),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val previousIntent = PendingIntent.getBroadcast(context, 4, Intent(context, MediaControlReceiver::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val nextIntent = PendingIntent.getBroadcast(context, 5, Intent(context, MediaControlReceiver::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        // Add other actions if needed

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Currently Playing")
            .setContentText("$name $duration")
            .setContentIntent(activityPendingIntent)
//            .addAction(R.drawable.ic_baseline_play_arrow_24, "Play", playIntent)
//            .addAction(R.drawable.ic_baseline_pause_24, "Pause", pauseIntent)
//            .addAction(R.drawable.ic_baseline_skip_next_24, "Next", nextIntent)
//            .addAction(R.drawable.ic_baseline_skip_previous_24, "Previous", previousIntent)
//            .setStyle(
//                androidx.media.app.NotificationCompat.MediaStyle()
//                    .setMediaSession(mediaSession.sessionToken)
//                    .setShowActionsInCompactView(0, 1, 2)  // Indices of actions to display
//            )
            .build()

        notificationManager.notify(1, notification)
    }

    companion object {
        const val CHANNEL_ID = "notif_id"
    }

    private fun createNotification(): Notification {
        val activityIntent = Intent(context, MainActivity::class.java)

        val activityPendingIntent = PendingIntent.getActivity(
            context, 1, activityIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val playIntent = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, MediaControlReceiver::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val pauseIntent = PendingIntent.getBroadcast(context, 3, Intent(context, MediaControlReceiver::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val previousIntent = PendingIntent.getBroadcast(context, 4, Intent(context, MediaControlReceiver::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val nextIntent = PendingIntent.getBroadcast(context, 5, Intent(context, MediaControlReceiver::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        // Add other actions if needed

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Currently Playing")
            .setContentText("playing....")
            .setContentIntent(activityPendingIntent)
//            .addAction(R.drawable.ic_baseline_play_arrow_24, "Play", playIntent)
            .addAction(R.drawable.ic_baseline_pause_24, "Pause", pauseIntent)
//            .addAction(R.drawable.ic_baseline_skip_next_24, "Next", nextIntent)
//            .addAction(R.drawable.ic_baseline_skip_previous_24, "Previous", previousIntent)
            .build()
    }
}