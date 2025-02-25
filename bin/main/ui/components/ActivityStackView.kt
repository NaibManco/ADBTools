package ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import utils.ActivityStackInfo

@Composable
fun ActivityStackView(stacks: List<ActivityStackInfo>) {
    if (stacks.isEmpty()) {
        Text("没有解析到任何活动栈")
        return
    }
    
    val lineColor = MaterialTheme.colorScheme.outline
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)  // 添加外边距
    ) {
        Text("解析到 ${stacks.size} 个活动栈", style = MaterialTheme.typography.titleSmall)
        Text(
            text = "Activity 栈结构图",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        stacks.forEach { stack ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),  // 添加阴影
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Task #${stack.taskId}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // 树状结构
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(  // 添加边框
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(8.dp)  // 内边距
                    ) {
                        // 绘制连接线
                        TreeLines(
                            activitiesCount = stack.activities.size,
                            lineColor = lineColor
                        )
                        
                        // Activity 卡片
                        Column(
                            modifier = Modifier.padding(start = 60.dp)  // 增加缩进
                        ) {
                            stack.activities.reversed().forEach { activity ->  // 反转列表，使顶部Activity显示在最上方
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .border(
                                            width = if (activity.isTop) 2.dp else 1.dp,
                                            color = if (activity.isTop) 
                                                MaterialTheme.colorScheme.primary
                                            else 
                                                MaterialTheme.colorScheme.outline,
                                            shape = MaterialTheme.shapes.small
                                        ),
                                    shape = MaterialTheme.shapes.small,
                                    color = if (activity.isTop)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface,
                                    tonalElevation = if (activity.isTop) 4.dp else 1.dp  // 添加立体感
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = activity.name.substringAfterLast('.'),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (activity.isTop) androidx.compose.ui.text.font.FontWeight.Bold else null
                                            )
                                            Text(
                                                text = activity.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = activity.state,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (activity.isTop)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TreeLines(
    activitiesCount: Int,
    lineColor: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height((activitiesCount * 80).dp)
    ) {
        val startX = 30.dp.toPx()
        
        // 绘制垂直主线
        drawLine(
            color = lineColor,
            start = Offset(startX, 0f),
            end = Offset(startX, size.height),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
        
        // 为每个活动绘制水平连接线
        repeat(activitiesCount) { index ->
            val y = index * 80.dp.toPx() + 40.dp.toPx()
            drawLine(
                color = lineColor,
                start = Offset(startX, y),
                end = Offset(startX + 20.dp.toPx(), y),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
    }
} 