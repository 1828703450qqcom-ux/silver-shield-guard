package com.yindun.shouhu.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yindun.shouhu.ui.theme.*

data class DialectItem(
    val code: String,
    val name: String,
    val region: String
)

private val dialects = listOf(
    DialectItem("zh", "普通话", "全国"),
    DialectItem("yue", "粤语", "广东、香港、澳门"),
    DialectItem("wuu", "吴语", "上海、江苏南部、浙江"),
    DialectItem("nan", "闽南语", "福建、台湾"),
    DialectItem("hak", "客家话", "广东、江西、福建"),
    DialectItem("gan", "赣语", "江西"),
    DialectItem("hsn", "湘语", "湖南"),
    DialectItem("cmn-nj", "南京话", "江苏南京"),
    DialectItem("cmn-sc", "四川话", "四川、重庆"),
    DialectItem("cmn-hb", "湖北话", "湖北"),
    DialectItem("cmn-sd", "山东话", "山东"),
    DialectItem("cmn-hn", "河南话", "河南"),
    DialectItem("cmn-he", "河北话", "河北"),
    DialectItem("cmn-sx", "山西话", "山西"),
    DialectItem("cmn-ln", "辽宁话", "辽宁"),
    DialectItem("cmn-jl", "吉林话", "吉林"),
    DialectItem("cmn-hl", "黑龙江话", "黑龙江"),
    DialectItem("cmn-zj", "浙江话", "浙江"),
    DialectItem("cmn-ah", "安徽话", "安徽"),
    DialectItem("cmn-ji", "江西话", "江西"),
    DialectItem("cmn-gz", "贵州话", "贵州"),
    DialectItem("cmn-yn", "云南话", "云南"),
    DialectItem("cmn-gx", "广西话", "广西"),
    DialectItem("cmn-sa", "陕西话", "陕西"),
    DialectItem("cmn-gs", "甘肃话", "甘肃"),
    DialectItem("cmn-nx", "宁夏话", "宁夏"),
    DialectItem("cmn-qh", "青海话", "青海"),
    DialectItem("cmn-xz", "西藏话", "西藏"),
    DialectItem("cmn-xj", "新疆话", "新疆"),
    DialectItem("cmn-ne", "东北话", "东北三省"),
    DialectItem("cmn-tj", "天津话", "天津"),
    DialectItem("cmn-bj", "北京话", "北京")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialectSettingsScreen(
    onBack: () -> Unit
) {
    var selectedDialect by remember { mutableStateOf("zh") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("方言设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "选择语音交互方言",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "可选择偏好的语音设置，实际识别效果取决于设备和系统语音服务",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn {
                items(dialects) { dialect ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = { selectedDialect = dialect.code }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dialect.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = dialect.region,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (selectedDialect == dialect.code) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "已选择",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
