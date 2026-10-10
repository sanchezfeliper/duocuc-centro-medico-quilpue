// ui/screens/validacion/ValidacionScreen.kt
package cl.cmq.salud.ui.screens.validacion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.ui.components.DocumentoCard
import cl.cmq.salud.ui.components.PrimaryButton
import cl.cmq.salud.ui.components.SecondaryButton
import cl.cmq.salud.ui.components.badgeColors
import cl.cmq.salud.ui.theme.*

/**
 * P06 Validacion (mapa de navegacion del Punto 8): bandeja de documentos
 * pendientes para JEF/RRHH con decision de aprobar, rechazar o solicitar
 * correccion (RF-15 a RF-17), siguiendo el mockup 5 del Punto 5.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidacionScreen(
    onVolver: () -> Unit,
    vm: ValidacionViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Meta 4: respuesta visible por evento unico (contador, no mensaje)
    LaunchedEffect(state.successEventId) {
        if (state.successEventId > 0) {
            snackbarHostState.showSnackbar(
                message = state.ultimoMensajeExito ?: "Decisión registrada",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Validaciones pendientes", color = Color.White, fontSize = 15.sp) },
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
                .verticalScroll(rememberScrollState())
        ) {
            Text("Bandeja de Jefatura", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text(
                "Revise, apruebe, rechace o solicite corrección de los documentos derivados (RF-15 a RF-17)",
                fontSize = 10.sp,
                color = Grey
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Green)
                    }
                }

                state.pendientes.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay documentos pendientes de validación. " +
                                "Los documentos cargados con derivación a Jefatura aparecerán aquí.",
                            color = Grey,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                else -> {
                    Text(
                        text = "Documentos en espera (${state.pendientes.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Grey
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    state.pendientes.forEach { doc ->
                        val isSelected = state.seleccionado?.idDocumento == doc.idDocumento
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) Navy else Color.Transparent,
                                    shape = RoundedCornerShape(5.dp)
                                )
                                .clickable { vm.onSeleccionar(doc.idDocumento) }
                        ) {
                            DocumentoCard(
                                documento = doc,
                                onClick = { vm.onSeleccionar(doc.idDocumento) }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Panel de decision del documento seleccionado (mockup 5)
                    state.seleccionado?.let { doc ->
                        Spacer(modifier = Modifier.height(4.dp))
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

/** Panel de revision del documento seleccionado: metadatos + comentario + decision. */
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
    val (badgeBg, badgeFg) = badgeColors(documento.estado)

    Card(
        colors = CardDefaults.cardColors(containerColor = Light),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Vista previa simulada (mini)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Navy, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📄 ${documento.nombreArchivo}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Funcionario", fontSize = 8.sp, color = Grey, fontWeight = FontWeight.SemiBold)
            Text(
                "Juan Pérez · ${documento.rutFuncionario}",
                fontSize = 11.sp,
                color = Navy,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tipo y estado", fontSize = 8.sp, color = Grey, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${documento.tipo} · ${documento.estado.etiqueta}",
                        fontSize = 11.sp,
                        color = badgeFg,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Cargado por", fontSize = 8.sp, color = Grey, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${documento.usuarioCarga} · ${documento.fechaCarga}",
                        fontSize = 10.sp,
                        color = Navy
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = comentario,
                onValueChange = onComentarioChange,
                label = { Text("Comentario del revisor") },
                placeholder = { Text("Observaciones de la revisión...", fontSize = 11.sp) },
                minLines = 2,
                isError = comentarioError != null,
                supportingText = {
                    if (comentarioError != null) {
                        Text(comentarioError ?: "", color = Red, fontSize = 10.sp)
                    } else {
                        Text(
                            "Obligatorio para rechazar o solicitar corrección; opcional al aprobar.",
                            fontSize = 10.sp,
                            color = Grey
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (isProcesando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRechazar,
                        colors = ButtonDefaults.buttonColors(containerColor = Red, contentColor = Color.White),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("RECHAZAR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    SecondaryButton(
                        text = "SOLICITAR CORRECCIÓN",
                        onClick = onCorregir,
                        modifier = Modifier.weight(1.4f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryButton(text = "APROBAR DOCUMENTO", onClick = onAprobar)
            }
        }
    }
}
