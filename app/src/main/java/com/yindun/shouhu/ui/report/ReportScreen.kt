package com.yindun.shouhu.ui.report

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yindun.shouhu.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "举报中心",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ReportSubmitted,
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
            // 紧急求助
            item {
                EmergencyCard()
            }

            // 提交报案
            item {
                SubmitReportCard()
            }

            // 我的案件
            item {
                Text(
                    text = "我的案件",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(3) { index ->
                CaseCard(
                    caseNumber = "YD2024${String.format("%06d", index + 1)}",
                    caseType = listOf("保健品诈骗", "投资诈骗", "冒充公检法")[index],
                    status = listOf("processing", "submitted", "resolved")[index],
                    time = "${index + 1}天前"
                )
            }

            // 数据贡献
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ContributeDataCard()
            }
        }
    }
}

@Composable
fun EmergencyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Danger
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "紧急求助",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "如遇紧急诈骗情况，立即拨打反诈热线",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* TODO: 拨打96110 */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Danger
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "一键拨打 96110",
                    color = Danger,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitReportCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* TODO: 提交报案 */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EditNote,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = Primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "我要报案",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "填写诈骗信息，生成报案材料",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseCard(
    caseNumber: String,
    caseType: String,
    status: String,
    time: String
) {
    val (statusText, statusColor) = when (status) {
        "submitted" -> "已提交" to Primary
        "processing" -> "处理中" to Warning
        "resolved" -> "已解决" to Safe
        else -> "未知" to TextHint
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* TODO: 查看案件详情 */ }
    ) {
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
                    text = caseNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = caseType,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "提交于 $time",
                style = MaterialTheme.typography.bodySmall,
                color = TextHint
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributeDataCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* TODO: 贡献诈骗数据 */ },
        colors = CardDefaults.cardColors(
            containerColor = Secondary.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.VolunteerActivism,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Secondary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "贡献诈骗数据",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "匿名提交您遇到的诈骗经历，帮助他人避坑",
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
