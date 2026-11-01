// ui/screens/perfil/PerfilScreen.kt
package cl.cmq.salud.ui.screens.perfil

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import cl.cmq.salud.ui.components.MainBottomNavBar
import cl.cmq.salud.ui.theme.Background
import cl.cmq.salud.ui.theme.Divider
import cl.cmq.salud.ui.theme.Grey
import cl.cmq.salud.ui.theme.Navy
import cl.cmq.salud.ui.theme.SoftSage
import cl.cmq.salud.ui.theme.Spruce
import cl.cmq.salud.ui.theme.SurfaceWhite
import cl.cmq.salud.ui.theme.Teal

/**
 * Pantalla Perfil: datos de la persona logueada.
 *
 * Deliberadamente distinta de Expediente (historial de documentos): aquí no
 * hay listado documental, solo la identidad y los datos laborales. El botón
 * "Ver mis documentos" lleva al historial, para que la diferencia quede clara
 * en la propia navegación.
 *
 * Según el perfil activo esta pantalla se alcanza de dos formas:
 *  - Como destino principal (perfiles sin bandeja de revisión), con barra
 *    inferior y sin flecha de retroceso: [activeIndex] >= 0.
 *  - Como pantalla secundaria empujada sobre otra (perfiles que revisan),
 *    con flecha de retroceso y sin barra: [activeIndex] < 0.
 */
@Composable
fun PerfilScreen(
    onVolver: () -> Unit,
    onVerMisDocumentos: () -> Unit,
    onCerrarSesion: () -> Unit,
    onMainDestinationSelected: (Int) -> Unit = {},
    activeIndex: Int = -1,
    vm: PerfilViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val esDestinoPrincipal = activeIndex >= 0

    Scaffold(
        containerColor = Background,
        topBar = {
            AppTopBar(
                title = "Mi perfil",
                onBack = if (esDestinoPrincipal) null else onVolver
            )
        },
        bottomBar = {
            if (esDestinoPrincipal) {
                MainBottomNavBar(
                    activeIndex = activeIndex,
                    onItemSelected = onMainDestinationSelected
                )
            }
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
            PerfilEncabezado(
                nombre = state.nombreCompleto,
                perfilNombre = state.perfilNombre
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Datos personales",
                        color = Spruce,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    DatoFila("Nombre", state.nombreCompleto)
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 10.dp))
                    DatoFila("RUT", state.rut)
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 10.dp))
                    DatoFila("Correo institucional", state.correoInstitucional)
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 10.dp))
                    DatoFila("Perfil", state.perfilNombre)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Datos laborales",
                        color = Spruce,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    if (state.tieneDatosLaborales) {
                        DatoFila("Cargo", state.cargo)
                        HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 10.dp))
                        DatoFila("Centro de salud", state.centroSalud)
                    } else {
                        Text(
                            if (state.cargando) "Cargando datos laborales…"
                            else "Aún no hay datos laborales registrados para tu RUT. Se completan al cargar tu primer documento o cuando administración los registre.",
                            color = Grey,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            androidx.compose.material3.Button(
                onClick = onVerMisDocumentos,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Spruce,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Ver mis documentos",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Cerrar sesión vive aquí (no en el Home): la identidad y el cierre
            // de sesión son responsabilidad de la pantalla de perfil.
            TextButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Cerrar sesión",
                    color = Teal,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PerfilEncabezado(nombre: String, perfilNombre: String) {
    val inicial = nombre.trim().take(1).uppercase().ifBlank { "?" }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(60.dp).background(SoftSage, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(inicial, color = Spruce, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                nombre.ifBlank { "Sesión" },
                color = Spruce,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(perfilNombre, color = Grey, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun DatoFila(etiqueta: String, valor: String) {
    Column {
        Text(etiqueta, color = Grey, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(2.dp))
        Text(
            valor.ifBlank { "—" },
            color = Navy,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
