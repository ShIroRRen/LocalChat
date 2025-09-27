package work.niggergo.localchat.uicfg

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
	primary = Color(0xFFFFFFFF),
	secondary = Color(0xFF888888),
	tertiary = Color(0xFF404040),
	background = Color(0xFF000000),
	surface = Color(0xFF202020),
	outline = Color(0xFF888888)
)

private val LightColorScheme = lightColorScheme(
	primary = Color(0xFF000000),
	secondary = Color(0xFF888888),
	tertiary = Color(0xFFE0E0E0),
	background = Color(0xFFF8F8F8),
	surface = Color(0xFFFFFFFF),
	outline = Color(0xFF888888)
)

@Composable
fun LocalChatTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) =
	MaterialTheme(
		colorScheme = when {
			darkTheme -> DarkColorScheme
			else      -> LightColorScheme
		}, content = content
	)