package cl.cmq.salud.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Composición de la barra inferior por perfil.
 *
 * Regla de negocio cubierta: el botón de Revisión (RF-15) es un quinto destino
 * que solo ve quien puede revisar, y Perfil cierra siempre la barra (es el
 * último botón) para todos los perfiles. Orden:
 *
 *  - Funcionario: Inicio, Expediente, Cargar, Perfil
 *  - Administración: Inicio, Expediente, Cargar, Revisión, Perfil
 */
class BottomNavBarTest {

    @Test
    fun `perfil sin bandeja ve cuatro destinos y ninguno de revision`() {
        val items = mainNavigationItemsPara(puedeRevisar = false)

        assertEquals(4, items.size)
        assertEquals(listOf("Inicio", "Expediente", "Cargar", "Perfil"), items.map { it.label })
        assertNull(BottomNavItem.indiceRevision(items))
    }

    @Test
    fun `perfil con bandeja suma revision antes de Perfil`() {
        val items = mainNavigationItemsPara(puedeRevisar = true)

        assertEquals(5, items.size)
        assertEquals(listOf("Inicio", "Expediente", "Cargar", "Revisión", "Perfil"), items.map { it.label })
    }

    @Test
    fun `Perfil es el ultimo boton en ambos perfiles`() {
        // Regla pedida: Perfil cierra la barra siempre, con o sin Revisión.
        assertEquals("Perfil", mainNavigationItemsPara(puedeRevisar = false).last().label)
        assertEquals("Perfil", mainNavigationItemsPara(puedeRevisar = true).last().label)
    }

    @Test
    fun `Revision va inmediatamente antes de Perfil`() {
        val items = mainNavigationItemsPara(puedeRevisar = true)
        val indiceRevision = BottomNavItem.indiceRevision(items)!!
        val indicePerfil = BottomNavItem.indicePerfil(items)!!

        assertEquals(indiceRevision + 1, indicePerfil)
        assertEquals("Revisión", items[indiceRevision].label)
    }

    @Test
    fun `los tres primeros destinos son comunes a todo perfil`() {
        for (puedeRevisar in listOf(false, true)) {
            val items = mainNavigationItemsPara(puedeRevisar)
            assertEquals("Inicio", items[BottomNavItem.INICIO].label)
            assertEquals("Expediente", items[BottomNavItem.EXPEDIENTE].label)
            assertEquals("Cargar", items[BottomNavItem.CARGA].label)
        }
    }

    // --- Mapeo índice -> destino (regresión: cada botón debe navegar a algo) ---

    @Test
    fun `el indice de Perfil abre Perfil en ambos perfiles`() {
        // Funcionario: Perfil ocupa el índice 3. Administración: el 4.
        assertEquals(RutaPrincipal.Perfil, BottomNavItem.destinoDe(3, puedeRevisar = false))
        assertEquals(RutaPrincipal.Perfil, BottomNavItem.destinoDe(4, puedeRevisar = true))
    }

    @Test
    fun `el indice 3 es Revision con bandeja y Perfil sin bandeja`() {
        val indice = BottomNavItem.REVISION

        assertEquals(RutaPrincipal.Revision, BottomNavItem.destinoDe(indice, puedeRevisar = true))
        assertEquals(RutaPrincipal.Perfil, BottomNavItem.destinoDe(indice, puedeRevisar = false))
    }

    @Test
    fun `los primeros destinos no dependen del perfil`() {
        for (puedeRevisar in listOf(false, true)) {
            assertEquals(RutaPrincipal.Inicio, BottomNavItem.destinoDe(BottomNavItem.INICIO, puedeRevisar))
            assertEquals(RutaPrincipal.Expediente, BottomNavItem.destinoDe(BottomNavItem.EXPEDIENTE, puedeRevisar))
            assertEquals(RutaPrincipal.Carga, BottomNavItem.destinoDe(BottomNavItem.CARGA, puedeRevisar))
        }
    }

    @Test
    fun `el quinto indice no existe sin bandeja de revision`() {
        assertNull(BottomNavItem.destinoDe(BottomNavItem.REVISION + 1, puedeRevisar = false))
    }

    @Test
    fun `un indice desconocido no resuelve a ningun destino`() {
        assertNull(BottomNavItem.destinoDe(-1, puedeRevisar = true))
        assertNull(BottomNavItem.destinoDe(99, puedeRevisar = true))
    }

    @Test
    fun `el mapeo de destino cubre todos los botones visibles`() {
        // Cada botón que se pinta debe navegar a algo: si la barra y el mapeo
        // se separan, este test falla en vez de que un toque no haga nada.
        for (puedeRevisar in listOf(false, true)) {
            val items = mainNavigationItemsPara(puedeRevisar)
            items.indices.forEach { index ->
                assertNotNull(
                    "El botón $index (${items[index].label}) no tiene destino",
                    BottomNavItem.destinoDe(index, puedeRevisar)
                )
            }
        }
    }
}
