package cl.cmq.salud.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.Warning
import cl.cmq.salud.ui.theme.AttentionBg
import cl.cmq.salud.ui.theme.AttentionInk

/*
 * AlertCard — advertencia visual (vencimientos, estados que requieren atención).
 *
 * Usa color junto con icono y texto: nunca codifica el estado solo por color.
 */
@Composable
fun AlertCard(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AttentionBg, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Advertencia",
            tint = AttentionInk,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = "Atención requerida",
                color = AttentionInk,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.size(2.dp))
            Text(
                text = text,
                color = AttentionInk,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
