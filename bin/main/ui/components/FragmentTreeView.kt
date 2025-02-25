package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import utils.FragmentInfo

@Composable
fun FragmentTreeView(fragments: List<FragmentInfo>) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        fragments.forEach { fragment ->
            FragmentNode(fragment, 0)
        }
    }
}

@Composable
private fun FragmentNode(fragment: FragmentInfo, level: Int) {
    Column(
        modifier = Modifier.padding(start = (level * 24).dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (fragment.state) {
                    "RESUMED" -> MaterialTheme.colorScheme.primaryContainer
                    "STARTED" -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = fragment.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${fragment.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "状态: ${fragment.state}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (fragment.tag != null) {
                    Text(
                        text = "Tag: ${fragment.tag}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // 递归显示子Fragment
        fragment.children.forEach { child ->
            FragmentNode(child, level + 1)
        }
    }
} 