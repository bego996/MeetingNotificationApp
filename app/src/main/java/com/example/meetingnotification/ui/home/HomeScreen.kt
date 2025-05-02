package com.example.meetingnotification.ui.home

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meetingnotification.ui.AppViewModelProvider
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.navigation.NavigationDestination
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object HomeDestination : NavigationDestination {
    override val route: String = "home"
}


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToSavedContacts: () -> Unit,
    navigateToTemplateScreen: () -> Unit,
    onSendMessagesClicked: () -> Unit,
    viewModel: HomeScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val dateMessageSentUiState = viewModel.dateMessageSendUiState.collectAsState()
    var dateLastTimeSendetMessages by remember { mutableStateOf( "Messages will be sent to all selected contacts") }

    LaunchedEffect(dateMessageSentUiState.value) {
        if (dateMessageSentUiState.value.lastDateSendet.isNotBlank()){
            Log.d("HomeScreen","if Statement entered, value is not blank.")
            dateLastTimeSendetMessages =
                "Letzte Sendung am ${LocalDate.parse(dateMessageSentUiState.value.lastDateSendet).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} " +
                        "um ${dateMessageSentUiState.value.lastTimeSendet} Uhr durchgeführt."
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Hintergrundbild (Platzhalter - ersetze mit deinem eigenen)
        Image(
            painter = painterResource(R.drawable.background_light2),
            contentDescription = "Hintergrundbild",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay für bessere Lesbarkeit
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.1f)
                        ),
                        startY = 0f,
                        endY = 1000f
                    )
                )
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header-Bereich
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { /* Settings oder Menü */ },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menü")
                }
            }

            // Hauptinhalt
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Haupt-CTA-Button
                Button(
                    onClick = onSendMessagesClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Text(
                        "SEND MESSAGES",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Sekundärer Button
                OutlinedButton(
                    onClick = navigateToTemplateScreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        // Container Color für den Hintergrund (optional transparent)
                        containerColor = Color.Transparent
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = Color.White
                    )
                ) {
                    Text("Check Templates")
                }

                // Info-Text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        dateLastTimeSendetMessages,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            // Footer-Bereich
            Button(
                onClick = navigateToSavedContacts,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5),
                    contentColor = Color.White
                )
            ) {
                Text("Saved Contacts", fontSize = 16.sp)
            }
        }
    }
}




