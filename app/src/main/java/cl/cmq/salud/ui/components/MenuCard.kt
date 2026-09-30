package cl.cmq.salud.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import cl.cmq.salud.ui.theme.Green
import cl.cmq.salud.ui.theme.Light
import cl.cmq.salud.ui.theme.Navy
import cl.cmq.salud.ui.theme.Grey

@Composable
fun MenuCard(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Light, RoundedCornerShape(6.dp))
            .border(width = 2.dp, color = Green, shape = RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Navy, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, color = Color.White, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Navy)
            Text(subtitle, fontSize = 9.sp, color = Grey)
        }
        Text(">", color = Grey, fontSize = 16.sp)
    }
}
