package utils

data class ActivityStackInfo(
    val taskId: String,
    val activities: List<ActivityInfo>
)

data class ActivityInfo(
    val name: String,
    val state: String,
    val isTop: Boolean
)

object ActivityStackParser {
    fun parse(output: String): List<ActivityStackInfo> {
        val stacks = mutableListOf<ActivityStackInfo>()
        var currentTaskId = ""
        val currentActivities = mutableListOf<ActivityInfo>()
        var topResumedActivity = ""
        
        println("开始解析输出...")
        
        // 首先找到顶层活动
        output.lines().forEach { line ->
            if (line.contains("topResumedActivity=ActivityRecord{")) {
                topResumedActivity = line.substringAfter("u0 ").substringBefore(" t")
                println("找到顶层活动: $topResumedActivity")
            }
        }
        
        output.lines().forEach { line ->
            when {
                // 匹配任务栈的开始
                line.contains("* Task{") -> {
                    println("找到任务栈: $line")
                    if (currentTaskId.isNotEmpty() && currentActivities.isNotEmpty()) {
                        stacks.add(ActivityStackInfo(currentTaskId, currentActivities.toList()))
                        currentActivities.clear()
                    }
                    // 提取任务ID，格式如: * Task{c6a801f #557 type=standard
                    currentTaskId = try {
                        line.substringAfter("#").substringBefore(" ")
                    } catch (e: Exception) {
                        println("解析任务ID失败: ${e.message}")
                        ""
                    }
                    println("提取的任务ID: $currentTaskId")
                }
                
                // 匹配活动记录，只处理 Hist 记录
                line.trim().startsWith("* Hist") -> {
                    println("找到Activity记录: $line")
                    try {
                        // 提取活动名称，格式如: * Hist #0: ActivityRecord{33a6f6 u0 com.onyx.youngy.tob.dev/com.schoolpad.module.SchoolHomeActivity t557}
                        val activityName = line.substringAfter("u0 ").substringBefore(" t")
                        
                        // 确定活动状态
                        val state = when {
                            activityName == topResumedActivity -> "运行中"
                            line.contains("STOPPED") || line.contains("stopped=true") -> "已停止"
                            line.contains("PAUSED") || line.contains("paused=true") -> "已暂停"
                            else -> "后台"
                        }
                        
                        val isTop = activityName == topResumedActivity
                        
                        println("解析到Activity: name=$activityName, state=$state, isTop=$isTop")
                        
                        // 避免重复添加
                        if (!currentActivities.any { it.name == activityName }) {
                            currentActivities.add(ActivityInfo(activityName, state, isTop))
                        }
                    } catch (e: Exception) {
                        println("解析Activity失败: ${e.message}")
                    }
                }
            }
        }
        
        // 添加最后一个任务栈
        if (currentTaskId.isNotEmpty() && currentActivities.isNotEmpty()) {
            stacks.add(ActivityStackInfo(currentTaskId, currentActivities))
        }
        
        println("解析完成，共找到 ${stacks.size} 个任务栈")
        stacks.forEach { stack ->
            println("任务栈 #${stack.taskId} 包含 ${stack.activities.size} 个活动:")
            stack.activities.forEach { activity ->
                println("  - ${activity.name} (${activity.state})")
            }
        }
        
        return stacks
    }
} 