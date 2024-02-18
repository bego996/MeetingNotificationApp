package com.example.meetingnotification.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.meetingnotification.ui.home.HomeScreenViewModel
import com.example.meetingnotification.ui.navigation.MettingNavHost


@Composable
fun NotificationApp(
    navController: NavHostController = rememberNavController(),
    viewModel: HomeScreenViewModel
) {
    MettingNavHost(
        navController = navController,
        viewModel = viewModel
    )
}