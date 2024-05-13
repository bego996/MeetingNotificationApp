package com.example.meetingnotification.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.meetingnotification.ui.contact.ContactsSearchScreenViewModel
import com.example.meetingnotification.ui.contact.BeforeTemplateDestination
import com.example.meetingnotification.ui.contact.ContactCheckScreen
import com.example.meetingnotification.ui.contact.SavedContacts
import com.example.meetingnotification.ui.contact.SavedContactsDestination
import com.example.meetingnotification.ui.contact.SearchContactDestination
import com.example.meetingnotification.ui.contact.SearchListScreen
import com.example.meetingnotification.ui.home.HomeDestination
import com.example.meetingnotification.ui.home.HomeScreen


@Composable
fun MettingNavHost(                                           // Hauptfunktion für den Navigations-Host
    navController: NavHostController,                         // Controller zur Verwaltung des Navigationsverhaltens
    modifier: Modifier = Modifier,
    viewModel: ContactsSearchScreenViewModel,                 // ViewModel
    onSendMessage: () -> Unit                                 // Callback für das Senden von Nachrichten
) {
    NavHost(
        navController = navController,                        // Bindet den NavController an den Host
        startDestination = HomeDestination.route,             // Legt die Start-Route fest
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {                 // Home-Screen als Start-Route
            HomeScreen(
                modifier = Modifier.background(Color.Magenta),      // Färbt den Hintergrund Magenta
                navigateToSavedContacts = { navController.navigate(SavedContactsDestination.route) },   // Navigiert zu den gespeicherten Kontakten
                navigateToTemplateScreen = { navController.navigate(BeforeTemplateDestination.route) }, // Navigiert zum Vorlagen-Screen
                onSendMessagesClicked = onSendMessage                                                   // Ruft die Funktion für das Senden von Nachrichten auf
            )
        }
        composable(route = BeforeTemplateDestination.route) {                         // Vorlagen-Screen für die Kontaktüberprüfung
            ContactCheckScreen(
                modifier = Modifier.background(Color.Cyan),
                onCancelClicked = { navController.popBackStack() },                   // Navigiert zurück zur vorherigen Route
                calenderEvents = viewModel.getCalender(),                             // Ruft die Kalenderereignisse aus dem ViewModel ab
                sendContactsToSmsService = { viewModel.insertContactsToSmsQueue(it) } // Fügt Kontakte zur SMS-Warteschlange hinzu
            )
        }
        composable(route = SavedContactsDestination.route) {            // Route für den Screen der gespeicherten Kontakte
            SavedContacts(
                modifier = Modifier.background(Color.DarkGray),         // Färbt den Hintergrund dunkelgrau
                navigateToSearchContactScreen = { navController.navigate(SearchContactDestination.route) }, // Navigiert zum Suchbildschirm
                onCancelClicked = {
                    navController.navigate(HomeDestination.route) { // Navigiert zurück zur Startseite
                        popUpTo(navController.graph.startDestinationId) { // Leert den Navigationsstapel bis zur Startdestination
                            inclusive = true
                        }
                        launchSingleTop = true // Verhindert das Duplizieren der Home-Destination im Backstack
                    }
                }
            )
        }
        composable(route = SearchContactDestination.route) { // Route für den Suchbildschirm
            SearchListScreen(
                modifier = Modifier.background(Color.DarkGray),
                viewModel = viewModel,
                onCancelCLicked = {
                    navController.navigate(HomeDestination.route) {             // Navigiert zurück zur Startseite
                        popUpTo(navController.graph.startDestinationId) {       // Leert den Navigationsstapel bis zur Startdestination
                            inclusive = false
                        }
                        launchSingleTop = true // Verhindert das Duplizieren der Home-Destination im Backstack
                    }
                },
                navigateToSavedContacts = { navController.popBackStack() }      // Geht zum vorherigen Bildschirm zurück
            )
        }
    }
}

