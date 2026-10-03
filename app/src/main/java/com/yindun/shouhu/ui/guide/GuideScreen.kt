package com.yindun.shouhu.ui.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GuideScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    val guidePages = listOf(
        GuidePage(
            icon = Icons.Default.Shield,
            title = "欢迎使用银盾守护",
            description = "您的专属防诈骗安全助手，全方位守护您和家人的财产安全。",
            color = Primary
        ),
        GuidePage(
            icon = Icons.Default.Security,
            title = "智能风险检测",
            description = "实时监控交易异常，智能识别诈骗电话，第一时间为您预警。",
            color = Secondary
        ),
        GuidePage(
            icon = Icons.Default.School,
            title = "AI防骗训练",
            description = "通过模拟真实诈骗场景，提升您的防骗意识，完成训练可获得应用内模拟积分。",
            color = Warning
        ),
        GuidePage(
            icon = Icons.Default.FamilyRestroom,
            title = "家庭守护",
            description = "绑定家人账号，远程守护父母安全，一起构建防诈骗安全网。",
            color = Safe
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 页面指示器
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(guidePages.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Primary else Primary.copy(alpha = 0.3f))
                )
            }
        }

        // 跳过按钮
        if (pagerState.currentPage < guidePages.size - 1) {
            TextButton(
                onClick = onComplete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 16.dp)
            ) {
                Text(
                    text = "跳过",
                    color = TextSecondary
                )
            }
        }

        // 页面内容
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            GuidePageContent(page = guidePages[page])
        }

        // 底部按钮
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        ) {
            if (pagerState.currentPage == guidePages.size - 1) {
                // 最后一页显示开始使用按钮
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    )
                ) {
                    Text(
                        text = "开始使用",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                // 其他页显示下一步按钮
                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    )
                ) {
                    Text(
                        text = "下一步",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GuidePageContent(page: GuidePage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 图标
        Surface(
            modifier = Modifier.size(150.dp),
            color = page.color.copy(alpha = 0.1f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = page.color
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 标题
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 描述
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

data class GuidePage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: Color
)
