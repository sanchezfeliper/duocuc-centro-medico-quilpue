// test/cl/cmq/salud/ui/screens/consulta/ConsultaViewModelTest.kt
package cl.cmq.salud.ui.screens.consulta

import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.data.local.FakeDocumentoLocalDataSource
import cl.cmq.salud.domain.model.EstadoDocumento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Semana 9.2: la pantalla P03 Consulta muestra el expediente que vive en el
 * almacenamiento local del dispositivo. Se verifica contra un doble en memoria
 * con la misma semilla que la base SQLite, sin Android ni emulador.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConsultaViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun vm() = ConsultaViewModel(DocumentRepository(FakeDocumentoLocalDataSource()))

    @Test
    fun `buscar el RUT semilla muestra su expediente y el total local`() = runTest(testDispatcher) {
        val vm = vm()
        vm.onRutChange("18.765.432-7")
        vm.onBuscar()

        val state = vm.uiState.value
        assertEquals(4, state.documentos.size)          // expediente de Juan Pérez (semilla)
        assertEquals(4, state.documentosFiltrados.size) // filtro por defecto: Todos
        assertEquals(4, state.totalEnAlmacenamiento)    // indicador de origen local (9.2)
        assertNotNull(state.funcionario)
        assertEquals("Juan Pérez", state.funcionario!!.nombreCompleto)
    }

    @Test
    fun `el filtro pendientes acota el listado sin perder el total local`() = runTest(testDispatcher) {
        val vm = vm()
        vm.onRutChange("18.765.432-7")
        vm.onBuscar()
        vm.onFiltroChange(FiltroDocumento.PENDIENTES)

        val state = vm.uiState.value
        assertTrue(state.documentosFiltrados.all {
            it.estado == EstadoDocumento.PENDIENTE ||
                it.estado == EstadoDocumento.PENDIENTE_CORRECCION
        })
        assertEquals(1, state.documentosFiltrados.size) // semilla: solo 'Certificado capacitacion'
        assertEquals(4, state.documentos.size)          // el expediente completo no se pierde
        assertEquals(4, state.totalEnAlmacenamiento)
    }

    @Test
    fun `RUT valido sin expediente informa estado vacio y conserva el total local`() = runTest(testDispatcher) {
        val vm = vm()
        vm.onRutChange("11.111.111-1") // formato y DV válidos, sin documentos
        vm.onBuscar()

        val state = vm.uiState.value
        assertTrue(state.documentos.isEmpty())
        assertNotNull(state.errorMessage)
        assertEquals(4, state.totalEnAlmacenamiento) // el dispositivo sigue mostrando lo que tiene
    }

    @Test
    fun `RUT con DV incorrecto se rechaza antes de consultar el almacenamiento`() = runTest(testDispatcher) {
        val vm = vm()
        vm.onRutChange("18.765.432-1") // DV debiera ser 7
        vm.onBuscar()

        val state = vm.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.documentos.isEmpty())
        assertEquals(0, state.totalEnAlmacenamiento) // no se consultó la base
    }
}
