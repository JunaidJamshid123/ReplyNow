package com.example.replynow.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.replynow.ui.screen.AppMessagesScreen
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
        composable(Screen.Home.route) {
            HomeScreen(
                onAppClick = { packageName, appName, iconRes, accentColor ->
                    navController.navigate(
                        Screen.AppMessages.createRoute(packageName, appName, iconRes, accentColor)
                    )
                }
            )
        }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(
            route = Screen.AppMessages.route,
            arguments = listOf(
                navArgument("packageName") { type = NavType.StringType },
                navArgument("appName") { type = NavType.StringType },
                navArgument("iconRes") { type = NavType.IntType },
                navArgument("accentColor") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val appName = backStackEntry.arguments?.getString("appName") ?: ""
            val iconRes = backStackEntry.arguments?.getInt("iconRes") ?: 0
            val accentColor = backStackEntry.arguments?.getLong("accentColor") ?: 0L
            AppMessagesScreen(
                appName = appName,
                iconRes = iconRes,
                accentColor = accentColor,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
