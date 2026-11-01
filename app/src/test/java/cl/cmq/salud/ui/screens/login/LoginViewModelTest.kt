// test/cl/cmq/salud/ui/screens/login/LoginViewModelTest.kt
package cl.cmq.salud.ui.screens.login

import cl.cmq.salud.domain.model.Perfil
import cl.cmq.salud.domain.session.SesionActual
import cl.cmq.salud.domain.validation.CatalogoCuentasDemo
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
 * Punto 3 del plan de separación de perfiles: el login resuelve las
 * credenciales contra el catálogo demo y deja la sesión en el contenedor.
 *
 * Se inyecta un capturador en lugar del singleton AppContainer para comprobar
 * que la sesión correcta llega, sin depender del estado global.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private var sesionAbierta: SesionActual? = null

    private fun crearVm() = LoginViewModel(
        catalogo = CatalogoCuentasDemo,
        abrirSesion = { sesionAbierta = it }
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sesionAbierta = null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ------------------------------------------------------------------
    // Casos de éxito
    // ------------------------------------------------------------------

    @Test
    fun `credenciales de admin abren sesion con perfil ADM`() = runTest(testDispatcher) {
        val vm = crearVm()
        vm.onRutChange(CatalogoCuentasDemo.ADMIN.sesion.rut)
        vm.onPasswordChange(CatalogoCuentasDemo.ADMIN.password)

        vm.login()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isAuthenticated)
        assertNull(vm.uiState.value.errorMessage)
        assertNotNull(sesionAbierta)
        assertEquals(Perfil.ADM, sesionAbierta?.perfil)
    }

    @Test
    fun `credenciales de funcionario abren sesion con perfil FUN`() = runTest(testDispatcher) {
        val vm = crearVm()
        vm.onRutChange(CatalogoCuentasDemo.FUNCIONARIO.sesion.rut)
        vm.onPasswordChange(CatalogoCuentasDemo.FUNCIONARIO.password)

        vm.login()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isAuthenticated)
        assertEquals(Perfil.FUN, sesionAbierta?.perfil)
        assertEquals(CatalogoCuentasDemo.FUNCIONARIO.sesion.rut, sesionAbierta?.rut)
    }

    // ------------------------------------------------------------------
    // Casos de error
    // ------------------------------------------------------------------

    @Test
    fun `clave incorrecta no abre sesion y muestra mensaje`() = runTest(testDispatcher) {
        val vm = crearVm()
        vm.onRutChange(CatalogoCuentasDemo.ADMIN.sesion.rut)
        vm.onPasswordChange("clave-incorrecta")

        vm.login()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isAuthenticated)
        assertNull(sesionAbierta)
        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `rut valido sin cuenta demo no abre sesion`() = runTest(testDispatcher) {
        val vm = crearVm()
        vm.onRutChange("11.111.111-1")
        vm.onPasswordChange("cualquiera")

        vm.login()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isAuthenticated)
        assertNull(sesionAbierta)
        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `campos vacios se rechazan antes de tocar el catalogo`() = runTest(testDispatcher) {
        val vm = crearVm()
        vm.login()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isAuthenticated)
        assertNull(sesionAbierta)
        assertEquals("Ingrese RUT y contraseña", vm.uiState.value.errorMessage)
    }

    @Test
    fun `formato de rut invalido se rechaza sin consultar el catalogo`() = runTest(testDispatcher) {
        val vm = crearVm()
        vm.onRutChange("abc")
        vm.onPasswordChange("admin123")

        vm.login()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isAuthenticated)
        assertNull(sesionAbierta)
    }

    @Test
    fun `escribir tras un error limpia el mensaje`() = runTest(testDispatcher) {
        val vm = crearVm()
        vm.onRutChange(CatalogoCuentasDemo.ADMIN.sesion.rut)
        vm.onPasswordChange("mala")
        vm.login()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.errorMessage)

        vm.onPasswordChange("admin123")

        assertNull(vm.uiState.value.errorMessage)
    }
}
