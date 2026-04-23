package com.example.replynow.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.replynow.ui.screen.HomeScreen
import com.example.replynow.ui.screen.SettingsScreen
import com.example.replynow.ui.screen.SplashScreen

@Composable
fun ReplyNowNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = { fadeIn() + slideInHorizontally { it / 4 } },
        exitTransition = { fadeOut() + slideOutHorizontally { -it / 4 } },
        popEnterTransition = { fadeIn() + slideInHorizontally { -it / 4 } },
        popExitTransition = { fadeOut() + slideOutHorizontally { it / 4 } }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
        composable(Screen.Home.route) { HomeScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
    }
}
