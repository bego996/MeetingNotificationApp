package com.example.meetingnotification.ui.contact




import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.meetingnotification.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch


object SearchContactDestination : NavigationDestination {        // Objekt für die Such-Route
    override val route = "searchScreen"                          // Legt die Route auf "searchScreen" fest
}


@Composable
fun SearchListScreen(                                            // Haupt-Composable für den Suchbildschirm
    modifier: Modifier = Modifier,
    viewModel: ContactsSearchScreenViewModel,                    // ViewModel
    onCancelCLicked: () -> Unit,                                 // Callback für den "Cancel" Button.
    navigateToSavedContacts: () -> Unit                          // Callback für das Navigieren zu gespeicherten Kontakten
) {
    val coroutineScope = rememberCoroutineScope()                // Coroutine-Bereich für Nebenläufigkeit
    val uiState = viewModel.contactsUiState.collectAsState()     // Überwacht den aktuellen Zustand der Kontakte
    val contactBuffer = viewModel.getContacts().observeAsState(listOf()) // Beobachtet die Kontakte aus dem LiveData
    var text by remember { mutableStateOf("") }            // Suchtext-Status
    var contactBufferSorted by remember { mutableStateOf(contactBuffer.value) }         // Gefilterte Kontaktliste
    var contactIdsRadioDepency by remember { mutableStateOf(listOf<MutablePairs>()) }   // Liste für die Abhängigkeiten von Radio-Buttons

    LaunchedEffect(contactBuffer.value) {                        // Aktualisiert die Radio-Button-Abhängigkeiten, wenn sich die Kontaktliste ändert
        contactIdsRadioDepency = contactBuffer.value.map { contact -> MutablePairs(contact.id, false) }
    }

    LaunchedEffect(text) {                                        // Filtert die Kontakte basierend auf dem Suchtext
        contactBufferSorted = if (text == "") {
            contactBuffer.value                                   // Gibt alle Kontakte zurück, wenn kein Suchtext vorhanden ist
        } else {
            contactBuffer.value
                .filter { it.firstName.contains(text, ignoreCase = true) }      // Filtert nach Vornamen
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()                                        // Füllt den gesamten verfügbaren Platz
            .padding(10.dp)                                       // Fügt einen Innenabstand von 10 dp hinzu
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),                                  // Füllt die gesamte verfügbare Breite aus
            verticalAlignment = Alignment.CenterVertically        // Zentriert die Elemente vertikal
        ) {
            Icon(
                imageVector = Icons.Default.Search,               // Such-Icon zur Darstellung der Suche
                contentDescription = "Search Icon",
                modifier = Modifier.size(55.dp)                   // Setzt die Größe des Icons auf 55 dp
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(55.dp),                                 // Setzt die Höhe des Textfelds auf 55 dp
                value = text,                                     // Bindet den aktuellen Suchtext-Wert
                onValueChange = { newText -> text = newText },    // Aktualisiert den Suchtext-Wert
                placeholder = { Text("Enter Search Options") },   // Platzhaltertext, wenn das Feld leer ist
                maxLines = 1                                           // Beschränkt die Eingabe auf eine Zeile
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Name | Surname | Sex | Number | Title", modifier = Modifier.fillMaxWidth())    // Überschriften für die Liste
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(contactBufferSorted) { contact ->               // Iteriert durch die gefilterten Kontakte
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(                                                   // Textanzeige für die Kontaktdaten
                        text = "${contact.firstName} | ${contact.lastName} | ${contact.sex} | ${contact.phone} | ${contact.title} |",
                        modifier = Modifier.weight(1f)
                    )
                    if (!uiState.value.contactList.contains(contact)) {     // Zeigt einen RadioButton an, wenn der Kontakt nicht bereits hinzugefügt wurde
                        RadioButton(
                            selected = contactIdsRadioDepency.firstOrNull { pair -> pair.first == contact.id }?.second ?: false,    // Prüft den aktuellen Status des Radio-Buttons
                            onClick = {
                                val updateList = contactIdsRadioDepency.toMutableList()         // Erstellt eine veränderbare Liste der Abhängigkeiten
                                val index = updateList.indexOfFirst { it.first == contact.id }  // Sucht den Index des aktuellen Kontakts
                                if (index != -1) {
                                    updateList[index] = MutablePairs(contact.id, !updateList[index].second)     // Aktualisiert den Status
                                }
                                contactIdsRadioDepency = updateList
                            }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Check,      // Zeigt ein Check-Icon, wenn der Kontakt bereits hinzugefügt wurde
                            contentDescription = "Häkchen",
                            tint = Color.Black                     // Setzt die Farbe auf Schwarz
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                //verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onCancelCLicked,                     // Ruft den "Cancel"-Callback auf
                ) {
                    Text(text = "Cancel")                          // Button Beschriftung "Cancel"
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        contactBufferSorted.isNotEmpty().and(contactIdsRadioDepency.any { it.second }).let {    // Überprüft, ob Kontakte vorhanden und ausgewählt sind
                            val idToMap = contactIdsRadioDepency.filter { it.second }.map { it.first }          // Holt die IDs der ausgewählten Kontakte
                            coroutineScope.launch {
                                viewModel.addContactsToDatabase(contactBufferSorted, idToMap)   // Fügt die ausgewählten Kontakte zur Datenbank hinzu
                                navigateToSavedContacts()                                       // Navigiert zu gespeicherten Kontakten(composable).
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,     // Plus-Icon zur Darstellung "Hinzufügen"
                        contentDescription = "Add Icon",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
