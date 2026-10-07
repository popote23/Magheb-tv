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
    var urlIdx by remember { mutableIntStateOf(0) }
    var showInfo by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }

    val exo = remember { ExoPlayer.Builder(context).build().apply { playWhenReady = true } }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlayerError(e: PlaybackException) {
                // Essaie automatiquement la source suivante de la même chaîne
                if (urlIdx < channels[index].urls.size - 1) urlIdx++ else error = true
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) error = false
            }
        }
        exo.addListener(listener)
        onDispose {
            exo.removeListener(listener)
            exo.release()
        }
    }

    LaunchedEffect(index, urlIdx) {
        error = false
        val url = channels[index].urls[urlIdx]
        val item = MediaItem.Builder().setUri(url).apply {
            if (!url.contains(".mpd")) setMimeType(MimeTypes.APPLICATION_M3U8)
        }.build()
        exo.setMediaItem(item)
        exo.prepare()
    }

    LaunchedEffect(index) {
        showInfo = true
        delay(3_000)
        showInfo = false
    }

    LaunchedEffect(Unit) { focus.requestFocus() }
    BackHandler(onBack = onBack)

    fun zap(delta: Int) {
        index = (index + delta + channels.size) % channels.size
        urlIdx = 0
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (e.key) {
                    Key.DirectionUp, Key.ChannelUp -> { zap(1); true }
                    Key.DirectionDown, Key.ChannelDown -> { zap(-1); true }
                    Key.DirectionCenter, Key.Enter -> { showInfo = true; true }
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
            val ch = channels[index]
            val src = if (ch.urls.size > 1) "   (source ${urlIdx + 1}/${ch.urls.size})" else ""
            Text(
                "${ch.number}  ${ch.name}$src",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(32.dp)
                    .background(Color(0x99000000))
                    .padding(12.dp)
            )
        }
        if (error) {
            Text(
                "Flux indisponible — ↑ / ↓ pour changer de chaîne",
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
