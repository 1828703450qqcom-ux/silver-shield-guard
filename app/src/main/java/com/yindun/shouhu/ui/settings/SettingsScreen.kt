package com.yindun.shouhu.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yindun.shouhu.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "设置",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 账户设置
            item {
                SettingsSection(title = "账户管理") {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "个人信息",
                        subtitle = "管理您的账户信息",
                        onClick = { /* TODO */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "修改密码",
                        subtitle = "修改登录密码",
                        onClick = { /* TODO */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.FamilyRestroom,
                        title = "家庭守护",
                        subtitle = "管理绑定的家庭成员",
                        onClick = { /* TODO */ }
                    )
                }
            }

            // 隐私设置
            item {
                SettingsSection(title = "隐私设置") {
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "数据加密",
                        subtitle = "管理数据加密设置",
                        onClick = { /* TODO */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.DeleteForever,
                        title = "删除数据",
                        subtitle = "删除所有本地存储的数据",
                        onClick = { /* TODO */ },
                        titleColor = Danger
                    )
                }
            }

            // 显示设置
            item {
                SettingsSection(title = "显示设置") {
                    SettingsItem(
                        icon = Icons.Default.TextIncrease,
                        title = "字体大小",
                        subtitle = "调整界面字体大小",
                        onClick = { /* TODO */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Brightness6,
                        title = "深色模式",
                        subtitle = "切换深色/浅色主题",
                        onClick = { /* TODO */ },
                        trailing = { DarkModeSwitch() }
                    )
                }
            }

            // 语音设置
            item {
                SettingsSection(title = "语音设置") {
                    SettingsItem(
                        icon = Icons.Default.RecordVoiceOver,
                        title = "方言设置",
                        subtitle = "选择语音交互的方言",
                        onClick = { /* TODO */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Speed,
                        title = "语速调整",
                        subtitle = "调整语音播放速度",
                        onClick = { /* TODO */ }
                    )
                }
            }

            // 通知设置
            item {
                SettingsSection(title = "通知设置") {
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "通知管理",
                        subtitle = "管理各类通知的开关",
                        onClick = { /* TODO */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.VolumeUp,
                        title = "语音提醒",
                        subtitle = "开启或关闭语音提醒",
                        onClick = { /* TODO */ }
                    )
                }
            }

            // 关于
            item {
                SettingsSection(title = "关于") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "关于银盾守护",
                        subtitle = "版本 1.0.0",
                        onClick = { /* TODO */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Description,
                        title = "用户协议",
                        onClick = { /* TODO */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.PrivacyTip,
                        title = "隐私政策",
                        onClick = { /* TODO */ }
                    )
                }
            }

            // 退出登录
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { /* TODO: 退出登录 */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Danger
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("退出登录")
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card {
            Column {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = TextPrimary,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = titleColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextHint
                )
            }
        }
    }
}

@Composable
fun DarkModeSwitch() {
    var isDarkMode by remember { mutableStateOf(false) }

    Switch(
        checked = isDarkMode,
        onCheckedChange = { isDarkMode = it }
    )
}
