package com.yindun.shouhu.ui.financial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    onBack: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

    // 模拟交易数据
    val transaction = TransactionDetail(
        id = transactionId,
        amount = -5000.00,
        counterparty = "张某某",
        description = "转账",
        time = System.currentTimeMillis() - 3600000,
        type = "transfer_out",
        status = "completed",
        riskLevel = 6,
        riskReason = "向陌生人大额转账",
        orderId = "TX20240830001234"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("交易详情") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 金额卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (transaction.amount < 0) Danger.copy(alpha = 0.1f) else Safe.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (transaction.amount < 0) "支出" else "收入",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "¥%.2f".format(Math.abs(transaction.amount)),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.amount < 0) Danger else Safe
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = when (transaction.status) {
                            "completed" -> Safe.copy(alpha = 0.2f)
                            "pending" -> Warning.copy(alpha = 0.2f)
                            else -> TextHint.copy(alpha = 0.2f)
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = when (transaction.status) {
                                "completed" -> "交易成功"
                                "pending" -> "处理中"
                                else -> "交易失败"
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = when (transaction.status) {
                                "completed" -> Safe
                                "pending" -> Warning
                                else -> TextHint
                            }
                        )
                    }
                }
            }

            // 风险提醒（如果有）
            if (transaction.riskLevel > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            transaction.riskLevel >= 7 -> Danger.copy(alpha = 0.1f)
                            transaction.riskLevel >= 4 -> Warning.copy(alpha = 0.1f)
                            else -> Primary.copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                transaction.riskLevel >= 7 -> Icons.Default.Error
                                transaction.riskLevel >= 4 -> Icons.Default.Warning
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = when {
                                transaction.riskLevel >= 7 -> Danger
                                transaction.riskLevel >= 4 -> Warning
                                else -> Primary
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "风险等级: ${transaction.riskLevel}/10",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    transaction.riskLevel >= 7 -> Danger
                                    transaction.riskLevel >= 4 -> Warning
                                    else -> Primary
                                }
                            )
                            Text(
                                text = transaction.riskReason,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // 交易信息
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "交易信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow(label = "交易类型", value = "转账")
                    DetailRow(label = "收款方", value = transaction.counterparty)
                    DetailRow(label = "交易时间", value = dateFormat.format(Date(transaction.time)))
                    DetailRow(label = "订单号", value = transaction.orderId)
                    if (transaction.description.isNotEmpty()) {
                        DetailRow(label = "备注", value = transaction.description)
                    }
                }
            }

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* 举报 */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Report, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("举报")
                }

                Button(
                    onClick = { /* 联系客服 */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    )
                ) {
                    Icon(Icons.Default.Headset, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("联系客服")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

data class TransactionDetail(
    val id: Long,
    val amount: Double,
    val counterparty: String,
    val description: String,
    val time: Long,
    val type: String,
    val status: String,
    val riskLevel: Int,
    val riskReason: String,
    val orderId: String
)
