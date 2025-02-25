package utils

object FragmentStackParser {
    fun parse(output: String): List<FragmentInfo> {
        val rootFragments = mutableListOf<FragmentInfo>()
        val fragmentStack = mutableListOf<FragmentInfo>()
        var currentIndent = 0
        
        output.lines().forEach { line ->
            val indent = line.countLeadingSpaces()
            
            when {
                // 找到一个新的Fragment定义
                line.contains("} (") && !line.trim().startsWith("Child") -> {
                    val fragment = createFragmentInfo(line)
                    
                    // 根据缩进判断层级关系
                    if (indent <= currentIndent) {
                        // 回到上层
                        while (fragmentStack.size > 0 && fragmentStack.last().indent >= indent) {
                            fragmentStack.removeLast()
                        }
                    }
                    
                    fragment.indent = indent
                    if (fragmentStack.isEmpty()) {
                        rootFragments.add(fragment)
                    } else {
                        (fragmentStack.last().children as MutableList).add(fragment)
                    }
                    fragmentStack.add(fragment)
                    currentIndent = indent
                }
                
                // 更新Fragment的状态信息
                line.contains("mState=") -> {
                    fragmentStack.lastOrNull()?.let { fragment ->
                        fragment.state = determineFragmentState(line)
                        fragment.isAdded = line.contains("mAdded=true")
                        fragment.isVisible = !line.contains("mHidden=true") && !line.contains("mDetached=true")
                    }
                }
                
                // 更新Fragment的标签
                line.contains("mTag=") -> {
                    fragmentStack.lastOrNull()?.let { fragment ->
                        fragment.tag = extractFragmentTag(line)
                    }
                }
            }
        }
        
        return rootFragments
    }
    
    private fun createFragmentInfo(line: String): FragmentInfo {
        // 从类似 "BaseModule{b23d945} (f0c5c01b-c07b-4dc4-bcf0-bdb95c2426e8 id=0x2)" 提取信息
        val name = line.substringBefore("{").trim()
        val id = when {
            line.contains("id=0x") -> line.substringAfter("id=").substringBefore(")").trim()
            else -> ""
        }
        
        return FragmentInfo(
            name = name,
            id = id,
            tag = null,
            state = "",
            children = mutableListOf(),
            indent = 0
        )
    }
    
    private fun determineFragmentState(line: String): String {
        return when {
            line.contains("mState=7") -> "RESUMED"
            line.contains("mState=5") -> "STARTED"
            line.contains("mState=4") -> "CREATED"
            line.contains("mState=3") -> "ACTIVITY_CREATED"
            line.contains("mState=2") -> "VIEW_CREATED"
            line.contains("mState=1") -> "CREATED"
            else -> "INITIALIZED"
        }
    }
    
    private fun extractFragmentTag(line: String): String? {
        val tag = line.substringAfter("mTag=").substringBefore(" ").trim()
        return if (tag == "null") null else tag
    }
    
    private fun String.countLeadingSpaces(): Int {
        return takeWhile { it.isWhitespace() }.count()
    }
} 