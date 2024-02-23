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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun ContactCheckScreen(){
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
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daniel | Prettner | Dr. Dipl.  | 04576374",
                            modifier = Modifier.weight(5f)
                        )
                        IconButton(
                            modifier = Modifier.weight(1f),
                            onClick = { /*TODO*/ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null)
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daniel | Prettner | Dr. | 04576374",
                            modifier = Modifier.weight(5f)
                        )
                        IconButton(
                            modifier = Modifier.weight(1f),
                            onClick = { /*TODO*/ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null)
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
                    onClick = { /*TODO*/ },
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
                    onClick = { /*TODO*/ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "SubmitAll")
                }

            }
        }


    }
}