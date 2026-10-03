package com.yindun.shouhu.ui.training

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
import com.yindun.shouhu.domain.usecase.PointsUseCase
import com.yindun.shouhu.domain.usecase.RewardItem
import com.yindun.shouhu.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsShopScreen(
    userId: Long,
    onBack: () -> Unit
) {
    var pointsBalance by remember { mutableStateOf(0.0) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val rewardsCatalog = listOf(
        RewardItem(1, "防诈骗手册", "纸质版防诈骗知识手册", 10.0, "physical"),
        RewardItem(2, "手机支架", "适老化大字体手机支架", 20.0, "physical"),
        RewardItem(3, "公交卡充值", "10元公交卡充值", 15.0, "service"),
        RewardItem(4, "话费充值", "10元话费充值", 15.0, "service"),
        RewardItem(5, "社区评优资格", "获得社区防诈骗先锋称号", 50.0, "honor"),
        RewardItem(6, "反诈保险", "1万元诈骗损失保险", 100.0, "insurance")
    )

    val categories = listOf(
        "全部" to null,
        "实物奖品" to "physical",
        "服务抵扣" to "service",
        "荣誉称号" to "honor",
        "保险保障" to "insurance"
    )

    val filteredRewards = if (selectedCategory != null) {
        rewardsCatalog.filter { it.category == selectedCategory }
    } else {
        rewardsCatalog
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("积分商城") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PointsGold
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
            // 积分余额卡片
            item {
                PointsBalanceCard(balance = pointsBalance)
            }

            // 分类筛选
            item {
                CategoryFilterRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = { selectedCategory = it }
                )
            }

            // 商品列表
            items(filteredRewards) { reward ->
                RewardCard(
                    reward = reward,
                    canAfford = pointsBalance >= reward.pointsCost,
                    onRedeem = { /* 兑换逻辑 */ }
                )
            }
        }
    }
}

@Composable
fun PointsBalanceCard(balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = PointsGold.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = PointsGold
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "我的积分",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
                Text(
                    text = "%.1f".format(balance),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = PointsGold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(onClick = { /* 查看明细 */ }) {
                Text("明细")
            }
        }
    }
}

@Composable
fun CategoryFilterRow(
    categories: List<Pair<String, String?>>,
    selectedCategory: String?,
    onCategorySelect: (String?) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = categories.indexOfFirst { it.second == selectedCategory },
        edgePadding = 0.dp,
        containerColor = Color.Transparent,
        divider = {}
    ) {
        categories.forEach { (name, category) ->
            Tab(
                selected = category == selectedCategory,
                onClick = { onCategorySelect(category) },
                text = { Text(name) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardCard(
    reward: RewardItem,
    canAfford: Boolean,
    onRedeem: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (canAfford) onRedeem else {{}}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Surface(
                modifier = Modifier.size(56.dp),
                color = when (reward.category) {
                    "physical" -> Primary.copy(alpha = 0.1f)
                    "service" -> Secondary.copy(alpha = 0.1f)
                    "honor" -> PointsGold.copy(alpha = 0.1f)
                    "insurance" -> Safe.copy(alpha = 0.1f)
                    else -> Primary.copy(alpha = 0.1f)
                },
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = when (reward.category) {
                        "physical" -> Icons.Default.Inventory2
                        "service" -> Icons.Default.CardMembership
                        "honor" -> Icons.Default.EmojiEvents
                        "insurance" -> Icons.Default.Shield
                        else -> Icons.Default.Redeem
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize(),
                    tint = when (reward.category) {
                        "physical" -> Primary
                        "service" -> Secondary
                        "honor" -> PointsGold
                        "insurance" -> Safe
                        else -> Primary
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reward.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = reward.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = RewardItem.getCategoryDisplayName(reward.category),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextHint
                )
            }

            // 价格和兑换按钮
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.0f 积分".format(reward.pointsCost),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PointsGold
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (canAfford) {
                    Button(
                        onClick = onRedeem,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Secondary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text("兑换", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text("积分不足", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
