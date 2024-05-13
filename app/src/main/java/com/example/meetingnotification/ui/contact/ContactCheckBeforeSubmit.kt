package com.example.meetingnotification.ui.contact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meetingnotification.ui.AppViewModelProvider
import com.example.meetingnotification.ui.data.Contact
import com.example.meetingnotification.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object BeforeTemplateDestination : NavigationDestination {
    override val route =
        "beforeTemplate"                         // Definiert die Route als "beforeTemplate"
}

@Composable
fun ContactCheckScreen(
    onCancelClicked: () -> Unit,                                   // Callback für die "Cancel"-Aktion
    calenderEvents: List<EventDateTitle>,                          // Liste von Kalenderereignissen
    sendContactsToSmsService: (List<ContactReadyForSms>) -> Unit,  // Callback zum Senden von Kontakten an den SMS-Dienst
    viewModel: ContactCheckBeforeSubmitViewModel = viewModel(factory = AppViewModelProvider.Factory), // ViewModel, wird mit einem Factory-Objekt erstellt
    modifier: Modifier
) {
    val coroutineScope =
        rememberCoroutineScope()                  // Erstellt eine Coroutine-Umgebung für Nebenläufigkeit
    val uiState =
        viewModel.contactUiState.collectAsState()        // Sammelt den Zustand der Kontakte im UI-State
    val contactsZipedWithDate by viewModel.calenderStateConnectedToContacts // Bekommt Kontakte, die mit Kalenderereignissen verbunden sind
    var templateIdDepencysMailIcon by remember { mutableStateOf(listOf<MutablePairs2>()) } // Initialisiert die Liste der Template-Abhängigkeiten
    var templateIdDepencysRadioButton by remember { mutableStateOf(listOf<MutablePairs2>()) } // Initialisiert die Liste der Template-Abhängigkeiten

    LaunchedEffect(uiState.value) {
        templateIdDepencysMailIcon = uiState.value.contactUiState.map {
            MutablePairs2(
                it.id,
                false
            )
        } // Aktualisiert die Abhängigkeiten mit den IDs der Kontakte
    }
    LaunchedEffect(Unit) {
        viewModel.loadCalenderData(calenderEvents)                // Lädt Kalenderdaten in das ViewModel
    }
    LaunchedEffect(uiState.value.contactUiState.size) {
        if (uiState.value.contactUiState.isNotEmpty()) {
            viewModel.zipDatesToContacts(uiState.value.contactUiState) // Verknüpft die geladenen Kontakte mit Kalenderdaten
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()                                         // Füllt den gesamten verfügbaren Platz
            .padding(10.dp)                                        // Fügt einen Innenabstand von 10 dp hinzu
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start             // Startet horizontal am Anfang
        ) {
            Text("Firstname | Surname | Title | Phone")           // Zeigt eine Überschrift für die Kontaktliste an
        }
        Spacer(modifier = Modifier.height(16.dp))                 // Fügt eine vertikale Lücke von 16 dp hinzu
        LazyColumn(
            modifier = Modifier
                .weight(1f),                                       // Teilt den verfügbaren Platz gleichmäßig
            content = {
                items(uiState.value.contactUiState) { contact -> // Durchläuft die Kontakte im UI-State
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically // Zentriert die Elemente vertikal
                    ) {
                        Text(
                            text = "${contact.firstName} | ${contact.lastName} | ${contact.title} | ${contact.phone}", // Zeigt den Kontakt an
                            modifier = Modifier.weight(5f)                       // Nimmt 5 Teile des verfügbaren Platzes
                        )
                        IconButton(
                            modifier = Modifier.weight(1f),                       // Nimmt 1 Teil des Platzes ein
                            onClick = {
                                val updatedList =
                                    templateIdDepencysMailIcon.toMutableList()    // Erstellt eine mutable Liste
                                val index =
                                    updatedList.indexOf(updatedList.firstOrNull { it.first == contact.id }
                                        ?: -1)  // Sucht den Index des aktuellen Kontakts
                                if (index != -1) {
                                    updatedList[index] = MutablePairs2(
                                        contact.id,
                                        !updatedList[index].second
                                    ) // Ändert den Zustand der Abhängigkeit
                                }
                                templateIdDepencysMailIcon = updatedList
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email, // E-Mail-Icon für das Bearbeiten von Templates
                                contentDescription = null
                            )
                        }
                        var isContactInCalender by remember { mutableStateOf(false) }
                        RadioButton(
                            selected = templateIdDepencysRadioButton.firstOrNull { it.first == contact.id }?.second
                                ?: false,                                      // Ausgewählt oder nicht.
                            modifier = Modifier.weight(1f),
                            enabled = isContactInCalender,
                            onClick = {
                                val updatedList = templateIdDepencysRadioButton.toMutableList()
                                val index =
                                    updatedList.indexOf(updatedList.firstOrNull { it.first == contact.id }
                                        ?: -1)
                                if (index != -1) {
                                    updatedList[index] =
                                        MutablePairs2(contact.id, !updatedList[index].second)
                                } else {
                                    updatedList.add(MutablePairs2(contact.id, true))
                                }
                                templateIdDepencysRadioButton = updatedList
                            })
                        Text(
                            text = contactsZipedWithDate.firstOrNull { it.contactId == contact.id } // Zeigt das Datum des Kontakts an
                                ?.let {
                                    isContactInCalender = true
                                    viewModel.getDayDuration(it.date) // Gibt die Dauer als Zeichenkette zurück
                                } ?: "Undefined".let {
                                isContactInCalender = false
                                it
                            },                                  // Pfalls first or null nichts findet und null zurückgibt, wird dieser default String gspeichert.
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (templateIdDepencysMailIcon.firstOrNull { it.first == contact.id }?.second == true) {
                        TemplateOverwatch(
                            contact.message,                                       // Übergibt die Nachricht des Kontakts
                            sendMessageToUpdateContact = { s ->                    // Funktion zum Aktualisieren der Nachricht
                                coroutineScope.launch {
                                    val updatedContact = Contact(
                                        contact.id,                                // ID des Kontakts
                                        contact.title,                             // Titel des Kontakts
                                        contact.firstName,                         // Vorname des Kontakts
                                        contact.lastName,                          // Nachname des Kontakts
                                        contact.sex,                               // Geschlecht des Kontakts
                                        contact.phone,                             // Telefonnummer des Kontakts
                                        s                                          // Neue Nachricht
                                    )
                                    viewModel.updateContact(updatedContact)        // Aktualisiert den Kontakt im ViewModel
                                }
                            }
                        )
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onCancelClicked,                                     // Ruft den onCancelClicked-Callback auf
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Cancel")                                          // Beschriftung "Cancel"
                }
                Button(
                    onClick = { /*TODO*/ },                                        // TODO: Implementiere die Ablehnungsfunktion
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "DeclineAll")
                }
                Button(
                    onClick = {
                        val selectedContactsReadyForSMS = mutableListOf<ContactReadyForSms>()
                        templateIdDepencysRadioButton.isNotEmpty().let {
                            templateIdDepencysRadioButton.forEach { contactSelected ->
                                val contactsReadyForSms =
                                    uiState.value.contactUiState.firstOrNull { it.id == contactSelected.first }
                                        ?.let {
                                            ContactReadyForSms(it.phone, it.message, it.firstName)
                                        }

                                contactsReadyForSms?.let {
                                    selectedContactsReadyForSMS.add(contactsReadyForSms)
                                }
                            }
                        }
                        viewModel.updateListReadyForSms(selectedContactsReadyForSMS) // Aktualisiert die Liste der Kontakte für SMS
                        sendContactsToSmsService(viewModel.getContactsReadyForSms()) // Sendet die Kontakte an den SMS-Service
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "SubmitAll")
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateOverwatch(
    receiveMessage: String,                                                        // Die ursprüngliche Nachricht
    sendMessageToUpdateContact: (String) -> Unit                                   // Callback zum Aktualisieren der Nachricht des Kontakts in der Datenbank.
) {
    var defaultText by remember { mutableStateOf(receiveMessage) }                 // Initialisiert den Text mit der übergebenen Nachricht

    Column {
        Row {
            TextField(
                value = defaultText,                                               // Setzt den Textfeld-Wert auf die ursprüngliche Nachricht
                modifier = Modifier.weight(1f),
                onValueChange = { newText ->
                    defaultText = newText
                }               // Aktualisiert den Text mit jeder Änderung
            )
            IconButton(
                onClick = { sendMessageToUpdateContact(defaultText) }              // Ruft die Aktualisierungsfunktion(callback) auf.
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,                              // Check-Icon zur Bestätigung der Aktualisierung
                    contentDescription = null,
                    tint = Color.Red                                               // Setzt die Farbe des Icons auf Rot
                )
            }
        }
    }
}
