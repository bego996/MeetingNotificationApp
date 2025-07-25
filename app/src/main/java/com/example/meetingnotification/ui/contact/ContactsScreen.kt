package com.example.meetingnotification.ui.contact

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meetingnotification.ui.AppViewModelProvider
import com.example.meetingnotification.ui.MettingTopAppBar
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.data.entities.Contact
import com.example.meetingnotification.ui.navigation.NavigationDestination
import kotlinx.coroutines.delay
import java.util.Locale


object SavedContactsDestination : NavigationDestination {     // Definiert eine statische Route für "SavedContacts"
    override val route = "saved"                              // Route-Name: "saved"
    override val titleRes: Int = R.string.saved_contacts

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedContacts(
    navigateToSearchContactScreen: () -> Unit,
    onCancelClicked: () -> Unit,
    modifier: Modifier,
    viewModel: ContactsScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
    deleteContactFromSmsQueueIfExisting: (contactId: Int) -> Unit,
    onNavigateUp: () -> Unit
    ) {

    val uiState = viewModel.contactsUiState.collectAsState()
    val defaultBackgroundPicture = viewModel.selectedBackgroundPictureId.collectAsState()


    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MettingTopAppBar(
                modifier = modifier,
                title = stringResource(SavedContactsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(defaultBackgroundPicture.value),
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
                    viemodel = viewModel,
                    deleteContactFromSmsQueueIfExists = { deleteContactFromSmsQueueIfExisting(it) },
                    uiState = uiState.value.contactUiState
                )
            }
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
            text = stringResource(R.string.your_contact_list_is_empty),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.type_on_plus_to_add_some_contacts),
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
                Text(stringResource(R.string.navigation_cancel))
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
                Text(stringResource(R.string.add_contacts))
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
    viemodel: ContactsScreenViewModel,
    deleteContactFromSmsQueueIfExists: (contactId: Int) -> Unit,
    uiState: List<Contact>
) {

    var text by rememberSaveable { mutableStateOf("") }               // Suchtext-State für das Eingabefeld
    val debouncedText = rememberDebounceText(text)
    val localeLanguage = Locale.getDefault().language


    val contactBufferSorted by remember(debouncedText,uiState) {
        derivedStateOf {
            if (debouncedText.isBlank()) uiState
            else uiState.filter {
                it.firstName.contains(debouncedText, ignoreCase = true) // Filtert Kontakte nach Vornamen
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        // 🔍 Suchfeld mit Icon
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .size(55.dp),                                // Setzt die Höhe des Textfelds auf 55 dp
            value = text,                                     // Bindet den aktuellen Suchtext-Wert
            onValueChange = { newText ->
                text = newText
            },    // Aktualisiert den Suchtext-Wert
            placeholder = { Text(stringResource(R.string.search_field_place_holder)) },
            maxLines = 1,
            leadingIcon = { Icon(Icons.Default.Search, null) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(contactBufferSorted, key = {it.id}) { contact ->
                var visible by remember { mutableStateOf(true) }
                var pendingDelete by remember { mutableStateOf(false) }

                if (pendingDelete){
                    LaunchedEffect(true) {
                        delay(300)
                        viemodel.deleteContact(contact)
                        deleteContactFromSmsQueueIfExists(contact.id)
                    }
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut(tween(durationMillis = 300)) + slideOutVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (localeLanguage == "de"){
                                    Text(text = "${stringResource(R.string.gender)} -> ${if (contact.sex == 'W') stringResource(R.string.contact_gender_female) else stringResource(R.string.contact_gender_male) }")
                                }
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

                            IconButton(
                                onClick = {
                                    visible = false
                                    // Warte kurz, bis die Animation durch ist, dann lösche
                                    pendingDelete = true
                                }
                            ) {
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
                Text(stringResource(R.string.navigation_cancel))
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
                Text(stringResource(R.string.add_contacts))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = null)
            }
        }
    }
}









