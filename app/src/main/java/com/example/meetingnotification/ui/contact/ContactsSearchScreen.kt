package com.example.meetingnotification.ui.contact


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.data.entities.Contact
import com.example.meetingnotification.ui.navigation.NavigationDestination
import kotlinx.coroutines.delay


object SearchContactDestination : NavigationDestination {        // Objekt für die Such-Route
    override val route = "searchScreen"                          // Legt die Route auf "searchScreen" fest
    override val titleRes: Int = R.string.search_contacts
}


@Composable
fun SearchListScreen(
    modifier: Modifier = Modifier,
    viewModel: ContactsSearchScreenViewModel,                 // ViewModel zur Bereitstellung von Daten
    onCancelCLicked: () -> Unit,                              // Callback für "Cancel"-Button
    navigateToSavedContacts: () -> Unit                      // Callback zum Navigieren zur gespeicherten Kontaktliste
) {
    val uiState = viewModel.contactsUiState.collectAsState()  // Beobachtet den aktuellen Zustand der gespeicherten Kontakte
    val contactBuffer = viewModel.getContacts().observeAsState(emptyList()) // Holt alle Kontakte aus dem LiveData
    var text by remember { mutableStateOf("") }               // Suchtext-State für das Eingabefeld
    val debouncedText = rememberDebounceText(text)
    val defaultBackgroundPicture = viewModel.selectedBackgroundPictureId.collectAsState()

    val contactBufferSorted by remember(debouncedText,contactBuffer.value) {
        derivedStateOf {
            if (debouncedText.isBlank()) contactBuffer.value
            else contactBuffer.value.filter {
                it.firstName.contains(debouncedText, ignoreCase = true) // Filtert Kontakte nach Vornamen
            }
        }
    }

    var contactIdsRadioDepency by remember(contactBufferSorted) {
        mutableStateOf(
            contactBufferSorted.map { contact -> MutablePairs(contact.id, false) } // Initialisiert die RadioButton-Abhängigkeiten
        )
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(defaultBackgroundPicture.value),
            contentDescription = "Hintergrundbild",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Skaliert das Bild, um es zu füllen
        )
        

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))  // Halbtransparentes Overlay für bessere Lesbarkeit
                .padding(16.dp)                              // Standard-Padding für Abstand
        ) {
            // 🔍 Suchfeld mit Icon
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(55.dp),                                 // Setzt die Höhe des Textfelds auf 55 dp
                value = text,                                     // Bindet den aktuellen Suchtext-Wert
                onValueChange = { newText ->
                    text = newText
                },    // Aktualisiert den Suchtext-Wert
                placeholder = { Text(stringResource(R.string.search_field_place_holder)) },
                maxLines = 1,
                leadingIcon = { Icon(Icons.Default.Search,null) }
            )

            Spacer(modifier = Modifier.height(24.dp))        // Abstand zum nächsten Element

            // 📜 Kontaktliste mit Scrollmöglichkeit
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(contactBufferSorted) { contact ->  // Iteriert durch gefilterte Kontakte, nutzt stabile Key-Zuweisung
                    val isSelected = contactIdsRadioDepency.firstOrNull { it.first == contact.id }?.second ?: false
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                    ) {
                        ContactRow(
                            contact = contact,
                            alreadySaved = uiState.value.contactList.any {
                                it.firstName == contact.firstName && it.lastName == contact.lastName // Prüft, ob Kontakt bereits gespeichert ist
                            },
                            isSelected = isSelected,
                            onToggle = {
                                contactIdsRadioDepency = contactIdsRadioDepency.map {
                                    if (it.first == contact.id) it.copy(second = !it.second) else it
                                } // Schaltet RadioButton-Zustand um
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))        // Abstand zur Button-Zeile

            // ⬅️➡️ Action Buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onCancelCLicked                // Abbruch
                ) {
                    Text(stringResource(R.string.navigation_cancel), color = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))     // Abstand zwischen Buttons
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val selectedIds = contactIdsRadioDepency.filter { it.second }.map { it.first } // Holt IDs der ausgewählten Kontakte
                        if (selectedIds.isNotEmpty()) {
                            viewModel.addContactsToDatabase(contactBufferSorted, selectedIds) // Übergibt Auswahl an ViewModel
                            navigateToSavedContacts()                 // Navigiert zurück zur gespeicherten Liste
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(stringResource(R.string.contact_add_all_selected), color = Color.Black)
                }
            }
        }
    }
}


@Composable
fun ContactRow(
    contact: Contact,                   // Einzelkontakt
    alreadySaved: Boolean,             // Gibt an, ob der Kontakt bereits gespeichert ist
    isSelected: Boolean,               // Aktueller Radio-Button-Status
    onToggle: () -> Unit               // Callback zum Umschalten des Auswahlstatus
) {
    Log.d("RECOMPOSE","ContactRow for ${contact.firstName}")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),  // Vertikaler Abstand zwischen Listenelementen
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 👤 Kontaktinformationen
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${stringResource(R.string.gender)} -> ${if (contact.sex == 'W') stringResource(R.string.contact_gender_female) else stringResource(R.string.contact_gender_male) }"
            )
            Text(
                text = "${stringResource(R.string.title)} -> ${contact.title}"
            )
            Text(text = "${stringResource(R.string.contact_name)} -> ${contact.firstName} ${contact.lastName}")
            Text(
                text = "📞 ${contact.phone}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
        }

        // ✅ Auswahlstatus
        if (alreadySaved) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color(0xFF116715),
                modifier = Modifier.padding(12.dp)
            )
        } else {
            RadioButton(
                selected = isSelected,
                onClick = onToggle,  // Setzt Auswahlstatus um
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF116715),
                    unselectedColor = Color.Black
                )
            )
        }
    }
}


@Composable
fun rememberDebounceText(input: String, delayMillis: Long = 500L): String {
    var debouncedText by remember { mutableStateOf(input) }

    LaunchedEffect(input) {
        delay(delayMillis)
        debouncedText = input
    }

    return debouncedText
}


