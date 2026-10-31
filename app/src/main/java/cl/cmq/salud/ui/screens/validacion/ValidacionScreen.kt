package cl.cmq.salud.ui.screens.validacion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.ui.components.AppTopBar
import cl.cmq.salud.ui.components.DocumentoCard
import cl.cmq.salud.ui.components.DocumentoPreview
import cl.cmq.salud.ui.components.EmptyState
import cl.cmq.salud.ui.components.MainBottomNavBar
import cl.cmq.salud.ui.components.PrimaryButton
import cl.cmq.salud.ui.components.SecondaryButton
import cl.cmq.salud.ui.components.StatusChip
import cl.cmq.salud.ui.theme.Background
import cl.cmq.salud.ui.theme.Blue
import cl.cmq.salud.ui.theme.Border
import cl.cmq.salud.ui.theme.Error
import cl.cmq.salud.ui.theme.Grey
import cl.cmq.salud.ui.theme.Light
import cl.cmq.salud.ui.theme.Navy
import cl.cmq.salud.ui.theme.Red
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidacionScreen(
    onVolver: () -> Unit,
    onMainDestinationSelected: (Int) -> Unit = {},
    vm: ValidacionViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.successEventId) {
        if (state.successEventId > 0) {
            snackbarHostState.showSnackbar(
                message = state.ultimoMensajeExito ?: "Decisión registrada",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Revisión") },
        bottomBar = {
            MainBottomNavBar(activeIndex = 3, onItemSelected = onMainDestinationSelected)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Bandeja de revisión",
                style = MaterialTheme.typography.displaySmall,
                color = Navy,
            )
            Text(
                "Aprueba, devuelve o rechaza documentos derivados.",
                color = Grey,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(16.dp))

            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Blue)
                }
                state.pendientes.isEmpty() -> EmptyState(
                    title = "Todo al día",
                    detail = "No hay documentos pendientes. Los que necesiten revisión aparecerán aquí.",
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.fillMaxWidth()
                )
                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Pendientes",
                            color = Navy,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Box(
                            modifier = Modifier
                                .background(Light, shape = RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "${state.pendientes.size} por revisar",
                                color = Blue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    state.pendientes.forEach { doc ->
                        val isSelected = state.seleccionado?.idDocumento == doc.idDocumento
                        DocumentoCard(documento = doc, onClick = vm::onSeleccionar, selected = isSelected)
                        Spacer(Modifier.height(9.dp))
                    }

                    state.seleccionado?.let { doc ->
                        Spacer(Modifier.height(4.dp))
                        DocumentoPreview(
                            documento = doc,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        PanelDecision(
                            documento = doc,
                            comentario = state.comentario,
                            comentarioError = state.comentarioError,
                            isProcesando = state.isProcesando,
                            onComentarioChange = vm::onComentarioChange,
                            onAprobar = { vm.decidir(EstadoDocumento.APROBADO) },
                            onRechazar = { vm.decidir(EstadoDocumento.RECHAZADO) },
                            onCorregir = { vm.decidir(EstadoDocumento.PENDIENTE_CORRECCION) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelDecision(
    documento: Documento,
    comentario: String,
    comentarioError: String?,
    isProcesando: Boolean,
    onComentarioChange: (String) -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    onCorregir: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "Decisión de revisión",
                color = Blue,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                documento.nombreArchivo,
                color = Navy,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "RUT ${documento.rutFuncionario}",
                color = Grey,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))

            // Metadatos con StatusChip consistente
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(Light, shape = RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = documento.tipo,
                        color = Navy,
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                StatusChip(estado = documento.estado)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Cargado por ${documento.usuarioCarga} · ${documento.fechaCarga}",
                color = Grey,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(16.dp))

            // Comentario de revisión (obligatorio para rechazar o pedir corrección)
            OutlinedTextField(
                value = comentario,
                onValueChange = onComentarioChange,
                label = { Text("Comentario de revisión") },
                placeholder = { Text("Escribe el motivo o las observaciones") },
                minLines = 3,
                isError = comentarioError != null,
                supportingText = {
                    if (comentarioError != null) {
                        Text(comentarioError, color = Red, fontSize = 12.sp)
                    } else {
                        Text(
                            "Requerido para rechazar o pedir correcciones.",
                            color = Grey,
                            fontSize = 12.sp
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            if (isProcesando) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Blue)
                }
            } else {
                // Tres decisiones: aprobar (primaria), corregir (secundaria), rechazar (destructiva)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onRechazar,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Error,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Rechazar", style = androidx.compose.material3.MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                    SecondaryButton(
                        text = "Pedir corrección",
                        onClick = onCorregir,
                        modifier = Modifier.weight(1.3f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                PrimaryButton(text = "Aprobar documento", onClick = onAprobar)
            }
        }
    }
}
