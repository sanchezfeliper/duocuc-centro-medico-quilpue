// test/cl/cmq/salud/ui/screens/perfil/PerfilViewModelTest.kt
package cl.cmq.salud.ui.screens.perfil

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Punto 6 del plan: la pantalla Perfil muestra los datos de la persona.
 *
 * La identidad sale de la sesión y los datos laborales se completan leyendo
 * E2 Funcionario de la base por RUT (no de un objeto en memoria).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModelTest {

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
        rut = "18.765.432-7",   // RUT de la semilla: tiene funcionario en la base
        nombreCompleto = "Juan Pérez",
        perfil = Perfil.FUN,
        correoInstitucional = "juan.perez@cmq.cl"
    )

    @Test
    fun `muestra la identidad de la sesion`() {
        val vm = PerfilViewModel(
            repo = DocumentRepository(FakeDocumentoLocalDataSource()),
            sesion = sesionFuncionario()
        )

        val state = vm.uiState.value
        assertEquals("Juan Pérez", state.nombreCompleto)
        assertEquals("18.765.432-7", state.rut)
        assertEquals("juan.perez@cmq.cl", state.correoInstitucional)
        assertEquals("Funcionario", state.perfilNombre)
    }

    @Test
    fun `completa los datos laborales desde la base por rut`() = runTest(testDispatcher) {
        val vm = PerfilViewModel(
            repo = DocumentRepository(FakeDocumentoLocalDataSource()),
            sesion = sesionFuncionario()
        )
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.tieneDatosLaborales)
        assertEquals("Enfermero", state.cargo)
        assertEquals("CESFAM Quilpué", state.centroSalud)
        assertFalse(state.cargando)
    }

    @Test
    fun `rut sin funcionario en la base deja los datos laborales vacios`() = runTest(testDispatcher) {
        val sesionSinRegistro = SesionActual(
            rut = "11.111.111-1",
            nombreCompleto = "Sin Registro",
            perfil = Perfil.FUN,
            correoInstitucional = "sin.registro@cmq.cl"
        )
        val vm = PerfilViewModel(
            repo = DocumentRepository(FakeDocumentoLocalDataSource()),
            sesion = sesionSinRegistro
        )
        advanceUntilIdle()

        assertFalse(vm.uiState.value.tieneDatosLaborales)
    }
}
