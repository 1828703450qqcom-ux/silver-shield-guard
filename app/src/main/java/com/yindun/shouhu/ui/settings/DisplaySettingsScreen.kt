package com.yindun.shouhu.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yindun.shouhu.domain.usecase.DialectOption
import com.yindun.shouhu.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplaySettingsScreen(
    onBack: () -> Unit
) {
    var fontSize by remember { mutableIntStateOf(2) } // 0:小 1:正常 2:大 3:特大
    var isDarkMode by remember { mutableStateOf(false) }
    var isElderlyMode by remember { mutableStateOf(true) }

    val fontSizes = listOf(
        0 to "小（85%）",
        1 to "正常（100%）",
        2 to "大（120%）",
        3 to "特大（150%）"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("显示设置") },
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
            // 字体大小
            item {
                Text(
                    text = "字体大小",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // 预览文本
                        Text(
                            text = "预览文字 Abcdefg 12345",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // 字体大小选择
                        fontSizes.forEach { (size, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = fontSize == size,
                                    onClick = { fontSize = size },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Primary
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }

            // 显示模式
            item {
                Text(
                    text = "显示模式",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsSwitchItem(
                            icon = Icons.Default.DarkMode,
                            label = "深色模式",
                            subtitle = "使用深色背景，减少眼睛疲劳",
                            checked = isDarkMode,
                            onCheckedChange = { isDarkMode = it }
                        )

                        Divider()

                        SettingsSwitchItem(
                            icon = Icons.Default.AccessibilityNew,
                            label = "适老化模式",
                            subtitle = "启用大字体、高对比度等适老化特性",
                            checked = isElderlyMode,
                            onCheckedChange = { isElderlyMode = it }
                        )
                    }
                }
            }

            // 其他显示设置
            item {
                Text(
                    text = "其他设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsClickableItem(
                            icon = Icons.Default.Brightness6,
                            label = "屏幕亮度",
                            subtitle = "调整应用内屏幕亮度",
                            onClick = { /* 调整亮度 */ }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.Animation,
                            label = "动画效果",
                            subtitle = "开启或关闭界面动画",
                            onClick = { /* 动画设置 */ }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.Vibration,
                            label = "触感反馈",
                            subtitle = "操作时的震动反馈",
                            onClick = { /* 触感设置 */ }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegacyDialectSettingsScreen(
    onBack: () -> Unit
) {
    var selectedDialect by remember { mutableStateOf("mandarin") }

    val dialects = listOf(
        DialectOption("mandarin", "普通话", "zh-CN"),
        DialectOption("cantonese", "粤语", "zh-HK"),
        DialectOption("shanghainese", "上海话", "zh-SH"),
        DialectOption("sichuanese", "四川话", "zh-SC"),
        DialectOption("henanese", "河南话", "zh-HN"),
        DialectOption("shandong", "山东话", "zh-SD"),
        DialectOption("hubei", "湖北话", "zh-HB"),
        DialectOption("hunan", "湖南话", "zh-HN"),
        DialectOption("fujian", "福建话", "zh-FJ"),
        DialectOption("anhui", "安徽话", "zh-AH"),
        DialectOption("jiangsu", "江苏话", "zh-JS"),
        DialectOption("zhejiang", "浙江话", "zh-ZJ"),
        DialectOption("guangxi", "广西话", "zh-GX"),
        DialectOption("yunnan", "云南话", "zh-YN"),
        DialectOption("guizhou", "贵州话", "zh-GZ"),
        DialectOption("xinjiang", "新疆话", "zh-XJ")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("方言设置") },
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 说明
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Primary.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "选择语音交互使用的方言，语音能力取决于设备和系统服务。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 方言列表
            items(dialects) { dialect ->
                Card(
                    onClick = { selectedDialect = dialect.code },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedDialect == dialect.code) {
                            Primary.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDialect == dialect.code,
                            onClick = { selectedDialect = dialect.code },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Primary
                            )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dialect.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedDialect == dialect.code) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                            Text(
                                text = dialect.locale,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        if (selectedDialect == dialect.code) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Primary
                            )
                        }
                    }
                }
            }

            // 测试按钮
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* 测试语音 */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("测试当前方言")
                }
            }
        }
    }
}
