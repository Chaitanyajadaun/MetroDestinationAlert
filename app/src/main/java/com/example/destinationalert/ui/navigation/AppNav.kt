package com.example.destinationalert.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.destinationalert.ui.screens.ActiveTripScreen
import com.example.destinationalert.ui.screens.TripSetupScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = "setup") {
        composable("setup") {
            TripSetupScreen(
                onTripStarted = {
                    navController.navigate("active") {
                        popUpTo("setup") { inclusive = true }
                    }
                }
            )
        }
        composable("active") {
            ActiveTripScreen(
                onTripEnded = {
                    navController.navigate("setup") {
                        popUpTo("active") { inclusive = true }
                    }
                }
            )
        }
    }
}
