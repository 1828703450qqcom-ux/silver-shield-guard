package com.yindun.shouhu.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

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

// 所有底部导航项
val bottomNavItems = listOf(
    BottomNavItem.Financial,
    BottomNavItem.Training,
    BottomNavItem.Call,
    BottomNavItem.Report,
    BottomNavItem.Settings
)

// 路由常量
object Routes {
    // 启动相关
    const val SPLASH = "splash"
    const val GUIDE = "guide"
    const val LOGIN = "login"

    const val HOME = "home"
    const val FINANCIAL = "financial"
    const val FINANCIAL_BIND_ACCOUNT = "financial/bind_account/{accountType}"
    const val FINANCIAL_ACCOUNT_DETAIL = "financial/account/{accountId}"
    const val FINANCIAL_TRANSACTION_DETAIL = "financial/transaction/{transactionId}"

    const val TRAINING = "training"
    const val TRAINING_SESSION = "training/session/{scriptId}"
    const val TRAINING_HISTORY = "training/history"
    const val TRAINING_POINTS = "training/points"
    const val TRAINING_POINTS_SHOP = "training/points/shop"
    const val TRAINING_RANDOM = "training/random"

    const val CALL = "call"
    const val CALL_WHITELIST = "call/whitelist"
    const val CALL_HISTORY = "call/history"

    const val REPORT = "report"
    const val REPORT_SUBMIT = "report/submit"
    const val REPORT_FAMILY_SUBMIT = "report/family_submit"
    const val REPORT_DETAIL = "report/detail/{caseId}"
    const val REPORT_PROGRESS = "report/progress/{caseId}"

    const val SETTINGS = "settings"
    const val SETTINGS_ACCOUNT = "settings/account"
    const val SETTINGS_PRIVACY = "settings/privacy"
    const val SETTINGS_DISPLAY = "settings/display"
    const val SETTINGS_DIALECT = "settings/dialect"
    const val SETTINGS_NOTIFICATION = "settings/notification"
    const val SETTINGS_ABOUT = "settings/about"

    const val FAMILY_GUARD = "family/guard"
}
