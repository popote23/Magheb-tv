package com.maghrebtv.box

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class Repository(private val context: Context) {
    fun cached(code: String): String =
        File(context.filesDir, "$code.m3u").takeIf { it.exists() }?.readText().orEmpty()

    /** Télécharge la playlist ; en cas d'échec, utilise la dernière copie en cache. */
    fun download(code: String): String {
        val f = File(context.filesDir, "$code.m3u")
        return try {
            val c = (URL("$PLAYLIST_BASE$code.m3u").openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 20_000
            }
            c.inputStream.bufferedReader().use { it.readText() }.also { f.writeText(it) }
        } catch (e: Exception) {
            cached(code)
        }
    }
}

object Builder {
    private val quality = Regex("""\((\d{3,4})p\)""")

    private fun channel(name: String, streams: List<Stream>): Channel {
        val sorted = streams.sortedWith(
            compareBy<Stream>(
                { it.name.contains("Geo-blocked", ignoreCase = true) },
                { -(quality.find(it.name)?.groupValues?.get(1)?.toIntOrNull() ?: 0) }
            )
        )
        return Channel(name, sorted.map { it.url }.distinct(), sorted.firstNotNullOfOrNull { it.logo })
    }

    fun build(texts: Map<String, String>): List<Bouquet> {
        val byCode = texts.mapValues { (_, t) -> M3u.parse(t).filterNot { Filters.isAdult(it) } }
        val index = byCode.values.flatten().groupBy { Filters.norm(it.name) }

        // Bouquets satellites
        val sats = Catalog.satellites.mapNotNull { sat ->
            var n = 0
            val cats = sat.categories.mapNotNull { (title, entries) ->
                val chans = entries.mapNotNull { entry ->
                    val names = entry.split("|")
                    val streams = names.flatMap { index[Filters.norm(it)].orEmpty() }
                    if (streams.isEmpty()) null else channel(names[0], streams).copy(number = ++n)
                }
                if (chans.isEmpty()) null else Category(title, chans)
            }
            if (cats.isEmpty()) null else Bouquet(sat.name, sat.position, cats)
        }

        // Bouquets par pays
        val countries = Catalog.countries.mapNotNull { (code, label) ->
            val groups = byCode[code].orEmpty().groupBy { Filters.norm(it.name) }.values.filter { it.isNotEmpty() }
            if (groups.isEmpty()) return@mapNotNull null
            val chans = groups.map { g -> Filters.category(g[0].group) to channel(Filters.display(g[0].name), g) }
            var n = 0
            val cats = chans.groupBy({ it.first }, { it.second })
                .toList()
                .sortedBy { Filters.order(it.first) }
                .map { (title, list) ->
                    Category(title, list.sortedBy { it.name.lowercase() }.map { it.copy(number = ++n) })
                }
            Bouquet(label, "Pays", cats)
        }
        return sats + countries
    }
}
