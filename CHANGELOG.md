# Changelog

## 2.0.0 — 2026-10-26

First functional release. The original 1.0 was an untouched Android Studio
template (a "Hello World!" screen, no player at all).

- Real local audio player: lists music from MediaStore, with play/pause,
  previous/next, seek, and a now-playing row.
- Background playback via AndroidX Media3: `PlaybackService` is a
  `MediaSessionService` hosting ExoPlayer, so audio keeps playing when the
  app is in the background and appears in system media controls, the lock
  screen, and headset/Bluetooth controls.
- Scoped-storage permissions done the current way: `READ_MEDIA_AUDIO`
  (Android 13+) / `READ_EXTERNAL_STORAGE` (Android 12 and below),
  `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, and runtime
  `POST_NOTIFICATIONS` for the media notification.
- Rewritten in Kotlin (was Java), Kotlin DSL build scripts, and a version
  catalog (`gradle/libs.versions.toml`).
- Toolchain: AGP 8.13.0, Gradle 8.14.3, Kotlin 2.3.0, compile/target SDK 36,
  min SDK 24, Java 17.
- Package renamed `com.example.media_player_service` ->
  `com.enonymous1.mediaplayer`; app renamed to "Media Player".
- Unit tests for playlist navigation and duration formatting; GitHub
  Actions CI builds the debug APK and runs unit tests.
- MIT LICENSE.

## 1.0 — 2023-12-25

Original Android Studio template commit. No playback code.
