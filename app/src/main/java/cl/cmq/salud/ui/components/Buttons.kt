package cl.cmq.salud.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.cmq.salud.ui.theme.Border
import cl.cmq.salud.ui.theme.Surface
import cl.cmq.salud.ui.theme.SurfaceWhite
import cl.cmq.salud.ui.theme.TextSecondary
import cl.cmq.salud.ui.theme.Spruce
import cl.cmq.salud.ui.theme.Teal

/*
 * Botones compartidos — rediseño UI.
 *
 * PrimaryButton: una acción dominante por contexto, con estado de carga
 * integrado (CircularProgressIndicator en lugar del texto mientras se procesa)
 * y estado deshabilitado. Radio de esquina 12 dp para consistencia con
 * los formularios.
 *
 * SecondaryButton: jerarquía claramente menor — contorno, sin relleno.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    cornerRadius: Dp = 12.dp
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Teal,
            contentColor = Surface,
            disabledContainerColor = Teal.copy(alpha = 0.38f),
            disabledContentColor = Surface.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.size(20.dp)) {
                CircularProgressIndicator(
                    color = Surface,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    icon: ImageVector? = null,
    iconTint: Color? = null
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Spruce,
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint ?: Spruce,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Spruce
        )
    }
}
