package cl.agnov.ameli.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.agnov.ameli.ui.screens.ActiveCallScreen
import cl.agnov.ameli.ui.screens.DialerScreen
import cl.agnov.ameli.ui.screens.HomeScreen
import cl.agnov.ameli.ui.screens.SettingsScreen

object AmeliDestinations {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val DIALER = "dialer"
    const val ACTIVE_CALL = "active_call"
}

@Composable
fun AmeliNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AmeliDestinations.HOME,
        modifier = modifier,
    ) {
        composable(AmeliDestinations.HOME) {
            HomeScreen(
                onOpenSettings = { navController.navigate(AmeliDestinations.SETTINGS) },
                onOpenDialer = { navController.navigate(AmeliDestinations.DIALER) },
            )
        }
        composable(AmeliDestinations.SETTINGS) {
            SettingsScreen(
                onSaved = { navController.popBackStack() },
            )
        }
        composable(AmeliDestinations.DIALER) {
            DialerScreen(
                onCallStarted = { navController.navigate(AmeliDestinations.ACTIVE_CALL) },
            )
        }
        composable(AmeliDestinations.ACTIVE_CALL) {
            ActiveCallScreen(
                onCallEnded = {
                    navController.popBackStack(AmeliDestinations.HOME, inclusive = false)
                },
            )
        }
    }
}
