// ui/navigation/AppNavHost.kt
package cl.cmq.salud.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.cmq.salud.ui.screens.carga.CargaDocumentoScreen
import cl.cmq.salud.ui.screens.carga.CargaViewModel
import cl.cmq.salud.ui.screens.consulta.ConsultaExpedienteScreen
import cl.cmq.salud.ui.screens.detalle.DetalleDocumentoScreen
import cl.cmq.salud.ui.screens.home.HomeScreen
import cl.cmq.salud.ui.screens.login.LoginScreen
import cl.cmq.salud.ui.screens.validacion.ValidacionScreen

/**
 * Grafo de navegacion de la app (Meta 5).
 *
 * Unico lugar donde se cablean pantallas y transiciones: las pantallas
 * exponen eventos (lambdas) y este grafo decide a donde navegar. Los
 * Toast son feedback temporal para destinos aun no implementados
 * (P08 Bitacora); al existir, cada uno tendra su ruta propia.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current

    fun navigateToMainDestination(index: Int) {
        val route = when (index) {
            0 -> AppDestinos.Home.route
            1 -> AppDestinos.Expediente.route
            2 -> AppDestinos.Carga.route
            3 -> AppDestinos.Revision.route
            else -> return
        }
        navController.navigate(route) {
            popUpTo(AppDestinos.Home.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestinos.Login.route
    ) {
        composable(AppDestinos.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppDestinos.Home.route) {
                        // Login deja de estar en el back stack: Atras desde Home no vuelve al login
                        popUpTo(AppDestinos.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinos.Home.route) {
            HomeScreen(
                onNavigateExpediente = { navigateToMainDestination(1) },
                onNavigateCarga = { navigateToMainDestination(2) },
                onNavigateRevision = { navigateToMainDestination(3) },
                onNavigateBitacora = {
                    Toast.makeText(context, "Próximamente: Bitácora de auditoría (P08)", Toast.LENGTH_SHORT).show()
                },
                onCerrarSesion = {
                    navController.navigate(AppDestinos.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinos.Expediente.route) {
            ConsultaExpedienteScreen(
                onVolver = { navController.popBackStack() },
                onMainDestinationSelected = ::navigateToMainDestination,
                // Meta 6: Listado -> Detalle pasando el ID por la ruta
                onDocumentoClick = { idDocumento ->
                    navController.navigate(AppDestinos.Detalle.crearRuta(idDocumento))
                }
            )
        }

        // Meta 6: P04 Detalle. El ID llega como argumento de navegacion y el
        // ViewModel del destino resuelve el documento contra la fuente unica.
        composable(AppDestinos.Detalle.route) { backStackEntry ->
            val idDocumento = backStackEntry.arguments
                ?.getString(AppDestinos.Detalle.ARG_ID)
                ?.toIntOrNull() ?: 0
            DetalleDocumentoScreen(
                idDocumento = idDocumento,
                onVolver = { navController.popBackStack() },
                // Semana 10: Editar abre el formulario en modo edicion con el ID.
                onEditar = { id -> navController.navigate(AppDestinos.EditarCarga.crearRuta(id)) },
                // Tras eliminar, el detalle deja de tener sentido: vuelve al expediente.
                onEliminado = {
                    navController.navigate(AppDestinos.Expediente.route) {
                        popUpTo(AppDestinos.Detalle.route) { inclusive = true }
                    }
                }
            )
        }

        // Semana 10: formulario de carga reutilizado en modo edicion.
        composable(AppDestinos.EditarCarga.route) { backStackEntry ->
            val idDocumento = backStackEntry.arguments
                ?.getString(AppDestinos.EditarCarga.ARG_ID)
                ?.toIntOrNull() ?: 0
            CargaDocumentoScreen(
                onVolver = { navController.popBackStack() },
                onMainDestinationSelected = ::navigateToMainDestination,
                vm = viewModel(
                    key = "carga_editar_$idDocumento",
                    factory = CargaViewModel.factory(idDocumento)
                ),
                onCargaExitosa = { id ->
                    Toast.makeText(context, "Documento #$id actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    // Vuelve al expediente para ver el registro ya actualizado.
                    navController.navigate(AppDestinos.Expediente.route) {
                        popUpTo(AppDestinos.Detalle.route) { inclusive = true }
                    }
                }
            )
        }

        // P06 Validacion (RF-15 a RF-17): bandeja de jefatura conectada al
        // mismo repositorio compartido por Carga y Consulta.
        composable(AppDestinos.Revision.route) {
            ValidacionScreen(
                onVolver = { navController.popBackStack() },
                onMainDestinationSelected = ::navigateToMainDestination
            )
        }

        composable(AppDestinos.Carga.route) {
            CargaDocumentoScreen(
                onVolver = { navController.popBackStack() },
                onMainDestinationSelected = ::navigateToMainDestination,
                onCargaExitosa = { idDocumento ->
                    Toast.makeText(context, "Documento #$idDocumento registrado exitosamente", Toast.LENGTH_SHORT).show()
                    navController.navigate(AppDestinos.Home.route) {
                        // Reinicia la pila en Home: la pantalla de carga sale del back stack
                        popUpTo(AppDestinos.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
