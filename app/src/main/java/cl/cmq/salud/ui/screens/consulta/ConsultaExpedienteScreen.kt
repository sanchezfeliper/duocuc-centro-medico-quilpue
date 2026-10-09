package cl.cmq.salud.ui.screens.consulta

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.ui.components.DocumentoCard
import cl.cmq.salud.ui.components.PrimaryButton
import cl.cmq.salud.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultaExpedienteScreen(
    onVolver: () -> Unit,
    onDocumentoClick: (Int) -> Unit,
    vm: ConsultaViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    val documentosFiltrados = remember(state.documentos, state.filtroActivo) {
        when (state.filtroActivo) {
            FiltroDocumento.TODOS -> state.documentos
            FiltroDocumento.VIGENTES -> state.documentos.filter { it.estado == EstadoDocumento.VIGENTE }
            FiltroDocumento.PENDIENTES -> state.documentos.filter { it.estado == EstadoDocumento.PENDIENTE }
            FiltroDocumento.VENCIDOS -> state.documentos.filter { it.estado == EstadoDocumento.VENCIDO }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expediente del funcionario", color = Color.White, fontSize = 15.sp) },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("<", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Buscador por RUT
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.rut,
                    onValueChange = { vm.onRutChange(it) },
                    placeholder = { Text("12.345.678-9", fontSize = 13.sp, color = Grey) },
                    label = { Text("RUT Funcionario", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { vm.onBuscar() },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Buscar", fontSize = 12.sp)
                }
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.errorMessage ?: "",
                    color = Red,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resumen del funcionario encontrado
            state.funcionario?.let { func ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Light),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "${func.nombreCompleto} · ${func.rut}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${func.cargo} · ${func.centroSalud}",
                            fontSize = 11.sp,
                            color = Grey
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filtros de documentos (Chips)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FiltroDocumento.entries.forEach { filtro ->
                        val isSelected = state.filtroActivo == filtro
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) Navy else Light,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { vm.onFiltroChange(filtro) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filtro.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Navy
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Indicador de búsqueda
            if (state.isBuscando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Blue)
                }
            } else if (state.funcionario != null) {
                if (documentosFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron documentos en esta categoría.",
                            color = Grey,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(documentosFiltrados) { doc ->
                            DocumentoCard(
                                documento = doc,
                                onClick = onDocumentoClick
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ingrese un RUT y presione Buscar para ver el expediente.",
                        color = Grey,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
