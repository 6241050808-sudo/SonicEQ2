package com.example.model

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.ui.graphics.Color

data class StreamingApp(
    val id: String,
    val name: String,
    val packageName: String,
    val brandColor: Color,
    val description: String,
    val isInstalled: Boolean = false
) {
    companion object {
        fun getSupportedApps(context: Context): List<StreamingApp> {
            val pm = context.packageManager
            val list = listOf(
                StreamingApp(
                    id = "spotify",
                    name = "Spotify",
                    packageName = "com.spotify.music",
                    brandColor = Color(0xFF1DB954),
                    description = "Settings → Equalizer opens SonicEQ directly"
                ),
                StreamingApp(
                    id = "yt_music",
                    name = "YouTube Music",
                    packageName = "com.google.android.apps.youtube.music",
                    brandColor = Color(0xFFFF0000),
                    description = "Settings → Equalizer full support"
                ),
                StreamingApp(
                    id = "apple_music",
                    name = "Apple Music",
                    packageName = "com.apple.android.music",
                    brandColor = Color(0xFFFA243C),
                    description = "High-Res Lossless & Spatial Audio"
                ),
                StreamingApp(
                    id = "tidal",
                    name = "TIDAL",
                    packageName = "com.aspiro.tidal",
                    brandColor = Color(0xFF00FFFF),
                    description = "Hi-Fi FLAC & Master Quality"
                ),
                StreamingApp(
                    id = "deezer",
                    name = "Deezer",
                    packageName = "deezer.android.app",
                    brandColor = Color(0xFFA238FF),
                    description = "Global music streaming & flow"
                ),
                StreamingApp(
                    id = "soundcloud",
                    name = "SoundCloud",
                    packageName = "com.soundcloud.android",
                    brandColor = Color(0xFFFF5500),
                    description = "Remixes, EDM, & Live sets"
                ),
                StreamingApp(
                    id = "amazon_music",
                    name = "Amazon Music",
                    packageName = "com.amazon.mp3",
                    brandColor = Color(0xFF25D1DA),
                    description = "HD & Ultra HD audio streaming"
                ),
                StreamingApp(
                    id = "poweramp",
                    name = "Poweramp",
                    packageName = "com.maxmpz.audioplayer",
                    brandColor = Color(0xFF00E676),
                    description = "Local & Hi-Res offline audio player"
                )
            )

            return list.map { app ->
                val installed = try {
                    pm.getPackageInfo(app.packageName, 0)
                    true
                } catch (_: PackageManager.NameNotFoundException) {
                    false
                }
                app.copy(isInstalled = installed)
            }
        }

        fun launchOrOpenStore(context: Context, app: StreamingApp) {
            if (app.isInstalled) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return
                }
            }
            // Open in Play Store
            try {
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${app.packageName}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(marketIntent)
            } catch (_: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${app.packageName}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
        }
    }
}
