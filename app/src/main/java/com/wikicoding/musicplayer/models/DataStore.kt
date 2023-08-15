package com.wikicoding.musicplayer.models

import android.content.Context
import android.net.Uri

object DataStore {
    fun saveCurrentDirectory(uri: Uri, context: Context) {
        val sharedPreferences =
            context.getSharedPreferences("MediaPlayerPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("CurrentDirectory", uri.toString())
            apply()
        }
    }

    fun saveCurrentFile(uri: Uri, context: Context) {
        val sharedPreferences =
            context.getSharedPreferences("MediaPlayerPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("CurrentFile", uri.toString())
            apply()
        }
    }

    fun loadCurrentDirectory(context: Context): Uri? {
        val sharedPreferences =
            context.getSharedPreferences("MediaPlayerPrefs", Context.MODE_PRIVATE)
        val directoryString = sharedPreferences.getString("CurrentDirectory", null)
        return directoryString?.let { Uri.parse(it) }
    }

    fun loadCurrentFile(context: Context): Uri? {
        val sharedPreferences =
            context.getSharedPreferences("MediaPlayerPrefs", Context.MODE_PRIVATE)
        val fileString = sharedPreferences.getString("CurrentFile", null)
        return fileString?.let { Uri.parse(it) }
    }
}