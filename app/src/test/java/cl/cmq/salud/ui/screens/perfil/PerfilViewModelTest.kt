package cl.cmq.salud.ui.screens.perfil

import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.data.local.FakeDocumentoLocalDataSource
import cl.cmq.salud.domain.model.Documento
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias del PerfilViewModel (RF-30 y RF-31).
 *
 * El ViewModel se prueba contra un repositorio en memoria (FakeDocumentoLocalDataSource)
 * con la misma semilla ficticia que la app, sin Android ni emulador.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repo = DocumentRepository(FakeDocumentoLocalDataSource())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun cargarPerfil(vm: PerfilViewModel) {
        vm.cargarDatos()
        advanceUntilIdle()
    }

    @Test
    fun `cargarDatos carga la ficha del usuario y su listado de documentos`() = runTest(testDispatcher) {
        // El usuario de sesión coincide con la semilla Room: 18.765.432-7 (Juan Pérez)
        val vm = PerfilViewModel(sessionRut = "18.765.432-7", repo = repo)
        cargarPerfil(vm)

        val state = vm.uiState.value
        assertEquals("18.765.432-7", state.rut)
        assertEquals("Juan Pérez", state.nombreCompleto)
        assertEquals("Enfermero", state.cargo)
        assertEquals("CESFAM Quilpué", state.centroSalud)
        assertEquals("juan.perez@cmq.cl", state.correo)
        assertEquals("FUN", state.perfilRol)
        assertNotNull(state.documentos)
        assertTrue(state.documentos.isNotEmpty())
    }

    @Test
    fun `el listado de documentos esta ordenado por fechaCarga descendente`() = runTest(testDispatcher) {
        val vm = PerfilViewModel(sessionRut = "18.765.432-7", repo = repo)
        cargarPerfil(vm)

        val documentos = vm.uiState.value.documentos
        assertTrue("Se esperaba al menos 2 documentos", documentos.size >= 2)
        for (i in 1 until documentos.size) {
            assertTrue("La fecha de carga no esta en orden descendente en ${i-1} y $i") {
                documentos[i - 1].fechaCarga >= documentos[i].fechaCarga
            }
        }
    }

    @Test
    fun `el documento PENDIENTE aparece con estado correcto`() = runTest(testDispatcher) {
        val vm = PerfilViewModel(sessionRut = "18.765.432-7", repo = repo)
        cargarPerfil(vm)

        val pendiente = vm.uiState.value.documentos.firstOrNull { it.estado == EstadoDocumento.PENDIENTE }
        assertNotNull(pendiente)
        assertEquals("Certificado_Capacitacion.pdf", pendiente!!.nombreArchivo)
    }

    @Test
    fun `si el usuario no existe en Room el rut cae al valor de sesion`() = runTest(testDispatcher) {
        val vm = PerfilViewModel(sessionRut = "99.999.999-9", repo = repo) // no existe en la semilla
        cargarPerfil(vm)

        val state = vm.uiState.value
        assertEquals("99.999.999-9", state.rut)
        assertEquals("99.999.999-9", state.nombreCompleto) // vacio
        assertTrue(state.documentos.isEmpty())
    }

    @Test
    fun `el estado de carga vuelve a false despues de cargar`() = runTest(testDispatcher) {
        val vm = PerfilViewModel(sessionRut = "18.765.432-7", repo = repo)
        cargarPerfil(vm)

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
    }
}
