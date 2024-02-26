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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meetingnotification.ui.AppViewModelProvider
import com.example.meetingnotification.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch


object SavedContactsDestination : NavigationDestination {
    override val route = "saved"
}


@Composable
fun SavedContacts(
    navigateToSearchContactScreen: () -> Unit,
    onCancelClicked: () -> Unit,
    modifier: Modifier,
    viewModel: ContactsScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.contactsUiState.collectAsState()

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
            viewModel
        )
    }
}

@Composable
fun EmptyListScreen(
    modifier: Modifier,
    navigateToSearchContactScreen: () -> Unit,
    onCancelClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "List is empty\npress + to add\nnew contacts")
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
                    onClick = onCancelClicked,
                ) {
                    Text(text = "Cancel")
                }
                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = navigateToSearchContactScreen
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Icon",
                        modifier = Modifier.fillMaxSize()
                    )
                }
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
    val coroutineScope = rememberCoroutineScope()

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
            Text(text = "Sex | Title| Name| Surname| Number| Edit| Delete|")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier
                .weight(1f),
            content = {
                items(savedContacts.contactsUiState.value.contactUiState) { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Text(text = "${contact.sex} | ${contact.title} | ${contact.firstName} | ${contact.lastName} | ${contact.phone}")
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    savedContacts.deleteContact(contact)
                                }
                            })
                        {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "delete icon"
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
                    onClick = onCancelClicked,
                ) {
                    Text(text = "Cancel")
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = navigateToSearchContactScreen
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Icon",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}








