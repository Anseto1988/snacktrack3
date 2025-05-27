package com.example.snacktrack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
import com.example.snacktrack.ui.screens.food.BarcodeScanner // Renamed to avoid conflict if necessary
import com.example.snacktrack.ui.viewmodel.DogViewModel

//ViewModel Factory for DogViewModel
class DogViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DogViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")

    object DogList : Screen("dog_list")

    object Dashboard : Screen("dashboard/{dogId}") {
        fun createRoute(dogId: String) = "dashboard/$dogId"
    }

    object AddDog : Screen("add_dog")
    object EditDog : Screen("edit_dog/{dogId}") {
        fun createRoute(dogId: String) = "edit_dog/$dogId"
    }

    object DogDetail : Screen("dog_detail/{dogId}") {
        fun createRoute(dogId: String) = "dog_detail/$dogId"
    }

    object BarcodeScannerNav : Screen("barcode_scanner_screen/{dogId}") { // Changed name to avoid class name collision
        fun createRoute(dogId: String) = "barcode_scanner_screen/$dogId"
    }

    object FoodDetail : Screen("food_detail/{foodId}/{dogId}") {
        fun createRoute(foodId: String, dogId: String) = "food_detail/$foodId/$dogId"
    }

    object ManualFoodEntry : Screen("manual_food_entry/{dogId}") {
        fun createRoute(dogId: String) = "manual_food_entry/$dogId"
    }

    object AddWeight : Screen("add_weight/{dogId}") {
        fun createRoute(dogId: String) = "add_weight/$dogId"
    }

    object WeightHistory : Screen("weight_history/{dogId}") {
        fun createRoute(dogId: String) = "weight_history/$dogId"
    }
}

@Composable
fun SnackTrackNavGraph(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.DogList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.DogList.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        popUpTo(Screen.Login.route) { inclusive = true } // Also pop login if it's in backstack
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.DogList.route) {
            DogListScreen(
                onDogClick = { dogId ->
                    navController.navigate(Screen.Dashboard.createRoute(dogId))
                },
                onAddDogClick = { navController.navigate(Screen.AddDog.route) }
            )
        }

        composable(
            route = Screen.Dashboard.route,
            arguments = listOf(navArgument("dogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            DashboardScreen(
                dogId = dogId,
                onDogDetailClick = { navController.navigate(Screen.DogDetail.createRoute(dogId)) },
                onManualEntryClick = { navController.navigate(Screen.ManualFoodEntry.createRoute(dogId)) },
                onWeightHistoryClick = { navController.navigate(Screen.WeightHistory.createRoute(dogId)) },
                onAddWeightClick = { navController.navigate(Screen.AddWeight.createRoute(dogId)) },
                onScannerClick = { navController.navigate(Screen.BarcodeScannerNav.createRoute(dogId)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AddDog.route) {
            AddEditDogScreen(
                dogId = null, // For adding a new dog
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditDog.route,
            arguments = listOf(navArgument("dogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId")
            AddEditDogScreen(
                dogId = dogId, // For editing an existing dog
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DogDetail.route,
            arguments = listOf(navArgument("dogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            val dogViewModel: DogViewModel = viewModel(factory = DogViewModelFactory(context))
            DogDetailScreen(
                dogId = dogId,
                navController = navController,
                dogViewModel = dogViewModel
            )
        }

        composable(
            route = Screen.BarcodeScannerNav.route,
            arguments = listOf(navArgument("dogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            BarcodeScanner( // from com.example.snacktrack.ui.screens.food.BarcodeScanner
                dogId = dogId,
                onFoodFound = { foodId ->
                    // First pop scanner, then navigate to food detail to avoid scanner in backstack when pressing back from FoodDetail
                    navController.popBackStack()
                    navController.navigate(Screen.FoodDetail.createRoute(foodId, dogId))
                },
                onFoodNotFound = { ean ->
                    // Handle EAN not found. Example: Navigate to manual entry or a submission screen.
                    // For now, pop scanner and go to manual entry.
                    navController.popBackStack()
                    navController.navigate(Screen.ManualFoodEntry.createRoute(dogId))
                    // TODO: Optionally pass 'ean' to the manual entry or a new food submission screen.
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.FoodDetail.route,
            arguments = listOf(
                navArgument("foodId") { type = NavType.StringType },
                navArgument("dogId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            FoodDetailScreen(
                foodId = foodId,
                dogId = dogId,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ManualFoodEntry.route,
            arguments = listOf(navArgument("dogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            ManualFoodEntryScreen(
                dogId = dogId,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddWeight.route,
            arguments = listOf(navArgument("dogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            AddWeightScreen(
                dogId = dogId,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.WeightHistory.route,
            arguments = listOf(navArgument("dogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            WeightHistoryScreen(
                dogId = dogId,
                navController = navController
            )
        }
    }
}