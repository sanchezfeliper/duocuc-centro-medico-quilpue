package cl.cmq.salud.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Composición de la barra inferior por perfil.
 *
 * Regla de negocio cubierta: el botón de Revisión (RF-15) es un quinto destino
 * que solo ve quien puede revisar; Perfil deja de ser exclusivo de los perfiles
 * sin bandeja y aparece siempre.
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
    fun `perfil con bandeja suma el quinto boton de revision`() {
        val items = mainNavigationItemsPara(puedeRevisar = true)

        assertEquals(5, items.size)
        assertEquals("Revisión", items.last().label)
        assertEquals(4, BottomNavItem.indiceRevision(items))
    }

    @Test
    fun `perfil sigue siendo el cuarto destino aunque exista revision`() {
        val items = mainNavigationItemsPara(puedeRevisar = true)

        assertEquals("Perfil", items[BottomNavItem.PERFIL].label)
        assertEquals("Cargar", items[BottomNavItem.CARGA].label)
        assertEquals("Expediente", items[BottomNavItem.EXPEDIENTE].label)
        assertEquals("Inicio", items[BottomNavItem.INICIO].label)
    }

    // --- Mapeo índice -> destino (regresión: Perfil debe abrirse desde la barra) ---

    @Test
    fun `el indice de Perfil abre Perfil para ambos perfiles`() {
        // Regresión: antes Home cableaba el índice 3 a Revisión (solo admin) y
        // Perfil no se abría desde ahí. Perfil es un destino para todo perfil.
        assertEquals(RutaPrincipal.Perfil, BottomNavItem.destinoDe(BottomNavItem.PERFIL, puedeRevisar = false))
        assertEquals(RutaPrincipal.Perfil, BottomNavItem.destinoDe(BottomNavItem.PERFIL, puedeRevisar = true))
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
    fun `Revision solo resuelve para quien puede revisar`() {
        val indiceRevision = BottomNavItem.PERFIL + 1

        assertEquals(RutaPrincipal.Revision, BottomNavItem.destinoDe(indiceRevision, puedeRevisar = true))
        assertNull(BottomNavItem.destinoDe(indiceRevision, puedeRevisar = false))
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
