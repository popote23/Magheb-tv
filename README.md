# Maghreb TV (Android TV / box, Android 14+)

IPTV : chaînes gratuites Maroc, Algérie, Tunisie, France.

## Compiler
1. Ouvrir le dossier dans Android Studio (Ladybug ou plus récent) → Sync.
2. Build > Build APK(s) → app/build/outputs/apk/debug/app-debug.apk
3. Installer sur la box : `adb connect IP_DE_LA_BOX:5555` puis `adb install app-debug.apk`
   (ou clé USB / appli « Downloader »).

## Télécommande
- Gauche/Droite/Bas : naviguer, OK : lire
- Pendant la lecture : Haut/Bas ou CH+/CH- = chaîne suivante/précédente, Retour = liste

## Sources
Les playlists M3U sont définies dans `Models.kt` (PLAYLIST_BASE). Remplacez-les par vos propres
sources officielles si besoin. Utilisez uniquement des flux dont la diffusion est autorisée.
