package com.example.meetingnotification.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.meetingnotification.ui.contact.BeforeTemplateDestination
import com.example.meetingnotification.ui.contact.ContactCheckScreen
import com.example.meetingnotification.ui.contact.ContactsSearchScreenViewModel
import com.example.meetingnotification.ui.contact.SavedContacts
import com.example.meetingnotification.ui.contact.SavedContactsDestination
import com.example.meetingnotification.ui.contact.SearchContactDestination
import com.example.meetingnotification.ui.contact.SearchListScreen
import com.example.meetingnotification.ui.home.HomeDestination
import com.example.meetingnotification.ui.home.HomeScreen
import com.example.meetingnotification.ui.home.InstructionsDestination
import com.example.meetingnotification.ui.home.InstructionsScreen


@Composable
fun MettingNavHost(                                           // Hauptfunktion für den Navigations-Host
    navController: NavHostController,                         // Controller zur Verwaltung des Navigationsverhaltens
    modifier: Modifier = Modifier,
    viewModel: ContactsSearchScreenViewModel                  // ViewModel
) {
    NavHost(
        navController = navController,                        // Bindet den NavController an den Host
        startDestination = HomeDestination.route,             // Legt die Start-Route fest
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {                 // Home-Screen als Start-Route
            HomeScreen(
                modifier = Modifier,
                navigateToSavedContacts = { navController.navigate(SavedContactsDestination.route) },   // Navigiert zu den gespeicherten Kontakten
                navigateToTemplateScreen = { navController.navigate(BeforeTemplateDestination.route) }, // Navigiert zum Vorlagen-Screen
                onSendMessagesClicked = { viewModel.sendCommandToSendAllMessages() },                   // Ruft die Funktion für das Senden von Nachrichten auf
                openInstructions = {navController.navigate(InstructionsDestination.route)}
            )
        }
        composable(route = BeforeTemplateDestination.route) {                         // Vorlagen-Screen für die Kontaktüberprüfung
            ContactCheckScreen(
                navigateToHomeScreen = {
                    navController.navigate(HomeDestination.route) {
                        popUpTo(navController.graph.startDestinationId) {
                        }
                        launchSingleTop = true
                    }
                },                   // Navigiert zurück zur vorherigen Route
                calenderEvents = viewModel.getCalender(),                             // Ruft die Kalenderereignisse aus dem ViewModel ab
                sendContactsToSmsService = { viewModel.insertContactsToSmsQueue(it) }, // Fügt Kontakte zur SMS-Warteschlange hinzu
                contactsInSmsQueueById = viewModel.getContactsFromSmsQueue() ?: emptyList(),
                removeContactFromSmsQueue = { viewModel.removeContactIfInSmsQueue(it) },
                onNavigateUp = {navController.popBackStack()}
            )
        }
        composable(route = SavedContactsDestination.route) {            // Route für den Screen der gespeicherten Kontakte
            SavedContacts(
                modifier = Modifier,
                navigateToSearchContactScreen = { navController.navigate(SearchContactDestination.route) }, // Navigiert zum Suchbildschirm
                deleteContactFromSmsQueueIfExisting = {viewModel.removeContactIfInSmsQueue(it)},
                onCancelClicked = {
                    navController.navigate(HomeDestination.route) { // Navigiert zurück zur Startseite
                        popUpTo(navController.graph.startDestinationId) { // Leert den Navigationsstapel bis zur Startdestination was eingestelt als HOmeScreen ist.
//                          inclusive = true                                Selbe logik wie composable zeile 61. Nur als demonstarion hier wenn es true währe, dann würde die instanz gelöscht werden und eine neue erstellt werden. Nützlich um z.b alte werte nicht zu behalten.
                        }
                        launchSingleTop = true // Verhindert das Duplizieren der Home-Destination im Backstack genau wie unten.
                    }
                },
                onNavigateUp = {navController.popBackStack()}
            )
        }
        composable(route = SearchContactDestination.route) { // Route für den Suchbildschirm
            SearchListScreen(
                modifier = Modifier,
                viewModel = viewModel,
                onCancelCLicked = {
                    navController.navigate(HomeDestination.route) {
                        popUpTo(navController.graph.startDestinationId) {
                        }
                        launchSingleTop = true
                    }

                },
                navigateToSavedContacts = { navController.popBackStack() },      // Geht zum vorherigen Bildschirm zurück
                onNavigateUp = {navController.popBackStack()}
            )
        }
        composable(route = InstructionsDestination.route) {
            InstructionsScreen(
                modifier = Modifier,
                onBack = {navController.popBackStack() },
                navigateToHome = {navController.popBackStack()}
            )
        }
    }
}

