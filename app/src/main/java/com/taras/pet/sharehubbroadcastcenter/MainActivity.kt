package com.taras.pet.sharehubbroadcastcenter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.taras.pet.sharehubbroadcastcenter.presenter.share.ShareScreen
import com.taras.pet.sharehubbroadcastcenter.presenter.system.SystemEventsScreen
import com.taras.pet.sharehubbroadcastcenter.presenter.ui.theme.ShareHubBroadcastCenterTheme
import dagger.hilt.android.AndroidEntryPoint

// Navigation items data class
data class BottomNavItem(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
)

// List of navigation items
val bottomNavItems = listOf(
    BottomNavItem(
        route = "share",
        titleRes = R.string.share_tab,
        icon = Icons.Filled.Share
    ),
    BottomNavItem(
        route = "system_events",
        titleRes = R.string.system_events_tab,
        icon = Icons.Filled.Notifications
    )
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShareHubBroadcastCenterTheme {
                AppScaffold()
            }
        }
    }
}

@Composable
fun AppScaffold() {
    val navController = rememberNavController()
  
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                bottomNavItems.forEach { item ->
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(item.titleRes)
                            )
                        },
                        label = { Text(stringResource(item.titleRes)) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "share",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("share") {
                ShareScreen()
            }
            composable("system_events") {
                SystemEventsScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScaffoldPreview() {
    ShareHubBroadcastCenterTheme {
        AppScaffold()
    }
}