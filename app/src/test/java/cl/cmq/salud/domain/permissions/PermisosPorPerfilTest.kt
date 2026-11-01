// test/cl/cmq/salud/domain/permissions/PermisosPorPerfilTest.kt
package cl.cmq.salud.domain.permissions

import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.model.Perfil
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Matriz de permisos por perfil (RF-07 / RNF-04). Regla pura del dominio:
 * no requiere Android, base de datos ni corrutinas.
 *
 * El RUT del funcionario de prueba coincide con la semilla para que la
 * propiedad del documento sea realista.
 */
class PermisosPorPerfilTest {

    private val rutFuncionario = "18.765.432-7"
    private val rutOtro = "12.345.678-9"

    // ------------------------------------------------------------------
    // Validación (RF-15 a RF-17)
    // ------------------------------------------------------------------

    @Test
    fun `solo administracion puede validar documentos`() {
        assertTrue(PermisosPorPerfil.puedeValidar(Perfil.ADM))
        assertFalse(PermisosPorPerfil.puedeValidar(Perfil.FUN))
    }

    @Test
    fun `los perfiles fuera del MVP no validan`() {
        listOf(Perfil.JEF, Perfil.RRHH, Perfil.SYS, Perfil.TIC).forEach { perfil ->
            assertFalse("$perfil no debería validar en el MVP", PermisosPorPerfil.puedeValidar(perfil))
        }
    }

    @Test
    fun `ver revision equivale a poder validar`() {
        assertTrue(PermisosPorPerfil.veRevision(Perfil.ADM))
        assertFalse(PermisosPorPerfil.veRevision(Perfil.FUN))
    }

    // ------------------------------------------------------------------
    // Búsqueda y carga a nombre de terceros (RF-12 / RF-08)
    // ------------------------------------------------------------------

    @Test
    fun `solo administracion busca el expediente de otros RUT`() {
        assertTrue(PermisosPorPerfil.puedeBuscarOtrosRuts(Perfil.ADM))
        assertFalse(PermisosPorPerfil.puedeBuscarOtrosRuts(Perfil.FUN))
    }

    @Test
    fun `solo administracion puede cargar a nombre de otro funcionario`() {
        assertTrue(PermisosPorPerfil.puedeCargarParaOtroRut(Perfil.ADM))
        assertFalse(PermisosPorPerfil.puedeCargarParaOtroRut(Perfil.FUN))
    }

    // ------------------------------------------------------------------
    // Edición y eliminación (Semana 10 + mínimo privilegio)
    // ------------------------------------------------------------------

    @Test
    fun `administracion edita cualquier documento en cualquier estado`() {
        EstadoDocumento.entries.forEach { estado ->
            assertTrue(
                "ADM debería editar un documento $estado",
                PermisosPorPerfil.puedeEditarDocumento(Perfil.ADM, estado, rutOtro, rutFuncionario)
            )
        }
    }

    @Test
    fun `funcionario edita su documento pendiente`() {
        assertTrue(
            PermisosPorPerfil.puedeEditarDocumento(
                Perfil.FUN, EstadoDocumento.PENDIENTE, rutFuncionario, rutFuncionario
            )
        )
    }

    @Test
    fun `funcionario edita su documento con correccion solicitada`() {
        assertTrue(
            PermisosPorPerfil.puedeEditarDocumento(
                Perfil.FUN, EstadoDocumento.PENDIENTE_CORRECCION, rutFuncionario, rutFuncionario
            )
        )
    }

    @Test
    fun `funcionario NO edita un documento ya aprobado`() {
        assertFalse(
            PermisosPorPerfil.puedeEditarDocumento(
                Perfil.FUN, EstadoDocumento.APROBADO, rutFuncionario, rutFuncionario
            )
        )
    }

    @Test
    fun `funcionario NO edita un documento rechazado o vencido`() {
        listOf(EstadoDocumento.RECHAZADO, EstadoDocumento.VENCIDO).forEach { estado ->
            assertFalse(
                "FUN no debería editar un documento $estado",
                PermisosPorPerfil.puedeEditarDocumento(Perfil.FUN, estado, rutFuncionario, rutFuncionario)
            )
        }
    }

    @Test
    fun `funcionario NO edita el documento de otro funcionario`() {
        assertFalse(
            PermisosPorPerfil.puedeEditarDocumento(
                Perfil.FUN, EstadoDocumento.PENDIENTE, rutOtro, rutFuncionario
            )
        )
    }

    @Test
    fun `eliminar sigue el mismo criterio que editar`() {
        // Documento propio y pendiente: se puede borrar
        assertTrue(
            PermisosPorPerfil.puedeEliminarDocumento(
                Perfil.FUN, EstadoDocumento.PENDIENTE, rutFuncionario, rutFuncionario
            )
        )
        // Documento propio pero aprobado: la decisión de jefatura manda
        assertFalse(
            PermisosPorPerfil.puedeEliminarDocumento(
                Perfil.FUN, EstadoDocumento.APROBADO, rutFuncionario, rutFuncionario
            )
        )
        // Documento ajeno: nunca
        assertFalse(
            PermisosPorPerfil.puedeEliminarDocumento(
                Perfil.FUN, EstadoDocumento.PENDIENTE, rutOtro, rutFuncionario
            )
        )
    }

    @Test
    fun `los perfiles fuera del MVP no editan ni eliminan`() {
        listOf(Perfil.JEF, Perfil.RRHH, Perfil.SYS, Perfil.TIC).forEach { perfil ->
            assertFalse(
                "$perfil no debería editar en el MVP",
                PermisosPorPerfil.puedeEditarDocumento(perfil, EstadoDocumento.PENDIENTE, rutOtro, rutOtro)
            )
        }
    }

    // ------------------------------------------------------------------
    // Bitácora (RF-19): fuera del MVP para todos
    // ------------------------------------------------------------------

    @Test
    fun `nadie ve la bitacora en el MVP`() {
        Perfil.entries.forEach { perfil ->
            assertFalse("$perfil no debería ver la bitácora en el MVP", PermisosPorPerfil.veBitacora(perfil))
        }
    }

    // ------------------------------------------------------------------
    // Robustez de la comparación de RUT
    // ------------------------------------------------------------------

    @Test
    fun `la propiedad del documento tolera espacios accidentales`() {
        assertTrue(
            PermisosPorPerfil.puedeEditarDocumento(
                Perfil.FUN, EstadoDocumento.PENDIENTE, "  $rutFuncionario  ", rutFuncionario
            )
        )
    }
}
