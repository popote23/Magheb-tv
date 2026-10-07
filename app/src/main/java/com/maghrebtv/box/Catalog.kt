package com.maghrebtv.box

class Sat(val name: String, val position: String, val categories: List<Pair<String, List<String>>>)

/**
 * Classement des chaînes gratuites (FTA) par satellite. Indicatif : les chaînes changent parfois de satellite.
 * Format d'une chaîne : "Nom affiché|autre nom|autre nom" (les autres noms aident à retrouver le flux internet).
 * Vous pouvez ajouter ou retirer des chaînes ici.
 */
object Catalog {
    /** Bouquets par pays : toutes les chaînes de la playlist du pays. */
    val countries = listOf("ma" to "Maroc", "dz" to "Algérie", "tn" to "Tunisie", "fr" to "France")

    private val extra = listOf(
        "eg", "sa", "ae", "qa", "kw", "bh", "om", "jo", "lb", "iq", "sy", "ly", "ps", "sd", "ye", "mr",
        "uk", "de", "us", "tr", "cn", "int"
    )
    val sources: List<String> = countries.map { it.first } + extra

    val satellites = listOf(
        Sat("Nilesat 201", "7°O", listOf(
            "Généralistes" to listOf(
                "Egyptian Channel 1|Al Oula|Egypt Channel 1", "Egyptian Channel 2|Al Thaniya|Egypt Channel 2",
                "Nile TV International|Nile TV", "CBC", "CBC Extra", "CBC Sofra", "ON|ON E", "Al Hayah|Al Hayat",
                "Al Nahar", "DMC", "Sada El Balad|Sada Elbalad", "Al Mehwar|Mehwar", "TEN|Ten TV",
                "Al Qahera Wal Nas|Al Kahera Wal Nas", "Al Assema", "MBC 1", "MBC Masr", "MBC Masr 2",
                "Dubai TV", "Abu Dhabi TV|Emirates TV"
            ),
            "Infos" to listOf(
                "Al Jazeera|Al Jazeera Arabic", "Al Jazeera Mubasher|Al Jazeera Mubasher Channel", "Al Arabiya",
                "Al Hadath", "Sky News Arabia", "Al Qahera News|Al Kahera News", "Nile News", "Extra News",
                "BBC Arabic", "France 24 Arabic", "DW Arabic", "Alhurra", "TRT Arabi", "CNBC Arabia",
                "Al Ghad", "Al Araby|Al Araby TV"
            ),
            "Sport" to listOf(
                "Nile Sport", "ON Sport", "Abu Dhabi Sports 1|AD Sports 1", "Abu Dhabi Sports 2|AD Sports 2",
                "Dubai Sports 1", "Dubai Sports 2", "Alkass One|Al Kass One", "Alkass Two|Al Kass Two",
                "Saudi Sports|KSA Sports", "Oman Sports", "Sharjah Sports", "Kuwait Sports"
            ),
            "Cinéma & Séries" to listOf(
                "Nile Drama", "Nile Cinema", "Nile Comedy", "CBC Drama", "DMC Drama", "Al Nahar Drama",
                "MBC 2", "MBC 4", "MBC Drama", "MBC Max", "Rotana Cinema", "Rotana Drama", "Rotana Classic",
                "Aflam TV", "Panorama Drama", "Panorama Film", "Time Drama", "Cima"
            ),
            "Musique" to listOf(
                "Rotana Clip", "Rotana Music", "Rotana Khalijiah", "Mazzika", "Melody", "Melody Classic",
                "Melody Aflam", "Wanasah"
            ),
            "Enfants" to listOf(
                "Spacetoon", "Baraem", "Jeem TV", "Majid Kids TV|Majid Kids", "Toyor Al Janna", "Karameesh",
                "MBC 3", "Cartoon Network Arabic|Cartoon Network Arabia", "Nile Family"
            ),
            "Religieux" to listOf(
                "Iqraa", "Al Majd", "Al Resalah|Al Risalah", "Al Nas", "Saudi Quran|Quran TV",
                "Saudi Sunnah|Sunnah TV", "Makkah TV|Makkah Live", "Madina TV|Madinah TV"
            ),
            "Culture & Documentaires" to listOf(
                "Nile Culture", "Nile Life", "Al Jazeera Documentary|Al Jazeera Doc", "Dubai Zaman", "Sama Dubai",
                "Sharjah TV"
            )
        )),
        Sat("Hotbird", "13°E", listOf(
            "Infos" to listOf(
                "France 24 Francais|France 24 French|France 24 FR", "France 24 English", "France 24 Arabic",
                "Euronews Francais|Euronews French", "Euronews English", "Euronews Arabic",
                "Al Jazeera English", "Al Jazeera|Al Jazeera Arabic", "BBC News", "BBC Arabic", "DW English|DW",
                "DW Arabic", "CGTN English|CGTN", "CGTN Francais|CGTN French", "CGTN Arabic", "TRT World",
                "TRT Arabi", "Africanews English|Africanews", "Africanews Francais", "Sky News Arabia"
            ),
            "Généralistes" to listOf(
                "TV5Monde Maghreb-Orient|TV5Monde Maghreb Orient", "TV5Monde Europe|TV5Monde",
                "TV5Monde Info", "2M Monde|2M", "Medi 1 TV|Medi1 TV", "Nile TV International|Nile TV"
            ),
            "Culture & Documentaires" to listOf(
                "Al Jazeera Documentary|Al Jazeera Doc", "Euronews", "NHK World", "Arirang"
            )
        )),
        Sat("Arabsat (Badr)", "26°E", listOf(
            "Généralistes" to listOf(
                "Saudi TV|Saudi Channel 1|KSA 1|Al Saudiya", "SBC|Saudi Broadcasting Corporation",
                "Al Ekhbariya|Al Ekhbaria|Saudi Ekhbariya", "Kuwait TV|Kuwait TV 1|KTV1", "Kuwait TV 2|KTV2",
                "Qatar TV", "Bahrain TV", "Oman TV", "Sama Dubai", "Sharjah TV", "Ajman TV",
                "Jordan TV|Jordan Television|JRTV", "Al Mamlaka", "Roya", "Palestine TV",
                "Al Iraqiya|Iraqiya", "Al Sharqiya|Al Sharqiya TV", "Dijlah",
                "Syria TV|Syrian TV|Al Souria", "Sudan TV|Sudan National TV", "Yemen TV|Yemen Satellite",
                "Libya Al Wataniya|Libya National Channel", "Libya Al Ahrar", "Mauritania TV|Al Mauritaniya",
                "LBCI|LBC International|LBC Lebanon", "Al Jadeed", "MTV Lebanon", "OTV", "Tele Liban"
            ),
            "Infos" to listOf(
                "Al Arabiya", "Al Hadath", "Al Jazeera|Al Jazeera Arabic", "Al Ekhbariya|Al Ekhbaria",
                "Al Ghad", "Alhurra", "Al Araby|Al Araby TV"
            ),
            "Sport" to listOf(
                "Saudi Sports|KSA Sports", "Kuwait Sports", "Oman Sports", "Bahrain Sports 1|Bahrain Sports",
                "Alkass One|Al Kass One", "Dubai Sports 1", "Sharjah Sports", "Abu Dhabi Sports 1|AD Sports 1",
                "Al Iraqiya Sport|Iraqiya Sport"
            ),
            "Religieux" to listOf(
                "Saudi Quran|Quran TV", "Saudi Sunnah|Sunnah TV", "Makkah TV|Makkah Live", "Madina TV|Madinah TV",
                "Al Majd", "Iqraa", "Al Resalah|Al Risalah"
            ),
            "Musique & Divertissement" to listOf(
                "Rotana Khalijiah", "Rotana Clip", "Rotana Music", "Wanasah", "Funoon"
            ),
            "Enfants" to listOf(
                "Spacetoon", "Majid Kids TV|Majid Kids", "Baraem", "Toyor Al Janna", "Jeem TV", "Karameesh"
            )
        ))
    )
}
