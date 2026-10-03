package com.yindun.shouhu.ui.financial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.yindun.shouhu.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BindAccountScreen(
    accountType: String, // "bank", "wechat", "alipay"
    onBindSuccess: (accountId: Long) -> Unit,
    onBack: () -> Unit
) {
    var bankName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var showCardNumber by remember { mutableStateOf(false) }
    var isBinding by remember { mutableStateOf(false) }

    val title = when (accountType) {
        "bank" -> "绑定银行卡"
        "wechat" -> "绑定微信钱包"
        "alipay" -> "绑定支付宝"
        else -> "绑定账户"
    }

    val banks = listOf(
        "中国工商银行",
        "中国农业银行",
        "中国银行",
        "中国建设银行",
        "交通银行",
        "招商银行",
        "浦发银行",
        "中信银行",
        "光大银行",
        "民生银行",
        "兴业银行",
        "平安银行",
        "其他"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
            // 说明信息
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
                            text = "绑定后系统将实时监控账户交易，识别异常行为并及时提醒。您的账户信息将被加密存储，请放心使用。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            when (accountType) {
                "bank" -> {
                    // 银行选择
                    item {
                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = bankName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("选择银行") },
                                leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                banks.forEach { bank ->
                                    DropdownMenuItem(
                                        text = { Text(bank) },
                                        onClick = {
                                            bankName = bank
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 卡号
                    item {
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it },
                            label = { Text("银行卡号") },
                            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showCardNumber = !showCardNumber }) {
                                    Icon(
                                        if (showCardNumber) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "显示/隐藏"
                                    )
                                }
                            },
                            visualTransformation = if (showCardNumber) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // 持卡人
                    item {
                        OutlinedTextField(
                            value = cardHolder,
                            onValueChange = { cardHolder = it },
                            label = { Text("持卡人姓名") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                "wechat", "alipay" -> {
                    // 手机号
                    item {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("绑定手机号") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // 验证码
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = "",
                                onValueChange = {},
                                label = { Text("验证码") },
                                leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            OutlinedButton(
                                onClick = { /* 发送验证码 */ }
                            ) {
                                Text("获取验证码")
                            }
                        }
                    }
                }
            }

            // 安全提示
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Safe.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "安全保障",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Safe
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        SecurityItem(text = "AES-256加密存储")
                        SecurityItem(text = "数据仅用于风险监控")
                        SecurityItem(text = "可随时解绑账户")
                        SecurityItem(text = "不用于商业用途")
                    }
                }
            }

            // 绑定按钮
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        // 执行绑定
                        onBindSuccess(1)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBinding,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    )
                ) {
                    if (isBinding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Link, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("确认绑定")
                    }
                }
            }

            // 解绑说明
            item {
                Text(
                    text = "绑定后可在“我的账户”中随时解绑，解绑后相关数据将被删除。",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun SecurityItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Safe,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}
