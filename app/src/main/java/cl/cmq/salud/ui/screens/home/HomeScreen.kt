<<<<<<< Updated upstream:app/src/main/java/cl/cmq/salud/ui/screens/login/home/HomeScreen.kt

package cl.cmq.salud.ui.screens.login.home
=======
package cl.cmq.salud.ui.screens.home
>>>>>>> Stashed changes:app/src/main/java/cl/cmq/salud/ui/screens/home/HomeScreen.kt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.cmq.salud.ui.components.AlertCard
import cl.cmq.salud.ui.components.BottomNavBar
import cl.cmq.salud.ui.components.MenuCard
import cl.cmq.salud.ui.components.NavItem
import cl.cmq.salud.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel = viewModel(),
    onNavigateConsulta: () -> Unit,
    onNavigateCarga: () -> Unit,
    onNavigateValidacion: () -> Unit,
    onNavigateBitacora: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inicio", color = Color.White, fontSize = 14.sp) },
                navigationIcon = { Text("=", color = Color.White) },
                actions = { Text("O", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue)
            )
        },
        bottomBar = {
            BottomNavBar(
                items = listOf(
                    NavItem("H", "Inicio"),
                    NavItem("L", "Expediente"),
                    NavItem("+", "Cargar"),
                    NavItem("U", "Perfil")
                ),
                activeIndex = 0,
                onItemSelected = { /* TODO navegacion inferior */ }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Saludo al usuario
            Text(
                "Hola, ${state.nombreUsuario}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )
            Text(
                "${state.perfil} · ${state.centroSalud}",
                fontSize = 10.sp,
                color = Grey
            )

            Spacer(Modifier.height(14.dp))

            // Menu principal
            MenuCard(
                icon = "L",
                title = "Consultar expediente",
                subtitle = "Buscar documentos por funcionario",
                onClick = onNavigateConsulta
            )
            Spacer(Modifier.height(8.dp))

            MenuCard(
                icon = "U",
                title = "Cargar documento",
                subtitle = "Archivo o captura con camara",
                onClick = onNavigateCarga
            )
            Spacer(Modifier.height(8.dp))

            MenuCard(
                icon = "V",
                title = "Validaciones pendientes",
                subtitle = "${state.validacionesPendientes} documentos en espera",
                onClick = onNavigateValidacion
            )
            Spacer(Modifier.height(8.dp))

            MenuCard(
                icon = "B",
                title = "Bitacora de auditoria",
                subtitle = "Trazabilidad de accesos",
                onClick = onNavigateBitacora
            )

            Spacer(Modifier.height(14.dp))

            // Alerta de vencimientos
            if (state.documentosPorVencer > 0) {
                AlertCard(
                    text = "! ${state.documentosPorVencer} documentos proximos a vencer"
                )
            }

            Spacer(Modifier.weight(1f))

            // Cierre de sesion (accion administrativa)
            TextButton(onClick = {
                vm.cerrarSesion()
                onCerrarSesion()
            }) {
                Text("Cerrar sesion", color = Navy, fontSize = 11.sp)
            }
        }
    }
}
