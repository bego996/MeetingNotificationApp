#############################################
# 🚀 Logging entfernen (nur Release)
#############################################
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** println(...);
}

-assumenosideeffects class java.lang.System {
    public static java.io.PrintStream out;
    public static java.io.PrintStream err;
}

#############################################
# 🛡️ Jetpack Compose
#############################################
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable <methods>;
}
# Verhindert Entfernen wichtiger Compose-Klassen
-keep class androidx.compose.** { *; }
-keep class kotlinx.coroutines.** { *; }

#############################################
# 🛡️ Room Database
#############################################
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keepattributes Signature
-keepattributes *Annotation*

#############################################
# 🛡️ Firebase + Crashlytics
#############################################
# Behalte Firebase Analytics & Crashlytics Klassen
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.measurement.** { *; }

# Crashlytics-Stacktrace-Mapping
-keepattributes SourceFile, LineNumberTable

#############################################
# 🧠 Generelle Reflection-Sicherheit
#############################################
-keepclassmembers class * {
    <fields>;
}
-keepattributes *Annotation*

#############################################
# 🛠️ Zusätzliche Sicherheit
#############################################
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlinx.coroutines.**

# 🧠 Hinweis:
# Diese Regeln verhindern nicht, dass die Klassen an sich vorhanden sind,
# sondern sorgen nur dafür, dass ihre Methoden **keine Nebeneffekte** haben
# → dadurch entfernt R8 sie komplett aus dem Bytecode
