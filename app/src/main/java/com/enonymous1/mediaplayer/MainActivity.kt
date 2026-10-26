package com.enonymous1.mediaplayer

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import com.enonymous1.mediaplayer.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture

/**
 * The single UI screen: a list of local audio files plus now-playing controls.
 *
 * Playback itself lives in [PlaybackService] (a Media3 MediaSessionService),
 * so audio keeps playing when this activity is in the background. This
 * activity only talks to the service through a [MediaController]; it never
 * creates a player of its own.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var trackAdapter: TrackAdapter

    private var playlist = Playlist(emptyList())
    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var pendingPlayIndex: Int? = null
    private var isUserSeeking = false

    private val progressHandler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            if (controller?.isPlaying == true) {
                progressHandler.postDelayed(this, PROGRESS_UPDATE_MS)
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlayPauseIcon(isPlaying)
            if (isPlaying) startProgressUpdates() else stopProgressUpdates()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val metadata = mediaItem?.mediaMetadata ?: return
            binding.nowPlayingTitle.text = metadata.title ?: getString(R.string.unknown_track)
            binding.nowPlayingArtist.text = metadata.artist ?: getString(R.string.unknown_artist)
        }
    }

    private val audioPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        if (results[audioPermission] == true) {
            loadTracks()
        } else {
            binding.statusText.text = getString(R.string.permission_denied)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        trackAdapter = TrackAdapter { index -> playAt(index) }
        binding.trackList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = trackAdapter
        }

        binding.previousButton.setOnClickListener {
            playlist.previousIndex()?.let { playAt(it) }
        }
        binding.playPauseButton.setOnClickListener {
            controller?.let { player ->
                if (player.isPlaying) player.pause() else player.play()
            }
        }
        binding.nextButton.setOnClickListener {
            playlist.nextIndex()?.let { playAt(it) }
        }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) controller?.seekTo(progress.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isUserSeeking = false
            }
        })

        if (hasAudioPermission()) {
            loadTracks()
        } else {
            val permissions = mutableListOf(audioPermission)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions += Manifest.permission.POST_NOTIFICATIONS
            }
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val future = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                try {
                    controller = future.get()
                    controller?.addListener(playerListener)
                    pendingPlayIndex?.let { playAt(it) }
                    pendingPlayIndex = null
                } catch (e: Exception) {
                    // Service unavailable; controls stay inert until the next start.
                    controller = null
                }
            },
            ContextCompat.getMainExecutor(this),
        )
    }

    override fun onStop() {
        super.onStop()
        stopProgressUpdates()
        controller?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        controller = null
    }

    override fun onDestroy() {
        super.onDestroy()
        progressHandler.removeCallbacks(progressRunnable)
    }

    private fun hasAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, audioPermission) ==
            PackageManager.PERMISSION_GRANTED

    private fun loadTracks() {
        val tracks = MediaStoreRepository.loadTracks(this)
        playlist = Playlist(tracks)
        trackAdapter.tracks = tracks
        binding.statusText.text = if (tracks.isEmpty()) {
            getString(R.string.no_music_found)
        } else {
            getString(R.string.track_count, tracks.size)
        }
    }

    private fun playAt(index: Int) {
        val track = playlist.select(index) ?: return
        trackAdapter.selectedIndex = index
        binding.nowPlayingTitle.text = track.title
        binding.nowPlayingArtist.text = track.artist
        binding.durationText.text = formatDuration(track.durationMs)
        binding.seekBar.max = track.durationMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

        val player = controller
        if (player == null) {
            // Controller not connected yet (service still starting); play as
            // soon as onStart's listener delivers it.
            pendingPlayIndex = index
            return
        }
        player.setMediaItems(playlist.toMediaItems(), index, 0L)
        player.prepare()
        player.play()
    }

    private fun updatePlayPauseIcon(isPlaying: Boolean) {
        binding.playPauseButton.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
        )
    }

    private fun updateProgress() {
        val player = controller ?: return
        if (isUserSeeking) return
        val duration = player.duration
        if (duration > 0) {
            binding.seekBar.max = duration.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            binding.durationText.text = formatDuration(duration)
        }
        binding.seekBar.progress = player.currentPosition.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        binding.positionText.text = formatDuration(player.currentPosition)
    }

    private fun startProgressUpdates() {
        progressHandler.removeCallbacks(progressRunnable)
        progressHandler.post(progressRunnable)
    }

    private fun stopProgressUpdates() {
        progressHandler.removeCallbacks(progressRunnable)
    }

    companion object {
        private const val PROGRESS_UPDATE_MS = 500L
    }
}
