package com.yindun.shouhu.ui.training

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsDetailScreen(
    onBack: () -> Unit,
    onShopClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)

    val pointsHistory = listOf(
        PointsRecord(1, "训练奖励", 0.2, "完成'免费体检诈骗'训练", System.currentTimeMillis() - 3600000),
        PointsRecord(2, "训练奖励", 0.15, "完成'高额回报诈骗'训练", System.currentTimeMillis() - 86400000),
        PointsRecord(3, "积分兑换", -50.0, "兑换话费充值", System.currentTimeMillis() - 172800000),
        PointsRecord(4, "训练奖励", 0.2, "完成'冒充警察诈骗'训练", System.currentTimeMillis() - 259200000),
        PointsRecord(5, "新用户奖励", 10.0, "注册赠送", System.currentTimeMillis() - 432000000)
    )

    val totalEarned = pointsHistory.filter { it.amount > 0 }.sumOf { it.amount }
    val totalSpent = pointsHistory.filter { it.amount < 0 }.sumOf { Math.abs(it.amount) }
    val balance = pointsHistory.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("积分明细") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PointsGold
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 积分概览
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = PointsGold.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "当前积分",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                        Text(
                            text = "%.1f".format(balance),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = PointsGold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "累计获得",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "+%.1f".format(totalEarned),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Safe
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "累计消费",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "-%.1f".format(totalSpent),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Danger
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onShopClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PointsGold
                            )
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("积分商城")
                        }
                    }
                }
            }

            // 积分获取方式说明
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "如何获取积分",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        PointsMethodItem(
                            icon = Icons.Default.School,
                            title = "完成防骗训练",
                            description = "每次0.05-0.2元",
                            color = Secondary
                        )
                        PointsMethodItem(
                            icon = Icons.Default.Campaign,
                            title = "举报诈骗电话",
                            description = "每次0.1元",
                            color = Primary
                        )
                        PointsMethodItem(
                            icon = Icons.Default.People,
                            title = "邀请好友注册",
                            description = "每次1元",
                            color = Warning
                        )
                    }
                }
            }

            // 积分记录标题
            item {
                Text(
                    text = "积分记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // 积分记录列表
            items(pointsHistory) { record ->
                PointsRecordCard(record = record, dateFormat = dateFormat)
            }
        }
    }
}

@Composable
fun PointsMethodItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = Safe
        )
    }
}

@Composable
fun PointsRecordCard(
    record: PointsRecord,
    dateFormat: SimpleDateFormat
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (record.amount > 0) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                contentDescription = null,
                tint = if (record.amount > 0) Safe else Danger,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.type,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = record.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = dateFormat.format(Date(record.time)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint
                )
            }

            Text(
                text = if (record.amount > 0) "+%.1f".format(record.amount) else "%.1f".format(record.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (record.amount > 0) Safe else Danger
            )
        }
    }
}

data class PointsRecord(
    val id: Long,
    val type: String,
    val amount: Double,
    val description: String,
    val time: Long
)
