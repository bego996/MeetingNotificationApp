# 🧼 Entfernt alle Aufrufe von android.util.Log im Release-Build
#-assumenosideeffects class android.util.Log {
#    public static *** v(...);
#    public static *** d(...);
#    public static *** i(...);
#    public static *** w(...);
#    public static *** e(...);
#    public static *** println(...);
#}

#🧼 Entfernt System.out / System.err (z. B. println)
-assumenosideeffects class java.lang.System {
    public static java.io.PrintStream out;
    public static java.io.PrintStream err;
}


# Jetpack Compose (Kotlin Metadata)
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable <methods>;
}

# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Prevent field name removal for reflection
-keepclassmembers class * {
    <fields>;
}

# 🧠 Hinweis:
# Diese Regeln verhindern nicht, dass die Klassen an sich vorhanden sind,
# sondern sorgen nur dafür, dass ihre Methoden **keine Nebeneffekte** haben
# → dadurch entfernt R8 sie komplett aus dem Bytecode
