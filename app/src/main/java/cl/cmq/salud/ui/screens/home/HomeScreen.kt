package cl.cmq.salud.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Surface
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import cl.cmq.salud.ui.components.AlertCard
import cl.cmq.salud.ui.components.AppTopBar
import cl.cmq.salud.ui.components.MainBottomNavBar
import cl.cmq.salud.ui.theme.Accent
import cl.cmq.salud.ui.theme.Background
import cl.cmq.salud.ui.theme.Grey
import cl.cmq.salud.ui.theme.Light
import cl.cmq.salud.ui.theme.Mint
import cl.cmq.salud.ui.theme.Navy
import cl.cmq.salud.ui.theme.SoftSage
import cl.cmq.salud.ui.theme.Spruce
import cl.cmq.salud.ui.theme.SurfaceWhite
import cl.cmq.salud.ui.theme.Teal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel = viewModel(),
    onNavigateExpediente: () -> Unit,
    onNavigateCarga: () -> Unit,
    onNavigateRevision: () -> Unit,
    onNavigateBitacora: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    LaunchedEffect(Unit) { vm.refrescarContador() }
    LaunchedEffect(state.isSessionActive) {
        if (!state.isSessionActive) onCerrarSesion()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
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
                            Text("Gestión documental", color = Grey, fontSize = 12.sp)
                        }
                    }
                },
                trailingContent = {
                    TextButton(onClick = { vm.cerrarSesion() }) {
                        Text("Salir", color = Teal, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        },
        bottomBar = {
            MainBottomNavBar(
                activeIndex = 0,
                onItemSelected = { index ->
                    when (index) {
                        1 -> onNavigateExpediente()
                        2 -> onNavigateCarga()
                        3 -> onNavigateRevision()
                    }
                }
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

            PriorityTaskCard(
                pendientes = state.validacionesPendientes,
                onClick = onNavigateRevision
            )

            SectionHeading(title = "Accesos rápidos", detail = "Tareas del centro")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, cl.cmq.salud.ui.theme.Divider)
            ) {
                Column {
                    HomeActionRow(Icons.Default.Search, "Consultar expediente", "Busca por RUT de funcionario", onNavigateExpediente)
                    HorizontalDivider(color = cl.cmq.salud.ui.theme.Divider.copy(alpha = 0.7f), modifier = Modifier.padding(start = 58.dp))
                    HomeActionRow(Icons.Default.Add, "Cargar documento", "Registra un archivo o captura", onNavigateCarga)
                    HorizontalDivider(color = cl.cmq.salud.ui.theme.Divider.copy(alpha = 0.7f), modifier = Modifier.padding(start = 58.dp))
                    HomeActionRow(Icons.Default.Menu, "Bitácora de auditoría", "Consulta los accesos registrados", onNavigateBitacora)
                }
            }

            if (state.documentosPorVencer > 0) {
                val vencimiento = if (state.documentosPorVencer == 1) {
                    "1 documento está próximo a vencer. Revisa su expediente."
                } else {
                    "${state.documentosPorVencer} documentos están próximos a vencer. Revisa sus expedientes."
                }
                AlertCard(text = vencimiento)
            }

            LocalStorageCard(total = state.documentosEnAlmacenamiento)
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

@Composable
private fun SectionHeading(title: String, detail: String) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Text(
            text = title,
            color = Spruce,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = detail,
            color = Grey,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun LocalStorageCard(total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Light)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Storage, contentDescription = null, tint = Teal, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Almacenamiento local activo",
                    color = Spruce,
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (total == 1) "1 documento · guardado en este dispositivo (SQLite)"
                    else "$total documentos · guardados en este dispositivo (SQLite)",
                    color = Grey,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
            Text("SQLite", color = Teal, style = androidx.compose.material3.MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HomeActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    detail: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Teal, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Spruce, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Text(detail, color = Grey, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Teal, modifier = Modifier.size(20.dp))
    }
}
