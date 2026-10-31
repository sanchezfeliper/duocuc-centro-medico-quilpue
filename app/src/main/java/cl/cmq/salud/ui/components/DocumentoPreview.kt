package cl.cmq.salud.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.ui.theme.Border
import cl.cmq.salud.ui.theme.Spruce
import cl.cmq.salud.ui.theme.Surface as SurfaceColor
import cl.cmq.salud.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/** Shows a real image or PDF page; it never substitutes a decorative placeholder. */
@Composable
fun DocumentoPreview(
    documento: Documento,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pagina by rememberSaveable(documento.idDocumento) { mutableIntStateOf(0) }
    val route = documento.rutaLocal
    val preview by produceState<PreviewResult>(
        initialValue = PreviewResult.Loading,
        key1 = route,
        key2 = pagina,
        key3 = documento.tipo
    ) {
        value = withContext(Dispatchers.IO) {
            loadPreview(context.applicationContext, route, documento.tipo, pagina)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(16.dp)),
        color = SurfaceColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            when (val result = preview) {
                PreviewResult.Loading -> Box(
                    Modifier.fillMaxWidth().heightIn(min = 200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Spruce)
                }

                is PreviewResult.Unavailable -> Text(
                    text = result.message,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp)
                )

                is PreviewResult.Ready -> {
                    Image(
                        bitmap = result.bitmap,
                        contentDescription = if (result.pageCount > 1) {
                            "Página ${result.pageIndex + 1} de ${result.pageCount} de ${documento.nombreArchivo}"
                        } else {
                            "Vista previa de ${documento.nombreArchivo}"
                        },
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp, max = 420.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    if (result.pageCount > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = { pagina = (pagina - 1).coerceAtLeast(0) },
                                enabled = result.pageIndex > 0,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.NavigateBefore,
                                    contentDescription = "Página anterior",
                                    tint = Spruce
                                )
                            }
                            Text(
                                "Página ${result.pageIndex + 1} de ${result.pageCount}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            IconButton(
                                onClick = { pagina = (pagina + 1).coerceAtMost(result.pageCount - 1) },
                                enabled = result.pageIndex < result.pageCount - 1,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.NavigateNext,
                                    contentDescription = "Página siguiente",
                                    tint = Spruce
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed interface PreviewResult {
    data object Loading : PreviewResult
    data class Ready(
        val bitmap: ImageBitmap,
        val pageIndex: Int = 0,
        val pageCount: Int = 1
    ) : PreviewResult
    data class Unavailable(val message: String) : PreviewResult
}

private fun loadPreview(
    context: Context,
    route: String?,
    documentType: String,
    requestedPage: Int
): PreviewResult {
    if (route.isNullOrBlank()) {
        return PreviewResult.Unavailable("Este registro no tiene un archivo adjunto disponible para revisar.")
    }

    return runCatching {
        val uri = route.takeIf { it.startsWith("content://") || it.startsWith("file://") }
            ?.let { Uri.parse(it) }
        val isPdf = documentType.equals("PDF", ignoreCase = true) ||
            route.substringBefore('?').endsWith(".pdf", ignoreCase = true) ||
            uri?.let { context.contentResolver.getType(it)?.equals("application/pdf", ignoreCase = true) } == true

        if (isPdf) {
            renderPdfPage(context, uri, route, requestedPage)
        } else {
            val bitmap = decodeImage(context, uri, route)
                ?: return PreviewResult.Unavailable("No se pudo leer la imagen adjunta. Vuelve a seleccionar el archivo.")
            PreviewResult.Ready(bitmap.asImageBitmap())
        }
    }.getOrElse {
        PreviewResult.Unavailable("No se pudo abrir el archivo adjunto. Verifica que siga disponible y vuelve a intentar.")
    }
}

private fun renderPdfPage(
    context: Context,
    uri: Uri?,
    route: String,
    requestedPage: Int
): PreviewResult {
    val descriptor = openDescriptor(context, uri, route)
        ?: return PreviewResult.Unavailable("No se pudo abrir el PDF adjunto. Vuelve a seleccionar el archivo.")

    descriptor.use { pfd ->
        PdfRenderer(pfd).use { renderer ->
            if (renderer.pageCount == 0) {
                return PreviewResult.Unavailable("El PDF no contiene páginas para mostrar.")
            }
            val pageIndex = requestedPage.coerceIn(0, renderer.pageCount - 1)
            renderer.openPage(pageIndex).use { page ->
                val scale = minOf(1f, 1800f / page.width, 2400f / page.height)
                val bitmap = Bitmap.createBitmap(
                    (page.width * scale).toInt().coerceAtLeast(1),
                    (page.height * scale).toInt().coerceAtLeast(1),
                    Bitmap.Config.ARGB_8888
                )
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                return PreviewResult.Ready(bitmap.asImageBitmap(), pageIndex, renderer.pageCount)
            }
        }
    }
}

private fun openDescriptor(context: Context, uri: Uri?, route: String): ParcelFileDescriptor? =
    if (uri != null) {
        if (uri.scheme == "content") context.contentResolver.openFileDescriptor(uri, "r")
        else uri.path?.let { ParcelFileDescriptor.open(File(it), ParcelFileDescriptor.MODE_READ_ONLY) }
    } else {
        ParcelFileDescriptor.open(File(context.filesDir, route), ParcelFileDescriptor.MODE_READ_ONLY)
    }

private fun decodeImage(context: Context, uri: Uri?, route: String): Bitmap? {
    val openStream: () -> InputStream? = when {
        uri != null && uri.scheme == "content" -> ({ context.contentResolver.openInputStream(uri) })
        uri?.scheme == "file" -> ({ uri.path?.let(::File)?.let(::FileInputStream) })
        else -> ({ FileInputStream(File(context.filesDir, route)) })
    }

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    openStream()?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > 1800 || bounds.outHeight / sampleSize > 2400) {
        sampleSize *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return openStream()?.use { BitmapFactory.decodeStream(it, null, options) }
}
