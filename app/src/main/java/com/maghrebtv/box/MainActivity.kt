package com.maghrebtv.box

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) { App(vm) }
        }
    }
}

@Composable
fun App(vm: MainViewModel) {
    var playing by remember { mutableStateOf<Pair<List<Channel>, Int>?>(null) }
    val current = playing
    if (current != null) {
        PlayerScreen(current.first, current.second, onBack = { playing = null })
    } else {
        HomeScreen(vm) { list, index -> playing = list to index }
    }
}

@Composable
fun HomeScreen(vm: MainViewModel, onPlay: (List<Channel>, Int) -> Unit) {
    var country by remember { mutableStateOf(Country.MA) }
    val list = vm.channels[country].orEmpty()

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F1A))
            .padding(32.dp)
    ) {
        Text("Maghreb TV", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Country.entries.forEach { c ->
                FocusChip(c.label, selected = c == country, onFocus = { country = c })
            }
            FocusChip("↻ Actualiser", selected = false, onClick = { vm.refresh() })
        }
        Spacer(Modifier.height(16.dp))
        when {
            vm.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            list.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Aucune chaîne disponible. Vérifiez la connexion.", color = Color.White, fontSize = 20.sp)
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(200.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(list) { i, ch -> ChannelCard(ch) { onPlay(list, i) } }
            }
        }
    }
}

@Composable
fun FocusChip(
    label: String,
    selected: Boolean,
    onFocus: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    var focused by remember { mutableStateOf(false) }
    Box(
        Modifier
            .onFocusChanged { focused = it.isFocused; if (it.isFocused) onFocus() }
            .clip(RoundedCornerShape(24.dp))
            .background(
                when {
                    focused -> Color(0xFFE53935)
                    selected -> Color(0xFF37474F)
                    else -> Color(0xFF1C2333)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Text(label, color = Color.White, fontSize = 18.sp)
    }
}

@Composable
fun ChannelCard(ch: Channel, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    Column(
        Modifier
            .onFocusChanged { focused = it.isFocused }
            .clip(shape)
            .border(3.dp, if (focused) Color.White else Color.Transparent, shape)
            .background(if (focused) Color(0xFF2A3350) else Color(0xFF151B2B))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ch.logo,
            contentDescription = null,
            modifier = Modifier.height(72.dp).fillMaxWidth(),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(8.dp))
        Text(
            ch.name,
            color = Color.White,
            fontSize = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
