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
import com.yindun.shouhu.data.local.entity.CaseProgressEntity
import com.yindun.shouhu.data.local.entity.EvidenceEntity
import com.yindun.shouhu.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseDetailScreen(
    caseId: Long,
    onBack: () -> Unit
) {
    // 模拟数据
    val case = CaseEntity(
        id = caseId,
        caseNumber = "YD20240830001",
        reporterId = 1,
        reporterType = "elderly",
        caseType = "冒充公检法",
        amount = 50000.0,
        suspectInfo = "{\"phone\":\"138****8888\",\"info\":\"冒充公安局\"}",
        description = "接到自称是XX公安局的电话，称我涉嫌洗钱案件，需要将资金转到安全账户配合调查。当时很害怕，就按照对方要求转了50000元。",
        status = "processing",
        createdAt = System.currentTimeMillis() - 86400000,
        updatedAt = System.currentTimeMillis()
    )

    val progressList = listOf(
        CaseProgressEntity(
            id = 1,
            caseId = caseId,
            status = "submitted",
            description = "案件已提交，等待审核",
            operator = "系统",
            timestamp = System.currentTimeMillis() - 86400000
        ),
        CaseProgressEntity(
            id = 2,
            caseId = caseId,
            status = "reviewing",
            description = "案件已分配至辖区派出所，正在审核中",
            operator = "系统",
            timestamp = System.currentTimeMillis() - 43200000
        ),
        CaseProgressEntity(
            id = 3,
            caseId = caseId,
            status = "processing",
            description = "民警已受理案件，将联系您了解详细情况",
            operator = "张警官",
            timestamp = System.currentTimeMillis()
        )
    )

    val evidenceList = listOf(
        EvidenceEntity(
            id = 1,
            caseId = caseId,
            evidenceType = "通话录音",
            filePath = "/storage/emulated/0/recordings/call_20240830.mp3",
            fileHash = "abc123",
            uploadedAt = System.currentTimeMillis() - 86400000
        ),
        EvidenceEntity(
            id = 2,
            caseId = caseId,
            evidenceType = "转账记录",
            filePath = "/storage/emulated/0/screenshots/transfer.png",
            fileHash = "def456",
            uploadedAt = System.currentTimeMillis() - 86400000
        )
    )

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("案件详情") },
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
            // 案件基本信息
            item {
                CaseInfoCard(case = case, dateFormat = dateFormat)
            }

            // 案件状态
            item {
                CaseStatusCard(status = case.status)
            }

            // 案件描述
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "案件描述",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = case.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 证据材料
            item {
                Text(
                    text = "证据材料",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(evidenceList) { evidence ->
                EvidenceCard(evidence = evidence, dateFormat = dateFormat)
            }

            // 案件进度
            item {
                Text(
                    text = "案件进度",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(progressList) { progress ->
                ProgressItem(progress = progress, dateFormat = dateFormat)
            }

            // 操作按钮
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* 添加证据 */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("补充证据")
                    }

                    Button(
                        onClick = { /* 拨打96110 */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Danger
                        )
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("拨打96110")
                    }
                }
            }
        }
    }
}

@Composable
fun CaseInfoCard(
    case: CaseEntity,
    dateFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Primary.copy(alpha = 0.1f)
        )
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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )

                Surface(
                    color = when (case.status) {
                        "submitted" -> Primary.copy(alpha = 0.2f)
                        "processing" -> Warning.copy(alpha = 0.2f)
                        "resolved" -> Safe.copy(alpha = 0.2f)
                        else -> TextHint.copy(alpha = 0.2f)
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = when (case.status) {
                            "submitted" -> "已提交"
                            "processing" -> "处理中"
                            "resolved" -> "已解决"
                            else -> "未知"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = when (case.status) {
                            "submitted" -> Primary
                            "processing" -> Warning
                            "resolved" -> Safe
                            else -> TextHint
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "诈骗类型", value = case.caseType)
            InfoRow(label = "涉案金额", value = "¥${case.amount}")
            InfoRow(label = "提交时间", value = dateFormat.format(Date(case.createdAt)))
        }
    }
}

@Composable
fun CaseStatusCard(status: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatusStep(
                title = "已提交",
                isActive = true,
                isCompleted = status != "submitted"
            )
            StatusStep(
                title = "审核中",
                isActive = status == "reviewing",
                isCompleted = status == "processing" || status == "resolved"
            )
            StatusStep(
                title = "处理中",
                isActive = status == "processing",
                isCompleted = status == "resolved"
            )
            StatusStep(
                title = "已解决",
                isActive = status == "resolved",
                isCompleted = false
            )
        }
    }
}

@Composable
fun StatusStep(
    title: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(32.dp),
            color = when {
                isCompleted -> Safe
                isActive -> Primary
                else -> TextHint.copy(alpha = 0.3f)
            },
            shape = MaterialTheme.shapes.small
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = if (isActive) "●" else "○",
                        color = if (isActive) Color.White else TextHint,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive || isCompleted) Primary else TextHint
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label：",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EvidenceCard(
    evidence: EvidenceEntity,
    dateFormat: SimpleDateFormat
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (evidence.evidenceType) {
                    "通话录音" -> Icons.Default.Mic
                    "转账记录" -> Icons.Default.Receipt
                    "聊天截图" -> Icons.Default.Chat
                    else -> Icons.Default.InsertDriveFile
                },
                contentDescription = null,
                tint = Primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evidence.evidenceType,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = evidence.filePath.substringAfterLast("/"),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = dateFormat.format(Date(evidence.uploadedAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint
                )
            }
        }
    }
}

@Composable
fun ProgressItem(
    progress: CaseProgressEntity,
    dateFormat: SimpleDateFormat
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // 时间线
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(12.dp),
                color = Primary,
                shape = MaterialTheme.shapes.small
            ) {}
            Surface(
                modifier = Modifier
                    .width(2.dp)
                    .height(40.dp),
                color = Primary.copy(alpha = 0.3f)
            ) {}
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 内容
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = progress.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${progress.operator} · ${dateFormat.format(Date(progress.timestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = TextHint
            )
        }
    }
}
