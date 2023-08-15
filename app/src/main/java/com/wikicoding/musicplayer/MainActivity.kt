package com.wikicoding.musicplayer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import com.wikicoding.musicplayer.adapters.MainActivityAdapter
import com.wikicoding.musicplayer.databinding.ActivityMainBinding
import com.wikicoding.musicplayer.models.AppPermission
import com.wikicoding.musicplayer.models.DataStore
import com.wikicoding.musicplayer.models.MusicControl
import com.wikicoding.musicplayer.models.Song
import com.wikicoding.musicplayer.notifications.MediaControlReceiver
import com.wikicoding.musicplayer.notifications.MusicNotificationService
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    var mp3Paths = mutableListOf<String>()
    private lateinit var musicList: ArrayList<Song>
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    var currentTrackIndex = 0
    private lateinit var lastPlayed: String
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var adapter: MainActivityAdapter
    private lateinit var service: MusicNotificationService
    private lateinit var currentlyPlayingName: String
    private lateinit var currentlyPlayingMusicDuration: String

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data!!.data.let { uri ->
                    if (uri != null) {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                        val directory = DocumentFile.fromTreeUri(this, uri)
                        /**Add the files to the MutableList**/
                        listFiles(directory)
                        /**saving the current uri in shared prefs**/
                        DataStore.saveCurrentDirectory(uri, this)

                        setupRecyclerView()
                        updateUIonDirectoryChange()
                    }
                }
            }
        }

    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> MusicControl.resume(mediaPlayer!!)
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> MusicControl.pause(mediaPlayer!!, handler, updateRunnable)
        }
    }

    private val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
        .setAcceptsDelayedFocusGain(true)
        .setOnAudioFocusChangeListener(afChangeListener)
        .build()

    private val updateRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    val currentPositionMillis = it.currentPosition
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(currentPositionMillis.toLong())
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(currentPositionMillis.toLong()) -
                            TimeUnit.MINUTES.toSeconds(minutes)
                    val formattedPosition = String.format("At: %02d:%02d", minutes, seconds)

                    binding!!.tvCurrentPosition.text = formattedPosition

                    binding!!.seekBar.progress = currentPositionMillis

                    // Run this again after a short delay
                    handler.postDelayed(this, 1000)
                }
            }
        }
    }

    @RequiresApi(33)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding!!.root)

        service = MusicNotificationService(applicationContext)

        val selectMusicFolderBtn = binding!!.btnSelectFolder
        val playBtn = binding!!.btnPlay
        val pauseBtn = binding!!.btnPause
        val skipBtn = binding!!.btnSkip
        val prevBtn = binding!!.btnPrevious
        val stopBtn = binding!!.btnStop
        musicList = arrayListOf()

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val loadedDir = DataStore.loadCurrentDirectory(this)
        if (loadedDir != null) {
            contentResolver.takePersistableUriPermission(
                loadedDir,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val directory = DocumentFile.fromTreeUri(this, loadedDir)
            /**Add the files to the MutableList**/
            listFiles(directory)

            /** if the directory has mp3 files check, the last song **/
            val lastPlayedFile = DataStore.loadCurrentFile(this)
            if (lastPlayedFile != null) {
                // TODO: make lastPlayedFile the global var
                lastPlayed = lastPlayedFile.toString()
                updateUIonDirectoryChange()
            }
        }

        selectMusicFolderBtn.setOnClickListener {
            selectDirectory()
        }

        setupRecyclerView()

        playBtn.setOnClickListener {
            if (mediaPlayer?.isPlaying == false) {
                MusicControl.resume(mediaPlayer!!)
                handler.post(updateRunnable)
            } else if (mp3Paths.isNotEmpty()) {
                play(currentTrackIndex) // Play the first mp3 in the list as an example
            }
        }

        pauseBtn.setOnClickListener {
            MusicControl.pause(mediaPlayer!!, handler, updateRunnable)
        }

        stopBtn.setOnClickListener {
            stop()
        }

        skipBtn.setOnClickListener {
            playNext()
        }

        prevBtn.setOnClickListener {
            playPrevious()
        }

        /** adding the swipe to advance on the track **/
        binding!!.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        AppPermission.requestingPermissions({
            service.showNotification(currentlyPlayingName, currentlyPlayingMusicDuration) }, this)

        val intentFilter = IntentFilter().apply {
            addAction("Pause")
            addAction("ACTION_PAUSE")
            // ... other actions
        }

        registerReceiver(MediaControlReceiver(), intentFilter)
    }

    private fun updateUIonDirectoryChange() {
        if (mp3Paths.contains(lastPlayed)) {
            currentTrackIndex = mp3Paths.indexOf(lastPlayed)
            binding!!.tvName.text = musicList[currentTrackIndex].title
            currentlyPlayingName = musicList[currentTrackIndex].title
        } else {
            if (musicList.isNotEmpty()) {
                currentTrackIndex = 0
                binding!!.tvName.text = musicList[currentTrackIndex].title
                currentlyPlayingName = musicList[currentTrackIndex].title
            }
        }

        if (currentTrackIndex != -1) {
            if (mp3Paths.isNotEmpty()){
                val uri = Uri.parse(mp3Paths[currentTrackIndex])

                val durationMillis = MusicControl.getAudioDuration(uri, applicationContext)

                val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) -
                        TimeUnit.MINUTES.toSeconds(minutes)

                // Display duration or do anything else you need with it
                val formattedDuration = String.format("/ %02d:%02d", minutes, seconds)
                binding!!.tvDuration.text = formattedDuration
                currentlyPlayingMusicDuration = formattedDuration
            }
        } else {
            val uri = Uri.parse(mp3Paths[0])

            val durationMillis = MusicControl.getAudioDuration(uri, applicationContext)

            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) -
                    TimeUnit.MINUTES.toSeconds(minutes)

            // Display duration or do anything else you need with it
            val formattedDuration = String.format("/ %02d:%02d", minutes, seconds)
            binding!!.tvDuration.text = formattedDuration
            currentlyPlayingMusicDuration = formattedDuration
        }
        setupRecyclerView()
    }

    private fun selectDirectory() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        resultLauncher.launch(intent)
    }

    private fun listFiles(directory: DocumentFile?) {
        mp3Paths = mutableListOf()
        musicList = arrayListOf()
        var id = 1

        directory?.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                listFiles(file)
            } else if (file.name?.endsWith(".mp3") == true) {
                mp3Paths.add(file.uri.toString())
                val nameUri = Uri.parse(file.uri.toString())
                val documentFile = DocumentFile.fromSingleUri(applicationContext, nameUri)
                val newSong = Song(id, documentFile?.name!!.replace(".mp3", ""), false)
                musicList.add(newSong)
                id++
            }
        }

        mp3Paths.sort()
        musicList.sortBy { s -> s.title }
    }

    private fun play(index: Int) {
        if (index in mp3Paths.indices) {
            val result = audioManager.requestAudioFocus(audioFocusRequest)

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                val uri = mp3Paths[index]
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(applicationContext, Uri.parse(uri))
                    MusicControl.resetSeekBar(binding!!)
                    prepare()

                    binding!!.seekBar.max = duration

                    // Get the duration in milliseconds
                    val durationMillis = duration
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis.toLong())
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis.toLong()) -
                            TimeUnit.MINUTES.toSeconds(minutes)
                    currentlyPlayingMusicDuration = String.format("/ %02d:%02d", minutes, seconds)

                    // Update the TextView with the duration
                    binding!!.tvDuration.text = currentlyPlayingMusicDuration

                    currentlyPlayingName = MusicControl.getSongName(index, mp3Paths, applicationContext).replace(".mp3", "")

                    // Update the TextView with the name
                    binding!!.tvName.text = currentlyPlayingName

                    service.showNotification(currentlyPlayingName, currentlyPlayingMusicDuration)

                    start()
                    handler.post(updateRunnable)

                    setOnCompletionListener {
                        // Play the next track when the current one ends
                        playNext()
                    }
                }

                currentTrackIndex = index
            }
        }
    }

    private fun playNext() {
        musicList[currentTrackIndex].isPlaying = false
        adapter.notifyItemChanged(currentTrackIndex)

        currentTrackIndex++
        if (currentTrackIndex >= mp3Paths.size) {
            currentTrackIndex = 0 // Wrap around to the first track if at the end
        }

        musicList[currentTrackIndex].isPlaying = true
        adapter.notifyItemChanged(currentTrackIndex)

        play(currentTrackIndex)
        handler.post(updateRunnable)
        MusicControl.resetSeekBar(binding!!)
    }

    private fun playPrevious() {
        musicList[currentTrackIndex].isPlaying = false
        adapter.notifyItemChanged(currentTrackIndex)

        currentTrackIndex--
        if (currentTrackIndex < 0) {
            currentTrackIndex = mp3Paths.size - 1 // Wrap around to the first track if at the end
        }

        musicList[currentTrackIndex].isPlaying = true
        adapter.notifyItemChanged(currentTrackIndex)

        play(currentTrackIndex)
        handler.post(updateRunnable)
        MusicControl.resetSeekBar(binding!!)
    }

    private fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        handler.removeCallbacks(updateRunnable)

        binding!!.tvCurrentPosition.text = "At: 00:00"
        MusicControl.resetSeekBar(binding!!)

        // Abandon audio focus
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
        // TODO: show the play icon
    }

    private fun setupRecyclerView() {
        musicList.forEach { song ->
            song.isPlaying = false
        }

        musicList[currentTrackIndex].isPlaying = true

        adapter = MainActivityAdapter(musicList)
        binding!!.rvMusics.layoutManager = LinearLayoutManager(this@MainActivity)
        binding!!.rvMusics.adapter = adapter

        adapter.setOnClick(object : MainActivityAdapter.OnClickList {
            override fun onClick(position: Int, model: Song) {
                val indexOfClickedItem = musicList.indexOf(model)
                musicList.forEach { song ->
                    song.isPlaying = false
                }

                musicList[indexOfClickedItem].isPlaying = true
                adapter.notifyItemChanged(currentTrackIndex)
                adapter.notifyItemChanged(indexOfClickedItem)
                play(indexOfClickedItem)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        binding = null
    }
}