// test/cl/cmq/salud/ui/screens/validacion/ValidacionViewModelTest.kt
package cl.cmq.salud.ui.screens.validacion

import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.data.local.FakeDocumentoLocalDataSource
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Meta 8: casos validos e invalidos de la decision de validacion (P06,
 * RF-16/RF-17). El comentario es obligatorio para rechazar o corregir.
 *
 * Los ViewModels lanzan corrutinas con delay() simulado (latencia de red):
 * con runTest + advanceUntilIdle el tiempo virtual avanza y las operaciones
 * completan sin esperas reales ni emulador.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ValidacionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repo = DocumentRepository(FakeDocumentoLocalDataSource())
    private lateinit var vm: ValidacionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher) // viewModelScope usa Dispatchers.Main
        vm = ValidacionViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Crea un pendiente propio y refresca la bandeja: no depende de otros tests. */
    private suspend fun TestScope.pendienteNuevo(): Documento {
        val doc = repo.guardarDocumento(
            Documento(
                idDocumento = 0, idFuncionario = 0, rutFuncionario = "20.111.222-2",
                tipo = "PDF", nombreArchivo = "pendiente_vm_${System.nanoTime()}.pdf",
                estado = EstadoDocumento.PENDIENTE, fechaCarga = "2026-10-10",
                usuarioCarga = "Test", descripcion = "Documento de prueba"
            )
        )
        vm.cargarPendientes()
        advanceUntilIdle()
        return doc
    }

    // ------------------------------------------------------------------
    // Casos VALIDOS
    // ------------------------------------------------------------------

    @Test
    fun `aprobar documento pendiente lo saca de la bandeja y notifica exito`() = runTest(testDispatcher) {
        val doc = pendienteNuevo()
        vm.onSeleccionar(doc.idDocumento)

        vm.decidir(EstadoDocumento.APROBADO)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.pendientes.none { it.idDocumento == doc.idDocumento })
        assertTrue(state.successEventId > 0)
        assertTrue(state.ultimoMensajeExito!!.contains("#${doc.idDocumento}"))
        assertEquals(EstadoDocumento.APROBADO, repo.buscarPorId(doc.idDocumento)!!.estado)
    }

    @Test
    fun `rechazar con comentario registra RECHAZADO y el motivo`() = runTest(testDispatcher) {
        val doc = pendienteNuevo()
        vm.onSeleccionar(doc.idDocumento)
        vm.onComentarioChange("El certificado no es legible, reenviar escaneado")

        vm.decidir(EstadoDocumento.RECHAZADO)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.successEventId > 0)
        val actualizado = repo.buscarPorId(doc.idDocumento)
        assertEquals(EstadoDocumento.RECHAZADO, actualizado!!.estado)
        assertEquals("El certificado no es legible, reenviar escaneado", actualizado.comentarioValidacion)
    }

    @Test
    fun `solicitar correccion con comentario valido marca PENDIENTE_CORRECCION`() = runTest(testDispatcher) {
        val doc = pendienteNuevo()
        vm.onSeleccionar(doc.idDocumento)
        vm.onComentarioChange("Falta la firma del funcionario en la página 2")

        vm.decidir(EstadoDocumento.PENDIENTE_CORRECCION)
        advanceUntilIdle()

        assertEquals(EstadoDocumento.PENDIENTE_CORRECCION, repo.buscarPorId(doc.idDocumento)!!.estado)
    }

    @Test
    fun `seleccionar dos veces el mismo documento cancela la seleccion`() = runTest(testDispatcher) {
        val doc = pendienteNuevo()
        vm.onSeleccionar(doc.idDocumento)
        vm.onSeleccionar(doc.idDocumento)
        assertNull(vm.uiState.value.seleccionado)
    }

    // ------------------------------------------------------------------
    // Casos INVALIDOS
    // ------------------------------------------------------------------

    @Test
    fun `rechazar sin comentario muestra error y no guarda nada`() = runTest(testDispatcher) {
        val doc = pendienteNuevo()
        vm.onSeleccionar(doc.idDocumento)

        vm.decidir(EstadoDocumento.RECHAZADO)

        val state = vm.uiState.value
        assertNotNull(state.comentarioError)                    // mensaje visible
        assertTrue(state.comentarioError!!.contains("obligatorio"))
        assertTrue(state.successEventId == 0L)                  // nada se proceso
        // El documento sigue pendiente en el repositorio
        assertEquals(EstadoDocumento.PENDIENTE, repo.buscarPorId(doc.idDocumento)!!.estado)
    }

    @Test
    fun `solicitar correccion con comentario muy corto muestra error`() = runTest(testDispatcher) {
        val doc = pendienteNuevo()
        vm.onSeleccionar(doc.idDocumento)
        vm.onComentarioChange("abc")

        vm.decidir(EstadoDocumento.PENDIENTE_CORRECCION)

        assertNotNull(vm.uiState.value.comentarioError)
        assertTrue(vm.uiState.value.successEventId == 0L)
    }
}
