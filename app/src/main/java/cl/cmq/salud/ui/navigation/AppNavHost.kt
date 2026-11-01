// ui/navigation/AppNavHost.kt
package cl.cmq.salud.ui.navigation

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.cmq.salud.di.AppContainer
import cl.cmq.salud.domain.permissions.PermisosPorPerfil
import cl.cmq.salud.domain.session.SesionActual
import cl.cmq.salud.ui.screens.carga.CargaDocumentoScreen
import cl.cmq.salud.ui.screens.carga.CargaViewModel
import cl.cmq.salud.ui.screens.consulta.ConsultaExpedienteScreen
import cl.cmq.salud.ui.screens.detalle.DetalleDocumentoScreen
import cl.cmq.salud.ui.screens.home.HomeScreen
import cl.cmq.salud.ui.screens.login.LoginScreen
import cl.cmq.salud.ui.screens.perfil.PerfilScreen
import cl.cmq.salud.ui.screens.validacion.ValidacionScreen
import cl.cmq.salud.ui.theme.Grey
import cl.cmq.salud.ui.theme.Navy
import cl.cmq.salud.ui.theme.Spruce

/**
 * Grafo de navegacion de la app (Meta 5).
 *
 * Unico lugar donde se cablean pantallas y transiciones: las pantallas
 * exponen eventos (lambdas) y este grafo decide a donde navegar. Los
 * Toast son feedback temporal para destinos aun no implementados
 * (P08 Bitacora); al existir, cada uno tendra su ruta propia.
 *
 * Separacion de perfiles: el grafo observa la sesion de [AppContainer] y
 * protege los destinos. Sin sesion no se entra a ninguna pantalla interna, y
 * los destinos reservados (revision) exigen ademas el permiso del perfil. Es
 * defensa en profundidad: ocultar un boton no basta, la ruta se valida aqui.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sesion by AppContainer.sesionActual.collectAsState()

    // Guardia de sesion: si la sesion se cierra (o nunca se abrio) estando en
    // una pantalla interna, se vuelve al login limpiando la pila.
    LaunchedEffect(sesion) {
        if (sesion == null) {
            irALogin(navController)
        }
    }

    fun navigateToMainDestination(index: Int) {
        val route = when (index) {
            0 -> AppDestinos.Home.route
            1 -> AppDestinos.Expediente.route
            2 -> AppDestinos.Carga.route
            3 -> AppDestinos.Revision.route
            else -> return
        }
        val perfilActivo = sesion?.perfil
        if (route == AppDestinos.Revision.route &&
            (perfilActivo == null || !PermisosPorPerfil.veRevision(perfilActivo))
        ) {
            Toast.makeText(context, "Tu perfil no tiene acceso a la bandeja de revisión.", Toast.LENGTH_SHORT).show()
            return
        }
        navController.navigate(route) {
            popUpTo(AppDestinos.Home.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestinos.Login.route,
        // Animaciones de transicion entre destinos (Compose Navigation).
        // Enter: la pantalla nueva se desliza desde la derecha al avanzar y
        // desde la izquierda al volver; las salidas se desvanecen. Da
        // continuidad visual y hace evidente el sentido de la navegacion.
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { ancho -> ancho / 6 },
                animationSpec = tween(DURACION_TRANSICION_MS)
            ) + fadeIn(animationSpec = tween(DURACION_TRANSICION_MS))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { ancho -> -ancho / 6 },
                animationSpec = tween(DURACION_TRANSICION_MS)
            ) + fadeOut(animationSpec = tween(DURACION_TRANSICION_MS))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { ancho -> -ancho / 6 },
                animationSpec = tween(DURACION_TRANSICION_MS)
            ) + fadeIn(animationSpec = tween(DURACION_TRANSICION_MS))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { ancho -> ancho / 6 },
                animationSpec = tween(DURACION_TRANSICION_MS)
            ) + fadeOut(animationSpec = tween(DURACION_TRANSICION_MS))
        }
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

        composable(AppDestinos.Perfil.route) {
            SesionRequerida(sesion) {
                PerfilScreen(
                    onVolver = { navController.popBackStack() },
                    // "Ver mis documentos" lleva al historial (Expediente).
                    onVerMisDocumentos = { navigateToMainDestination(1) }
                )
            }
        }

        composable(AppDestinos.Home.route) {
            HomeScreen(
                onNavigateExpediente = { navigateToMainDestination(1) },
                onNavigateCarga = { navigateToMainDestination(2) },
                onNavigateRevision = { navigateToMainDestination(3) },
                onNavigateBitacora = {
                    Toast.makeText(context, "Próximamente: Bitácora de auditoría (P08)", Toast.LENGTH_SHORT).show()
                },
                onNavigatePerfil = { navController.navigate(AppDestinos.Perfil.route) },
                onCerrarSesion = {
                    AppContainer.cerrarSesion()
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
        // mismo repositorio compartido por Carga y Consulta. Reservada a los
        // perfiles con permiso de revisión (RF-07 / minimo privilegio).
        composable(AppDestinos.Revision.route) {
            SesionRequerida(sesion) { sesionActiva ->
                if (PermisosPorPerfil.veRevision(sesionActiva.perfil)) {
                    ValidacionScreen(
                        onVolver = { navController.popBackStack() },
                        onMainDestinationSelected = ::navigateToMainDestination
                    )
                } else {
                    AccesoDenegado(onVolver = { navController.popBackStack() })
                }
            }
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

/** Duracion unica de las transiciones de navegacion (ms), en un solo lugar. */
private const val DURACION_TRANSICION_MS = 280

/** Vuelve al login limpiando toda la pila (usado por la guardia de sesion). */
private fun irALogin(navController: NavHostController) {
    // Idempotente: si ya estamos en el login (arranque o cierre reiterado),
    // no se vuelve a navegar para no apilar destinos repetidos.
    if (navController.currentDestination?.route == AppDestinos.Login.route) return
    navController.navigate(AppDestinos.Login.route) {
        popUpTo(0) { inclusive = true }
    }
}

/**
 * Envoltorio de destino que exige sesion activa.
 *
 * Recibe la sesion ya observada por el grafo: si es null, el destino interno
 * no se pinta (la guardia de arriba, ademas, redirige al login).
 */
@Composable
private fun SesionRequerida(
    sesion: SesionActual?,
    contenido: @Composable (SesionActual) -> Unit
) {
    if (sesion != null) {
        contenido(sesion)
    } else {
        AccesoDenegado(onVolver = {})
    }
}

/** Aviso para un destino alcanzado sin el permiso del perfil. */
@Composable
private fun AccesoDenegado(onVolver: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Acceso no autorizado",
                style = MaterialTheme.typography.titleLarge,
                color = Spruce
            )
            Text(
                "Tu perfil no tiene permiso para ver esta sección.",
                style = MaterialTheme.typography.bodyMedium,
                color = Grey
            )
            TextButton(onClick = onVolver) {
                Text("Volver")
            }
        }
    }
}
