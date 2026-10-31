package cl.cmq.salud.ui.screens.consulta

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.cmq.salud.ui.components.AppTopBar
import cl.cmq.salud.ui.components.DocumentoCard
import cl.cmq.salud.ui.components.EmptyState
import cl.cmq.salud.ui.components.MainBottomNavBar
import cl.cmq.salud.ui.theme.Background
import cl.cmq.salud.ui.theme.Blue
import cl.cmq.salud.ui.theme.Grey
import cl.cmq.salud.ui.theme.Light
import cl.cmq.salud.ui.theme.Mint
import cl.cmq.salud.ui.theme.Navy
import cl.cmq.salud.ui.theme.Red
import cl.cmq.salud.ui.theme.SoftSage
import cl.cmq.salud.ui.theme.Spruce
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultaExpedienteScreen(
    onVolver: () -> Unit,
    onDocumentoClick: (Int) -> Unit,
    onMainDestinationSelected: (Int) -> Unit = {},
    vm: ConsultaViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        containerColor = Background,
        topBar = { AppTopBar(title = "Expediente") },
        bottomBar = {
            MainBottomNavBar(activeIndex = 1, onItemSelected = onMainDestinationSelected)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                "Buscar expediente",
                style = MaterialTheme.typography.displaySmall,
                color = Spruce,
            )
            Text("Busca por RUT y accede al expediente local.", color = Grey, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = state.rut,
                    onValueChange = vm::onRutChange,
                    placeholder = { Text("12.345.678-9", color = Grey) },
                    label = { Text("RUT del funcionario") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { vm.onBuscar() },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.heightIn(min = 56.dp)
                ) {
                    Text("Buscar", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }

            state.errorMessage?.let { message ->
                Spacer(Modifier.height(8.dp))
                Text(
                    message,
                    color = Red,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(12.dp))

            state.funcionario?.let { funcionario ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Mint, CircleShape)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                funcionario.nombreCompleto.take(1).uppercase(),
                                color = Blue,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                funcionario.nombreCompleto,
                                color = Spruce,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${funcionario.rut} · ${funcionario.cargo}",
                                color = Grey,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                funcionario.centroSalud,
                                color = Grey,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Filtros de documento
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiltroDocumento.entries.forEach { filtro ->
                        val selected = state.filtroActivo == filtro
                        FilterChip(
                            selected = selected,
                            onClick = { vm.onFiltroChange(filtro) },
                            label = { Text(filtro.label, fontSize = 12.sp) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Blue,
                                selectedLabelColor = Color.White,
                                labelColor = Spruce
                            )
                        )
                    }
                }
                Text(
                    "${state.documentos.size} documentos · almacenamiento local (${state.totalEnAlmacenamiento} en el dispositivo)",
                    color = Grey,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                )
            }

            when {
                state.isBuscando -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Blue)
                }
                state.funcionario != null && state.documentosFiltrados.isNotEmpty() -> LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.documentosFiltrados) { documento ->
                        DocumentoCard(documento = documento, onClick = onDocumentoClick)
                    }
                }
                state.funcionario != null && state.documentos.isEmpty() -> EmptyState(
                    title = "Sin documentos en esta categoría",
                    detail = "Prueba otro filtro para ampliar los resultados.",
                    modifier = Modifier.weight(1f)
                )
                state.funcionario != null -> EmptyState(
                    title = "Sin resultados",
                    detail = "No se encontraron documentos para el filtro seleccionado.",
                    modifier = Modifier.weight(1f)
                )
                else -> EmptyState(
                    title = "Tu expediente, a mano",
                    detail = "Ingresa el RUT de un funcionario y pulsa Buscar para consultar sus documentos.",
                    icon = Icons.Default.Search,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
