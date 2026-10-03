package com.yindun.shouhu.ui.financial

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
fun FinancialScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "财务安全监控",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
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
            // 绑定账户卡片
            item {
                BindAccountCard()
            }

            // 账户列表
            item {
                Text(
                    text = "我的账户",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // 示例账户卡片
            items(1) { index ->
                AccountCard(
                    bankName = "示例银行",
                    accountNumber = "**** **** **** 1234",
                    balance = "12,345.67",
                    accountType = "银行卡"
                )
            }

            // 风险提醒区域
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "风险提醒",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                RiskAlertCard(
                    alertLevel = RiskLevel.LOW,
                    message = "近期有一笔大额转账，请确认是否为本人操作",
                    time = "2分钟前"
                )
            }

            item {
                RiskAlertCard(
                    alertLevel = RiskLevel.HIGH,
                    message = "检测到异常高频小额转账，疑似诈骗",
                    time = "10分钟前"
                )
            }
        }
    }
}

enum class RiskLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

@Composable
fun BindAccountCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Primary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "绑定账户",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "绑定银行卡或微信钱包，实时监控交易安全",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    text = "绑定银行卡",
                    icon = Icons.Default.CreditCard,
                    onClick = { /* TODO */ }
                )

                ActionButton(
                    text = "绑定微信",
                    icon = Icons.Default.AccountBalanceWallet,
                    onClick = { /* TODO */ }
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

@Composable
fun AccountCard(
    bankName: String,
    accountNumber: String,
    balance: String,
    accountType: String
) {
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
                imageVector = Icons.Default.AccountBalance,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bankName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = accountNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = accountType,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "余额",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = balance,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
    }
}

@Composable
fun RiskAlertCard(
    alertLevel: RiskLevel,
    message: String,
    time: String
) {
    val (backgroundColor, iconColor, icon) = when (alertLevel) {
        RiskLevel.LOW -> Triple(Safe.copy(alpha = 0.1f), Safe, Icons.Default.Info)
        RiskLevel.MEDIUM -> Triple(Warning.copy(alpha = 0.1f), Warning, Icons.Default.Warning)
        RiskLevel.HIGH -> Triple(Danger.copy(alpha = 0.1f), Danger, Icons.Default.Error)
        RiskLevel.CRITICAL -> Triple(RiskCritical.copy(alpha = 0.1f), RiskCritical, Icons.Default.ErrorOutline)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (alertLevel) {
                        RiskLevel.LOW -> "低风险提醒"
                        RiskLevel.MEDIUM -> "中风险提醒"
                        RiskLevel.HIGH -> "高风险警告"
                        RiskLevel.CRITICAL -> "紧急风险警报"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = TextHint
            )
        }
    }
}
