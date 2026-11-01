// test/cl/cmq/salud/ui/screens/detalle/DetalleViewModelTest.kt
package cl.cmq.salud.ui.screens.detalle

import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.data.local.FakeDocumentoLocalDataSource
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.model.Perfil
import cl.cmq.salud.domain.session.SesionActual
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Semana 10: eliminación de un documento desde P04 Detalle (CRUD - Delete).
 * Se prueba contra el doble en memoria con la misma semilla que SQLite,
 * sin Android ni emulador.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetalleViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    /** Sesión de administración: puede operar sobre cualquier expediente (RF-07). */
    private val sesionAdmin = SesionActual(
        rut = "12.345.678-9",
        nombreCompleto = "Maria Gonzalez",
        perfil = Perfil.ADM,
        correoInstitucional = "maria.gonzalez@cmq.cl"
    )

    /** Sesión del funcionario dueño del expediente semilla (RUT 18.765.432-7). */
    private val sesionFuncionario = SesionActual(
        rut = "18.765.432-7",
        nombreCompleto = "Juan Pérez",
        perfil = Perfil.FUN,
        correoInstitucional = "juan.perez@cmq.cl"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `detalle carga el documento existente`() = runTest(testDispatcher) {
        val repo = DocumentRepository(FakeDocumentoLocalDataSource())
        val vm = DetalleViewModel(repo, idDocumento = 1, sesion = sesionAdmin)
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.documento)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `eliminar requiere confirmacion explicita antes de borrar`() = runTest(testDispatcher) {
        val repo = DocumentRepository(FakeDocumentoLocalDataSource())
        val vm = DetalleViewModel(repo, idDocumento = 1, sesion = sesionAdmin)
        advanceUntilIdle()

        vm.abrirConfirmacionEliminar()
        assertTrue(vm.uiState.value.showConfirmarEliminar)
        // Aún no se ha borrado nada
        assertNotNull(repo.buscarPorId(1))

        vm.cancelarEliminar()
        assertFalse(vm.uiState.value.showConfirmarEliminar)
        assertNotNull(repo.buscarPorId(1))
    }

    @Test
    fun `confirmar eliminacion borra el documento de la fuente local`() = runTest(testDispatcher) {
        val repo = DocumentRepository(FakeDocumentoLocalDataSource())
        val antes = repo.contarDocumentos()
        val vm = DetalleViewModel(repo, idDocumento = 1, sesion = sesionAdmin)
        advanceUntilIdle()

        vm.abrirConfirmacionEliminar()
        vm.eliminarDocumento()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.documentoEliminado)
        assertNull(repo.buscarPorId(1))
        assertTrue(repo.contarDocumentos() == antes - 1)
    }

    @Test
    fun `eliminar un ID ya inexistente reporta error y no marca exito`() = runTest(testDispatcher) {
        val repo = DocumentRepository(FakeDocumentoLocalDataSource())
        val vm = DetalleViewModel(repo, idDocumento = 99999, sesion = sesionAdmin)
        advanceUntilIdle()

        vm.eliminarDocumento()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.documentoEliminado)
        assertNotNull(vm.uiState.value.errorEliminar)
    }

    // ------------------------------------------------------------------
    // Separacion de perfiles (punto 9): permisos por estado y propiedad
    // ------------------------------------------------------------------

    @Test
    fun `administracion puede editar y eliminar cualquier documento`() = runTest(testDispatcher) {
        val repo = DocumentRepository(FakeDocumentoLocalDataSource())
        // Documento #3 (APROBADO): solo administración puede tocarlo.
        val vm = DetalleViewModel(repo, idDocumento = 3, sesion = sesionAdmin)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.puedeEditar)
        assertTrue(vm.uiState.value.puedeEliminar)
    }

    @Test
    fun `el funcionario puede editar y eliminar su documento pendiente`() = runTest(testDispatcher) {
        val repo = DocumentRepository(FakeDocumentoLocalDataSource())
        // Documento #2 (PENDIENTE) del propio funcionario Juan Pérez.
        val vm = DetalleViewModel(repo, idDocumento = 2, sesion = sesionFuncionario)
        advanceUntilIdle()

        assertEquals(EstadoDocumento.PENDIENTE, vm.uiState.value.documento!!.estado)
        assertTrue(vm.uiState.value.puedeEditar)
        assertTrue(vm.uiState.value.puedeEliminar)
    }

    @Test
    fun `el funcionario no puede editar ni eliminar un documento ya aprobado`() = runTest(testDispatcher) {
        val repo = DocumentRepository(FakeDocumentoLocalDataSource())
        // Documento #3 (APROBADO), aunque sea suyo.
        val vm = DetalleViewModel(repo, idDocumento = 3, sesion = sesionFuncionario)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.puedeEditar)
        assertFalse(vm.uiState.value.puedeEliminar)
    }

    @Test
    fun `el funcionario no puede eliminar su documento vigente`() = runTest(testDispatcher) {
        val repo = DocumentRepository(FakeDocumentoLocalDataSource())
        // Documento #1 (VIGENTE) del propio funcionario.
        val vm = DetalleViewModel(repo, idDocumento = 1, sesion = sesionFuncionario)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.puedeEliminar)
    }

    @Test
    fun `abrir la confirmacion de borrado sin permiso no habilita el dialogo`() = runTest(testDispatcher) {
        val repo = DocumentRepository(FakeDocumentoLocalDataSource())
        val vm = DetalleViewModel(repo, idDocumento = 1, sesion = sesionFuncionario)
        advanceUntilIdle()

        vm.abrirConfirmacionEliminar()

        assertFalse(vm.uiState.value.showConfirmarEliminar)
        assertNotNull(vm.uiState.value.errorEliminar)
    }

    @Test
    fun `eliminar sin permiso no borra el documento aunque se invoque el metodo`() = runTest(testDispatcher) {
        val repo = DocumentRepository(FakeDocumentoLocalDataSource())
        val vm = DetalleViewModel(repo, idDocumento = 3, sesion = sesionFuncionario)
        advanceUntilIdle()

        // Se salta el paso de confirmacion a propósito (defensa en profundidad).
        vm.eliminarDocumento()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.documentoEliminado)
        assertNotNull(repo.buscarPorId(3)) // el documento sigue ahí
        assertNotNull(vm.uiState.value.errorEliminar)
    }

    @Test
    fun `sin sesion no se ofrecen acciones de gestion`() = runTest(testDispatcher) {
        val repo = DocumentRepository(FakeDocumentoLocalDataSource())
        val vm = DetalleViewModel(repo, idDocumento = 1, sesion = null)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.puedeEditar)
        assertFalse(vm.uiState.value.puedeEliminar)
    }
}
