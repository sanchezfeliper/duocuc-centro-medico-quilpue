package cl.cmq.salud.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.cmq.salud.ui.theme.Divider
import cl.cmq.salud.ui.theme.SoftSage
import cl.cmq.salud.ui.theme.Spruce
import cl.cmq.salud.ui.theme.Surface
import cl.cmq.salud.ui.theme.TextSecondary

/*
 * BottomNavBar — navegación inferior compartida.
 *
 * Usa iconografía de una sola familia (iconos Material) en lugar de
 * caracteres Unicode, etiquetas siempre visibles y anuncio semántico
 * del destino seleccionado. Reserva su espacio sin solapar scroll.
 *
 * Cuatro destinos principales: Inicio, Expediente, Cargar y Revisión.
 * Targetas táctiles de al menos 48 dp.
 */
data class NavItem(
    val icon: ImageVector,
    val label: String,
    val contentDescription: String? = null
)

@Composable
fun BottomNavBar(
    items: List<NavItem>,
    activeIndex: Int = 0,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        containerColor = Surface,
        contentColor = Spruce,
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == activeIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.contentDescription ?: item.label,
                        tint = if (selected) Spruce else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) Spruce else TextSecondary,
                        textAlign = TextAlign.Center
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Spruce,
                    selectedTextColor = Spruce,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = SoftSage,
                    disabledIconColor = TextSecondary.copy(alpha = 0.5f),
                    disabledTextColor = TextSecondary.copy(alpha = 0.5f)
                ),
                alwaysShowLabel = true
            )
        }
    }
}

private val MainNavigationItems = listOf(
    NavItem(Icons.Default.Home, "Inicio", "Ir a inicio"),
    NavItem(Icons.Default.Search, "Expediente", "Consultar expediente"),
    NavItem(Icons.Default.Add, "Cargar", "Cargar documento"),
    NavItem(Icons.Default.CheckCircle, "Revisión", "Bandeja de revisión")
)

@Composable
fun MainBottomNavBar(
    activeIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BottomNavBar(
        items = MainNavigationItems,
        activeIndex = activeIndex,
        onItemSelected = onItemSelected,
        modifier = modifier
    )
}
