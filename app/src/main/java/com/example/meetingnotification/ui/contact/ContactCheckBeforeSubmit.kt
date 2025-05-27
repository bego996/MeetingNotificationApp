package com.example.meetingnotification.ui.contact

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meetingnotification.ui.AppViewModelProvider
import com.example.meetingnotification.ui.MettingTopAppBar
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.navigation.NavigationDestination

object BeforeTemplateDestination : NavigationDestination {
    override val route = "beforeTemplate"                         // Definiert die Route als "beforeTemplate"
    override val titleRes: Int = R.string.contact_list
}

private const val TAG = "ContactCheckBeforeSubmitScreen"


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactCheckScreen(
    modifier: Modifier = Modifier,
    navigateToHomeScreen: () -> Unit,
    calenderEvents: List<EventDateTitle>,
    sendContactsToSmsService: (List<ContactReadyForSms>) -> Unit,
    contactsInSmsQueueById: List<Int>,
    removeContactFromSmsQueue: (Int) -> Unit,
    viewModel: ContactCheckBeforeSubmitViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateUp: () -> Unit
) {
    val dataStillLoading by viewModel.isLoading

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MettingTopAppBar(
                modifier = modifier,
                title = stringResource(BeforeTemplateDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        LoadingScreen(isLoading = dataStillLoading) {
            ContactCheckScreenContent(
                navigateToHomeScreen,
                calenderEvents,
                sendContactsToSmsService,
                contactsInSmsQueueById,
                removeContactFromSmsQueue,
                viewModel,
                innerPadding
            )
        }
    }
}


// TemplateOverwatch unverändert (wie original)
@Composable
fun TemplateOverwatch(receiveMessage: String, sendMessageToUpdateContact: (String) -> Unit) {
    var defaultText by rememberSaveable { mutableStateOf(receiveMessage) }
    Column {
        Row {
            TextField(
                value = defaultText,
                modifier = Modifier.weight(1f),
                onValueChange = { defaultText = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            IconButton(onClick = { sendMessageToUpdateContact(defaultText) }) {
                Icon(Icons.Filled.Check, "Bestätigen", tint = Color.Green)
            }
        }
    }
}


@Composable
fun ContactCheckScreenContent(
    navigateToHomeScreen: () -> Unit,
    calenderEvents: List<EventDateTitle>,
    sendContactsToSmsService: (List<ContactReadyForSms>) -> Unit,
    contactsInSmsQueueById: List<Int>,
    removeContactFromSmsQueue: (Int) -> Unit,
    viewModel: ContactCheckBeforeSubmitViewModel,
    paddingValueForTopBar: PaddingValues
) {
    val uiState = viewModel.contactUiState.collectAsState()
    val contactsZipedWithDate by viewModel.calenderStateConnectedToContacts
    var templateIdDepencysMailIcon by rememberSaveable { mutableStateOf(listOf<MutablePairs2>()) }
    var templateIdDepencysRadioButton by rememberSaveable { mutableStateOf(listOf<MutablePairs2>()) }
    var contactsFromSmsServiceQueueByIds by rememberSaveable { mutableStateOf(contactsInSmsQueueById) }
    val contactsWithEvents by viewModel.contactWithEvents      //Nur nötig um beim ersten rendern die lazycolumn unten zu aktualisieren.
    val defaultBackgroundPicture = viewModel.selectedBackgroundPictureId.collectAsState()
    var text by rememberSaveable { mutableStateOf("") }               // Suchtext-State für das Eingabefeld
    val debouncedText = rememberDebounceText(text)
    var showNotifyables by rememberSaveable { mutableStateOf(false) } //Toogles when user press button to show just notifyable Contact for the next 10 days.


    val contactBufferSorted by remember(debouncedText,uiState.value,showNotifyables) {
        derivedStateOf {
            if (debouncedText.isBlank() && !showNotifyables) {
                uiState.value.contactUiState
            } else if (debouncedText.isBlank() && showNotifyables) {
                uiState.value.contactUiState.filter { contact -> contactsZipedWithDate.any { it.contactId == contact.id } }
            } else if (debouncedText.isNotBlank() && showNotifyables) {
                uiState.value.contactUiState.filter { contact -> contactsZipedWithDate.any { it.contactId == contact.id } }.filter { it.firstName.contains(debouncedText, ignoreCase = true) }
            } else {
                uiState.value.contactUiState.filter { it.firstName.contains(debouncedText, ignoreCase = true)} // Filtert Kontakte nach Vornamen
            }
        }
    }

    // Deine originalen LaunchedEffects
    LaunchedEffect(uiState.value) {
            Log.d(TAG, "templateIds updated")
            if (templateIdDepencysMailIcon.isEmpty()) {
                templateIdDepencysMailIcon = uiState.value.contactUiState.map { MutablePairs2(it.id, false) }
            }
    }


    LaunchedEffect(Unit) {
        viewModel.loadCalenderData(calenderEvents)
        Log.d(TAG, "Calender loaded in launchedEffect()")
    }


    //Weil hier async funktion vorhanden muss ich warten auf contactsWithEvents oben um die lazyColumn unten neu zu rendern.
    LaunchedEffect(uiState.value.contactUiState.size) {
        if (uiState.value.contactUiState.isNotEmpty()) {
            viewModel.zipDatesToContacts(uiState.value.contactUiState)
            Log.d(TAG, "Dates to Contacts Zipped in LaunchedEffect()")
            viewModel.deleteEventsThatDontExistsInCalenderAnymoreFromDatabase()
            Log.d(TAG, "Delete Events that dont exist in Calender in LaunchedEffect()")
            viewModel.insertEventForContact(contactsZipedWithDate)
            Log.d(TAG, "Insert Events for Contacts called in LaunchedEffect()")
            viewModel.updateContactsMessageAfterZippingItWithDates(contactsZipedWithDate, uiState.value.contactUiState)
            Log.d(TAG, "Update contacts messages called in LaunchedEffect()")
        }
    }


    Box(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValueForTopBar)
    ) {
        // Hintergrund (original)
        Image(
            painter = painterResource(defaultBackgroundPicture.value),
            contentDescription = "Hintergrundbild",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay für Lesbarkeit
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(
            modifier = Modifier
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

            Spacer(modifier = Modifier.height(8.dp))


            Row (modifier = Modifier) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    onClick = {showNotifyables = !showNotifyables},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showNotifyables) Color(0xFF1BB625) else Color(0xFF046406), // Grün
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.show_actives))
                }
                Text(
                    "${uiState.value.contactUiState.size} ${stringResource(R.string.contacts)}",
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kontaktliste (LazyColumn mit originaler Logik)
            LazyColumn(
                modifier = Modifier
                    .weight(1f),
                content = {
                    items(contactBufferSorted, key = { it.id }) { contact ->

                        var isContactInCalender = contactsZipedWithDate.any { it.contactId == contact.id }
                        //Wird mit if geprüft um beim ersten rendern zu warten bis launchefekt oben mit async funktion fertig ist, dann wird lazy column neu gerendert.
                        val isContactsNextEventNotified = if (contactsWithEvents.isNotEmpty()) viewModel.isContactNotifiedForUpcomingEvent(contact.id) else false
                        val isContactSelectedInRadioButton = templateIdDepencysRadioButton.firstOrNull { it.first == contact.id }?.second ?: false
                        val isContactInMessageQueue = contactsFromSmsServiceQueueByIds.any { contactIdFromSmsQueue -> contactIdFromSmsQueue == contact.id }
                        templateIdDepencysMailIcon.firstOrNull { it.first == contact.id }?.second ?:false

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                // Kontaktinfo-Zeile
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            "${contact.firstName} ${contact.lastName}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "${contact.title} • ${contact.phone}",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 13.sp
                                        )
                                    }

                                    // Originale Icon/RadioButton-Logik
                                    IconButton(
                                        onClick = {
                                            val updatedList = templateIdDepencysMailIcon.toMutableList()
                                            val index = updatedList.indexOfFirst { it.first == contact.id }
                                            if (index != -1) {
                                                updatedList[index] = MutablePairs2(
                                                    contact.id,
                                                    !updatedList[index].second
                                                )
                                                templateIdDepencysMailIcon = updatedList
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Template bearbeiten",
                                            tint = if (templateIdDepencysMailIcon.any { it.first == contact.id && it.second })
                                                Color.Green
                                            else
                                                Color.White
                                        )
                                    }

                                    RadioButton(
                                        selected = (isContactsNextEventNotified || isContactSelectedInRadioButton || isContactInMessageQueue),
                                        colors = RadioButtonColors(
                                            selectedColor = Color.Green,
                                            unselectedColor = Color.White,
                                            disabledSelectedColor = Color.Black,
                                            disabledUnselectedColor = Color.Black
                                        ),
                                        enabled = (!isContactsNextEventNotified && isContactInCalender),
                                        onClick = {
                                            val updatedList = templateIdDepencysRadioButton.toMutableList()
                                            val index = updatedList.indexOf(updatedList.firstOrNull { it.first == contact.id } ?: -1)

                                            if (isContactInMessageQueue) {
                                                val smsQueueToEdit = contactsFromSmsServiceQueueByIds.toMutableList()
                                                smsQueueToEdit.remove(contact.id)
                                                contactsFromSmsServiceQueueByIds = smsQueueToEdit
                                                removeContactFromSmsQueue(contact.id)
                                            } else {
                                                if (index != -1) {
                                                    updatedList[index] = MutablePairs2(
                                                        contact.id,
                                                        !updatedList[index].second
                                                    )
                                                } else {
                                                    updatedList.add(MutablePairs2(contact.id, true))
                                                }
                                            }
                                            templateIdDepencysRadioButton = updatedList
                                        })
                                }

                                // Termininfo (originale Logik)
                                Text(
                                    text = contactsZipedWithDate.firstOrNull { it.contactId == contact.id }
                                        ?.let {
                                            isContactInCalender = true
                                            "${viewModel.getDayDuration(it.date)} ${stringResource(R.string.days_left)}"
                                        } ?: stringResource(R.string.deufault_message_status).also {
                                        isContactInCalender = false
                                    },
                                    color = if (isContactInCalender) Color.Green else Color.White.copy(
                                        alpha = 0.6f
                                    ),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                // Template Editor (original)
                                if (templateIdDepencysMailIcon.any { it.first == contact.id && it.second }) {
                                    Log.i(TAG,"templateWithicon bollean found")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TemplateOverwatch(
                                        contact.message,
                                        sendMessageToUpdateContact = { newMessage ->
                                            viewModel.updateContact(contact.copy(message = newMessage))
                                            val updatedList = templateIdDepencysMailIcon.toMutableList()
                                            val index = updatedList.indexOfFirst { it.first == contact.id }
                                            if (index != -1) {
                                                updatedList[index] = MutablePairs2(
                                                    contact.id,
                                                    !updatedList[index].second
                                                )
                                                templateIdDepencysMailIcon = updatedList
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
            // Footer-Buttons (originale Logik)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = navigateToHomeScreen,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.navigation_cancel))
                }

                Button(
                    onClick = {
                        val selectedContactsReadyForSMS = mutableListOf<ContactReadyForSms>()
                        templateIdDepencysRadioButton.isNotEmpty().let {
                            templateIdDepencysRadioButton.forEach { contactSelected ->
                                val contactsReadyForSms =
                                    uiState.value.contactUiState.firstOrNull { it.id == contactSelected.first }
                                        ?.let {
                                            ContactReadyForSms(
                                                it.id,
                                                it.phone,
                                                it.message,
                                                "${it.firstName} ${it.lastName}"
                                            )
                                        }
                                contactsReadyForSms?.let {
                                    selectedContactsReadyForSMS.add(contactsReadyForSms)
                                }
                            }
                        }
                        viewModel.updateListReadyForSms(selectedContactsReadyForSMS) // Aktualisiert die Liste der Kontakte für SMS
                        sendContactsToSmsService(viewModel.getContactsReadyForSms()) // Sendet die Kontakte an den SMS-Service
                        navigateToHomeScreen()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1BB625), // Grün
                        contentColor = Color.White,
                        disabledContentColor = Color.White,
                        disabledContainerColor = Color(0xFF046406)
                    ),
                    enabled = templateIdDepencysRadioButton.any { it.second } || contactsFromSmsServiceQueueByIds.isNotEmpty()
                ) {
                    Text("${stringResource(R.string.send)} (${templateIdDepencysRadioButton.count { it.second } + contactsFromSmsServiceQueueByIds.size})")
                }
            }
        }
    }
}


@Composable
fun LoadingScreen(
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .pointerInput(Unit) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("${stringResource(R.string.loading)}...", color = Color.White)
                }
            }
        }
    }
}
