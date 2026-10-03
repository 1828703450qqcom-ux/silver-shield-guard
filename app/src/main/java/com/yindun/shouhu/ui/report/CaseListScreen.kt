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
import androidx.compose.ui.unit.dp
import com.yindun.shouhu.data.local.entity.CaseEntity
import com.yindun.shouhu.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseListScreen(
    isChildView: Boolean = false,
    onViewCase: (Long) -> Unit,
    onSubmitCase: () -> Unit,
    onBack: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)

    // 模拟数据
    val cases = listOf(
        CaseEntity(
            id = 1,
            caseNumber = "YD20240830001",
            reporterId = 1,
            reporterType = "elderly",
            caseType = "冒充公检法",
            amount = 50000.0,
            suspectInfo = "{}",
            description = "接到冒充公安局电话",
            status = "processing",
            createdAt = System.currentTimeMillis() - 86400000,
            updatedAt = System.currentTimeMillis()
        ),
        CaseEntity(
            id = 2,
            caseNumber = "YD20240828002",
            reporterId = 1,
            reporterType = "elderly",
            caseType = "保健品诈骗",
            amount = 8000.0,
            suspectInfo = "{}",
            description = "免费体检推销保健品",
            status = "submitted",
            createdAt = System.currentTimeMillis() - 172800000,
            updatedAt = System.currentTimeMillis() - 172800000
        ),
        CaseEntity(
            id = 3,
            caseNumber = "YD20240825003",
            reporterId = 1,
            reporterType = "child",
            caseType = "投资理财诈骗",
            amount = 100000.0,
            suspectInfo = "{}",
            description = "高收益投资骗局",
            status = "resolved",
            createdAt = System.currentTimeMillis() - 432000000,
            updatedAt = System.currentTimeMillis() - 259200000
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isChildView) "家属案件" else "我的案件") },
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
                onClick = onSubmitCase,
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "提交新案件")
            }
        }
    ) { paddingValues ->
        if (cases.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = TextHint.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无案件记录",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextHint
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onSubmitCase) {
                        Text("提交新案件")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 统计卡片
                item {
                    CaseStatsCard(cases = cases)
                }

                // 案件列表
                items(cases) { case ->
                    CaseCard(
                        case = case,
                        dateFormat = dateFormat,
                        onClick = { onViewCase(case.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CaseStatsCard(cases: List<CaseEntity>) {
    val submittedCount = cases.count { it.status == "submitted" }
    val processingCount = cases.count { it.status == "processing" }
    val resolvedCount = cases.count { it.status == "resolved" }

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
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(count = cases.size, label = "总案件", color = Primary)
            StatItem(count = submittedCount, label = "已提交", color = Primary)
            StatItem(count = processingCount, label = "处理中", color = Warning)
            StatItem(count = resolvedCount, label = "已解决", color = Safe)
        }
    }
}

@Composable
fun StatItem(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
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
fun CaseCard(
    case: CaseEntity,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    val statusColor = when (case.status) {
        "submitted" -> Primary
        "processing" -> Warning
        "resolved" -> Safe
        else -> TextHint
    }

    val statusText = when (case.status) {
        "submitted" -> "已提交"
        "processing" -> "处理中"
        "resolved" -> "已解决"
        else -> "未知"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
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
                    text = case.caseNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = case.caseType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "¥${case.amount}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "提交于 ${dateFormat.format(Date(case.createdAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = TextHint
            )

            if (case.reporterType == "child") {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Secondary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "守护人提交",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Secondary
                    )
                }
            }
        }
    }
}
