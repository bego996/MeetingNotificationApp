package com.example.meetingnotification.ui.contact

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meetingnotification.ui.AppViewModelProvider
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.navigation.NavigationDestination


object SavedContactsDestination : NavigationDestination {     // Definiert eine statische Route für "SavedContacts"
    override val route = "saved"                              // Route-Name: "saved"
}

@Composable
fun SavedContacts(
    navigateToSearchContactScreen: () -> Unit,
    onCancelClicked: () -> Unit,
    modifier: Modifier,
    viewModel: ContactsScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.contactsUiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background_light2),
            contentDescription = "Hintergrundbild",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // semi-transparent overlay for better readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        if (uiState.value.contactUiState.isEmpty()) {
            EmptyListScreen(
                modifier = modifier,
                navigateToSearchContactScreen = navigateToSearchContactScreen,
                onCancelClicked = onCancelClicked
            )
        } else {
            FilledListscreen(
                modifier = modifier,
                onCancelClicked = onCancelClicked,
                navigateToSearchContactScreen = navigateToSearchContactScreen,
                savedContacts = viewModel
            )
        }
    }
}


@Composable
fun EmptyListScreen(
    modifier: Modifier,
    navigateToSearchContactScreen: () -> Unit,
    onCancelClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Deine Kontaktliste ist leer.",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Tippe auf das + unten, um neue Kontakte hinzuzufügen.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onCancelClicked,
                border = BorderStroke(1.dp,Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Zurück")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                modifier = Modifier.weight(1f),
                onClick = navigateToSearchContactScreen,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50), // Grün
                    contentColor = Color.White
                )
            ) {
                Text("Kontakt hinzufügen")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = null)
            }
        }
    }
}

@Composable
fun FilledListscreen(
    modifier: Modifier = Modifier,
    onCancelClicked: () -> Unit,
    navigateToSearchContactScreen: () -> Unit,
    savedContacts: ContactsScreenViewModel
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gespeicherte Kontakte",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(savedContacts.contactsUiState.value.contactUiState) { contact ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Geschlecht -> ${if (contact.sex == 'W') "Weiblich" else "Männlich" }"
                            )
                            Text(
                                text = "Titel -> ${contact.title}"
                            )
                            Text(text = "Name -> ${contact.firstName} ${contact.lastName}")
                            Text(
                                text = "📞 ${contact.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }

                        IconButton(onClick = { savedContacts.deleteContact(contact) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Kontakt löschen",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onCancelClicked,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp,Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Zurück")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = navigateToSearchContactScreen,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50), // Grün
                    contentColor = Color.White
                )
            ) {
                Text("Kontakt hinzufügen")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = null)
            }
        }
    }
}









