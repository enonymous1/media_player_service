package com.enonymous1.mediaplayer

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Background playback service.
 *
 * Media3's [MediaSessionService] owns the ExoPlayer instance and publishes a
 * media session, so playback survives leaving the app and shows up in the
 * system media controls, the lock screen, and Bluetooth/headset controls.
 * It also posts and updates the foreground-service notification itself:
 * this class does not build a notification by hand.
 *
 * Declared in the manifest with `foregroundServiceType="mediaPlayback"`,
 * which is what Android 14+ requires for this kind of service.
 */
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .build(),
                // Let the player handle audio focus (ducking/pausing for calls, etc.).
                true,
            )
            // Pause when headphones are unplugged.
            setHandleAudioBecomingNoisy(true)
        }
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
