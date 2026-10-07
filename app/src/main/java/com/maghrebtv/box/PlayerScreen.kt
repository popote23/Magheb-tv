package com.maghrebtv.box

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

/** Lecture plein écran. Haut/Bas ou CH+/CH- : changer de chaîne. Retour : revenir à la liste. */
@Composable
fun PlayerScreen(channels: List<Channel>, startIndex: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    var index by remember { mutableIntStateOf(startIndex) }
    var showInfo by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val focus = remember { FocusRequester() }

    val exo = remember { ExoPlayer.Builder(context).build().apply { playWhenReady = true } }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlayerError(e: PlaybackException) {
                error = "Flux indisponible"
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) error = null
            }
        }
        exo.addListener(listener)
        onDispose {
            exo.removeListener(listener)
            exo.release()
        }
    }

    LaunchedEffect(index) {
        error = null
        val url = channels[index].url
        val item = MediaItem.Builder().setUri(url).apply {
            if (!url.contains(".mpd")) setMimeType(MimeTypes.APPLICATION_M3U8)
        }.build()
        exo.setMediaItem(item)
        exo.prepare()
        showInfo = true
        delay(3_000)
        showInfo = false
    }

    LaunchedEffect(Unit) { focus.requestFocus() }
    BackHandler(onBack = onBack)

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (e.key) {
                    Key.DirectionUp, Key.ChannelUp -> {
                        index = (index + 1) % channels.size; true
                    }
                    Key.DirectionDown, Key.ChannelDown -> {
                        index = (index - 1 + channels.size) % channels.size; true
                    }
                    Key.DirectionCenter, Key.Enter -> {
                        showInfo = true; true
                    }
                    else -> false
                }
            }
            .focusRequester(focus)
            .focusable()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    keepScreenOn = true
                    isFocusable = false
                    player = exo
                }
            }
        )
        if (showInfo) {
            Text(
                "${index + 1}/${channels.size}  ${channels[index].name}",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(32.dp)
                    .background(Color(0x99000000))
                    .padding(12.dp)
            )
        }
        error?.let {
            Text(
                "$it — ↑ / ↓ pour changer de chaîne",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color(0xCC000000))
                    .padding(16.dp)
            )
        }
    }
}
