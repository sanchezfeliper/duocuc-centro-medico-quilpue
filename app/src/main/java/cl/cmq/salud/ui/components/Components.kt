package cl.cmq.salud.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.cmq.salud.ui.theme.Border
import cl.cmq.salud.ui.theme.Grey
import cl.cmq.salud.ui.theme.Light
import cl.cmq.salud.ui.theme.Navy
import cl.cmq.salud.ui.theme.Red
import cl.cmq.salud.ui.theme.SurfaceWhite

/**
 * FormField — campo de formulario compartido con etiqueta persistente,
 * valor, mensaje de ayuda/error debajo y semántica accesible.
 *
 * No usar placeholder como única etiqueta: el label siempre se muestra.
 * El color de error toma precedencia sobre el de ayuda.
 */
@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    error: String? = null,
    help: String? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 5,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        isError = error != null,
        supportingText = {
            when {
                error != null -> {
                    Text(error, color = Red, fontSize = 12.sp)
                }
                help != null -> {
                    Text(help, color = Grey, fontSize = 12.sp)
                }
            }
        },
        shape = RoundedCornerShape(cornerRadius),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Navy,
            unfocusedBorderColor = Border,
            focusedLabelColor = Navy,
            unfocusedLabelColor = Grey,
            focusedTextColor = Navy,
            unfocusedTextColor = Navy,
            cursorColor = Navy
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * DocumentSummary — fila resumida para listados.
 * Nombre, tipo, fecha y estado con orden consistente.
 * Maneja nombres largos sin desbordar.
 */
@Composable
fun DocumentRow(
    nombreArchivo: String,
    tipo: String,
    fecha: String,
    estado: cl.cmq.salud.domain.model.EstadoDocumento? = null,
    modifier: Modifier = Modifier,
    onClick: ((Int) -> Unit)? = null,
    idDocumento: Int = 0
) {
    val bgColor = SurfaceWhite
    val borderColor = Border

    Card(
        onClick = { onClick?.invoke(idDocumento) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = nombreArchivo,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Navy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "$tipo · $fecha",
                fontSize = 12.sp,
                color = Grey
            )
        }
    }
}

/**
 * InfoPill — etiqueta de información corta (tipo, origen).
 * Forma de píldora para estados cortos.
 */
@Composable
fun InfoPill(
    text: String,
    modifier: Modifier = Modifier,
    bgColor: Color = Light,
    textColor: Color = Navy
) {
    Box(
        modifier = modifier
            .background(bgColor, shape = RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
