// test/cl/cmq/salud/domain/validation/CatalogoCuentasDemoTest.kt
package cl.cmq.salud.domain.validation

import cl.cmq.salud.domain.model.Perfil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Cuentas de demostración (S-01): casos válidos e inválidos del login simulado.
 * Prueba pura de dominio, sin Android ni corrutinas.
 */
class CatalogoCuentasDemoTest {

    @Test
    fun `credenciales de administracion resuelven al perfil ADM`() {
        val sesion = CatalogoCuentasDemo.resolver("12.345.678-9", "admin123")

        assertNotNull(sesion)
        assertEquals(Perfil.ADM, sesion!!.perfil)
        assertEquals("12.345.678-9", sesion.rut)
    }

    @Test
    fun `credenciales de funcionario resuelven al perfil FUN`() {
        val sesion = CatalogoCuentasDemo.resolver("18.765.432-7", "funcionario123")

        assertNotNull(sesion)
        assertEquals(Perfil.FUN, sesion!!.perfil)
        // El RUT coincide con el de la semilla, para que su expediente ya exista
        assertEquals("18.765.432-7", sesion.rut)
    }

    @Test
    fun `RUT valido con contrasena incorrecta no resuelve`() {
        assertNull(CatalogoCuentasDemo.resolver("12.345.678-9", "clave-mala"))
    }

    @Test
    fun `RUT desconocido no resuelve aunque la contrasena exista`() {
        assertNull(CatalogoCuentasDemo.resolver("20.111.222-2", "admin123"))
    }

    @Test
    fun `el RUT se resuelve ignorando espacios alrededor`() {
        assertNotNull(CatalogoCuentasDemo.resolver("  18.765.432-7 ", "funcionario123"))
    }

    @Test
    fun `cada cuenta describe su perfil y RUT sin exponer la contrasena`() {
        val descripcion = CatalogoCuentasDemo.ADMIN.descripcion

        assertEquals("Personal administrativo · RUT 12.345.678-9", descripcion)
        assertEquals(false, descripcion.contains("admin123"))
    }

    @Test
    fun `el catalogo expone exactamente las dos cuentas del MVP`() {
        assertEquals(2, CatalogoCuentasDemo.cuentas.size)
        assertEquals(
            listOf(Perfil.ADM, Perfil.FUN),
            CatalogoCuentasDemo.cuentas.map { it.sesion.perfil }
        )
    }
}
