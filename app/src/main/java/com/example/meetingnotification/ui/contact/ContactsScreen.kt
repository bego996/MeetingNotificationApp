package com.example.meetingnotification.ui.contact

import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.meetingnotification.ui.navigation.NavigationDestination


object SavedContactsDestination: NavigationDestination {
    override val route = "saved"
}


@Composable
fun EmptyListScreen(
    modifier: Modifier,
    navigateToSearchContactScreen: () -> Unit
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
                    onClick = { /*TODO*/ },
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







@Preview
@Composable
fun FilledListscreen(
    modifier: Modifier = Modifier
) {
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
            Text(text = "Sex|")
            Text(text = "Title|")
            Text(text = "Name|")
            Text(text = "Surname|")
            Text(text = "Number|")
            Text(text = "Edit|")
            Text(text = "Delete|")
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f),
            content = {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Text(text = "M|")
                        Text(text = "Mag|")
                        Text(text = "Charlie|")
                        Text(text = "Heisenberg|")
                        Text(text = "04632274873|")
                        IconButton(
                            onClick = { /*TODO*/ })
                        {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "edit icon"
                            )
                        }
                        IconButton(
                            onClick = { /*TODO*/ })
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
                    onClick = { /*TODO*/ },
                ) {
                    Text(text = "Cancel")
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = { /*TODO*/ }
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








