package com.example.meetingnotification.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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


@Composable
fun MettingNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: ContactsSearchScreenViewModel,
    onSendMessage : () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier,
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                modifier = Modifier.background(Color.Magenta),
                navigateToSavedContacts = { navController.navigate(SavedContactsDestination.route)},
                navigateToTemplateScreen = {navController.navigate(BeforeTemplateDestination.route)},
                onSendMessagesClicked = onSendMessage
            )
        }
        composable(route = BeforeTemplateDestination.route){
            ContactCheckScreen(
                modifier = Modifier.background(Color.Cyan),
                onCancelClicked = {navController.popBackStack()},
                calenderEvents = viewModel.getCalender(),
                smsSendService = viewModel.getServiceFromBag()
            )
        }
        composable(route = SavedContactsDestination.route) {
            SavedContacts (
                modifier = Modifier.background(Color.DarkGray),
                navigateToSearchContactScreen = {navController.navigate(SearchContactDestination.route)},
                onCancelClicked = {
                    navController.navigate(HomeDestination.route){
                        popUpTo(navController.graph.startDestinationId){
                            inclusive = true
                        }
                        launchSingleTop = true //sinleTop nicht erforderlich , außer wenn home destination zwei mal am backstacks ein kann.
                    }
                }
            )
        }
        composable(route = SearchContactDestination.route) {
            SearchListScreen(
                modifier = Modifier.background(Color.DarkGray),
                viewModel = viewModel,
                onCancelCLicked = {
                    navController.navigate(HomeDestination.route){
                        popUpTo(navController.graph.startDestinationId){
                            inclusive = false
                        }
                        launchSingleTop = true //sinleTop nicht erforderlich , außer wenn home destination zwei mal am backstacks ein kann.
                    }
                },
                navigateToSavedContacts = { navController.popBackStack()}
            )
        }
    }
}
