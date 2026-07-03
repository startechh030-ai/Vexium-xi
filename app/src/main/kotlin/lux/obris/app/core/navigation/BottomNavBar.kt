package lux.obris.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import lux.obris.app.core.theme.NeonCyan
import lux.obris.app.core.theme.SpaceBlue

/** Bottom nav items for Obris */
data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Screen,
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home, Screen.Home),
    BottomNavItem("Games", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports, Screen.Games),
    BottomNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person, Screen.Profile),
)

@Composable
fun ObrisBottomBar(
    currentRoute: Screen?,
    onNavigate: (Screen) -> Unit,
) {
    NavigationBar(
        containerColor = Color(0xFF060610),
        contentColor = NeonCyan,
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(item.label, style = MaterialTheme.typography.labelSmall)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NeonCyan,
                    selectedTextColor = NeonCyan,
                    unselectedIconColor = Color(0xFF555570),
                    unselectedTextColor = Color(0xFF555570),
                    indicatorColor = NeonCyan.copy(alpha = 0.10f),
                ),
            )
        }
    }
}
