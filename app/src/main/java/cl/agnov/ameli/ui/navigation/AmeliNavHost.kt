package cl.agnov.ameli.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.agnov.ameli.AmeliApplication
import cl.agnov.ameli.sip.model.CallConnectionState
import cl.agnov.ameli.sip.model.CallDirection
import cl.agnov.ameli.ui.screens.ActiveCallScreen
import cl.agnov.ameli.ui.screens.DialerScreen
import cl.agnov.ameli.ui.screens.HistoryScreen
import cl.agnov.ameli.ui.screens.HomeScreen
import cl.agnov.ameli.ui.screens.IncomingCallScreen
import cl.agnov.ameli.ui.screens.SettingsScreen

object AmeliDestinations {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val DIALER = "dialer"
    const val ACTIVE_CALL = "active_call"
    const val INCOMING_CALL = "incoming_call"
    const val HISTORY = "history"
}

@Composable
fun AmeliNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val application = LocalContext.current.applicationContext as AmeliApplication
    val callState by application.container.callManager.callState.collectAsState()
    val currentRoute by navController.currentBackStackEntryAsState()

    LaunchedEffect(callState?.connectionState, currentRoute) {
        val isIncomingRinging = callState?.direction == CallDirection.INCOMING &&
            callState?.connectionState == CallConnectionState.INCOMING_RINGING
        val alreadyShowingIncomingCall = currentRoute?.destination?.route == AmeliDestinations.INCOMING_CALL
        if (isIncomingRinging && !alreadyShowingIncomingCall) {
            navController.navigate(AmeliDestinations.INCOMING_CALL)
        }
    }

    NavHost(
        navController = navController,
        startDestination = AmeliDestinations.HOME,
        modifier = modifier,
    ) {
        composable(AmeliDestinations.HOME) {
            HomeScreen(
                onOpenSettings = { navController.navigate(AmeliDestinations.SETTINGS) },
                onOpenDialer = { navController.navigate(AmeliDestinations.DIALER) },
                onOpenHistory = { navController.navigate(AmeliDestinations.HISTORY) },
            )
        }
        composable(AmeliDestinations.HISTORY) {
            HistoryScreen()
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
        composable(AmeliDestinations.INCOMING_CALL) {
            IncomingCallScreen(
                onAnswered = {
                    navController.navigate(AmeliDestinations.ACTIVE_CALL) {
                        popUpTo(AmeliDestinations.INCOMING_CALL) { inclusive = true }
                    }
                },
                onDismissed = {
                    navController.popBackStack(AmeliDestinations.HOME, inclusive = false)
                },
            )
        }
    }
}
