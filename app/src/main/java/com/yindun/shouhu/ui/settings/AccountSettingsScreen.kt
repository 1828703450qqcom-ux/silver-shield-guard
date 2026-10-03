package com.yindun.shouhu.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("张三") }
    var phone by remember { mutableStateOf("138****1234") }
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人信息") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { isEditing = !isEditing }) {
                        Text(if (isEditing) "保存" else "编辑")
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
            // 头像
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            color = Primary.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // 基本信息
            item {
                Text(
                    text = "基本信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsInputItem(
                            icon = Icons.Default.Person,
                            label = "姓名",
                            value = name,
                            onValueChange = { name = it },
                            enabled = isEditing
                        )

                        Divider()

                        SettingsInputItem(
                            icon = Icons.Default.Phone,
                            label = "手机号",
                            value = phone,
                            onValueChange = { phone = it },
                            enabled = isEditing
                        )

                        Divider()

                        SettingsInfoItem(
                            icon = Icons.Default.Cake,
                            label = "年龄",
                            value = "72岁"
                        )

                        Divider()

                        SettingsInfoItem(
                            icon = Icons.Default.LocationOn,
                            label = "地址",
                            value = "未设置"
                        )
                    }
                }
            }

            // 账户安全
            item {
                Text(
                    text = "账户安全",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsClickableItem(
                            icon = Icons.Default.Lock,
                            label = "修改密码",
                            subtitle = "定期修改密码可提高安全性",
                            onClick = { /* 修改密码 */ }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.Security,
                            label = "登录设备管理",
                            subtitle = "查看和管理登录过的设备",
                            onClick = { /* 设备管理 */ }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.History,
                            label = "登录历史",
                            subtitle = "查看最近的登录记录",
                            onClick = { /* 登录历史 */ }
                        )
                    }
                }
            }

            // 家庭绑定
            item {
                Text(
                    text = "家庭绑定",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsClickableItem(
                            icon = Icons.Default.FamilyRestroom,
                            label = "绑定子女账号",
                            subtitle = "让子女远程守护您的安全",
                            onClick = { /* 绑定子女 */ }
                        )

                        Divider()

                        SettingsClickableItem(
                            icon = Icons.Default.Group,
                            label = "绑定社区志愿者",
                            subtitle = "获得社区帮助",
                            onClick = { /* 绑定志愿者 */ }
                        )
                    }
                }
            }

            // 危险操作
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "危险操作",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Danger
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Danger.copy(alpha = 0.05f)
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsClickableItem(
                            icon = Icons.Default.DeleteForever,
                            label = "注销账号",
                            subtitle = "永久删除账号和所有数据",
                            titleColor = Danger,
                            onClick = { showDeleteDialog = true }
                        )
                    }
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认注销") },
            text = {
                Text("注销账号将永久删除您的所有数据，包括交易记录、训练记录、案件信息等。此操作不可恢复，您确定要继续吗？")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        // 执行注销
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Danger
                    )
                ) {
                    Text("确认注销")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun SettingsInputItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(80.dp)
        )

        if (enabled) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(80.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsClickableItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String = "",
    titleColor: Color = TextPrimary,
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (titleColor == Danger) Danger else Primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = titleColor
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextHint
            )
        }
    }
}
