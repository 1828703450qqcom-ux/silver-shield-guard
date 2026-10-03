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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yindun.shouhu.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI防骗训练",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Secondary,
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
            // 积分卡片
            item {
                PointsCard(
                    points = 150.5,
                    completedCount = 12
                )
            }

            // 开始训练按钮
            item {
                StartTrainingCard()
            }

            // 诈骗类型分类
            item {
                Text(
                    text = "选择训练场景",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // 诈骗类型列表
            val fraudTypes = listOf(
                FraudType("health_product", "保健品诈骗", Icons.Default.LocalHospital, "警惕免费体检、健康讲座"),
                FraudType("investment", "投资理财诈骗", Icons.Default.TrendingUp, "警惕高额回报承诺"),
                FraudType("impersonate", "冒充公检法", Icons.Default.Security, "警惕要求转账到'安全账户'"),
                FraudType("lottery", "中奖诈骗", Icons.Default.EmojiEvents, "警惕先交税后领奖"),
                FraudType("family_emergency", "冒充亲属紧急情况", Icons.Default.FamilyRestroom, "警惕要求立即汇款"),
                FraudType("customer_service", "冒充客服", Icons.Default.Headset, "警惕退款、理赔骗局")
            )

            items(fraudTypes) { type ->
                FraudTypeCard(
                    type = type,
                    onClick = { /* TODO: 开始训练 */ }
                )
            }

            // 训练记录
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "最近训练",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(3) { index ->
                TrainingRecordCard(
                    typeName = listOf("保健品诈骗", "投资理财诈骗", "冒充公检法")[index],
                    score = listOf(85, 92, 78)[index],
                    reward = 0.1,
                    time = "${index + 1}天前"
                )
            }
        }
    }
}

data class FraudType(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val description: String
)

@Composable
fun PointsCard(
    points: Double,
    completedCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = PointsGold.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = PointsGold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "安全积分",
                style = MaterialTheme.typography.titleLarge,
                color = TextSecondary
            )

            Text(
                text = "%.1f".format(points),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = PointsGold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "已完成 $completedCount 次训练",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { /* TODO: 查看积分详情 */ }
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("查看积分详情")
            }
        }
    }
}

@Composable
fun StartTrainingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Secondary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "开始今日训练",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "通过语音模拟真实诈骗场景，提升防骗意识",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { /* TODO: 开始随机训练 */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "开始训练",
                    color = Secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FraudTypeCard(
    type: FraudType,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = type.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = type.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextHint
            )
        }
    }
}

@Composable
fun TrainingRecordCard(
    typeName: String,
    score: Int,
    reward: Double,
    time: String
) {
    val scoreColor = when {
        score >= 90 -> Safe
        score >= 70 -> Warning
        else -> Danger
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${score}分",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
                Text(
                    text = "+%.1f积分".format(reward),
                    style = MaterialTheme.typography.bodySmall,
                    color = PointsGold
                )
            }
        }
    }
}
