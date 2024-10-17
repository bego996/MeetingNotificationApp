package com.example.meetingnotification.ui.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.navigation.NavigationDestination

object HomeDestination : NavigationDestination {
    override val route: String = "home"
}


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToSavedContacts: () -> Unit,
    navigateToTemplateScreen: () -> Unit,
    onSendMessagesClicked: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background_picture1),
            contentDescription = "Hintergrundbild",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Skaliert das Bild, um den verfügbaren Raum zu füllen
        )
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Column
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Row {
                    Box(
                        modifier = Modifier

                    ) {
                        IconButton(
                            onClick = { /*TODO*/ },
                            modifier = Modifier
                                .background(Color.Transparent)
                        ) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                        }
                    }
                    // Add other components as needed
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = navigateToTemplateScreen
                ) {
                    Text("Check Template before Send")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSendMessagesClicked,
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White,
                        containerColor = Color.Red
                    )
                ) {
                    Text(text = "Send")
                }
                Text(text = "<blank>", color = Color.Yellow)
            }
            // Bottom Column
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Button(
                    onClick = navigateToSavedContacts,
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Saved Contacts")
                }
            }
        }
    }
}




