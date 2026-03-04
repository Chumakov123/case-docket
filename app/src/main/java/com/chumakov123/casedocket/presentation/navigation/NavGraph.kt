package com.chumakov123.casedocket.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chumakov123.casedocket.presentation.screens.DraftListScreen
import com.chumakov123.casedocket.presentation.screens.EditDraftScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "draft_list"
    ) {
        composable("draft_list") {
            DraftListScreen(
                onNavigateToEdit = { taskId ->
                    navController.navigate("edit_draft/$taskId")
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
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}