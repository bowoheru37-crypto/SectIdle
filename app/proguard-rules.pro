# Add project specific ProGuard rules here.
# Keep game models and data classes serialized via Moshi or JSON
-keep class com.sect.idle.models.** { *; }
-keep class com.sect.idle.gameplay.SectData { *; }

# Keep Room entities / DAOs
-keep class androidx.room.** { *; }

# Keep custom views and game core
-keep class com.sect.idle.ui.** { *; }
