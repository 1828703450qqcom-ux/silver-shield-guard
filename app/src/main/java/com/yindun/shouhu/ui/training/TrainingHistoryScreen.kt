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
import com.yindun.shouhu.domain.engine.PerformanceLevel
import com.yindun.shouhu.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingHistoryScreen(
    onBack: () -> Unit
) {
    val historyItems = listOf(
        TrainingHistoryItem(
            id = 1,
            scriptName = "免费体检诈骗",
            category = "保健品诈骗",
            score = 85,
            reward = 0.15,
            completedAt = "2小时前"
        ),
        TrainingHistoryItem(
            id = 2,
            scriptName = "高额回报诈骗",
            category = "投资理财诈骗",
            score = 92,
            reward = 0.2,
            completedAt = "1天前"
        ),
        TrainingHistoryItem(
            id = 3,
            scriptName = "冒充警察诈骗",
            category = "冒充公检法",
            score = 78,
            reward = 0.15,
            completedAt = "2天前"
        ),
        TrainingHistoryItem(
            id = 4,
            scriptName = "中奖通知诈骗",
            category = "中奖诈骗",
            score = 65,
            reward = 0.1,
            completedAt = "3天前"
        ),
        TrainingHistoryItem(
            id = 5,
            scriptName = "冒充子女诈骗",
            category = "冒充亲属紧急情况",
            score = 95,
            reward = 0.2,
            completedAt = "4天前"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("训练记录") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Secondary
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
            // 统计卡片
            item {
                StatsCard(
                    totalSessions = historyItems.size,
                    totalRewards = historyItems.sumOf { it.reward },
                    averageScore = historyItems.map { it.score }.average().toInt()
                )
            }

            // 历史记录
            items(historyItems) { item ->
                HistoryItemCard(item = item)
            }
        }
    }
}

@Composable
fun StatsCard(
    totalSessions: Int,
    totalRewards: Double,
    averageScore: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Secondary.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = "$totalSessions",
                label = "训练次数",
                color = Secondary
            )

            Divider(
                modifier = Modifier
                    .height(48.dp)
                    .width(1.dp),
                color = TextHint.copy(alpha = 0.3f)
            )

            StatItem(
                value = "%.1f".format(totalRewards),
                label = "获得奖励(元)",
                color = PointsGold
            )

            Divider(
                modifier = Modifier
                    .height(48.dp)
                    .width(1.dp),
                color = TextHint.copy(alpha = 0.3f)
            )

            StatItem(
                value = "$averageScore",
                label = "平均分数",
                color = Primary
            )
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryItemCard(item: TrainingHistoryItem) {
    val scoreColor = when {
        item.score >= 90 -> Safe
        item.score >= 70 -> Secondary
        item.score >= 50 -> Warning
        else -> Danger
    }

    val performanceLevel = when {
        item.score >= 90 -> "优秀"
        item.score >= 70 -> "良好"
        item.score >= 50 -> "及格"
        else -> "需加强"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* 查看详情 */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 分数圆圈
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .padding(4.dp)
            ) {
                CircularProgressIndicator(
                    progress = item.score / 100f,
                    modifier = Modifier.fillMaxSize(),
                    color = scoreColor,
                    strokeWidth = 4.dp,
                    trackColor = scoreColor.copy(alpha = 0.2f)
                )
                Text(
                    text = "${item.score}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.scriptName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = item.completedAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint
                )
            }

            // 等级和奖励
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = scoreColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = performanceLevel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = scoreColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+%.2f元".format(item.reward),
                    style = MaterialTheme.typography.bodySmall,
                    color = PointsGold
                )
            }
        }
    }
}

data class TrainingHistoryItem(
    val id: Long,
    val scriptName: String,
    val category: String,
    val score: Int,
    val reward: Double,
    val completedAt: String
)
