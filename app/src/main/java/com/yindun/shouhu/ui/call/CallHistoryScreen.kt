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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yindun.shouhu.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("all") }

    val callHistory = listOf(
        CallHistoryItem(
            phoneNumber = "138****1234",
            contactName = "张三",
            time = System.currentTimeMillis() - 3600000,
            duration = 120,
            riskLevel = 0,
            action = "normal",
            isIncoming = true
        ),
        CallHistoryItem(
            phoneNumber = "170****5678",
            contactName = null,
            time = System.currentTimeMillis() - 7200000,
            duration = 0,
            riskLevel = 8,
            action = "blocked",
            isIncoming = true
        ),
        CallHistoryItem(
            phoneNumber = "150****9012",
            contactName = null,
            time = System.currentTimeMillis() - 86400000,
            duration = 45,
            riskLevel = 5,
            action = "warned",
            isIncoming = true
        ),
        CallHistoryItem(
            phoneNumber = "139****3456",
            contactName = "李四",
            time = System.currentTimeMillis() - 172800000,
            duration = 300,
            riskLevel = 0,
            action = "normal",
            isIncoming = false
        ),
        CallHistoryItem(
            phoneNumber = "131****7890",
            contactName = null,
            time = System.currentTimeMillis() - 259200000,
            duration = 0,
            riskLevel = 9,
            action = "blocked",
            isIncoming = true
        )
    )

    val filters = listOf(
        "all" to "全部",
        "high_risk" to "高风险",
        "blocked" to "已拦截",
        "normal" to "正常"
    )

    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("通话记录") },
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
        ) {
            // 筛选标签
            ScrollableTabRow(
                selectedTabIndex = filters.indexOfFirst { it.first == selectedFilter },
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                filters.forEach { (key, name) ->
                    Tab(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        text = { Text(name) }
                    )
                }
            }

            // 统计信息
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                    StatItem(
                        value = "${callHistory.size}",
                        label = "总通话",
                        color = Primary
                    )
                    StatItem(
                        value = "${callHistory.count { it.riskLevel >= 7 }}",
                        label = "高风险",
                        color = Danger
                    )
                    StatItem(
                        value = "${callHistory.count { it.action == "blocked" }}",
                        label = "已拦截",
                        color = Warning
                    )
                }
            }

            // 通话列表
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filteredList = when (selectedFilter) {
                    "high_risk" -> callHistory.filter { it.riskLevel >= 7 }
                    "blocked" -> callHistory.filter { it.action == "blocked" }
                    "normal" -> callHistory.filter { it.riskLevel < 4 }
                    else -> callHistory
                }

                items(filteredList) { item ->
                    CallHistoryItemCard(
                        item = item,
                        dateFormat = dateFormat
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String, color: Color) {
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
fun CallHistoryItemCard(
    item: CallHistoryItem,
    dateFormat: SimpleDateFormat
) {
    val riskColor = when {
        item.riskLevel >= 8 -> Danger
        item.riskLevel >= 5 -> Warning
        item.riskLevel >= 3 -> Primary
        else -> Safe
    }

    val actionIcon = when (item.action) {
        "blocked" -> Icons.Default.Block
        "warned" -> Icons.Default.Warning
        else -> if (item.isIncoming) Icons.Default.CallReceived else Icons.Default.CallMade
    }

    val actionColor = when (item.action) {
        "blocked" -> Danger
        "warned" -> Warning
        else -> Safe
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* 查看详情 */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 通话图标
            Surface(
                modifier = Modifier.size(48.dp),
                color = riskColor.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        tint = actionColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 通话信息
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.contactName ?: item.phoneNumber,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (item.contactName != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.phoneNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextHint
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateFormat.format(Date(item.time)),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    if (item.duration > 0) {
                        Text(
                            text = " · ${item.duration}秒",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 风险状态
            Column(horizontalAlignment = Alignment.End) {
                if (item.riskLevel > 0) {
                    Surface(
                        color = riskColor.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "风险:${item.riskLevel}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = riskColor
                        )
                    }
                }

                if (item.action == "blocked") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "已拦截",
                        style = MaterialTheme.typography.bodySmall,
                        color = Danger,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class CallHistoryItem(
    val phoneNumber: String,
    val contactName: String?,
    val time: Long,
    val duration: Int,
    val riskLevel: Int,
    val action: String,
    val isIncoming: Boolean
)
