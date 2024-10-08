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


object SearchContactDestination: NavigationDestination{
    override val route = "searchScreen"

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchListScreen(
    modifier: Modifier = Modifier,
    viewModel: ContactsSearchScreenViewModel,
    onCancelCLicked : () -> Unit,
    navigateToSavedContacts : () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val uiState = viewModel.contactsUiState.collectAsState()

    val contactBuffer = viewModel.getContacts().observeAsState(listOf())

    var text by remember { mutableStateOf("")}

    var contactBufferSorted by remember { mutableStateOf(contactBuffer.value)}

    var contactIdsRadioDepency by remember { mutableStateOf(listOf<MutablePairs>()) }

    LaunchedEffect(contactBuffer.value){
        contactIdsRadioDepency = contactBuffer.value.map { contact -> MutablePairs(contact.id, false) }
    }

    LaunchedEffect(text){
        contactBufferSorted = if (text == ""){
            contactBuffer.value
        }else{
            contactBuffer.value
                .filter { it.firstName.contains(text,true)}
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                modifier = Modifier.size(55.dp)
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(55.dp),
                value = text,
                onValueChange = { newText ->

                    text = newText
                },
                placeholder = { Text("Enter Search Options") },
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(16.dp)) // Add some spacing between the search bar and LazyColumn
        Text(text = "Name | Surname | Sex | Number | Title", modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(contactBufferSorted) { contact ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${contact.firstName} | ${contact.lastName} | ${contact.sex} | ${contact.phone} | ${contact.title} |",
                        modifier = Modifier.weight(1f)
                    )
                    if (!uiState.value.contactList.contains(contact)){
                        RadioButton(
                            selected = contactIdsRadioDepency.firstOrNull { pair ->
                                pair.first == contact.id
                            }?.second ?: false,
                            onClick = {
                                val updateList = contactIdsRadioDepency.toMutableList()
                                val index = updateList.indexOfFirst { it.first == contact.id }
                                if (index != -1) {
                                    updateList[index] =
                                        MutablePairs(contact.id, !updateList[index].second)
                                }
                                contactIdsRadioDepency = updateList
                            }
                        )
                    }else{
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Häckchen",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
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
                    onClick = onCancelCLicked,
                ) {
                    Text(text = "Cancel")
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        contactBufferSorted.isNotEmpty().and(contactIdsRadioDepency.any {it.second}).let {
                            val idToMap = contactIdsRadioDepency.filter {it.second}.map {it.first}
                            coroutineScope.launch {
                                viewModel.addContactsToDatabase(contactBufferSorted,idToMap)
                                navigateToSavedContacts()
                            }
                        }
                    }
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

data class MutablePairs(var first: Int, var second: Boolean)