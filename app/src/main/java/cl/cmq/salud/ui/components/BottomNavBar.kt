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

/** Tres destinos comunes hasta antes de la última posición: Inicio, Expediente y Cargar. */
private val MainNavigationItems = listOf(
    NavItem(Icons.Default.Home, "Inicio", "Ir a inicio"),
    NavItem(Icons.Default.Search, "Expediente", "Consultar expediente"),
    NavItem(Icons.Default.Add, "Cargar", "Cargar documento")
)

/** Perfil: cierre de la barra para el funcionario (sin bandeja de revisión). */
private val PerfilNavItem = NavItem(Icons.Default.Person, "Perfil", "Ver mi perfil")

/** Revisión: cuarto destino, disponible solo para quien tiene bandeja (RF-15). */
private val RevisionNavItem = NavItem(
    Icons.AutoMirrored.Filled.FactCheck,
    "Revisión",
    "Abrir bandeja de revisión"
)

/**
 * Destinos de la barra inferior según el perfil activo.
 *
 * El funcionario ve `Inicio, Expediente, Cargar, Perfil` y administración suma
 * "Revisión" **antes** de Perfil: `Inicio, Expediente, Cargar, Revisión, Perfil`.
 * Perfil cierra siempre la barra (es el último botón) y Revisión se inserta
 * delante; así el orden de los tres primeros es común y cada índice de
 * [BottomNavItem] tiene un valor estable por perfil.
 */
fun mainNavigationItemsPara(puedeRevisar: Boolean): List<NavItem> =
    if (puedeRevisar) MainNavigationItems + RevisionNavItem + PerfilNavItem
    else MainNavigationItems + PerfilNavItem

/**
 * Índices canónicos de la barra inferior, en un solo lugar para no usar números sueltos.
 *
 * Los tres primeros índices son fijos (Inicio, Expediente, Cargar). [revisionDe]
 * y [perfilDe] resuelven las dos últimas posiciones, que dependen de si el
 * perfil tiene bandeja: Perfil es el último botón siempre, y Revisión va justo
 * antes cuando existe.
 */
object BottomNavItem {
    const val INICIO = 0
    const val EXPEDIENTE = 1
    const val CARGA = 2

    /**
     * Posición de "Revisión" cuando el perfil tiene bandeja (justo antes de
     * Perfil). Solo es alcanzable por quien revisa; para el resto esa posición
     * la ocupa Perfil.
     */
    const val REVISION = CARGA + 1

    /**
     * Índice de "Revisión" en [items] (justo antes de Perfil), o null si el
     * perfil activo no tiene bandeja. Se pregunta por el índice en lugar de
     * asumir uno fijo: si la barra cambia de forma, el orden se sigue aquí.
     */
    fun indiceRevision(items: List<NavItem>): Int? =
        items.indexOfFirst { it.label == "Revisión" }.takeIf { it >= 0 }

    /** Índice de "Perfil" en [items] (el último botón), o null si no está. */
    fun indicePerfil(items: List<NavItem>): Int? =
        items.indexOfFirst { it.label == "Perfil" }.takeIf { it >= 0 }

    /**
     * Mapea el índice tocado en la barra inferior a su destino de navegación.
     *
     * Es la única tabla índice -> ruta de la app: las pantallas reenvían el
     * índice crudo y el grafo resuelve aquí a dónde ir, de modo que ninguna
     * mantiene una copia paralela que pueda desincronizarse. Los tres primeros
     * destinos son comunes; las dos últimas posiciones dependen del perfil:
     * con bandeja el índice 3 es Revisión y el 4 Perfil; sin bandeja el índice
     * 3 es Perfil y no hay Revisión. Devuelve la [RutaPrincipal] a la que
     * navegar, o null si el índice no corresponde a ningún destino conocido.
     */
    fun destinoDe(indice: Int, puedeRevisar: Boolean): RutaPrincipal? = when (indice) {
        INICIO -> RutaPrincipal.Inicio
        EXPEDIENTE -> RutaPrincipal.Expediente
        CARGA -> RutaPrincipal.Carga
        REVISION -> if (puedeRevisar) RutaPrincipal.Revision else RutaPrincipal.Perfil
        REVISION + 1 -> if (puedeRevisar) RutaPrincipal.Perfil else null
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
