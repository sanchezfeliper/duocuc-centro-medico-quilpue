// ui/screens/detalle/DetalleDocumentoScreen.kt
package cl.cmq.salud.ui.screens.detalle

import androidx.compose.foundation.background
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
import cl.cmq.salud.ui.components.PrimaryButton
import cl.cmq.salud.ui.components.SecondaryButton
import cl.cmq.salud.ui.components.badgeColors
import cl.cmq.salud.ui.theme.*

/**
 * P04 Detalle de documento (Meta 6). Recibe el ID por argumento de ruta
 * y solo dibuja el estado expuesto por DetalleViewModel.
 */
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
        topBar = {
            TopAppBar(
                title = { Text("Detalle del documento", color = Color.White, fontSize = 15.sp) },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("<", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue)
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green)
                }
            }

            state.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.error ?: "",
                        color = Red,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    SecondaryButton(text = "Volver a la consulta", onClick = onVolver)
                }
            }

            else -> {
                val doc = state.documento ?: return@Scaffold
                val (badgeBg, badgeFg) = badgeColors(doc.estado)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Vista previa del documento (simulada)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Navy, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = doc.tipo,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Vista previa del documento",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Metadatos del expediente (RN-08)
                    BloqueMetadato(label = "FOLIO", value = "#${doc.idDocumento}")
                    BloqueMetadato(label = "NOMBRE DEL ARCHIVO", value = doc.nombreArchivo)

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            BloqueMetadato(label = "TIPO", value = doc.tipo)
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            BloqueMetadato(
                                label = "ESTADO",
                                value = doc.estado.name.lowercase().replaceFirstChar { it.uppercase() },
                                valueColor = badgeFg,
                                containerColor = badgeBg
                            )
                        }
                    }

                    BloqueMetadato(label = "FECHA DE CARGA", value = doc.fechaCarga)
                    BloqueMetadato(label = "CARGADO POR", value = doc.usuarioCarga)
                    BloqueMetadato(
                        label = "FUNCIONARIO ASOCIADO",
                        value = "RUT ${doc.rutFuncionario} · ID ${doc.idFuncionario}"
                    )
                    BloqueMetadato(
                        label = "DESCRIPCIÓN",
                        value = doc.descripcion ?: "Sin descripción registrada"
                    )

                    Spacer(Modifier.height(16.dp))

                    // RF-14: descarga con registro de acceso
                    if (state.accesoRegistrado) {
                        Text(
                            text = "Acceso registrado en la bitácora (RF-18, simulado hasta P08).",
                            color = Green,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    PrimaryButton(
                        text = if (state.accesoRegistrado) "Registrar otro acceso" else "Registrar acceso de descarga",
                        onClick = { vm.registrarAccesoDescarga() }
                    )

                    Spacer(Modifier.height(8.dp))
                    SecondaryButton(text = "Volver al expediente", onClick = onVolver)
                }
            }
        }
    }
}

/** Tarjeta de metadato estilo review-block del mockup P04. */
@Composable
private fun BloqueMetadato(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF2C3E50),
    containerColor: Color = Light
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = label,
                fontSize = 8.sp,
                color = Grey,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                color = valueColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
