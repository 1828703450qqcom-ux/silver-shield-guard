package com.yindun.shouhu.ui.training

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yindun.shouhu.domain.engine.*
import com.yindun.shouhu.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TrainingSessionScreen(
    scriptId: Long,
    onSessionComplete: (TrainingFeedback) -> Unit,
    onBack: () -> Unit
) {
    var sessionState by remember { mutableStateOf<TrainingSessionState>(TrainingSessionState.Idle) }
    var scriptContent by remember { mutableStateOf<ScriptContent?>(null) }
    var currentRound by remember { mutableStateOf(0) }
    var dialogueHistory by remember { mutableStateOf<List<DialogueItem>>(emptyList()) }
    var identifiedKeywords by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedKeywords by remember { mutableStateOf<List<String>>(emptyList()) }

    // 初始化话术内容（这里应该从ViewModel获取）
    LaunchedEffect(scriptId) {
        // 模拟加载话术
        scriptContent = ScriptContent(
            dialogue = listOf(
                DialogueLine("scammer", "您好，我是XX健康中心的工作人员，我们有免费体检活动。", keywords = listOf("免费体检")),
                DialogueLine("scammer", "检查结果显示您身体有严重问题，需要购买我们的特效药。", keywords = listOf("特效药")),
                DialogueLine("scammer", "这个药很贵但是效果好，您可以先付定金。", keywords = listOf("定金"))
            ),
            keywords = listOf("免费体检", "特效药", "定金"),
            tips = "正规医疗机构不会在街头推销，体检应去正规医院。",
            warnings = emptyList()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("训练进行中") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { /* 暂停 */ }) {
                        Icon(Icons.Default.Pause, "暂停")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Secondary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 进度指示器
            scriptContent?.let { content ->
                LinearProgressIndicator(
                    progress = (currentRound.toFloat() / content.dialogue.size).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Secondary,
                    trackColor = Secondary.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "第 ${currentRound + 1}/${content.dialogue.size} 回合",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 对话历史
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dialogueHistory) { item ->
                    DialogueBubble(item = item)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 关键词选择区域
            if (sessionState is TrainingSessionState.ListeningForKeywords) {
                KeywordSelectionPanel(
                    expectedKeywords = (sessionState as TrainingSessionState.ListeningForKeywords).expectedKeywords,
                    selectedKeywords = selectedKeywords,
                    onKeywordToggle = { keyword ->
                        selectedKeywords = if (keyword in selectedKeywords) {
                            selectedKeywords - keyword
                        } else {
                            selectedKeywords + keyword
                        }
                    },
                    onSubmit = {
                        identifiedKeywords = identifiedKeywords + selectedKeywords
                        currentRound++
                        selectedKeywords = emptyList()
                        // 模拟下一回合
                        if (currentRound < (scriptContent?.dialogue?.size ?: 0)) {
                            sessionState = TrainingSessionState.ListeningForKeywords(
                                expectedKeywords = scriptContent?.dialogue?.get(currentRound)?.keywords ?: emptyList(),
                                timeLimit = 15000
                            )
                        } else {
                            // 训练完成
                            val feedback = TrainingFeedback(
                                score = 80,
                                performanceLevel = PerformanceLevel.GOOD,
                                reward = 0.15,
                                feedbackMessage = "做得很好！您识别了大部分诈骗关键词。",
                                learningPoints = listOf("注意识别免费体检、特效药等关键词"),
                                missedKeywords = emptyList(),
                                tips = "正规医疗机构不会在街头推销。"
                            )
                            onSessionComplete(feedback)
                        }
                    }
                )
            }

            // 语音输入按钮
            if (sessionState is TrainingSessionState.InProgress ||
                sessionState is TrainingSessionState.ListeningForKeywords) {
                VoiceInputButton(
                    isListening = false,
                    onClick = { /* 开始语音识别 */ }
                )
            }

            // 底部提示
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Warning.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Warning,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "仔细听诈骗话术，点击下方按钮识别其中的关键词",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun DialogueBubble(item: DialogueItem) {
    val isScammer = item.role == "scammer"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isScammer) Arrangement.Start else Arrangement.End
    ) {
        if (!isScammer) {
            Spacer(modifier = Modifier.weight(1f))
        }

        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isScammer) 16.dp else 4.dp,
                        bottomEnd = if (isScammer) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isScammer) Primary.copy(alpha = 0.1f) else Secondary
                )
                .padding(12.dp)
        ) {
            Text(
                text = if (isScammer) "诈骗分子" else "您",
                style = MaterialTheme.typography.labelSmall,
                color = if (isScammer) Primary else Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isScammer) TextPrimary else Color.White
            )
        }

        if (isScammer) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeywordSelectionPanel(
    expectedKeywords: List<String>,
    selectedKeywords: List<String>,
    onKeywordToggle: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceLight
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "识别到的关键词",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 关键词标签
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                expectedKeywords.forEach { keyword ->
                    val isSelected = keyword in selectedKeywords
                    KeywordChip(
                        text = keyword,
                        isSelected = isSelected,
                        onClick = { onKeywordToggle(keyword) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("确认识别")
            }
        }
    }
}

@Composable
fun KeywordChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) Secondary else Color.Transparent,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = if (isSelected) Secondary else TextHint,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Color.White else TextPrimary
            )
        }
    }
}

@Composable
fun VoiceInputButton(
    isListening: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(80.dp)
            .scale(if (isListening) scale else 1f)
            .clip(CircleShape)
            .background(if (isListening) Danger else Primary)
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
            contentDescription = if (isListening) "停止录音" else "开始录音",
            modifier = Modifier.size(40.dp),
            tint = Color.White
        )
    }
}

@Composable
fun TrainingResultScreen(
    feedback: TrainingFeedback,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 分数圆圈
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(
                    when (feedback.performanceLevel) {
                        PerformanceLevel.EXCELLENT -> Safe
                        PerformanceLevel.GOOD -> Secondary
                        PerformanceLevel.FAIR -> Warning
                        PerformanceLevel.POOR -> Danger
                    }.copy(alpha = 0.1f)
                )
                .border(
                    width = 4.dp,
                    color = when (feedback.performanceLevel) {
                        PerformanceLevel.EXCELLENT -> Safe
                        PerformanceLevel.GOOD -> Secondary
                        PerformanceLevel.FAIR -> Warning
                        PerformanceLevel.POOR -> Danger
                    },
                    shape = CircleShape
                )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${feedback.score}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (feedback.performanceLevel) {
                        PerformanceLevel.EXCELLENT -> Safe
                        PerformanceLevel.GOOD -> Secondary
                        PerformanceLevel.FAIR -> Warning
                        PerformanceLevel.POOR -> Danger
                    }
                )
                Text(
                    text = "分",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when (feedback.performanceLevel) {
                PerformanceLevel.EXCELLENT -> "太棒了！"
                PerformanceLevel.GOOD -> "做得很好！"
                PerformanceLevel.FAIR -> "还不错！"
                PerformanceLevel.POOR -> "继续加油！"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = feedback.feedbackMessage,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 奖励卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = PointsGold.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = PointsGold,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "获得 ${feedback.reward} 模拟积分",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PointsGold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 学习要点
        if (feedback.learningPoints.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "学习要点",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    feedback.learningPoints.forEach { point ->
                        Text(
                            text = "• $point",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("返回首页")
            }

            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("再练一次")
            }
        }
    }
}
