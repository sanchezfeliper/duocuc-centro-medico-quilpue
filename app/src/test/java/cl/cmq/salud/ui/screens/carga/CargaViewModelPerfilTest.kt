// test/cl/cmq/salud/ui/screens/carga/CargaViewModelPerfilTest.kt
package cl.cmq.salud.ui.screens.carga

import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.data.local.FakeDocumentoLocalDataSource
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Punto 7 del plan: el formulario de carga fija el RUT del funcionario.
 *
 * Con perfil FUN el campo viene bloqueado con el RUT de la sesión y cualquier
 * intento de cambio se ignora; con perfil ADM el RUT sigue siendo editable
 * (RF-08/RF-09 / mínimo privilegio).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CargaViewModelPerfilTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sesionFuncionario() = SesionActual(
        rut = "18.765.432-7",
        nombreCompleto = "Juan Pérez",
        perfil = Perfil.FUN,
        correoInstitucional = "juan.perez@cmq.cl"
    )

    private fun sesionAdmin() = SesionActual(
        rut = "12.345.678-9",
        nombreCompleto = "Maria Gonzalez",
        perfil = Perfil.ADM,
        correoInstitucional = "maria.gonzalez@cmq.cl"
    )

    private fun vmCon(sesion: SesionActual?) = CargaViewModel(
        repo = DocumentRepository(FakeDocumentoLocalDataSource()),
        sesion = sesion
    )

    @Test
    fun `funcionario parte con su rut fijo y bloqueado`() {
        val vm = vmCon(sesionFuncionario())

        val state = vm.uiState.value
        assertTrue(state.rutFijo)
        assertEquals("18.765.432-7", state.rut)
    }

    @Test
    fun `funcionario no puede cambiar su rut aunque lo intente`() {
        val vm = vmCon(sesionFuncionario())

        vm.onRutChange("12.345.678-9")

        assertEquals("18.765.432-7", vm.uiState.value.rut)
    }

    @Test
    fun `cargar otro conserva el rut fijo del funcionario`() = runTest(testDispatcher) {
        val vm = vmCon(sesionFuncionario())
        vm.cargarOtro()

        assertTrue(vm.uiState.value.rutFijo)
        assertEquals("18.765.432-7", vm.uiState.value.rut)
    }

    @Test
    fun `admin puede escribir el rut de cualquier funcionario`() {
        val vm = vmCon(sesionAdmin())

        assertFalse(vm.uiState.value.rutFijo)
        vm.onRutChange("18.765.432-7")

        assertEquals("18.765.432-7", vm.uiState.value.rut)
    }

    @Test
    fun `sin sesion el rut es editable como antes`() {
        val vm = vmCon(null)

        assertFalse(vm.uiState.value.rutFijo)
    }

    @Test
    fun `funcionario guarda documento asociado a su propio rut`() = runTest(testDispatcher) {
        val vm = vmCon(sesionFuncionario())
        // No se toca el RUT: viene fijo. Se completa el resto del formulario.
        vm.onFileSelected("content://test/certificado.pdf", "certificado.pdf", "application/pdf")
        vm.onDescripcionChange("Certificado de capacitación del funcionario")
        vm.onAutenticidadChange(true)

        vm.guardarDocumento()
        advanceUntilIdle()

        val creado = vm.uiState.value.documentoCreado
        assertNotNull(creado)
        assertEquals("18.765.432-7", creado!!.rutFuncionario)
    }
}
