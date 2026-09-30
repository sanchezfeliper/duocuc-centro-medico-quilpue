package cl.cmq.salud.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.cmq.salud.ui.theme.Green
import cl.cmq.salud.ui.theme.Navy

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Green,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy),
        border = BorderStroke(1.dp, Navy),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}