// ui/screens/carga/CamaraCaptura.kt
package cl.cmq.salud.ui.screens.carga

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import cl.cmq.salud.data.local.AlmacenFotosLocales
import cl.cmq.salud.di.AppContainer
import cl.cmq.salud.ui.theme.Blue
import cl.cmq.salud.ui.theme.Green
import cl.cmq.salud.ui.theme.Red

/**
 * P05 · Recurso nativo: camara del dispositivo (Semana 9.3, RF-09).
 *
 * Pantalla de captura del documento fisico. Usa CameraX (Preview + ImageCapture)
 * enlazado al ciclo de vida del host; la fotografia se guarda en el
 * almacenamiento interno de la app y su ruta y huella vuelven al formulario.
 * El ViewModel no participa del archivo: recibe solo la ruta y el hash.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamaraCapturaScreen(
    onFotoCapturada: (rutaRelativa: String, huella: String) -> Unit,
    onCancelar: () -> Unit,
    almacen: AlmacenFotosLocales = AppContainer.almacenFotos
) {
    val context = LocalContext.current
    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> permisoConcedido = concedido }

    // Al entrar se solicita el permiso de camara (RF-09) si aun no esta concedido.
    LaunchedEffect(Unit) {
        if (!permisoConcedido) lanzadorPermiso.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capturar documento físico (RF-09)", color = Color.White, fontSize = 14.sp) },
                navigationIcon = {
                    TextButton(onClick = onCancelar) {
                        Text("<", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            if (permisoConcedido) {
                VistaCamara(
                    almacen = almacen,
                    onFotoCapturada = onFotoCapturada,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Se necesita acceso a la cámara del dispositivo para digitalizar " +
                            "el documento físico (RF-09). Sin este permiso no es posible capturar la imagen.",
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = { lanzadorPermiso.launch(Manifest.permission.CAMERA) }) {
                        Text("Conceder permiso de cámara")
                    }
                    Spacer(Modifier.height(6.dp))
                    TextButton(onClick = onCancelar) {
                        Text("Cancelar", color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Vista previa de la camara trasera y boton obturador. Enlaza CameraX al ciclo
 * de vida y, al capturar, escribe el JPEG en el almacen de fotos de la app.
 */
@Composable
private fun VistaCamara(
    almacen: AlmacenFotosLocales,
    onFotoCapturada: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val ejecutor = remember { ContextCompat.getMainExecutor(context) }

    var camaraLista by remember { mutableStateOf(false) }
    var capturando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(lifecycleOwner) {
        val futuro = ProcessCameraProvider.getInstance(context)
        val alListo = Runnable {
            try {
                val proveedor = futuro.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                proveedor.unbindAll()
                proveedor.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
                camaraLista = true
            } catch (e: Exception) {
                error = "No se pudo iniciar la cámara del dispositivo: ${e.message}"
            }
        }
        futuro.addListener(alListo, ejecutor)
        onDispose {
            runCatching { futuro.get().unbindAll() }
        }
    }

    fun tomarFoto() {
        if (capturando || !camaraLista) return
        capturando = true
        error = null
        val archivo = almacen.nuevoArchivo()
        val opciones = ImageCapture.OutputFileOptions.Builder(archivo).build()
        imageCapture.takePicture(
            opciones,
            ejecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(resultado: ImageCapture.OutputFileResults) {
                    capturando = false
                    onFotoCapturada(almacen.rutaRelativa(archivo), almacen.huella(archivo))
                }

                override fun onError(excepcion: ImageCaptureException) {
                    capturando = false
                    error = "No se pudo guardar la fotografía: ${excepcion.message}"
                }
            }
        )
    }

    Box(modifier = modifier) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // Guia de encuadre (referencia visual para el documento fisico)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.85f)
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.08f))
        )

        error?.let { mensaje ->
            Text(
                text = mensaje,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(Red.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    !camaraLista -> "Iniciando cámara del dispositivo…"
                    capturando -> "Guardando fotografía…"
                    else -> "Encuadre el documento y presione el botón para capturar"
                },
                color = Color.White,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(if (camaraLista && !capturando) Color.White else Color.White.copy(alpha = 0.4f))
                    .clickable(enabled = camaraLista && !capturando) { tomarFoto() }
                    .semantics { contentDescription = "Botón capturar fotografía" },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Green)
                )
            }
        }
    }
}
