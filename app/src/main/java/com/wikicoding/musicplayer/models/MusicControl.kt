package com.wikicoding.musicplayer.models

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import androidx.documentfile.provider.DocumentFile
import com.wikicoding.musicplayer.adapters.MainActivityAdapter
import com.wikicoding.musicplayer.databinding.ActivityMainBinding
import com.wikicoding.musicplayer.notifications.MusicNotificationService
import java.util.concurrent.TimeUnit

object MusicControl {
    private var musicDuration: String = ""
    var name: String = ""

    fun pause(mediaPlayer: MediaPlayer, handler: Handler, updateRunnable: Runnable) {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            handler.removeCallbacks(updateRunnable)
        }
    }

    fun resume(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
    }

//    fun play(
//        context: Context,
//        index: Int,
//        mediaPlayer: MediaPlayer,
//        musicList: ArrayList<Song>,
//        mp3Paths: MutableList<String>,
//        audioManager: AudioManager,
//        audioFocusRequest: AudioFocusRequest,
//        handler: Handler,
//        updateRunnable: Runnable,
//        binding: ActivityMainBinding,
//        adapter: MainActivityAdapter
//    ) {
//        if (index in mp3Paths.indices) {
//            val result = audioManager.requestAudioFocus(audioFocusRequest)
//
//            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                val uri = mp3Paths[index]
//                mediaPlayer.stop()
//                mediaPlayer.release()
//                mediaPlayer.apply {
//                    setDataSource(context, Uri.parse(uri))
//                    resetSeekBar(binding)
//                    prepare()
//
//                    binding.seekBar.max = duration
//
//                    // Get the duration in milliseconds
//                    val durationMillis = duration
//                    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis.toLong())
//                    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis.toLong()) -
//                            TimeUnit.MINUTES.toSeconds(minutes)
//                    musicDuration = String.format("/ %02d:%02d", minutes, seconds)
//
//                    // Update the TextView with the duration
//                    binding.tvDuration.text = musicDuration
//
//                    name = getSongName(index, mp3Paths, context).replace(".mp3", "")
//
//                    // Update the TextView with the name
//                    binding.tvName.text = name
//
//                    val service = MusicNotificationService(context)
//                    service.showNotification(name, musicDuration)
//
//                    start()
//                    handler.post(updateRunnable)
//
//                    setOnCompletionListener {
//                        // Play the next track when the current one ends
//                        playNext(
//                            context,
//                            mediaPlayer,
//                            musicList,
//                            audioManager,
//                            audioFocusRequest,
//                            index,
//                            adapter,
//                            mp3Paths,
//                            handler,
//                            updateRunnable,
//                            binding
//                        )
//                    }
//                }
//
//                //currentTrackIndex = index
//            }
//        }
//    }
//
//    fun playNext(
//        context: Context,
//        mediaPlayer: MediaPlayer,
//        musicList: ArrayList<Song>,
//        audioManager: AudioManager,
//        audioFocusRequest: AudioFocusRequest,
//        currentTrackIndex: Int,
//        adapter: MainActivityAdapter,
//        mp3Paths: MutableList<String>,
//        handler: Handler,
//        updateRunnable: Runnable,
//        binding: ActivityMainBinding
//    ) {
//        musicList[currentTrackIndex].isPlaying = false
//        adapter.notifyItemChanged(currentTrackIndex)
//
//        var updatedIndex = currentTrackIndex + 1
//        if (updatedIndex >= mp3Paths.size) {
//            updatedIndex = 0 // Wrap around to the first track if at the end
//        }
//
//        musicList[updatedIndex].isPlaying = true
//        adapter.notifyItemChanged(updatedIndex)
//
//        play(
//            context,
//            updatedIndex,
//            mediaPlayer,
//            musicList,
//            mp3Paths,
//            audioManager,
//            audioFocusRequest,
//            handler,
//            updateRunnable,
//            binding,
//            adapter
//        )
//        handler.post(updateRunnable)
//        resetSeekBar(binding)
//    }
//
//    fun playPrevious(
//        context: Context,
//        mediaPlayer: MediaPlayer,
//        musicList: ArrayList<Song>,
//        audioManager: AudioManager,
//        audioFocusRequest: AudioFocusRequest,
//        currentTrackIndex: Int,
//        adapter: MainActivityAdapter,
//        mp3Paths: MutableList<String>,
//        handler: Handler,
//        updateRunnable: Runnable,
//        binding: ActivityMainBinding
//    ) {
//        musicList[currentTrackIndex].isPlaying = false
//        adapter.notifyItemChanged(currentTrackIndex)
//
//        var updatedIndex = currentTrackIndex - 1
//        if (updatedIndex < 0) {
//            updatedIndex = mp3Paths.size - 1 // Wrap around to the first track if at the end
//        }
//
//        musicList[updatedIndex].isPlaying = true
//        adapter.notifyItemChanged(updatedIndex)
//
//        play(
//            context,
//            updatedIndex,
//            mediaPlayer,
//            musicList,
//            mp3Paths,
//            audioManager,
//            audioFocusRequest,
//            handler,
//            updateRunnable,
//            binding,
//            adapter
//        )
//        handler.post(updateRunnable)
//        resetSeekBar(binding)
//    }

    fun getSongName(index: Int, mp3Paths: MutableList<String>, context: Context): String {
        // Retrieve the name using DocumentFile
        val nameUri = Uri.parse(mp3Paths[index])
        DataStore.saveCurrentFile(nameUri, context)
        val documentFile = DocumentFile.fromSingleUri(context, nameUri)

        return documentFile?.name ?: "Unknown"
    }

    fun getAudioDuration(uri: Uri, context: Context): Long {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, uri)
        val durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        mediaMetadataRetriever.release()
        return durationStr?.toLong() ?: 0L
    }

    fun resetSeekBar(binding: ActivityMainBinding) {
        val seekBar = binding.seekBar
        seekBar.progress = 0
    }
}