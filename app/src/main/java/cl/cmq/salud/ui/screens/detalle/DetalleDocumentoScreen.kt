package cl.cmq.salud.ui.screens.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.cmq.salud.ui.components.AppTopBar
import cl.cmq.salud.ui.components.DocumentoPreview
import cl.cmq.salud.ui.components.PrimaryButton
import cl.cmq.salud.ui.components.SecondaryButton
import cl.cmq.salud.ui.components.StatusChip
import cl.cmq.salud.ui.theme.Background
import cl.cmq.salud.ui.theme.Blue
import cl.cmq.salud.ui.theme.Border
import cl.cmq.salud.ui.theme.Grey
import cl.cmq.salud.ui.theme.Mint
import cl.cmq.salud.ui.theme.Navy
import cl.cmq.salud.ui.theme.Red
import cl.cmq.salud.ui.theme.SoftSage
import cl.cmq.salud.ui.theme.Spruce
import cl.cmq.salud.ui.theme.SurfaceWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleDocumentoScreen(
    idDocumento: Int,
    onVolver: () -> Unit,
    vm: DetalleViewModel = viewModel(
        key = "detalle_$idDocumento",
        factory = DetalleViewModel.factory(idDocumento)
    )
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        containerColor = Background,
        topBar = { AppTopBar(title = "Detalle del documento", onBack = onVolver) }
    ) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Blue)
            }
            state.error != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    state.error ?: "No fue posible abrir el documento.",
                    color = Red,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(16.dp))
                SecondaryButton(text = "Volver al expediente", onClick = onVolver)
            }
            else -> {
                val doc = state.documento ?: return@Scaffold
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    DocumentoPreview(documento = doc)

                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "FOLIO #${doc.idDocumento}",
                                color = Blue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                doc.nombreArchivo,
                                color = Spruce,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        StatusChip(estado = doc.estado)
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Información del documento",
                        color = Spruce,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))

                    BloqueMetadato(
                        label = "ORIGEN",
                        value = "Almacenamiento local del dispositivo · SQLite"
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            BloqueMetadato(label = "TIPO", value = doc.tipo)
                        }
                        Spacer(Modifier.width(9.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            BloqueMetadato(label = "FECHA DE CARGA", value = doc.fechaCarga)
                        }
                    }
                    BloqueMetadato(label = "CARGADO POR", value = doc.usuarioCarga)
                    BloqueMetadato(
                        label = "FUNCIONARIO ASOCIADO",
                        value = "RUT ${doc.rutFuncionario} · ID ${doc.idFuncionario}"
                    )
                    BloqueMetadato(
                        label = "DESCRIPCIÓN",
                        value = doc.descripcion ?: "Sin descripción registrada"
                    )

                    doc.rutaLocal?.let { ruta ->
                        val origen = if (ruta.startsWith("content://")) {
                            "Archivo digital seleccionado"
                        } else {
                            "Cámara del dispositivo"
                        }
                        BloqueMetadato(label = "ORIGEN DEL ARCHIVO", value = origen)
                        BloqueMetadato(label = "ARCHIVO LOCAL", value = ruta)
                        doc.hashArchivo?.let { hash ->
                            BloqueMetadato(label = "HUELLA SHA-256", value = hash)
                        }
                    }
                    doc.comentarioValidacion?.let { comentario ->
                        BloqueMetadato(
                            label = "COMENTARIO DEL REVISOR",
                            value = comentario
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    if (state.accesoRegistrado) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Mint)
                        ) {
                            Text(
                                "Acceso registrado en la bitácora de auditoría.",
                                color = Blue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                    PrimaryButton(
                        text = if (state.accesoRegistrado) "Registrar otro acceso" else "Registrar acceso de descarga",
                        onClick = { vm.registrarAccesoDescarga() }
                    )
                    Spacer(Modifier.height(9.dp))
                    SecondaryButton(text = "Volver al expediente", onClick = onVolver)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun BloqueMetadato(
    label: String,
    value: String,
    valueColor: Color = Navy,
    containerColor: Color = Color.White
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
            Text(
                label,
                fontSize = 12.sp,
                color = Grey,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontSize = 13.sp,
                color = valueColor,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )
        }
    }
}
