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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
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

private val BG = Color(0xFF0B0F1A)
private val PANEL = Color(0xFF111827)
private val ACCENT = Color(0xFFE53935)
private val MUTED = Color(0xFF9CA3AF)

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
    var selected by remember { mutableStateOf("") }
    val bouquets = vm.bouquets
    val bouquet = bouquets.firstOrNull { it.title == selected } ?: bouquets.firstOrNull()
    var cat by remember(bouquet?.title) { mutableStateOf("Tous") }

    Row(Modifier.fillMaxSize().background(BG)) {
        // Colonne de gauche : satellites et pays
        Column(Modifier.width(300.dp).fillMaxHeight().background(PANEL).padding(20.dp)) {
            Text("Maghreb TV", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Récepteur IPTV", fontSize = 14.sp, color = MUTED)
            Spacer(Modifier.height(16.dp))
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bouquets) { b ->
                    BouquetItem(b, b.title == bouquet?.title) { selected = b.title }
                }
                item { FocusChip("↻ Actualiser", selected = false, onClick = { vm.refresh() }) }
            }
        }

        // Partie droite : catégories et chaînes
        Column(Modifier.weight(1f).fillMaxHeight().padding(24.dp)) {
            if (bouquet == null) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    if (vm.loading) {
                        CircularProgressIndicator()
                    } else {
                        Text(
                            "Aucune chaîne disponible. Vérifiez la connexion, puis Actualiser.",
                            color = Color.White, fontSize = 20.sp
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(bouquet.title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text("${bouquet.subtitle} · ${bouquet.all.size} chaînes", fontSize = 16.sp, color = MUTED)
                    if (vm.updating) Text("   ⟳ mise à jour…", fontSize = 14.sp, color = MUTED)
                }
                Spacer(Modifier.height(12.dp))
                val tabs = listOf("Tous") + bouquet.categories.map { it.title }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(tabs) { t -> FocusChip(t, selected = t == cat, onFocus = { cat = t }) }
                }
                Spacer(Modifier.height(12.dp))
                val list = if (cat == "Tous") bouquet.all
                else bouquet.categories.firstOrNull { it.title == cat }?.channels ?: bouquet.all
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(190.dp),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(list) { i, ch -> ChannelCard(ch) { onPlay(list, i) } }
                }
            }
        }
    }
}

@Composable
fun BouquetItem(b: Bouquet, selected: Boolean, onFocus: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused; if (it.isFocused) onFocus() }
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    focused -> ACCENT
                    selected -> Color(0xFF263042)
                    else -> Color(0xFF1C2333)
                }
            )
            .clickable(onClick = onFocus)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(b.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Text("${b.subtitle} · ${b.all.size} chaînes", color = Color(0xFFD1D5DB), fontSize = 13.sp)
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
                    focused -> ACCENT
                    selected -> Color(0xFF37474F)
                    else -> Color(0xFF1C2333)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp)
    ) {
        Text(label, color = Color.White, fontSize = 17.sp)
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
        Text(
            "${ch.number}", color = ACCENT, fontSize = 14.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        AsyncImage(
            model = ch.logo,
            contentDescription = null,
            modifier = Modifier.height(64.dp).fillMaxWidth(),
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
