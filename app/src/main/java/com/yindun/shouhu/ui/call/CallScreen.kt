package com.yindun.shouhu.ui.call

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yindun.shouhu.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var isMonitoringEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "来电防护",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CallIncoming,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 监控状态卡片
            item {
                MonitoringStatusCard(
                    isEnabled = isMonitoringEnabled,
                    onToggle = { isMonitoringEnabled = it }
                )
            }

            // 风险等级说明
            item {
                RiskLevelInfoCard()
            }

            // 白名单管理
            item {
                WhitelistCard(
                    onManageClick = { navController.navigate("call/whitelist") }
                )
            }

            // 最近来电记录
            item {
                Text(
                    text = "最近来电",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(5) { index ->
                CallRecordCard(
                    phoneNumber = "138****${8000 + index}",
                    time = "${index + 1}小时前",
                    riskLevel = listOf(0, 3, 7, 1, 9)[index],
                    action = listOf("正常", "提醒", "阻止", "正常", "阻止")[index]
                )
            }
        }
    }
}

@Composable
fun MonitoringStatusCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) Safe.copy(alpha = 0.1f) else TextHint.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = if (isEnabled) Safe else TextHint
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEnabled) "防护已开启" else "防护已关闭",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) Safe else TextHint
                )
                Text(
                    text = if (isEnabled) "正在监听陌生来电" else "开启后可识别诈骗电话",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Safe
                )
            )
        }
    }
}

@Composable
fun RiskLevelInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "风险等级说明",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            RiskLevelItem(
                level = "低风险",
                description = "弹窗提醒'疑似推销电话'",
                color = Safe,
                icon = Icons.Default.Info
            )

            Spacer(modifier = Modifier.height(8.dp))

            RiskLevelItem(
                level = "中风险",
                description = "语音提醒'请注意防骗'",
                color = Warning,
                icon = Icons.Default.Warning
            )

            Spacer(modifier = Modifier.height(8.dp))

            RiskLevelItem(
                level = "高风险",
                description = "强制中断电话，语音播报'疑似诈骗电话'",
                color = Danger,
                icon = Icons.Default.Error
            )
        }
    }
}

@Composable
fun RiskLevelItem(
    level: String,
    description: String,
    color: Color,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = level,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun WhitelistCard(
    onManageClick: () -> Unit
) {
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
                imageVector = Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "白名单管理",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "设置亲属、社区电话，避免误拦截",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            OutlinedButton(onClick = onManageClick) {
                Text("管理")
            }
        }
    }
}

@Composable
fun CallRecordCard(
    phoneNumber: String,
    time: String,
    riskLevel: Int,
    action: String
) {
    val (riskColor, riskText) = when {
        riskLevel >= 7 -> Danger to "高风险"
        riskLevel >= 4 -> Warning to "中风险"
        riskLevel > 0 -> Safe to "低风险"
        else -> TextHint to "安全"
    }

    val actionIcon = when (action) {
        "提醒" -> Icons.Default.Notifications
        "阻止" -> Icons.Default.Block
        else -> Icons.Default.CheckCircle
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = riskColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = phoneNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = riskText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = riskColor
                )
                Icon(
                    imageVector = actionIcon,
                    contentDescription = action,
                    tint = riskColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
