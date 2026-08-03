package cl.agnov.ameli.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AmeliCyan,
    onPrimary = AmeliNavyDeep,
    secondary = AmeliNavyGrey80,
    tertiary = AmeliCyanDeep80,
    background = AmeliNavyDeep,
    surface = AmeliNavy,
)

private val LightColorScheme = lightColorScheme(
    primary = AmeliCyanDeep,
    secondary = AmeliNavyGrey40,
    tertiary = AmeliCyan40,
)

/**
 * Tema visual de AMELI Softphone. `dynamicColor` está desactivado por
 * defecto para que la identidad de marca (navy + cian, del logo oficial) se
 * mantenga consistente en vez de que Android 12+ la reemplace por los
 * colores dinámicos del fondo de pantalla del sistema.
 */
@Composable
fun AmeliTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AmeliTypography,
        content = content,
    )
}
