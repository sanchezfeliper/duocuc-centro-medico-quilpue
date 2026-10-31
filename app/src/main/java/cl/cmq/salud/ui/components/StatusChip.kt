package cl.cmq.salud.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.ui.theme.AttentionBg
import cl.cmq.salud.ui.theme.AttentionInk
import cl.cmq.salud.ui.theme.Error
import cl.cmq.salud.ui.theme.ErrorBg
import cl.cmq.salud.ui.theme.SoftSage
import cl.cmq.salud.ui.theme.Success
import cl.cmq.salud.ui.theme.SuccessBg
import cl.cmq.salud.ui.theme.TextSecondary

/*
 * StatusChip — etiqueta con color e icono/forma consistentes para los estados
 * del documento (vigente, pendiente, aprobado, rechazado, error, vencimiento).
 *
 * Nunca codifica un estado solo por color: siempre incluye texto y, de ser
 * posible, un icono. La forma es de píldora (50 dp) para estados cortos.
 */
@Composable
fun StatusChip(
    estado: EstadoDocumento,
    modifier: Modifier = Modifier
) {
    val (bg, fg, icon) = chipColors(estado)
    Box(
        modifier = modifier
            .background(bg, shape = RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = estado.etiqueta,
                tint = fg,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = estado.etiqueta,
                color = fg,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Paleta de colores e iconos por estado. Compartida con DocumentoCard,
 * DetalleDocumentoScreen y ValidacionScreen.
 *
 * Devuelve un triple: (fondo, tinta, icono).
 */
fun chipColors(estado: EstadoDocumento): Triple<Color, Color, androidx.compose.ui.graphics.vector.ImageVector> =
    when (estado) {
        EstadoDocumento.VIGENTE -> Triple(SuccessBg, Success, Icons.Default.CheckCircle)
        EstadoDocumento.PENDIENTE -> Triple(AttentionBg, AttentionInk, Icons.Default.Schedule)
        EstadoDocumento.APROBADO -> Triple(SuccessBg, Success, Icons.Default.CheckCircle)
        EstadoDocumento.RECHAZADO -> Triple(ErrorBg, Error, Icons.Default.Error)
        EstadoDocumento.PENDIENTE_CORRECCION -> Triple(AttentionBg, AttentionInk, Icons.Default.Warning)
        EstadoDocumento.VENCIDO -> Triple(SoftSage, TextSecondary, Icons.Default.Warning)
    }

/** Alias preservado: badgeColors devolvía Pair(bg, fg); ahora StatusChip
 *  incluye icono. Se mantiene para compatibilidad con código existente. */
fun badgeColors(estado: EstadoDocumento): Pair<Color, Color> {
    val (bg, fg, _) = chipColors(estado)
    return bg to fg
}
