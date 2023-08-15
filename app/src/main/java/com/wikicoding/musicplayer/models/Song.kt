package com.wikicoding.musicplayer.models

data class Song(
    var id: Int,
    var title: String,
    var isPlaying: Boolean = false
)

