package com.yindun.shouhu.ui.family

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
fun FamilyGuardScreen(
    onBack: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)

    // 模拟家庭成员数据
    val familyMembers = listOf(
        FamilyMember(
            id = 1,
            name = "张父",
            relationship = "父亲",
            phoneNumber = "138****1234",
            isProtected = true,
            recentAlerts = 2
        ),
        FamilyMember(
            id = 2,
            name = "张母",
            relationship = "母亲",
            phoneNumber = "139****5678",
            isProtected = true,
            recentAlerts = 0
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("家庭守护") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Secondary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* 添加家庭成员 */ },
                containerColor = Secondary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "添加家庭成员")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 说明卡片
            item {
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Secondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "绑定家庭成员后，您可以远程守护他们的安全，及时收到风险提醒。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 家庭成员列表
            item {
                Text(
                    text = "家庭成员",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(familyMembers) { member ->
                FamilyMemberCard(
                    member = member,
                    onViewCases = { /* 查看案件 */ },
                    onSubmitCase = { /* 代为报案 */ },
                    onManageAlerts = { /* 管理提醒 */ }
                )
            }

            // 快捷操作
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "快捷操作",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                QuickActionsCard(
                    onSubmitForFamily = { /* 为家属报案 */ },
                    onViewAllCases = { /* 查看所有案件 */ },
                    onManageWhitelist = { /* 管理白名单 */ }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMemberCard(
    member: FamilyMember,
    onViewCases: () -> Unit,
    onSubmitCase: () -> Unit,
    onManageAlerts: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 头像
                Surface(
                    modifier = Modifier.size(56.dp),
                    color = Secondary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 信息
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (member.isProtected) Safe.copy(alpha = 0.1f) else TextHint.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = if (member.isProtected) "守护中" else "未绑定",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (member.isProtected) Safe else TextHint
                            )
                        }
                    }
                    Text(
                        text = member.relationship,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = member.phoneNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextHint
                    )
                }

                // 风险提醒数
                if (member.recentAlerts > 0) {
                    Surface(
                        color = Danger.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Danger
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${member.recentAlerts}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Danger
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewCases,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Cases, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("案件")
                }

                Button(
                    onClick = onSubmitCase,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Secondary
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("代为报案")
                }

                OutlinedButton(
                    onClick = onManageAlerts,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("提醒")
                }
            }
        }
    }
}

@Composable
fun QuickActionsCard(
    onSubmitForFamily: () -> Unit,
    onViewAllCases: () -> Unit,
    onManageWhitelist: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            QuickActionItem(
                icon = Icons.Default.Edit,
                title = "为家属报案",
                subtitle = "代为提交诈骗线索",
                onClick = onSubmitForFamily
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            QuickActionItem(
                icon = Icons.Default.Cases,
                title = "查看所有案件",
                subtitle = "管理家庭成员的案件",
                onClick = onViewAllCases
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            QuickActionItem(
                icon = Icons.Default.Phone,
                title = "管理白名单",
                subtitle = "设置信任号码",
                onClick = onManageWhitelist
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Secondary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
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

data class FamilyMember(
    val id: Long,
    val name: String,
    val relationship: String,
    val phoneNumber: String,
    val isProtected: Boolean,
    val recentAlerts: Int
)
