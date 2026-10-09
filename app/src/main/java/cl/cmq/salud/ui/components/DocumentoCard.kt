<<<<<<< Updated upstream
=======
package cl.cmq.salud.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.ui.theme.*

@Composable
fun DocumentoCard(
    documento: Documento,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val (badgeBg, badgeFg) = badgeColors(documento.estado)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(5.dp))
            .border(1.dp, Border, RoundedCornerShape(5.dp))
            .clickable { onClick(documento.idDocumento) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono de tipo
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Red, RoundedCornerShape(3.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(documento.tipo, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(documento.nombreArchivo, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Navy)
            Text("${documento.fechaCarga} · ${documento.usuarioCarga}", fontSize = 7.5.sp, color = Grey)
        }
        // Badge de estado
        Box(
            modifier = Modifier
                .background(badgeBg, RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(documento.estado.name.lowercase().replaceFirstChar { it.uppercase() },
                fontSize = 7.sp, fontWeight = FontWeight.SemiBold, color = badgeFg)
        }
    }
}

private fun badgeColors(estado: EstadoDocumento): Pair<Color, Color> = when (estado) {
    EstadoDocumento.VIGENTE  -> Color(0xFFD1ECF1) to Color(0xFF0C5460)
    EstadoDocumento.PENDIENTE -> Color(0xFFFFF3CD) to Color(0xFF856404)
    EstadoDocumento.APROBADO -> Color(0xFFD4EDDA) to Color(0xFF155724)
    EstadoDocumento.RECHAZADO -> Color(0xFFF8D7DA) to Color(0xFF721C24)
    EstadoDocumento.VENCIDO  -> Color(0xFFE2E3E5) to Color(0xFF383D41)
}
>>>>>>> Stashed changes
