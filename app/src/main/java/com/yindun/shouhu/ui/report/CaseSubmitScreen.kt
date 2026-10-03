package com.yindun.shouhu.ui.report

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseSubmitScreen(
    isChildSubmit: Boolean = false,
    onSubmitSuccess: (caseId: Long, caseNumber: String) -> Unit,
    onBack: () -> Unit
) {
    var caseType by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var suspectPhone by remember { mutableStateOf("") }
    var suspectInfo by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var additionalInfo by remember { mutableStateOf("") }
    var evidencePaths by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSubmitting by remember { mutableStateOf(false) }

    val caseTypes = listOf(
        "保健品诈骗",
        "投资理财诈骗",
        "冒充公检法",
        "中奖诈骗",
        "冒充亲属紧急情况",
        "冒充客服",
        "贷款诈骗",
        "其他"
    )

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            evidencePaths = evidencePaths + it.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isChildSubmit) "为家属报案" else "我要报案") },
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
            // 标题说明
            item {
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isChildSubmit) {
                                "您正在为家属提交诈骗线索，系统将自动关联绑定老年人近72小时内的高风险行为数据。"
                            } else {
                                "请填写诈骗相关信息，提交后系统将自动生成报案材料。"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 诈骗类型选择
            item {
                Text(
                    text = "诈骗类型 *",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = caseType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("选择诈骗类型") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        caseTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    caseType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 涉案金额
            item {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("涉案金额（元）") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // 对方联系方式
            item {
                OutlinedTextField(
                    value = suspectPhone,
                    onValueChange = { suspectPhone = it },
                    label = { Text("对方联系方式") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // 对方信息
            item {
                OutlinedTextField(
                    value = suspectInfo,
                    onValueChange = { suspectInfo = it },
                    label = { Text("对方信息（姓名、账号等）") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            // 案件描述
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("案件描述 *") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }

            // 子女补充信息
            if (isChildSubmit) {
                item {
                    OutlinedTextField(
                        value = additionalInfo,
                        onValueChange = { additionalInfo = it },
                        label = { Text("补充说明（如诈骗话术等）") },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }

            // 证据材料
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "证据材料",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(onClick = { filePickerLauncher.launch("*/*") }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加文件")
                    }
                }
            }

            if (evidencePaths.isNotEmpty()) {
                items(evidencePaths) { path ->
                    EvidenceItem(
                        fileName = path.substringAfterLast("/"),
                        onRemove = {
                            evidencePaths = evidencePaths - path
                        }
                    )
                }
            }

            // 提交按钮
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        // TODO: 提交案件
                        onSubmitSuccess(1, "YD20240830001")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = caseType.isNotEmpty() && description.isNotEmpty() && !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("提交报案")
                    }
                }
            }

            // 一键拨打96110
            item {
                OutlinedButton(
                    onClick = { /* 拨打96110 */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Danger
                    )
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("一键拨打 96110 国家反诈热线")
                }
            }
        }
    }
}

@Composable
fun EvidenceItem(
    fileName: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = Primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除",
                    tint = Danger
                )
            }
        }
    }
}
