package com.yindun.shouhu.ui.financial

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yindun.shouhu.ui.theme.Primary

/**
 * Financial institution integration is not available in this prototype.
 * Keep the demonstration screen read-only so it never solicits real card or login details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BindAccountScreen(accountType: String, onBack: () -> Unit) {
    val isBank = accountType == "bank"
    val accountLabel = if (isBank) "银行卡" else "支付钱包"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$accountLabel · 演示说明") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Icon(
                imageVector = if (isBank) Icons.Default.AccountBalance else Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(56.dp)
            )
            Text("当前仅提供本地演示", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "银盾守护尚未获得银行或支付平台的数据接口授权，无法绑定真实账户，也不会读取实时交易。首页显示的账户与风险提醒均为示例数据。",
                style = MaterialTheme.typography.bodyLarge
            )
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = null)
                    Text("请勿在原型中输入银行卡号、支付密码、短信验证码或身份证信息。")
                }
            }
            Card {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Text("未来如提供账户接入，需要机构授权、用户明确同意、可撤销权限及真实设备上的安全验证。")
                }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("返回演示页面")
            }
        }
    }
}
