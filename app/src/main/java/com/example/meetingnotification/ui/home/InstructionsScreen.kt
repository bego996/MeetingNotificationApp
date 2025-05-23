package com.example.meetingnotification.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.meetingnotification.ui.MettingTopAppBar
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.navigation.NavigationDestination

object InstructionsDestination : NavigationDestination {
    override val route: String = "help"
    override val titleRes: Int = R.string.instructions
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionsScreen(
    onBack: () -> Unit,
    modifier: Modifier,
) {
    Scaffold(
        topBar = {
            MettingTopAppBar(
                modifier = modifier,
                title = stringResource(InstructionsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = { onBack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            HelpSection("📲 Funktionweise der App", """
                Diese App verknüpft automatisch deine Kontakte mit passenden Kalendereinträgen und erstellt individuelle SMS-Erinnerungen. 
                Wähle Kontakte aus, überprüfe Termine im Kalender, passe die Nachricht an und sende sie mit einem Klick.
                Die App benachrichtigt Wöchentlich, wie viele Kontakte man für die nächsten 7 Tage benachrichtigen kann.
            """.trimIndent(),null)

            HelpSectionFirstSteps(
                "\uD83C\uDFC3\uD83C\uDFFC Erste Schritte",
                """
                    - Erstelle (oder nutze) einen Kontakt in deinem Telefonbuch.
                    o Wenn der Contact Mänlich ist, dann füge ein "m" oder "M" neben seinem Vornamen hinzu
                      anderfalls ein "w" oder "W" für Weiblich. Wenn "m" oder "w" weggelassen wird,
                      dann wird der Contact als Weiblich in der App erkannt.
                    o Wenn der Contact einen akademischen Titel hatt und man diesen in der Anrede haben will,
                      dann sollte man auch diesen beim hinzufügen des Kontakts angeben.
                    o Eine Telefonnummer muss auch angegeben werden.
                    o Nachdem der Kontakt hinzugefügt wurde und man in die App zurückkehrt, wird er dort erkannt.
                """.trimIndent(),
                R.drawable.addcontact,
                """
                   
                    - Erstelle ein Erreignis im Kalender passend zum vorhandenen Kontakt Vornamen und Nachnamen.
                    o Das Ereigniss muss ein Datum sowie eine Zeit haben und darf nicht Ganztätig sein, es muss auch in der Zukunft liegen.
                    o Nachdem das Ereigniss gepeichert wird und man in die App zurückkehrt, wird der Kontakt mit dem Erreigniss automatisch gekoppelt.
                """.trimIndent(),
                R.drawable.addevent
            )

            HelpSection("📇 Gespeicherte Kontakte Bildschirm", """
                - Zeigt alle bereits in der App Datenbank gespeicherten Kontakte.
                - Lösche den Kontakt aus der App Datenbank über das „X“ Symbol.
            """.trimIndent(),R.drawable.savedcontact)

            HelpSection("\uD83D\uDD0D Suche Kontakte Bildschirm", """
                - Hier werden alle Kontakte aus dem Telefonbuch augelistet.
                - Wenn man welche auswählt und auf "Ausgewählte hinzufügen" klickt,
                  dann werden sie in der App Datenbank gespeichert und mit Events gekoppelt in der App.
            """.trimIndent(),R.drawable.searchcontact)

            HelpSection("\uD83D\uDCDD Zeige Nachricht Vorlagen Bildschirm", """
                - Kontakte auswählen.
                - Klick auf „Senden“ fügt sie zur SMS Warteschlange hinzu.
            """.trimIndent(),R.drawable.templatecheck)
            HelpSectionChangeMessage("""
                - Jeder in der App Datenbank gespeicherte Kontakt hatt von anfang an eine Standart Nachricht
                - Die Nachricht ist auf seinen Titel (falls enthalten) sowie seinen Namen angepasst.
                - Die Nachricht kann hier belibig oft bearbeitete werden und wird dauerhaft in der App gespeichert.
                - Wichtig zu beachten beim ändern der Nachricht: Das Datum sowie die Uhrzeit in der Nachricht
                  darf nicht verändert werden, da die App diese automatisch anpasst, alles andere kann verändert werden.
            """.trimIndent(),R.drawable.templatechange
            )

            HelpSection(
                "\uD83C\uDFE0 Start Bildschirm",
                """
                - Hauptfunktion: „Nachrichten senden“:
                Wenn man draufklickt, dann sieht man nochmal alle in der SmS Warteschlange hinzugefügten Kontakte,
                nachdem akzeptieren versendet man SmS Errinerungen zum Termin im Hintergrund per Standard SMS App.
                - Menü rechts oben im Eck um das Design der App zu ändern oder um die Anleitung der app anzuzeigen.
                - Letztes Versand Datum sowie Uhrzeit von Errinerungen wird unten angezeigt.
            """.trimIndent(), R.drawable.homescreen
            )

            HelpSection("🔄 Aktualisierungen", """
                - Änderungen (z. B. im Kalender oder Telefonbuch) erfordern Zurücknavigieren zum Startbildschirm, wenn man zur App zurückkehrt.
                Wenn die App jedoch komplett geschlossen wurde (Tab geschlossen) und man änderungen vornimmt und wieder die App öffnet, wird
                alles automatisch aktualisiert.
            """.trimIndent(),null)
        }
    }
}

@Composable
fun HelpSection(title: String,description: String,picRes:Int?) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(4.dp))
        Text(description, style = MaterialTheme.typography.bodyMedium)
        ImageHelper(picRes)
    }
}

@Composable
fun HelpSectionChangeMessage(
    addMessageChangeDescription: String,addMessagePic:Int?
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(addMessageChangeDescription, style = MaterialTheme.typography.bodyMedium)
        ImageHelper(addMessagePic)
    }
}

@Composable
fun HelpSectionFirstSteps(
    title: String,
    addContactDescription: String,addContactPic:Int?,
    addEventDescription: String, addEventPic:Int?
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(4.dp))
        Text(addContactDescription, style = MaterialTheme.typography.bodyMedium)
        ImageHelper(addContactPic)
        Text(addEventDescription,style = MaterialTheme.typography.bodyMedium)
        ImageHelper(addEventPic)
    }
}

@Composable
fun ImageHelper(imageRes:Int?){
    imageRes?.let {
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            painter = painterResource(id = it),
            contentDescription = "title",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        )
    }
}
