package cl.cmq.salud.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.cmq.salud.ui.theme.Background
import cl.cmq.salud.ui.theme.Blue
import cl.cmq.salud.ui.theme.Light
import cl.cmq.salud.ui.theme.Navy

/*
 * AppTopBar — barra de aplicación compartida.
 *
 * Garantiza misma altura, alineación, título y acción atrás en destinos
 * secundarios. El botón de atrás es opcional: se muestra solo cuando
 * se provee onBack (home no lo necesita). El contenido secundario
 * permite acciones como "Salir" en el home.
 *
 * Reemplaza la implementación inline que tenía HomeScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String = "",
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
    titleContent: @Composable (() -> Unit)? = null
) {
    val tc = titleContent
    val ac = trailingContent
    TopAppBar(
        title = {
            if (tc != null) {
                tc()
            } else {
                Text(
                    text = title,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    color = Navy,
                )
            }
        },
        navigationIcon = if (onBack != null) {
            {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Blue,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Light, CircleShape)
                    )
                }
            }
        } else {
            {}
        },
        actions = {
            ac?.invoke()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Background,
            titleContentColor = Navy,
            navigationIconContentColor = Blue
        ),
        modifier = modifier
    )
}
