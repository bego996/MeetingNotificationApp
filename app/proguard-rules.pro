# 🧼 Entfernt alle Aufrufe von android.util.Log im Release-Build
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** println(...);
}

# 🧼 Entfernt System.out / System.err (z. B. println)
-assumenosideeffects class java.lang.System {
    public static java.io.PrintStream out;
    public static java.io.PrintStream err;
}


# 🧠 Hinweis:
# Diese Regeln verhindern nicht, dass die Klassen an sich vorhanden sind,
# sondern sorgen nur dafür, dass ihre Methoden **keine Nebeneffekte** haben
# → dadurch entfernt R8 sie komplett aus dem Bytecode
