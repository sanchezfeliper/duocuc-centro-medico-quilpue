// test/cl/cmq/salud/ui/screens/carga/CargaViewModelTest.kt
package cl.cmq.salud.ui.screens.carga

import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.data.local.FakeDocumentoLocalDataSource
import cl.cmq.salud.domain.model.EstadoDocumento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 * Meta 8: ciclo Ingresar -> Validar -> Procesar -> Entregar respuesta del
 * formulario de carga (Meta 2/3), verificado sin emulador. El guardado usa
 * delay() simulado: runTest + advanceUntilIdle completa el procesamiento.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CargaViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun cargarFormularioValido(vm: CargaViewModel) {
        vm.onRutChange("18.765.432-7")          // DV correcto (Módulo 11)
        vm.onNombreArchivoChange("contrato_juan_perez.pdf")
        vm.onDescripcionChange("Contrato vigente del funcionario")
        vm.onAutenticidadChange(true)
    }

    // ------------------------------------------------------------------
    // Casos VALIDOS
    // ------------------------------------------------------------------

    @Test
    fun `formulario completo valido registra el documento y muestra el dialogo de exito`() = runTest(testDispatcher) {
        val vm = CargaViewModel(DocumentRepository(FakeDocumentoLocalDataSource()))
        cargarFormularioValido(vm)

        vm.guardarDocumento()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNull(state.generalError)
        assertNotNull(state.documentoCreado)
        assertTrue(state.showSuccessDialog)
        // El RUT ingresado determina el expediente (Meta 6)
        assertEquals("18.765.432-7", state.documentoCreado!!.rutFuncionario)
        // Sin switch activo queda VIGENTE
        assertEquals(EstadoDocumento.VIGENTE, state.documentoCreado!!.estado)
    }

    @Test
    fun `switch de derivacion a jefatura asigna estado PENDIENTE`() = runTest(testDispatcher) {
        val vm = CargaViewModel(DocumentRepository(FakeDocumentoLocalDataSource()))
        cargarFormularioValido(vm)
        vm.onRequiereValidacionChange(true)

        vm.guardarDocumento()
        advanceUntilIdle()

        assertEquals(EstadoDocumento.PENDIENTE, vm.uiState.value.documentoCreado!!.estado)
    }

    // ------------------------------------------------------------------
    // Casos INVALIDOS (ejemplo de la pauta: campo obligatorio vacío)
    // ------------------------------------------------------------------

    @Test
    fun `guardar con formulario vacio muestra errores por campo y no procesa`() = runTest(testDispatcher) {
        val vm = CargaViewModel(DocumentRepository(FakeDocumentoLocalDataSource()))

        vm.guardarDocumento()

        val state = vm.uiState.value
        assertNotNull(state.rutError)
        assertNotNull(state.nombreArchivoError)
        assertNotNull(state.descripcionError)
        assertNotNull(state.autenticidadError)
        assertNotNull(state.generalError)
        assertNull(state.documentoCreado)   // nada se guardo
        assertTrue(!state.isSubmitting)
    }

    @Test
    fun `nombre sin extension es rechazado con mensaje`() = runTest(testDispatcher) {
        val vm = CargaViewModel(DocumentRepository(FakeDocumentoLocalDataSource()))
        vm.onRutChange("18.765.432-7")
        vm.onNombreArchivoChange("Reserva laboratorio")
        vm.onDescripcionChange("Documento de prueba valido")
        vm.onAutenticidadChange(true)

        vm.guardarDocumento()

        assertNotNull(vm.uiState.value.nombreArchivoError)
        assertNull(vm.uiState.value.documentoCreado)
    }

    @Test
    fun `rut con digito verificador incorrecto es rechazado`() = runTest(testDispatcher) {
        val vm = CargaViewModel(DocumentRepository(FakeDocumentoLocalDataSource()))
        vm.onRutChange("18.765.432-1") // DV debiera ser 7
        vm.onNombreArchivoChange("contrato.pdf")
        vm.onDescripcionChange("Documento de prueba valido")
        vm.onAutenticidadChange(true)

        vm.guardarDocumento()

        assertNotNull(vm.uiState.value.rutError)
        assertTrue(vm.uiState.value.rutError!!.contains("dígito verificador"))
        assertNull(vm.uiState.value.documentoCreado)
    }

    // ------------------------------------------------------------------
    // Semana 9.3: recurso nativo camara (RF-09)
    // ------------------------------------------------------------------

    @Test
    fun `origen camara sin captura no permite registrar`() = runTest(testDispatcher) {
        val vm = CargaViewModel(DocumentRepository(FakeDocumentoLocalDataSource()))
        vm.onRutChange("18.765.432-7")
        vm.onOrigenChange(OrigenDocumento.CAMARA)
        vm.onDescripcionChange("Documento de prueba valido")
        vm.onAutenticidadChange(true)

        vm.guardarDocumento()

        val state = vm.uiState.value
        assertNotNull(state.fotoError)
        assertTrue(state.camposConError.contains("Fotografía del documento"))
        assertNull(state.documentoCreado)   // nada se guardo
    }

    @Test
    fun `captura con camara asocia la foto y registra el documento`() = runTest(testDispatcher) {
        val vm = CargaViewModel(DocumentRepository(FakeDocumentoLocalDataSource()))
        vm.onRutChange("18.765.432-7")
        vm.onOrigenChange(OrigenDocumento.CAMARA)
        vm.onFotoCapturada("fotos/captura_20261012_120000.jpg", "abc123")
        vm.onDescripcionChange("Documento de prueba valido")
        vm.onAutenticidadChange(true)

        vm.guardarDocumento()
        advanceUntilIdle()

        val doc = vm.uiState.value.documentoCreado
        assertNotNull(doc)
        assertEquals("fotos/captura_20261012_120000.jpg", doc!!.rutaLocal)
        assertEquals("abc123", doc.hashArchivo)          // huella SHA-256 (RF-11)
        assertEquals("captura_20261012_120000.jpg", doc.nombreArchivo)
        assertEquals("JPG", doc.tipo)                    // derivado de la extension de la captura
        assertNull(vm.uiState.value.fotoError)
    }

    @Test
    fun `volver a archivo digital descarta la captura tomada`() = runTest(testDispatcher) {
        val vm = CargaViewModel(DocumentRepository(FakeDocumentoLocalDataSource()))
        vm.onOrigenChange(OrigenDocumento.CAMARA)
        vm.onFotoCapturada("fotos/captura_x.jpg", "hashX")

        vm.onOrigenChange(OrigenDocumento.DIGITAL)

        val state = vm.uiState.value
        assertNull(state.fotoRuta)
        assertNull(state.fotoHash)
        assertEquals("", state.nombreArchivo) // el nombre provenia de la captura y se limpia
    }
}
