package com.example.meetingnotification.ui.contact

import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meetingnotification.ui.AppViewModelProvider
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.navigation.NavigationDestination

object BeforeTemplateDestination : NavigationDestination {
    override val route =
        "beforeTemplate"                         // Definiert die Route als "beforeTemplate"
}

private const val TAG = "ContactCheckBeforeSubmitScreen"

@Composable
fun ContactCheckScreen(
    navigateToHomeScreen: () -> Unit,
    calenderEvents: List<EventDateTitle>,
    sendContactsToSmsService: (List<ContactReadyForSms>) -> Unit,
    viewModel: ContactCheckBeforeSubmitViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState = viewModel.contactUiState.collectAsState()
    val contactsZipedWithDate by viewModel.calenderStateConnectedToContacts
    var templateIdDepencysMailIcon by remember { mutableStateOf(listOf<MutablePairs2>()) }
    var templateIdDepencysRadioButton by remember { mutableStateOf(listOf<MutablePairs2>()) }

    // Deine originalen LaunchedEffects
    LaunchedEffect(uiState.value) {
        templateIdDepencysMailIcon = uiState.value.contactUiState.map { MutablePairs2(it.id, false) }
    }


    LaunchedEffect(Unit) {
        viewModel.loadCalenderData(calenderEvents)
        Log.d(TAG,"Calender loaded in launchedEffect()")
    }


    LaunchedEffect(uiState.value.contactUiState.size) {
        if (uiState.value.contactUiState.isNotEmpty()) {
            viewModel.zipDatesToContacts(uiState.value.contactUiState)
            Log.d(TAG,"Dates to Contacts Zipped in LaunchedEffect()")
            viewModel.loadContactsWithEvents()
            Log.d(TAG,"Contacts load with event called in LaunchedEffect()")
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        // Hintergrund (original)
        Image(
            painter = painterResource(R.drawable.background_light2),
            contentDescription = "Hintergrundbild",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay für Lesbarkeit
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Kontaktliste",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    "${uiState.value.contactUiState.size} Kontakte",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kontaktliste (LazyColumn mit originaler Logik)
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.value.contactUiState, key = {it.id}) { contact ->
                    var isContactInCalender by remember { mutableStateOf(false) }
                    val isContactsNextEventNotified = viewModel.isContactNotifiedForUpcomingEvent(contact.id)
                    val isContactSelectedInRadioButton = templateIdDepencysRadioButton.firstOrNull { it.first == contact.id }?.second ?: false

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        )
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
                                    selected = (isContactsNextEventNotified || isContactSelectedInRadioButton),
                                    colors = RadioButtonColors(
                                        selectedColor = Color.Green,
                                        unselectedColor = Color.White,
                                        disabledSelectedColor = Color.Black,
                                        disabledUnselectedColor = Color.Black
                                    ),
                                    enabled = (!isContactsNextEventNotified && isContactInCalender),
                                    onClick = {
                                        val updatedList = templateIdDepencysRadioButton.toMutableList()
                                        val index =
                                            updatedList.indexOf(updatedList.firstOrNull { it.first == contact.id } ?: -1)
                                        if (index != -1) {
                                            updatedList[index] = MutablePairs2(contact.id, !updatedList[index].second)
                                        } else {
                                            updatedList.add(MutablePairs2(contact.id, true))
                                        }
                                        templateIdDepencysRadioButton = updatedList
                                    })
                            }

                            // Termininfo (originale Logik)
                            Text(
                                text = contactsZipedWithDate.firstOrNull { it.contactId == contact.id }
                                    ?.let {
                                        isContactInCalender = true
                                        viewModel.getDayDuration(it.date)
                                    } ?: stringResource(R.string.deufault_message_status).also {
                                    isContactInCalender = false
                                },
                                color = if (isContactInCalender) Color.Green else Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            // Template Editor (original)
                            if (templateIdDepencysMailIcon.any { it.first == contact.id && it.second }) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TemplateOverwatch(
                                    contact.message,
                                    sendMessageToUpdateContact = { newMessage ->
                                        viewModel.updateContact(
                                            contact.copy(message = newMessage)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

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
                    Text("Abbrechen")
                }

                Button(
                    onClick = {
                        val selectedContactsReadyForSMS = mutableListOf<ContactReadyForSms>()
                        templateIdDepencysRadioButton.isNotEmpty().let {
                            templateIdDepencysRadioButton.forEach { contactSelected ->
                                val contactsReadyForSms = uiState.value.contactUiState.firstOrNull { it.id == contactSelected.first }
                                    ?.let {
                                        ContactReadyForSms(
                                            it.id,
                                            it.phone,
                                            it.message,
                                            it.firstName
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
                        containerColor = Color(0xFF4CAF50), // Grün
                        contentColor = Color.White
                    ),
                    enabled = templateIdDepencysRadioButton.any { it.second }
                ) {
                    Text("Senden (${templateIdDepencysRadioButton.count { it.second }})")
                }
            }
        }
    }
}

// TemplateOverwatch unverändert (wie original)
@Composable
fun TemplateOverwatch(receiveMessage: String, sendMessageToUpdateContact: (String) -> Unit) {
    var defaultText by remember { mutableStateOf(receiveMessage) }
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

