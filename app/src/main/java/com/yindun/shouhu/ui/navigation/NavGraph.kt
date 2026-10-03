package com.yindun.shouhu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yindun.shouhu.ui.financial.FinancialScreen
import com.yindun.shouhu.ui.financial.AccountListScreen
import com.yindun.shouhu.ui.financial.BindAccountScreen
import com.yindun.shouhu.ui.financial.TransactionDetailScreen
import com.yindun.shouhu.ui.training.TrainingScreen
import com.yindun.shouhu.ui.training.TrainingSessionScreen
import com.yindun.shouhu.ui.training.TrainingHistoryScreen
import com.yindun.shouhu.ui.training.PointsShopScreen
import com.yindun.shouhu.ui.training.PointsDetailScreen
import com.yindun.shouhu.ui.call.CallScreen
import com.yindun.shouhu.ui.call.WhitelistScreen
import com.yindun.shouhu.ui.call.CallHistoryScreen
import com.yindun.shouhu.ui.report.ReportScreen
import com.yindun.shouhu.ui.report.CaseSubmitScreen
import com.yindun.shouhu.ui.report.CaseDetailScreen
import com.yindun.shouhu.ui.report.CaseListScreen
import com.yindun.shouhu.ui.family.FamilyGuardScreen
import com.yindun.shouhu.ui.settings.SettingsScreen
import com.yindun.shouhu.ui.settings.AccountSettingsScreen
import com.yindun.shouhu.ui.settings.PrivacySettingsScreen
import com.yindun.shouhu.ui.settings.DisplaySettingsScreen
import com.yindun.shouhu.ui.settings.DialectSettingsScreen
import com.yindun.shouhu.ui.settings.NotificationSettingsScreen
import com.yindun.shouhu.ui.settings.AboutScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Financial.route,
        modifier = modifier
    ) {
        // ==================== 底部导航主页面 ====================
        composable(BottomNavItem.Financial.route) {
            FinancialScreen(navController = navController)
        }

        composable(BottomNavItem.Training.route) {
            TrainingScreen(navController = navController)
        }

        composable(BottomNavItem.Call.route) {
            CallScreen(navController = navController)
        }

        composable(BottomNavItem.Report.route) {
            ReportScreen(navController = navController)
        }

        composable(BottomNavItem.Settings.route) {
            SettingsScreen(navController = navController)
        }

        // ==================== 财务监控子页面 ====================
        composable("financial/accounts") {
            AccountListScreen(
                onAddAccount = { navController.navigate("financial/bind/bank") },
                onAccountClick = { accountId ->
                    navController.navigate("financial/account/$accountId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "financial/bind/{accountType}",
            arguments = listOf(navArgument("accountType") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountType = backStackEntry.arguments?.getString("accountType") ?: "bank"
            BindAccountScreen(
                accountType = accountType,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "financial/account/{accountId}",
            arguments = listOf(navArgument("accountId") { type = NavType.LongType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getLong("accountId") ?: return@composable
            // AccountDetailScreen
        }

        composable(
            route = "financial/transaction/{transactionId}",
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: return@composable
            TransactionDetailScreen(
                transactionId = transactionId,
                onBack = { navController.popBackStack() }
            )
        }

        // ==================== 防骗训练子页面 ====================
        composable(
            route = "training/session/{scriptId}",
            arguments = listOf(navArgument("scriptId") { type = NavType.LongType })
        ) { backStackEntry ->
            val scriptId = backStackEntry.arguments?.getLong("scriptId") ?: return@composable
            TrainingSessionScreen(
                scriptId = scriptId,
                onSessionComplete = { feedback ->
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("training/history") {
            TrainingHistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("training/points") {
            PointsDetailScreen(
                onBack = { navController.popBackStack() },
                onShopClick = { navController.navigate("training/points/shop") }
            )
        }

        composable("training/points/shop") {
            PointsShopScreen(
                userId = 1,
                onBack = { navController.popBackStack() }
            )
        }

        // ==================== 来电防护子页面 ====================
        composable("call/whitelist") {
            WhitelistScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("call/history") {
            CallHistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ==================== 举报中心子页面 ====================
        composable("report/submit") {
            CaseSubmitScreen(
                isChildSubmit = false,
                onSubmitSuccess = { caseId, caseNumber ->
                    navController.navigate("report/detail/$caseId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("report/family_submit") {
            CaseSubmitScreen(
                isChildSubmit = true,
                onSubmitSuccess = { caseId, caseNumber ->
                    navController.navigate("report/detail/$caseId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "report/detail/{caseId}",
            arguments = listOf(navArgument("caseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val caseId = backStackEntry.arguments?.getLong("caseId") ?: return@composable
            CaseDetailScreen(
                caseId = caseId,
                onBack = { navController.popBackStack() }
            )
        }

        composable("report/list") {
            CaseListScreen(
                isChildView = false,
                onViewCase = { caseId ->
                    navController.navigate("report/detail/$caseId")
                },
                onSubmitCase = { navController.navigate("report/submit") },
                onBack = { navController.popBackStack() }
            )
        }

        // ==================== 家庭守护页面 ====================
        composable("family/guard") {
            FamilyGuardScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ==================== 设置子页面 ====================
        composable("settings/account") {
            AccountSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings/privacy") {
            PrivacySettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings/display") {
            DisplaySettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings/dialect") {
            DialectSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings/notification") {
            NotificationSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings/about") {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
