package com.example.meetingnotification.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.meetingnotification.ui.AppViewModelProvider
import com.example.meetingnotification.ui.contact.EmptyListScreen

import com.example.meetingnotification.ui.contact.SavedContactsDestination
import com.example.meetingnotification.ui.contact.SearchContactDestination
import com.example.meetingnotification.ui.contact.SearchListScreen

import com.example.meetingnotification.ui.home.HomeDestination
import com.example.meetingnotification.ui.home.HomeScreen
import com.example.meetingnotification.ui.home.HomeScreenViewModel


@Composable
fun MettingNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                modifier = Modifier.background(Color.Magenta),
                navigateToSavedContacts = { navController.navigate(SavedContactsDestination.route) },
                viewModel = viewModel
            )
        }
        composable(route = SavedContactsDestination.route) {
            EmptyListScreen (
                modifier = Modifier.background(Color.DarkGray),
                navigateToSearchContactScreen = {navController.navigate(SearchContactDestination.route)}
            )
        }
        composable(route = SearchContactDestination.route) {
            SearchListScreen(
                modifier = Modifier.background(Color.DarkGray)
            )
        }
    }
}
