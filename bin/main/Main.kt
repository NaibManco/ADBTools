import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ui.MainScreen

fun main() = application {
    val windowState = rememberWindowState(
        size = DpSize(1200.dp, 800.dp)
    )
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "ADB工具",
        state = windowState
    ) {
        MaterialTheme {
            Surface(
                color = MaterialTheme.colorScheme.background
            ) {
                MainScreen()
            }
        }
    }
} 