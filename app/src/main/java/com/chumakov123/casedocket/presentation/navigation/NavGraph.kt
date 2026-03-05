package com.chumakov123.casedocket.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chumakov123.casedocket.presentation.screens.EditDraftScreen
import com.chumakov123.casedocket.presentation.screens.MainScreen
import com.chumakov123.casedocket.presentation.screens.SettingsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onNavigateToEditDraft = { taskId ->
                    navController.navigate("edit_draft/$taskId")
                },
                onNavigateToEditConfirmed = { confirmedId ->
                    navController.navigate("edit_confirmed/$confirmedId")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }
        composable(
            "edit_draft/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: return@composable
            EditDraftScreen(
                taskId = taskId,
                confirmedId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            "edit_confirmed/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: return@composable
            EditDraftScreen(
                taskId = null,
                confirmedId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}