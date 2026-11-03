package cl.cmq.salud.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.Icon
import cl.cmq.salud.ui.components.AlertCard
import cl.cmq.salud.ui.components.AppTopBar
import cl.cmq.salud.ui.components.BottomNavItem
import cl.cmq.salud.ui.components.MainBottomNavBar
import cl.cmq.salud.ui.components.mainNavigationItemsPara
import cl.cmq.salud.ui.theme.Accent
import cl.cmq.salud.ui.theme.Background
import cl.cmq.salud.ui.theme.Grey
import cl.cmq.salud.ui.theme.SoftSage
import cl.cmq.salud.ui.theme.Spruce
import cl.cmq.salud.ui.theme.SurfaceWhite
import cl.cmq.salud.ui.theme.Teal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel = viewModel(),
    onNavigateExpediente: () -> Unit,
    onNavigateRevision: () -> Unit,
    onMainDestinationSelected: (Int) -> Unit = {},
    onCerrarSesion: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    LaunchedEffect(Unit) { vm.refrescarContador() }
    LaunchedEffect(state.isSessionActive) {
        if (!state.isSessionActive) onCerrarSesion()
    }

    // La barra depende del perfil: administración suma el quinto botón de
    // revisión, el funcionario mantiene cuatro. El índice tocado se reenvía
    // crudo al grafo, que es el único que conoce el mapeo índice -> ruta: así
    // esta pantalla no mantiene una tabla paralela que pueda desincronizarse.
    val itemsBarra = mainNavigationItemsPara(state.puedeRevisar)

    Scaffold(
        containerColor = Background,
        topBar = {
            // El encabezado solo identifica la app y el perfil activo: los
            // accesos ("Mi perfil", "Salir") viven en la barra inferior y en
            // la pantalla de perfil, no compiten con el contenido del Home.
            AppTopBar(
                titleContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Teal, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("CMQ", color = SurfaceWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("CMQ Salud", color = Spruce, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(state.perfilNombre, color = Grey, fontSize = 12.sp)
                        }
                    }
                }
            )
        },
        bottomBar = {
            MainBottomNavBar(
                activeIndex = BottomNavItem.INICIO,
                items = itemsBarra,
                onItemSelected = onMainDestinationSelected
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardHero(
                nombre = state.nombreUsuario,
                centroSalud = state.centroSalud
            )

            // Administración ve la bandeja de revisión (RF-15); el funcionario
            // ve el estado de su propio expediente (mínimo privilegio).
            if (state.puedeRevisar) {
                PriorityTaskCard(
                    pendientes = state.validacionesPendientes,
                    onClick = onNavigateRevision
                )
            } else {
                MiExpedienteCard(
                    total = state.misDocumentos,
                    porCorregir = state.documentosPorVencer,
                    onClick = onNavigateExpediente
                )
            }

            // Sección "Accesos rápidos" retirada: esos destinos (consultar
            // expediente, cargar documento y bitácora de auditoría) se
            // alcanzan desde la barra inferior, así que repetirlos aquí solo
            // duplicaba navegación en el Home.

            if (state.documentosPorVencer > 0) {
                val vencimiento = if (state.puedeRevisar) {
                    if (state.documentosPorVencer == 1) {
                        "1 documento está próximo a vencer. Revisa su expediente."
                    } else {
                        "${state.documentosPorVencer} documentos están próximos a vencer. Revisa sus expedientes."
                    }
                } else {
                    if (state.documentosPorVencer == 1) {
                        "Tienes 1 documento en revisión o con corrección solicitada."
                    } else {
                        "Tienes ${state.documentosPorVencer} documentos en revisión o con corrección solicitada."
                    }
                }
                AlertCard(text = vencimiento)
            }
        }
    }
}

/**
 * Tarjeta prioritaria del funcionario: su expediente, no la bandeja ajena.
 * Informa cuántos documentos tiene y cuántos requieren su atención.
 */
@Composable
private fun MiExpedienteCard(total: Int, porCorregir: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Spruce),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Mi expediente",
                    color = Color.White,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    when {
                        total == 0 -> "Aún no tienes documentos cargados"
                        porCorregir > 0 -> "$total documento(s) · $porCorregir requieren tu atención"
                        else -> "$total documento(s) en tu expediente"
                    },
                    color = Color.White.copy(alpha = 0.84f),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Ver mis documentos", color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = Accent, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun DashboardHero(nombre: String, centroSalud: String) {
    val primerNombre = nombre.substringBefore(" ").ifBlank { nombre }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Hola, $primerNombre",
                    color = Spruce,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    centroSalud,
                    color = Grey,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(SoftSage, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    primerNombre.take(1).uppercase(),
                    color = Spruce,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PriorityTaskCard(pendientes: Int, onClick: () -> Unit) {
    val hayPendientes = pendientes > 0
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Spruce),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (hayPendientes) "Revisión pendiente" else "Revisión al día",
                    color = Color.White,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (hayPendientes) {
                        if (pendientes == 1) "1 documento requiere tu decisión" else "$pendientes documentos requieren tu decisión"
                    } else "No hay solicitudes pendientes",
                    color = Color.White.copy(alpha = 0.84f),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Abrir bandeja", color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = Accent, modifier = Modifier.size(28.dp))
        }
    }
}
