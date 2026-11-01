// test/cl/cmq/salud/ui/screens/home/HomeViewModelTest.kt
package cl.cmq.salud.ui.screens.home

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
 * Punto 5 del plan: Home por rol.
 *
 * Administración ve la bandeja de revisión global y puede buscar otros RUT;
 * el funcionario ve el estado de su propio expediente y no recibe contadores
 * de terceros (mínimo privilegio).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun repo() = DocumentRepository(FakeDocumentoLocalDataSource())

    private fun sesion(perfil: Perfil) = SesionActual(
        rut = if (perfil == Perfil.ADM) "12.345.678-9" else "18.765.432-7",
        nombreCompleto = if (perfil == Perfil.ADM) "Maria Gonzalez" else "Juan Pérez",
        perfil = perfil,
        correoInstitucional = "usuario@cmq.cl"
    )

    @Test
    fun `admin ve la bandeja de revision y puede buscar otros ruts`() = runTest(testDispatcher) {
        val vm = HomeViewModel(repo(), sesion(Perfil.ADM))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.puedeRevisar)
        assertTrue(state.puedeBuscarOtrosRuts)
        assertEquals("Maria Gonzalez", state.nombreUsuario)
    }

    @Test
    fun `funcionario no ve la bandeja de revision ni busca otros ruts`() = runTest(testDispatcher) {
        val vm = HomeViewModel(repo(), sesion(Perfil.FUN))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.puedeRevisar)
        assertFalse(state.puedeBuscarOtrosRuts)
        // No recibe contadores de la bandeja global
        assertEquals(0, state.validacionesPendientes)
    }

    @Test
    fun `funcionario ve el conteo de sus propios documentos`() = runTest(testDispatcher) {
        val vm = HomeViewModel(repo(), sesion(Perfil.FUN))
        advanceUntilIdle()

        // La semilla tiene 4 documentos para el RUT 18.765.432-7
        assertEquals(4, vm.uiState.value.misDocumentos)
    }

    @Test
    fun `admin no recibe el conteo de expediente propio`() = runTest(testDispatcher) {
        val vm = HomeViewModel(repo(), sesion(Perfil.ADM))
        advanceUntilIdle()

        assertEquals(0, vm.uiState.value.misDocumentos)
    }

    @Test
    fun `sin sesion no se cargan contadores`() = runTest(testDispatcher) {
        val vm = HomeViewModel(repo(), null)
        advanceUntilIdle()

        assertEquals("", vm.uiState.value.nombreUsuario)
        assertEquals(0, vm.uiState.value.misDocumentos)
    }
}
