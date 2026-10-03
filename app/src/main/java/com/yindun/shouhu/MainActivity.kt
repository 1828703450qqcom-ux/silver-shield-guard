package com.yindun.shouhu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yindun.shouhu.di.AppModule
import com.yindun.shouhu.ui.guide.GuideScreen
import com.yindun.shouhu.ui.login.LoginScreen
import com.yindun.shouhu.ui.navigation.NavGraph
import com.yindun.shouhu.ui.splash.SplashScreen
import com.yindun.shouhu.ui.theme.YinDunShouHuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 初始化依赖注入
        AppModule.init(applicationContext)

        setContent {
            YinDunShouHuTheme {
                AppNavigation()
            }
        }
    }
}

// 底部导航项
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Financial : BottomNavItem(
        route = "financial",
        title = "财务监控",
        icon = Icons.Default.AccountBalance
    )

    data object Training : BottomNavItem(
        route = "training",
        title = "防骗训练",
        icon = Icons.Default.School
    )

    data object Call : BottomNavItem(
        route = "call",
        title = "来电防护",
        icon = Icons.Default.Phone
    )

    data object Report : BottomNavItem(
        route = "report",
        title = "举报中心",
        icon = Icons.Default.Report
    )

    data object Settings : BottomNavItem(
        route = "settings",
        title = "设置",
        icon = Icons.Default.Settings
    )
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf("splash") }

    when (currentScreen) {
        "splash" -> SplashScreen(
            onSplashComplete = {
                currentScreen = "guide"
            }
        )
        "guide" -> GuideScreen(
            onComplete = {
                currentScreen = "login"
            }
        )
        "login" -> LoginScreen(
            onLoginSuccess = {
                currentScreen = "main"
            },
            onSkipLogin = {
                currentScreen = "main"
            }
        )
        "main" -> MainScreen()
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem.Financial,
        BottomNavItem.Training,
        BottomNavItem.Call,
        BottomNavItem.Report,
        BottomNavItem.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
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
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
