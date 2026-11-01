package cl.cmq.salud.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.cmq.salud.ui.theme.Blue
import cl.cmq.salud.ui.theme.Grey
import cl.cmq.salud.ui.theme.Light
import cl.cmq.salud.ui.theme.Navy
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description

/*
 * EmptyState — estado vacío compartido.
 *
 * Mensajes que dejan claro por qué no hay contenido y qué puede
 * hacer el usuario para corregirlo. Incluye un icono semántico.
 *
 * Animacion (rubrica EP2): la tarjeta aparece con un desvanecimiento y un
 * leve crecimiento (escala 0.94 -> 1.0). Al vivir el efecto aqui, TODAS las
 * pantallas que usan EmptyState (Expediente, Validacion, Perfil, ...) heredan
 * la animacion sin duplicar codigo.
 */
@Composable
fun EmptyState(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    // Progreso animado 0f -> 1f al entrar en composicion; tween corto y suave,
    // coherente con la duracion de las transiciones de navegacion.
    val progreso = remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) { progreso.value = 1f }
    val avance by animateFloatAsState(
        targetValue = progreso.value,
        animationSpec = tween(durationMillis = 320)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(avance)
            .scale(0.94f + 0.06f * avance),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Light)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Blue,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Blue,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                color = Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = detail,
                color = Grey,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}
