package cl.cmq.salud.ui.screens.perfil

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.ui.components.AppTopBar
import cl.cmq.salud.ui.components.MainBottomNavBar
import cl.cmq.salud.ui.components.StatusChip
import cl.cmq.salud.ui.theme.Background
import cl.cmq.salud.ui.theme.Grey
import cl.cmq.salud.ui.theme.Spruce
import cl.cmq.salud.ui.theme.SoftSage
import cl.cmq.salud.ui.theme.SurfaceWhite

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PerfilScreen(
    sessionRut: String,
    onMainDestinationSelected: (Int) -> Unit = {},
    vm: PerfilViewModel = viewModel(
        key = "perfil-$sessionRut",
        factory = PerfilViewModel.factory(sessionRut)
    ),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by vm.uiState.collectAsState()

    Scaffold(
        containerColor = Background,
        topBar = { AppTopBar(title = "Perfil") },
        bottomBar = { MainBottomNavBar(activeIndex = 4, onItemSelected = onMainDestinationSelected) }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProfileFicha(
                    rut = state.rut,
                    nombreCompleto = state.nombreCompleto,
                    cargo = state.cargo,
                    centroSalud = state.centroSalud,
                    correo = state.correo,
                    perfilRol = state.perfilRol
                )
            }
            item { Text("Documentos subidos", style = MaterialTheme.typography.titleLarge, color = Spruce) }
            if (state.documentos.isEmpty()) {
                item {
                    Text(
                        "Aún no hay documentos asociados a este perfil.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Grey,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }
            } else {
                items(state.documentos, key = { it.idDocumento }) { doc ->
                    DocumentRow(doc) {
                        Toast.makeText(context, "Docs #${doc.idDocumento}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileFicha(
    rut: String,
    nombreCompleto: String,
    cargo: String,
    centroSalud: String,
    correo: String,
    perfilRol: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SoftSage, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = Spruce,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = nombreCompleto,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Spruce
                    )
                    Text(
                        text = centroSalud,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Grey
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            InfoRow(label = "RUT", value = rut)
            InfoRow(label = "Cargo", value = cargo)
            InfoRow(label = "Centro de Salud", value = centroSalud)
            InfoRow(label = "Correo", value = correo)
            InfoRow(label = "Perfil rol", value = perfilRol)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Grey,
            modifier = Modifier.weight(0.38f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Spruce,
            modifier = Modifier.weight(0.62f)
        )
    }
}

@Composable
private fun DocumentRow(
    doc: Documento,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(doc.nombreArchivo, style = MaterialTheme.typography.bodyMedium, color = Spruce)
                Text(
                    "${doc.tipo} · ${doc.fechaCarga.takeIf { it.isNotEmpty() } ?: "-"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Grey
                )
            }
            StatusChip(estado = doc.estado)
        }
    }
}
