// ui/components/ImagenLocal.kt
package cl.cmq.salud.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import cl.cmq.salud.ui.theme.Grey
import cl.cmq.salud.ui.theme.Light
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Dibuja una imagen guardada en el almacenamiento interno de la app
 * (Semana 9.3): la fotografia capturada con la camara. Se decodifica fuera del
 * hilo principal y no requiere ninguna libreria de imagenes externa.
 */
@Composable
fun ImagenLocal(
    rutaRelativa: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, rutaRelativa) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                BitmapFactory.decodeFile(File(context.filesDir, rutaRelativa).absolutePath)
                    ?.asImageBitmap()
            }.getOrNull()
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(Light),
            contentAlignment = Alignment.Center
        ) {
            Text("Cargando imagen…", fontSize = 12.sp, color = Grey)
        }
    }
}
