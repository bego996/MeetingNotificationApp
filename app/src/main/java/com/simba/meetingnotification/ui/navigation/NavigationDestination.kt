package com.simba.meetingnotification.ui.navigation

//Schnittstele die in jedem Ui composable implementiert wird um zwischen Navcontroller und Bildschirm navigieren zu können.
interface NavigationDestination {
    val route: String
    val titleRes :Int
}