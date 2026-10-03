package com.yindun.shouhu.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yindun.shouhu.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit
) {
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var isRiskAlertEnabled by remember { mutableStateOf(true) }
    var isCaseProgressEnabled by remember { mutableStateOf(true) }
    var isTrainingReminderEnabled by remember { mutableStateOf(true) }
    var isVoiceAlertEnabled by remember { mutableStateOf(true) }
    var isVibrationEnabled by remember { mutableStateOf(true) }
    var speechRate by remember { mutableFloatStateOf(1.0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("通知设置") },
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
            // 总开关
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    SettingsSwitchItem(
                        icon = Icons.Default.Notifications,
                        label = "启用通知",
                        subtitle = "开启后可接收各类提醒通知",
                        checked = isNotificationsEnabled,
                        onCheckedChange = { isNotificationsEnabled = it }
                    )
                }
            }

            // 通知类型
            item {
                Text(
                    text = "通知类型",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsSwitchItem(
                            icon = Icons.Default.Warning,
                            label = "风险警报",
                            subtitle = "检测到诈骗电话或异常交易时提醒",
                            checked = isRiskAlertEnabled && isNotificationsEnabled,
                            onCheckedChange = { isRiskAlertEnabled = it }
                        )

                        Divider()

                        SettingsSwitchItem(
                            icon = Icons.Default.Cases,
                            label = "案件进度",
                            subtitle = "案件状态更新时提醒",
                            checked = isCaseProgressEnabled && isNotificationsEnabled,
                            onCheckedChange = { isCaseProgressEnabled = it }
                        )

                        Divider()

                        SettingsSwitchItem(
                            icon = Icons.Default.School,
                            label = "训练提醒",
                            subtitle = "提醒您进行防骗训练",
                            checked = isTrainingReminderEnabled && isNotificationsEnabled,
                            onCheckedChange = { isTrainingReminderEnabled = it }
                        )
                    }
                }
            }

            // 语音设置
            item {
                Text(
                    text = "语音设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsSwitchItem(
                            icon = Icons.Default.VolumeUp,
                            label = "语音提醒",
                            subtitle = "重要提醒使用语音播报",
                            checked = isVoiceAlertEnabled,
                            onCheckedChange = { isVoiceAlertEnabled = it }
                        )

                        Divider()

                        // 语速调节
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "语速调节",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "%.1fx".format(speechRate),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Primary
                                )
                            }

                            Slider(
                                value = speechRate,
                                onValueChange = { speechRate = it },
                                valueRange = 0.5f..2.0f,
                                steps = 5,
                                colors = SliderDefaults.colors(
                                    thumbColor = Primary,
                                    activeTrackColor = Primary
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("慢", style = MaterialTheme.typography.bodySmall, color = TextHint)
                                Text("正常", style = MaterialTheme.typography.bodySmall, color = TextHint)
                                Text("快", style = MaterialTheme.typography.bodySmall, color = TextHint)
                            }
                        }
                    }
                }
            }

            // 提醒方式
            item {
                Text(
                    text = "提醒方式",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsSwitchItem(
                            icon = Icons.Default.Vibration,
                            label = "震动提醒",
                            subtitle = "收到通知时震动",
                            checked = isVibrationEnabled,
                            onCheckedChange = { isVibrationEnabled = it }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.RingVolume,
                            label = "通知铃声",
                            subtitle = "选择通知铃声",
                            onClick = { /* 选择铃声 */ }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.NotificationsActive,
                            label = "免打扰模式",
                            subtitle = "设置免打扰时间段",
                            onClick = { /* 免打扰设置 */ }
                        )
                    }
                }
            }

            // 测试
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { /* 发送测试通知 */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("发送测试通知")
                }
            }
        }
    }
}
