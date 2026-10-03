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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yindun.shouhu.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountListScreen(
    onAddAccount: () -> Unit,
    onAccountClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val accounts = listOf(
        AccountItem(1, "中国工商银行", "**** **** **** 1234", "bank_card", 125680.50),
        AccountItem(2, "微信钱包", "138****1234", "wechat", 3250.80),
        AccountItem(3, "支付宝", "138****1234", "alipay", 8960.25)
    )

    val totalBalance = accounts.sumOf { it.balance }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的账户") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAccount,
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加账户")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 总资产卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Primary
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "总资产",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "¥%.2f".format(totalBalance),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "共 ${accounts.size} 个账户",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // 账户列表标题
            item {
                Text(
                    text = "账户列表",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // 账户列表
            items(accounts) { account ->
                AccountCard(
                    account = account,
                    onClick = { onAccountClick(account.id) }
                )
            }

            // 添加账户提示
            item {
                OutlinedButton(
                    onClick = onAddAccount,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加新账户")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountCard(
    account: AccountItem,
    onClick: () -> Unit
) {
    val (icon, color) = when (account.type) {
        "bank_card" -> Icons.Default.CreditCard to Primary
        "wechat" -> Icons.Default.Chat to Color(0xFF07C160)
        "alipay" -> Icons.Default.AccountBalanceWallet to Color(0xFF1677FF)
        else -> Icons.Default.AccountBalance to Primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Surface(
                modifier = Modifier.size(48.dp),
                color = color.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 账户信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.bankName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = account.cardNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // 余额
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "余额",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "¥%.2f".format(account.balance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
    }
}

data class AccountItem(
    val id: Long,
    val bankName: String,
    val cardNumber: String,
    val type: String,
    val balance: Double
)
