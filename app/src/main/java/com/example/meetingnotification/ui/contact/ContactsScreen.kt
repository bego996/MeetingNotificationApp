package com.example.meetingnotification.ui.contact

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meetingnotification.ui.AppViewModelProvider
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.navigation.NavigationDestination


object SavedContactsDestination : NavigationDestination {     // Definiert eine statische Route für "SavedContacts"
    override val route = "saved"                              // Route-Name: "saved"
}

@Composable
fun SavedContacts(                                            // Haupt-Composable für die Anzeige gespeicherter Kontakte
    navigateToSearchContactScreen: () -> Unit,                // Callback zum Navigieren zum Suchfenster Composable per Navhostcontroller.
    onCancelClicked: () -> Unit,                              // Callback für "Cancel" Button.
    modifier: Modifier,
    viewModel: ContactsScreenViewModel = viewModel(factory = AppViewModelProvider.Factory) // Initialisiert das ViewModel mit einer Factory
) {
    val uiState = viewModel.contactsUiState.collectAsState()  // Holt den aktuellen Zustand der Kontakte aus der Datenbank Room.

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background_picture1),
            contentDescription = "Hintergrundbild",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Skaliert das Bild, um den verfügbaren Raum zu füllen
        )
        if (uiState.value.contactUiState.isEmpty()) {             // Wenn keine Kontakte vorhanden sind
            EmptyListScreen(                                      // Zeige die leere Liste an (composable).
                modifier = modifier,
                navigateToSearchContactScreen = navigateToSearchContactScreen,
                onCancelClicked = onCancelClicked
            )
        } else {
            FilledListscreen(                                     // Zeige die Liste mit gespeicherten Kontakten an(composable).
                modifier = modifier,
                onCancelClicked = onCancelClicked,
                navigateToSearchContactScreen = navigateToSearchContactScreen,
                savedContacts = viewModel
            )
        }
    }
}

@Composable
fun EmptyListScreen(                                          // Composable für den leeren Listenbildschirm
    modifier: Modifier,
    navigateToSearchContactScreen: () -> Unit,                // Callback zum Navigieren zum Suchfenster Composable per Navhostcontroller.
    onCancelClicked: () -> Unit                               // Callback für "Cancel"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()                                    // Füllt den gesamten verfügbaren Platz
            .padding(16.dp),                                  // Fügt einen Innenabstand von 16 dp hinzu
        horizontalAlignment = Alignment.CenterHorizontally,   // Zentriert horizontal
        verticalArrangement = Arrangement.SpaceBetween        // Platziert Kinder mit gleichmäßigem Abstand
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),                              // Füllt die gesamte Breite
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "List is empty\npress + to add\nnew contacts")      // Hinweistext für den Benutzer
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                //verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onCancelClicked,                // Ruft den "Cancel"-Callback auf
                ) {
                    Text(text = "Cancel")                     // Button Beschriftung "Cancel"
                }
                Spacer(modifier = Modifier.weight(1f))        // Platzhalter für Ausgleich
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = navigateToSearchContactScreen   // Navigieren zum Suchfenster Composable per Navhostcontroller.
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle, // Plus-Icon zur Darstellung "Hinzufügen"
                        contentDescription = "Add Icon",       // Beschreibender Text für das Icon
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun FilledListscreen(                                         // Composable für die Liste mit gespeicherten Kontakten
    modifier: Modifier = Modifier,
    onCancelClicked: () -> Unit,                              // Callback für "Cancel"
    navigateToSearchContactScreen: () -> Unit,                // Callback zum Navigieren zum Suchfenster Composable per Navhostcontroller.
    savedContacts: ContactsScreenViewModel                    // ViewModel, das die Kontaktliste bereitstellt
) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)                                   // Fügt einen Innenabstand von 10 dp hinzu
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(text = "${stringResource(R.string.contact_sex)} | ",color = Color.White, fontWeight = FontWeight.Bold)    // Überschriften für die Kontaktliste
            Text(text = "${stringResource(R.string.contact_title)} | ",color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = "${stringResource(R.string.contact_firstname)} | ",color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = "${stringResource(R.string.contact_surname)} | ",color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = "${stringResource(R.string.contact_phonenumber)} |",color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))             // Fügt eine vertikale Lücke von 16 dp hinzu
        LazyColumn(                                           // LazyColumn um einträge nach unten scrollen zu können.
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            content = {
                items(savedContacts.contactsUiState.value.contactUiState) { contact ->      // Durchläuft die gespeicherten Kontakte
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Text(text = "${contact.sex} | ", color = Color.Green)    // Zeigt den Kontakt an
                        Text(text = "${contact.title} | ",color = Color.Green)
                        Text(text = "${contact.firstName} | ",color = Color.Green)
                        Text(text = "${contact.lastName} | ",color = Color.Green)
                        Text(text = "${contact.phone} |",color = Color.Green)
                        IconButton(
                            onClick = { savedContacts.deleteContact(contact) }) //löschen von kontakt mit viewmodel coroutine scope im viewmodel.
                        {
                            Icon(
                                imageVector = Icons.Default.Clear,      // Löschen-Icon
                                contentDescription = "delete icon", // Beschreibender Text für das Löschen-Icon
                                tint = Color.Red
                            )
                        }
                    }
                }
            })
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                //verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onCancelClicked,                // Ruft den "Cancel"-Callback auf
                ) {
                    Text(
                        text = "Cancel",
                        style = TextStyle(fontWeight = FontWeight.Bold, color = Color.Black)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = navigateToSearchContactScreen   // Ruft Callback zum Navigieren zum Suchfenster Composable per Navhostcontroller.
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle, // Plus-Icon zur Darstellung "Hinzufügen"
                        contentDescription = "Add Icon",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Black
                    )
                }
            }
        }
    }
}









