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
import cl.cmq.salud.ui.components.BottomNavItem
import cl.cmq.salud.ui.components.RutaPrincipal
import cl.cmq.salud.ui.components.mainNavigationItemsPara
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

    // ¿El perfil con sesión puede revisar? Fuente única para la barra inferior
    // y para validar el destino de Revisión (RF-15 / mínimo privilegio).
    fun puedeRevisarActivo(): Boolean =
        sesion?.perfil?.let { PermisosPorPerfil.veRevision(it) } == true

    fun navigateToMainDestination(index: Int) {
        val puedeRevisar = puedeRevisarActivo()
        // El mapeo índice -> destino vive en BottomNavItem.destinoDe: es la
        // única tabla de la app, así ninguna pantalla puede desincronizarse de
        // ella. Aquí solo se traduce el destino a su ruta y se valida el
        // permiso (defensa en profundidad: ocultar el botón no basta).
        val route = when (val destino = BottomNavItem.destinoDe(index, puedeRevisar)) {
            RutaPrincipal.Inicio -> AppDestinos.Home.route
            RutaPrincipal.Expediente -> AppDestinos.Expediente.route
            RutaPrincipal.Carga -> AppDestinos.Carga.route
            RutaPrincipal.Perfil -> AppDestinos.Perfil.route
            RutaPrincipal.Revision -> AppDestinos.Revision.route
            null -> {
                // Sin destino solo queda el caso de Revisión pedida sin permiso
                // (el botón existe para otros perfiles, no para este).
                Toast.makeText(context, "Tu perfil no tiene acceso a la bandeja de revisión.", Toast.LENGTH_SHORT).show()
                return
            }
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
            SesionRequerida(sesion) { sesionActiva ->
                // Perfil cierra la barra: su índice depende de si hay Revisión.
                val itemsPerfil = mainNavigationItemsPara(PermisosPorPerfil.veRevision(sesionActiva.perfil))
                PerfilScreen(
                    onVolver = { navController.popBackStack() },
                    // "Ver mis documentos" lleva al historial (Expediente).
                    onVerMisDocumentos = { navigateToMainDestination(BottomNavItem.EXPEDIENTE) },
                    onCerrarSesion = {
                        AppContainer.cerrarSesion()
                        navController.navigate(AppDestinos.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    // Perfil es un destino de la barra inferior para todos los
                    // perfiles, también para administración: la bandeja de
                    // revisión no reemplaza el acceso a los propios datos.
                    onMainDestinationSelected = ::navigateToMainDestination,
                    items = itemsPerfil,
                    activeIndex = BottomNavItem.indicePerfil(itemsPerfil) ?: 0
                )
            }
        }

        composable(AppDestinos.Home.route) {
            HomeScreen(
                // La barra inferior reenvía el índice tocado a este mismo mapeo,
                // igual que Expediente, Carga y Revisión: una sola tabla
                // índice -> ruta en todo el grafo.
                onMainDestinationSelected = ::navigateToMainDestination,
                // Las tarjetas del contenido sí apuntan a un destino concreto.
                onNavigateExpediente = { navigateToMainDestination(BottomNavItem.EXPEDIENTE) },
                onNavigateRevision = { navigateToMainDestination(BottomNavItem.REVISION) },
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
                // La barra refleja el perfil activo: administración también ve
                // su botón de Revisión desde aquí, no solo los cuatro comunes.
                items = mainNavigationItemsPara(puedeRevisarActivo()),
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
                items = mainNavigationItemsPara(puedeRevisarActivo()),
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
                        onMainDestinationSelected = ::navigateToMainDestination,
                        puedeRevisar = true
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
                items = mainNavigationItemsPara(puedeRevisarActivo()),
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
