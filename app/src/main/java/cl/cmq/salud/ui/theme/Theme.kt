package cl.cmq.salud.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/*
 * Esquema de colores CMQ Salud — rediseño UI.
 *
 * Mapea la paleta semántica a los roles de Material 3 para que los
 * componentes estándar (Button, TopAppBar, Scaffold, etc.) consuman los
 * colores correctos sin valores literales en las pantallas.
 *
 * dynamicColor se mantiene en la firma por compatibilidad de llamadores,
 * pero se ignora: la identidad CMQ es fija para mantener consistencia
 * visual institucional en todos los dispositivos.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Teal,
    onPrimary = Surface,
    primaryContainer = SoftSage,
    onPrimaryContainer = Spruce,
    secondary = Success,
    onSecondary = Surface,
    tertiary = Accent,
    onTertiary = Spruce,
    background = Spruce,
    onBackground = Surface,
    surface = Color(0xFF1D4544),
    onSurface = Color(0xFFE6F2EE),
    surfaceVariant = Color(0xFF4A5D57),
    onSurfaceVariant = Color(0xFFC2D2CC),
    outline = Divider,
    error = Error,
    onError = Surface,
    inversePrimary = Teal,
    inverseSurface = Surface,
    inverseOnSurface = Spruce
)

private val LightColorScheme = lightColorScheme(
    primary = Teal,                       // acción primaria
    onPrimary = Surface,                  // texto sobre acción primaria
    primaryContainer = SoftSage,          // contenedor de acento suave
    onPrimaryContainer = Spruce,
    secondary = Success,                  // estados de éxito
    onSecondary = Surface,
    tertiary = Accent,                    // acento secundario (ámbar)
    onTertiary = Spruce,
    background = Paper,                   // fondo general de pantalla
    onBackground = Spruce,                // texto sobre fondo
    surface = Surface,                    // formularios, paneles, contenido
    onSurface = Spruce,                   // texto sobre superficie
    surfaceVariant = SoftSage,            // variantes de superficie
    onSurfaceVariant = TextSecondary,
    outline = Divider,                    // separadores y contornos
    error = Error,                        // errores y rechazo
    onError = Surface,
    inversePrimary = Spruce,
    inverseSurface = Spruce,
    inverseOnSurface = Surface
)

@Composable
fun CMQSaludTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // La marca CMQ usa colores fijos, pero debe respetar el modo del sistema.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
