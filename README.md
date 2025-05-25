# 📅 Meeting Notification App

Die **Meeting Notification App** ist eine Android-Anwendung, die Kontakte automatisch mit Kalenderereignissen verknüpft und darauf basierend personalisierte SMS-Erinnerungen versendet.

---

## 🌐 Features

* ✉️ SMS-Erinnerung für Termine
* 📅 Automatische Verknüpfung von Kontakten mit Kalenderereignissen
* ✏️ Anpassbare Nachrichtentemplates (mit Vorname, Nachname, Datum, Uhrzeit)
* 📄 Speicherung von Nachrichtenstatus und Eventdaten in Room
* ⏳ Wöchentlicher Hintergrunddienst (WorkManager), der benachrichtigt, wenn Kontakte zu terminieren sind
* ☁️ Lokalisierung in Deutsch und Englisch
* 🌈 Anpassbares App-Design (Hintergründe auswählbar)
* 📂 Hilfe-/Anleitungsscreen mit Screenshots und Schritt-für-Schritt Beschreibung

---

## 🚀 Erste Schritte

### Kontakte:

* Kontakte müssen im Telefonbuch gespeichert sein
* Titel wie "Dr.", "Mag.", etc. können für die Anrede gespeichert werden
* Telefonnummer ist Pflicht

### Kalender:

* Kalenderereignis muss Vorname + Nachname im Titel enthalten
* Muss in der Zukunft liegen
* Kein ganztägiges Ereignis (also mit Uhrzeit!)

Die App erkennt automatisch alle passenden Kontakte und verknüpft sie mit Kalenderereignissen.

---

## 📆 Ablauf zur Erinnerung

1. Kontakt + Event werden erkannt
2. SMS-Vorlage wird generiert oder vom Nutzer angepasst
3. Kontakte können in SMS-Warteschlange aufgenommen werden
4. Nach Zustimmung wird über Standard-SMS-App gesendet
5. Event wird als "isNotified" in DB markiert

---

## 🚜 Navigation

* 📠 Startbildschirm: Hauptfunktionen (Nachricht senden, Templates überarbeiten, Kontakte anzeigen)
* 📄 Gespeicherte Kontakte: Anzeige und Löschung
* 🔍 Kontaktsuche: Telefonbuchintegration und Datenbankspeicherung
* 📆 Nachricht-Vorlagen: Vor dem Versand editierbar
* ❓ Hilfe-Screen: Übersicht über alle App-Funktionen inkl. Screenshots

---

## 📂 Projektstruktur

```
/app
  ├─┤ ui/
  │    ├─┤ home/, screens/, components/
  ├─┤ data/
  │    ├─┤ dao/, entities/, repositories/
  ├─┤ services/
  │    └─┤ SmsSendingService.kt
  ├─┤ workers/
  │    └─┤ WeeklyReminderWorker.kt
  └─┤ MainActivity.kt, App.kt
```

---

## ⚙️ Tech Stack

* **Kotlin**, **Jetpack Compose**, **Material 3**
* **Room** Datenbank
* **WorkManager** (periodisch)
* **SmsManager**, API-Level-spezifisch (mit Abfragen für API >= 31)
* **DataStore** für Benutzereinstellungen (z. B. Hintergrundbild)

---

## 📅 Permissions

* `SEND_SMS`
* `READ_CONTACTS`, `WRITE_CONTACTS`
* `READ_CALENDAR`, `WRITE_CALENDAR`
* `POST_NOTIFICATIONS` (für API 33+)

---

## 🏠 Release-Info

* Zielplattform: Android 26 - 34
* `minSdk`: 26
* `compileSdk`: 34
* Appgröße: \~20 MB (optimiert mit .webp Assets)

---

## ❓ Lizenz / Autor







