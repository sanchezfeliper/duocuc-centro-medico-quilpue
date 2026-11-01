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
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
 * Destinos principales: Inicio, Expediente, Cargar y Perfil, con una quinta
 * posición de Revisión para los perfiles con bandeja de revisión (RF-15).
 * Tarjetas táctiles de al menos 48 dp.
 *
 * [MainNavigationItems] son los cuatro destinos comunes a todos los perfiles.
 * [mainNavigationItemsPara] agrega el quinto destino "Revisión" cuando el
 * perfil activo puede revisar: el botón es fijo y explícito, no una sustitución
 * del de Perfil, de modo que administración conserva tanto la bandeja como sus
 * propios datos. Los perfiles sin bandeja ven cuatro botones: nunca se les
 * ofrece una entrada que no pueden usar (mínimo privilegio, RF-07).
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

/** Cuatro destinos comunes: Inicio, Expediente, Cargar y Perfil. */
private val MainNavigationItems = listOf(
    NavItem(Icons.Default.Home, "Inicio", "Ir a inicio"),
    NavItem(Icons.Default.Search, "Expediente", "Consultar expediente"),
    NavItem(Icons.Default.Add, "Cargar", "Cargar documento"),
    NavItem(Icons.Default.Person, "Perfil", "Ver mi perfil")
)

/** Quinto destino, disponible solo para quien tiene bandeja de revisión (RF-15). */
private val RevisionNavItem = NavItem(
    Icons.AutoMirrored.Filled.FactCheck,
    "Revisión",
    "Abrir bandeja de revisión"
)

/**
 * Destinos de la barra inferior según el perfil activo.
 *
 * Devuelve los cuatro destinos comunes y, solo si [puedeRevisar], agrega
 * "Revisión" al final. Los índices de [BottomNavItem] son la posición en la
 * barra: quien navegue debe usar los valores de este modelo, no constantes.
 */
fun mainNavigationItemsPara(puedeRevisar: Boolean): List<NavItem> =
    if (puedeRevisar) MainNavigationItems + RevisionNavItem else MainNavigationItems

/** Índices canónicos de la barra inferior, en un solo lugar para no usar números sueltos. */
object BottomNavItem {
    const val INICIO = 0
    const val EXPEDIENTE = 1
    const val CARGA = 2
    const val PERFIL = 3

    /**
     * Índice del botón de revisión de [items] (el último, cuando existe), o
     * null si el perfil activo no lo tiene. Preguntar por el índice en lugar
     * de asumir 4 deja la barra correcta aunque un perfil tenga otro orden.
     */
    fun indiceRevision(items: List<NavItem>): Int? =
        items.lastIndex.takeIf { it > PERFIL }

    /**
     * Mapea el índice tocado en la barra inferior a su destino de navegación.
     *
     * Es la única tabla índice -> ruta de la app: las pantallas reenvían el
     * índice crudo y el grafo resuelve aquí a dónde ir, de modo que ninguna
     * mantiene una copia paralela que pueda desincronizarse. En particular,
     * Perfil es siempre [PERFIL] para todo perfil (la bandeja de revisión es
     * una entrada adicional, no una sustitución), y Revisión es el único
     * destino condicionado al perfil. Devuelve la [RutaPrincipal] a la que
     * navegar, o null si el índice no corresponde a ningún destino conocido.
     */
    fun destinoDe(indice: Int, puedeRevisar: Boolean): RutaPrincipal? = when (indice) {
        INICIO -> RutaPrincipal.Inicio
        EXPEDIENTE -> RutaPrincipal.Expediente
        CARGA -> RutaPrincipal.Carga
        PERFIL -> RutaPrincipal.Perfil
        PERFIL + 1 -> if (puedeRevisar) RutaPrincipal.Revision else null
        else -> null
    }
}

/** Destinos alcanzables desde la barra inferior, sin acoplarse a las rutas del grafo. */
enum class RutaPrincipal { Inicio, Expediente, Carga, Perfil, Revision }

/**
 * Barra inferior de la zona interna.
 *
 * [items] ajusta los destinos al perfil activo; por defecto usa los cuatro
 * destinos comunes ([MainNavigationItems]). Para los perfiles que revisan,
 * pasar `mainNavigationItemsPara(puedeRevisar = true)`.
 */
@Composable
fun MainBottomNavBar(
    activeIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavItem> = MainNavigationItems
) {
    BottomNavBar(
        items = items,
        activeIndex = activeIndex,
        onItemSelected = onItemSelected,
        modifier = modifier
    )
}
