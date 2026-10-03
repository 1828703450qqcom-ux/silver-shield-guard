package com.yindun.shouhu.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yindun.shouhu.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    onBack: () -> Unit
) {
    var isDataEncryptionEnabled by remember { mutableStateOf(true) }
    var isAutoSyncEnabled by remember { mutableStateOf(true) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("隐私设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 数据安全
            item {
                Text(
                    text = "数据安全",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsSwitchItem(
                            icon = Icons.Default.Lock,
                            label = "数据加密",
                            subtitle = "使用AES-256加密本地存储的数据",
                            checked = isDataEncryptionEnabled,
                            onCheckedChange = { isDataEncryptionEnabled = it }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.Key,
                            label = "管理加密密钥",
                            subtitle = "查看和管理数据加密密钥",
                            onClick = { /* 管理密钥 */ }
                        )
                    }
                }
            }

            // 数据同步
            item {
                Text(
                    text = "数据同步",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsSwitchItem(
                            icon = Icons.Default.Sync,
                            label = "自动同步",
                            subtitle = "自动同步诈骗话术库和风险数据",
                            checked = isAutoSyncEnabled,
                            onCheckedChange = { isAutoSyncEnabled = it }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.CloudUpload,
                            label = "立即同步",
                            subtitle = "手动触发数据同步",
                            onClick = { /* 立即同步 */ }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.CloudDownload,
                            label = "备份数据",
                            subtitle = "备份本地数据到云端",
                            onClick = { /* 备份数据 */ }
                        )
                    }
                }
            }

            // 隐私权限
            item {
                Text(
                    text = "隐私权限",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsClickableItem(
                            icon = Icons.Default.Phone,
                            label = "电话权限",
                            subtitle = "用于来电监听和风险检测",
                            onClick = { /* 管理电话权限 */ }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.Mic,
                            label = "麦克风权限",
                            subtitle = "用于语音交互和训练",
                            onClick = { /* 管理麦克风权限 */ }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.LocationOn,
                            label = "位置权限",
                            subtitle = "用于显示诈骗电话归属地",
                            onClick = { /* 管理位置权限 */ }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.Storage,
                            label = "存储权限",
                            subtitle = "用于保存证据和生成PDF",
                            onClick = { /* 管理存储权限 */ }
                        )
                    }
                }
            }

            // 数据使用
            item {
                Text(
                    text = "数据使用",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsClickableItem(
                            icon = Icons.Default.Assessment,
                            label = "数据使用统计",
                            subtitle = "查看各类数据的使用情况",
                            onClick = { /* 查看统计 */ }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.Description,
                            label = "数据收集说明",
                            subtitle = "了解我们如何收集和使用数据",
                            onClick = { /* 查看说明 */ }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.Delete,
                            label = "清除缓存",
                            subtitle = "清除临时文件缓存",
                            onClick = { /* 清除缓存 */ }
                        )
                    }
                }
            }

            // 数据删除
            item {
                Text(
                    text = "数据删除",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Danger
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Danger.copy(alpha = 0.05f)
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsClickableItem(
                            icon = Icons.Default.DeleteForever,
                            label = "删除所有本地数据",
                            subtitle = "删除所有本地存储的数据，此操作不可恢复",
                            titleColor = Danger,
                            onClick = { showClearDataDialog = true }
                        )
                    }
                }
            }

            // 隐私政策
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsClickableItem(
                            icon = Icons.Default.PrivacyTip,
                            label = "隐私政策",
                            subtitle = "查看完整的隐私政策",
                            onClick = { /* 查看隐私政策 */ }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.Description,
                            label = "用户协议",
                            subtitle = "查看用户服务协议",
                            onClick = { /* 查看用户协议 */ }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.Info,
                            label = "数据安全说明",
                            subtitle = "了解我们的数据安全措施",
                            onClick = { /* 查看安全说明 */ }
                        )
                    }
                }
            }
        }
    }

    // 清除数据确认对话框
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("确认删除") },
            text = {
                Text("此操作将删除所有本地存储的数据，包括：\n\n• 交易记录\n• 训练记录\n• 通话记录\n• 案件信息\n• 设置数据\n\n此操作不可恢复，您确定要继续吗？")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDataDialog = false
                        // 执行清除
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Danger
                    )
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String = "",
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
