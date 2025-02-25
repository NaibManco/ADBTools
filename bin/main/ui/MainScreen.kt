package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import utils.AdbExecutor
import utils.ActivityStackInfo
import utils.ActivityStackParser
import ui.components.ActivityStackView
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import utils.FragmentInfo
import utils.FragmentStackParser
import ui.components.FragmentTreeView

@Composable
fun MainScreen() {
    var selectedItem by remember { mutableStateOf(ToolbarItem.ACTIVITY) }
    var activityOutput by remember { mutableStateOf("") }
    var fragmentOutput by remember { mutableStateOf("") }
    var commonOutput by remember { mutableStateOf("") }
    var logOutput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Row(modifier = Modifier.fillMaxSize()) {
        // 左侧工具栏
        Surface(
            modifier = Modifier.fillMaxHeight().width(200.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                ToolbarItem.values().forEach { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedItem == item,
                                onClick = { selectedItem = item }
                            )
                            .padding(8.dp),
                        color = if (selectedItem == item) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = item.title,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selectedItem == item) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        // 右侧内容区域
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                when (selectedItem) {
                    ToolbarItem.ACTIVITY -> ActivityCommands(
                        output = activityOutput,
                        onOutputChange = { activityOutput = it }
                    )
                    ToolbarItem.FRAGMENT -> FragmentCommands(
                        output = fragmentOutput,
                        onOutputChange = { fragmentOutput = it }
                    )
                    ToolbarItem.COMMON -> CommonCommands(
                        output = commonOutput,
                        onOutputChange = { commonOutput = it }
                    )
                    ToolbarItem.LOGS -> LogViewer(
                        output = logOutput,
                        onOutputChange = { logOutput = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityCommands(
    output: String,
    onOutputChange: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var activityStacks by remember { mutableStateOf(emptyList<ActivityStackInfo>()) }
    var fragmentStacks by remember { mutableStateOf(emptyList<FragmentInfo>()) }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部Tab栏
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 }
            ) {
                Text("Activity栈")
            }
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 }
            ) {
                Text("顶部Activity信息")
            }
        }

        when (selectedTab) {
            0 -> {
                // Activity栈视图
                // 顶部按钮区
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                val result = AdbExecutor.executeCommand("adb shell dumpsys activity activities")
                                onOutputChange(result)
                                val parsedStacks = ActivityStackParser.parse(result)
                                println("解析到 ${parsedStacks.size} 个任务栈")
                                parsedStacks.forEach { stack ->
                                    println("任务栈 #${stack.taskId} 包含 ${stack.activities.size} 个活动")
                                }
                                activityStacks = parsedStacks
                            }
                        }
                    ) {
                        Text("当前Activity栈")
                    }
                }

                // 内容区域分为上下两部分
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    // 上半部分：树状图
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f)
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        if (activityStacks.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "点击「当前Activity栈」按钮查看活动栈结构图",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            ActivityStackView(activityStacks)
                        }
                    }

                    // 下半部分：原始输出
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f)
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "命令输出",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(output))
                                    }
                                ) {
                                    Text("复制")
                                }
                            }
                            
                            OutlinedTextField(
                                value = output,
                                onValueChange = onOutputChange,
                                modifier = Modifier.fillMaxSize(),
                                readOnly = true,
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            1 -> {
                // 顶部Activity信息视图
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                val topActivity = ActivityStackParser.parse(
                                    AdbExecutor.executeCommand("adb shell dumpsys activity activities")
                                ).firstOrNull()?.activities?.firstOrNull { it.isTop }
                                
                                if (topActivity != null) {
                                    val result = AdbExecutor.executeCommand(
                                        "adb shell dumpsys activity ${topActivity.name}"
                                    )
                                    onOutputChange(result)
                                    fragmentStacks = FragmentStackParser.parse(result)
                                }
                            }
                        }
                    ) {
                        Text("刷新Fragment信息")
                    }
                    
                    // 内容区域分为上下两部分
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        // 上半部分：Fragment树状图
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.5f)
                                .padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            if (fragmentStacks.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "点击「刷新Fragment信息」按钮查看Fragment结构图",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                FragmentTreeView(fragmentStacks)
                            }
                        }

                        // 下半部分：原始输出
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.5f)
                                .padding(top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "命令输出",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    
                                    TextButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(output))
                                        }
                                    ) {
                                        Text("复制")
                                    }
                                }
                                
                                OutlinedTextField(
                                    value = output,
                                    onValueChange = onOutputChange,
                                    modifier = Modifier.fillMaxSize(),
                                    readOnly = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FragmentCommands(
    output: String,
    onOutputChange: (String) -> Unit
) {
    Column {
        Text("Fragment 相关指令", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        
        // TODO: 添加Fragment相关命令按钮
        
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = output,
            onValueChange = onOutputChange,
            modifier = Modifier.fillMaxWidth().weight(1f),
            readOnly = true,
            label = { Text("输出信息") }
        )
    }
}

@Composable
private fun CommonCommands(
    output: String,
    onOutputChange: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    Column {
        Text("常用指令", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        onOutputChange(AdbExecutor.executeCommand("adb devices"))
                    }
                }
            ) {
                Text("设备列表")
            }
            
            Button(
                onClick = {
                    scope.launch {
                        onOutputChange(
                            AdbExecutor.executeCommand("adb shell pm list packages -3")
                        )
                    }
                }
            ) {
                Text("应用列表")
            }
            
            Button(
                onClick = {
                    scope.launch {
                        val timestamp = System.currentTimeMillis()
                        val filename = "screenshot_$timestamp.png"
                        val result = AdbExecutor.executeCommand("adb exec-out screencap -p > $filename")
                        onOutputChange(
                            if (result.contains("错误")) result
                            else "截图已保存为: $filename"
                        )
                    }
                }
            ) {
                Text("截图")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = output,
            onValueChange = onOutputChange,
            modifier = Modifier.fillMaxWidth().weight(1f),
            readOnly = true,
            label = { Text("输出信息") }
        )
    }
}

@Composable
private fun LogViewer(
    output: String,
    onOutputChange: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    Column {
        Text("日志查看", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        onOutputChange(AdbExecutor.executeCommand("adb logcat"))
                    }
                }
            ) {
                Text("查看日志")
            }
            
            Button(
                onClick = {
                    scope.launch {
                        onOutputChange(AdbExecutor.executeCommand("adb logcat -c"))
                        onOutputChange("日志已清除")
                    }
                }
            ) {
                Text("清除日志")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = output,
            onValueChange = onOutputChange,
            modifier = Modifier.fillMaxWidth().weight(1f),
            readOnly = true,
            label = { Text("日志输出") }
        )
    }
} 