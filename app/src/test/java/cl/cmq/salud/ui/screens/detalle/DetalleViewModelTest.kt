// test/cl/cmq/salud/ui/screens/detalle/DetalleViewModelTest.kt
package cl.cmq.salud.ui.screens.detalle

import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.data.local.FakeDocumentoLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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
        val vm = DetalleViewModel(repo, idDocumento = 1)
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.documento)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `eliminar requiere confirmacion explicita antes de borrar`() = runTest(testDispatcher) {
        val repo = DocumentRepository(FakeDocumentoLocalDataSource())
        val vm = DetalleViewModel(repo, idDocumento = 1)
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
        val vm = DetalleViewModel(repo, idDocumento = 1)
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
        val vm = DetalleViewModel(repo, idDocumento = 99999)
        advanceUntilIdle()

        vm.eliminarDocumento()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.documentoEliminado)
        assertNotNull(vm.uiState.value.errorEliminar)
    }
}
