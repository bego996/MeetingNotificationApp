package com.example.meetingnotification.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.meetingnotification.ui.contact.ContactsSearchScreenViewModel
import com.example.meetingnotification.ui.navigation.MettingNavHost


@Composable
fun NotificationApp(
    navController: NavHostController = rememberNavController(),
    viewModel: ContactsSearchScreenViewModel,
    sendMessage : () -> Unit
) {
    MettingNavHost(
        navController = navController,
        viewModel = viewModel,
        onSendMessage = sendMessage
    )
}