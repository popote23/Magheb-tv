package com.maghrebtv.box

import java.text.Normalizer

/** Source des playlists M3U (une par pays). */
const val PLAYLIST_BASE = "https://iptv-org.github.io/iptv/countries/"

data class Stream(val name: String, val url: String, val logo: String?, val group: String?)

data class Channel(
    val name: String,
    val urls: List<String>,   // plusieurs sources : la suivante est essayée si une échoue
    val logo: String?,
    val number: Int = 0
)

data class Category(val title: String, val channels: List<Channel>)

data class Bouquet(val title: String, val subtitle: String, val categories: List<Category>) {
    val all: List<Channel> get() = categories.flatMap { it.channels }
}

object M3u {
    private val attr = Regex("""([\w-]+)="([^"]*)"""")

    fun parse(text: String): List<Stream> {
        val out = mutableListOf<Stream>()
        var name: String? = null
        var logo: String? = null
        var group: String? = null
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            when {
                line.startsWith("#EXTINF") -> {
                    val a = attr.findAll(line).associate { it.groupValues[1] to it.groupValues[2] }
                    logo = a["tvg-logo"]?.takeIf { it.isNotBlank() }
                    group = a["group-title"]?.takeIf { it.isNotBlank() }
                    name = line.substringAfterLast(',').trim()
                }
                line.isNotEmpty() && !line.startsWith("#") -> {
                    val n = name
                    if (n != null && n.isNotBlank() && line.startsWith("http")) {
                        out += Stream(n, line, logo, group)
                    }
                    name = null; logo = null; group = null
                }
            }
        }
        return out
    }
}

object Filters {
    // Mots qui font exclure une chaîne (contenu adulte)
    private val adultWords = listOf(
        "xxx", "porn", "adult", "erotic", "erotik", "playboy", "hustler", "penthouse",
        "brazzers", "dorcel", "redlight", "babestation", "sexy", "sextv", "nsfw"
    )
    private val notAlnum = Regex("[^a-z0-9]")

    fun isAdult(s: Stream): Boolean =
        listOf(s.name, s.group.orEmpty()).any { t ->
            val n = t.lowercase().replace(notAlnum, "")
            adultWords.any { n.contains(it) }
        }

    private val noise = Regex("""\(.*?\)|\[.*?\]""")
    private val words = Regex("""\b(hd|sd|fhd|uhd|tv|channel)\b""")
    private val marks = Regex("\\p{Mn}+")
    private val keep = Regex("[^a-z0-9\\u0600-\\u06FF]")

    /** Nom normalisé pour comparer deux noms de chaînes. */
    fun norm(s: String): String {
        var t = s.lowercase().replace(noise, " ")
        t = Normalizer.normalize(t, Normalizer.Form.NFD).replace(marks, "")
        return t.replace(words, " ").replace(keep, "")
    }

    fun display(name: String): String = name.replace(noise, "").trim().ifEmpty { name }

    private val catFr = linkedMapOf(
        "general" to "Généralistes", "news" to "Infos", "sports" to "Sport",
        "movies" to "Cinéma", "series" to "Séries", "entertainment" to "Divertissement",
        "music" to "Musique", "kids" to "Enfants", "animation" to "Animation",
        "religious" to "Religieux", "documentary" to "Documentaires", "culture" to "Culture",
        "education" to "Éducation", "family" to "Famille", "comedy" to "Comédie",
        "lifestyle" to "Lifestyle", "business" to "Économie", "science" to "Science",
        "legislative" to "Parlement", "public" to "Service public", "weather" to "Météo",
        "travel" to "Voyage", "cooking" to "Cuisine", "outdoor" to "Nature", "classic" to "Classiques"
    )
    private val catOrder = catFr.values.toList() + "Autres"

    fun category(group: String?): String {
        val k = group?.substringBefore(';')?.trim()?.lowercase() ?: return "Autres"
        return catFr[k] ?: "Autres"
    }

    fun order(title: String): Int = catOrder.indexOf(title).let { if (it < 0) 99 else it }
}
