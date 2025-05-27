package com.example.snacktrack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.snacktrack.ui.screens.auth.LoginScreen
import com.example.snacktrack.ui.screens.auth.RegisterScreen
import com.example.snacktrack.ui.screens.dashboard.DashboardScreen
import com.example.snacktrack.ui.screens.dog.AddEditDogScreen
import com.example.snacktrack.ui.screens.dog.DogDetailScreen
import com.example.snacktrack.ui.screens.dog.DogListScreen
import com.example.snacktrack.ui.screens.food.FoodDetailScreen
import com.example.snacktrack.ui.screens.food.ManualFoodEntryScreen
import com.example.snacktrack.ui.screens.weight.AddWeightScreen
import com.example.snacktrack.ui.screens.weight.WeightHistoryScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object AddEditDog : Screen("add_edit_dog/{dogId}")
    object DogDetail : Screen("dog_detail/{dogId}")
    object DogList : Screen("dog_list")
    object FoodDetail : Screen("food_detail/{foodId}/{dogId}")
    object ManualFoodEntry : Screen("manual_food_entry/{dogId}")
    object AddWeight : Screen("add_weight/{dogId}")
    object WeightHistory : Screen("weight_history/{dogId}")
}

@Composable
fun SnackTrackNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Dashboard.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Dashboard.route) },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onDogDetailClick = { dogId -> navController.navigate("dog_detail/$dogId") },
                onManualEntryClick = { dogId -> navController.navigate("manual_food_entry/$dogId") },
                onWeightHistoryClick = { dogId -> navController.navigate("weight_history/$dogId") },
                onAddWeightClick = { dogId -> navController.navigate("add_weight/$dogId") },
                onScannerClick = { dogId -> navController.navigate("food_detail/0/$dogId") },
                onBackClick = { navController.popBackStack() },
                dogId = ""
            )
        }
        composable("add_edit_dog/{dogId}") { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            AddEditDogScreen(
                dogId = dogId,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("dog_detail/{dogId}") { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            DogDetailScreen(dogId = dogId, navController = navController)
        }
        composable(Screen.DogList.route) {
            DogListScreen(
                onDogClick = { dogId -> navController.navigate("dog_detail/$dogId") },
                onAddDogClick = { navController.navigate("add_edit_dog/") }
            )
        }
        composable("food_detail/{foodId}/{dogId}") { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            FoodDetailScreen(
                foodId = foodId,
                dogId = dogId,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("manual_food_entry/{dogId}") { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            ManualFoodEntryScreen(
                dogId = dogId,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("add_weight/{dogId}") { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            AddWeightScreen(
                dogId = dogId,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("weight_history/{dogId}") { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            WeightHistoryScreen(dogId = dogId, navController = navController)
        }
    }
}
