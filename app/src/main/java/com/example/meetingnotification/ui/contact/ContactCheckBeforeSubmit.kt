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

object BeforeTemplateDestination : NavigationDestination{
    override val route = "beforeTemplate"
}

@Composable
fun ContactCheckScreen(
    modifier: Modifier,
    onCancelClicked : () -> Unit,
    viewModel: ContactCheckBeforeSubmitViewModel = viewModel(factory = AppViewModelProvider.Factory)
){
    val uiState = viewModel.contactUiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    var templateIdDepencys by remember { mutableStateOf(listOf<MutablePairs2>())}

    LaunchedEffect(uiState.value){
        templateIdDepencys = uiState.value.contactUiState.map { MutablePairs2(it.id,false)}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text("Firstname | Surname | Title | Phone")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier
                .weight(1f),
            content = {
                items(uiState.value.contactUiState) { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${contact.firstName} | ${contact.lastName} | ${contact.title} | ${contact.phone}",
                            modifier = Modifier.weight(5f)
                        )
                        IconButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val updatedList = templateIdDepencys.toMutableList()
                                val index = updatedList.indexOf(updatedList.firstOrNull{it.first == contact.id} ?: -1)
                                if (index != -1){
                                    updatedList[index] = MutablePairs2(contact.id,!updatedList[index].second)
                                }
                                templateIdDepencys = updatedList
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null
                            )
                        }
                        RadioButton(
                            selected = false,
                            modifier = Modifier.weight(1f),
                            onClick = { /*TODO*/ })
                        Text(
                            text = "2 Days Left",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (templateIdDepencys.firstOrNull{ it.first == contact.id}?.second == true) {
                        TemplateOverwatch(
                            contact.message,
                            sendMessageToUpdateContact = {s ->
                                coroutineScope.launch {
                                    val updatedContact = Contact(contact.id,contact.title,contact.firstName,contact.lastName,contact.sex,contact.phone,s)
                                    viewModel.updateContact(updatedContact)
                                }
                            }
                        )
                    }
                }
        })
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onCancelClicked,
                    modifier = Modifier.weight(1f)) {
                  Text(text = "Cancel")
                }
                Button(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "DeclineAll")
                }
                Button(
                    onClick = {},
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
    receiveMessage : String,
    sendMessageToUpdateContact: (String) -> Unit
){
    var defaultText by remember { mutableStateOf(receiveMessage)}

    Column {
        Row {
            TextField(
                value = defaultText ,
                modifier = Modifier.weight(1f),
                onValueChange = { newText ->
                    defaultText = newText
                }
            )
            IconButton(
                onClick = {
                    sendMessageToUpdateContact(defaultText)
                }
            ) {
               Icon(
                   imageVector = Icons.Filled.Check,
                   contentDescription = null,
                   tint = Color.Red
               )
            }
        }

    }
}