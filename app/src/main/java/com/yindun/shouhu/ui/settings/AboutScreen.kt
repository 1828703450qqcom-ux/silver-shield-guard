package com.yindun.shouhu.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yindun.shouhu.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于银盾守护") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 应用图标和名称
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        color = Primary,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "银盾守护",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )

                    Text(
                        text = "老年人防诈骗安全守护应用",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Primary.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "版本 1.0.0",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Primary
                        )
                    }
                }
            }

            // 核心功能
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        AboutFeatureItem(
                            icon = Icons.Default.AccountBalance,
                            title = "财务异常监控",
                            description = "实时监控银行卡、微信钱包交易，识别异常行为"
                        )

                        Divider()

                        AboutFeatureItem(
                            icon = Icons.Default.School,
                            title = "AI防骗训练",
                            description = "模拟诈骗话术，提升防骗意识，获得应用内模拟积分"
                        )

                        Divider()

                        AboutFeatureItem(
                            icon = Icons.Default.Phone,
                            title = "来电防护",
                            description = "监听陌生来电，识别诈骗电话并及时预警"
                        )

                        Divider()

                        AboutFeatureItem(
                            icon = Icons.Default.Report,
                            title = "报案追踪",
                            description = "一键报案，自动生成材料，实时追踪进度"
                        )
                    }
                }
            }

            // 技术特性
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "技术特性",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FeatureTag(text = "0.3秒极速识别")
                        FeatureTag(text = "系统语音能力")
                        FeatureTag(text = "离线可用")
                        FeatureTag(text = "数据加密")
                        FeatureTag(text = "适老化设计")
                    }
                }
            }

            // 团队信息
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "关于我们",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "银盾守护是一款专为老年人设计的防诈骗安全守护应用，集财务监控、AI训练、来电防护、报案追踪于一体，致力于为老年人提供全方位的防诈骗保护。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AboutLinkItem(
                            icon = Icons.Default.Email,
                            title = "联系我们",
                            subtitle = "support@yindunshouhu.com"
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        AboutLinkItem(
                            icon = Icons.Default.Language,
                            title = "官方网站",
                            subtitle = "www.yindunshouhu.com"
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        AboutLinkItem(
                            icon = Icons.Default.Shield,
                            title = "隐私政策",
                            subtitle = "查看完整的隐私政策"
                        )
                    }
                }
            }

            // 版权信息
            item {
                Text(
                    text = "© 2024 银盾守护团队\n保留所有权利",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
fun AboutFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            color = Primary.copy(alpha = 0.1f),
            shape = MaterialTheme.shapes.medium
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun FeatureTag(text: String) {
    Surface(
        modifier = Modifier.padding(4.dp),
        color = Secondary.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Secondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutLinkItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        onClick = { /* 打开链接 */ },
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
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
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
