package org.openedx.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.openedx.core.ui.theme.appColors

/**
 * Main screen with bottom navigation.
 * This is the Compose replacement for MainFragment's ViewPager2 + BottomNavigationView.
 *
 * Currently a placeholder that will gradually replace MainFragment
 * as child Fragments are converted to composables.
 */

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
)

@Composable
fun MainScreen(
    isDownloadsEnabled: Boolean = false,
    isDatesEnabled: Boolean = false,
    onTabSelected: (String) -> Unit = {},
) {
    val tabs = buildList {
        add(BottomNavItem("Learn", Icons.Default.School, "learn"))
        add(BottomNavItem("Discover", Icons.Default.Explore, "discover"))
        if (isDownloadsEnabled) {
            add(BottomNavItem("Downloads", Icons.Default.Download, "downloads"))
        }
        if (isDatesEnabled) {
            add(BottomNavItem("Dates", Icons.Default.DateRange, "dates"))
        }
        add(BottomNavItem("Profile", Icons.Default.Person, "profile"))
    }

    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.appColors.background,
            ) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            onTabSelected(item.route)
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        // Tab content will be rendered here
        // Each tab hosts its own screen composable
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (tabs.getOrNull(selectedIndex)?.route) {
                "learn" -> {
                    // TODO: Host LearnScreen composable
                    PlaceholderScreen("Learn")
                }
                "discover" -> {
                    // TODO: Host DiscoveryScreen composable
                    PlaceholderScreen("Discover")
                }
                "downloads" -> {
                    // TODO: Host DownloadsScreen composable
                    PlaceholderScreen("Downloads")
                }
                "dates" -> {
                    // TODO: Host DatesScreen composable
                    PlaceholderScreen("Dates")
                }
                "profile" -> {
                    // TODO: Host ProfileView composable
                    PlaceholderScreen("Profile")
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}
