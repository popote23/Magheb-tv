package com.maghrebtv.box

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Source des playlists M3U (une par pays). Modifiable : mettez vos propres URL ici. */
const val PLAYLIST_BASE = "https://iptv-org.github.io/iptv/countries/"

enum class Country(val label: String, val code: String) {
    MA("Maroc", "ma"),
    DZ("Algérie", "dz"),
    TN("Tunisie", "tn"),
    FR("France", "fr")
}

data class Channel(
    val name: String,
    val url: String,
    val logo: String?,
    val group: String?,
    val country: Country
)

object M3uParser {
    private val attr = Regex("""([\w-]+)="([^"]*)"""")

    fun parse(text: String, country: Country): List<Channel> {
        val out = mutableListOf<Channel>()
        var name: String? = null
        var logo: String? = null
        var group: String? = null
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            when {
                line.startsWith("#EXTINF") -> {
                    val attrs = attr.findAll(line).associate { it.groupValues[1] to it.groupValues[2] }
                    logo = attrs["tvg-logo"]?.takeIf { it.isNotBlank() }
                    group = attrs["group-title"]?.takeIf { it.isNotBlank() }
                    name = line.substringAfterLast(',').trim()
                }
                line.isNotEmpty() && !line.startsWith("#") -> {
                    val n = name
                    if (n != null && line.startsWith("http")) {
                        out += Channel(n, line, logo, group, country)
                    }
                    name = null; logo = null; group = null
                }
            }
        }
        return out
    }
}

class ChannelRepository(private val context: Context) {
    /** Télécharge la playlist ; en cas d'échec, utilise la dernière copie en cache. */
    suspend fun load(country: Country): List<Channel> = withContext(Dispatchers.IO) {
        val cache = File(context.filesDir, "${country.code}.m3u")
        val text = try {
            val conn = (URL("$PLAYLIST_BASE${country.code}.m3u").openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 15_000
            }
            conn.inputStream.bufferedReader().use { it.readText() }.also { cache.writeText(it) }
        } catch (e: Exception) {
            if (cache.exists()) cache.readText() else ""
        }
        M3uParser.parse(text, country).distinctBy { it.url }
    }
}
