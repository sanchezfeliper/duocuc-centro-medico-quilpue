// ui/screens/carga/CargaDocumentoScreen.kt
package cl.cmq.salud.ui.screens.carga

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.provider.OpenableColumns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.cmq.salud.ui.components.AppTopBar
import cl.cmq.salud.ui.components.ImagenLocal
import cl.cmq.salud.ui.components.MainBottomNavBar
import cl.cmq.salud.ui.components.PrimaryButton
import cl.cmq.salud.ui.components.SecondaryButton
import cl.cmq.salud.ui.theme.Background
import cl.cmq.salud.ui.theme.Blue
import cl.cmq.salud.ui.theme.Border
import cl.cmq.salud.ui.theme.ErrorBg
import cl.cmq.salud.ui.theme.Grey
import cl.cmq.salud.ui.theme.Light
import cl.cmq.salud.ui.theme.Mint
import cl.cmq.salud.ui.theme.Navy
import cl.cmq.salud.ui.theme.Red
import cl.cmq.salud.ui.theme.SoftSage
import cl.cmq.salud.ui.theme.Spruce
import cl.cmq.salud.ui.theme.SurfaceWhite

// Fondo semántico del banner de errores.
private val BannerErrorBg = ErrorBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargaDocumentoScreen(
    onVolver: () -> Unit,
    onCargaExitosa: (Int) -> Unit = {},
    onMainDestinationSelected: (Int) -> Unit = {},
    vm: CargaViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val ctx = LocalContext.current

    // RF-07: selector de documentos del sistema para archivo digital real
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri == null) {
                vm.setFilePickerOpen(false)
                return@rememberLauncherForActivityResult
            }
            val persistido = runCatching {
                ctx.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }.isSuccess
            if (!persistido) {
                vm.onFileSelectionFailed("No se pudo conservar el acceso a este archivo. Elige otro desde el dispositivo.")
                return@rememberLauncherForActivityResult
            }
            val nombre = runCatching {
                ctx.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
                }
            }.getOrNull()?.takeIf { it.isNotBlank() }
                ?: uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
                ?: "archivo_${System.currentTimeMillis()}"
            val mimeType = ctx.contentResolver.getType(uri)
            vm.onFileSelected(uri.toString(), nombre, mimeType)
        }
    )

    // Semana 9.3 (RF-09): la captura con la camara ocupa toda la pantalla.
    if (state.isCamaraAbierta) {
        CamaraCapturaScreen(
            onFotoCapturada = { ruta, huella -> vm.onFotoCapturada(ruta, huella) },
            onCancelar = { vm.cerrarCamara() }
        )
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Meta 4: respuesta visible ante error.
    LaunchedEffect(state.errorEventId) {
        if (state.errorEventId > 0) {
            snackbarHostState.showSnackbar(
                message = "No se pudo registrar el documento: revise los campos marcados en rojo",
                actionLabel = "Entendido",
                duration = SnackbarDuration.Short
            )
        }
    }

    // Meta 4: mensaje de éxito visible al elegir "Cargar otro"
    LaunchedEffect(state.successEventId) {
        if (state.successEventId > 0) {
            snackbarHostState.showSnackbar(
                message = "Documento #${state.ultimoFolioRegistrado ?: "-"} registrado correctamente",
                duration = SnackbarDuration.Short
            )
        }
    }

    // Diálogo de confirmación para Limpiar formulario
    val (showClearDialog, setShowClearDialog) = remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = if (state.isEditando) "Editar documento" else "Cargar documento") },
        bottomBar = {
            MainBottomNavBar(activeIndex = 2, onItemSelected = onMainDestinationSelected)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Título de pantalla ---
            Text(
                text = "Registra un documento",
                style = androidx.compose.material3.MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
                color = Spruce
            )
            Text(
                text = "Completa los datos para incorporarlo al expediente.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                color = Grey
            )

            Spacer(Modifier.height(20.dp))

            // ===== 1. Funcionario =====
            Text(
                text = "Funcionario",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Spruce
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = state.rut,
                onValueChange = { vm.onRutChange(it) },
                label = { Text("RUT Funcionario *") },
                placeholder = { Text("Ej: 18.765.432-7") },
                singleLine = true,
                isError = state.rutError != null,
                supportingText = {
                    if (state.rutError != null) {
                        Text(state.rutError ?: "", color = Red, fontSize = 12.sp)
                    } else {
                        Text(
                            "Formato 12.345.678-9; se verifica el dígito verificador",
                            color = Grey,
                            fontSize = 12.sp
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))

            // ===== 2. Documento =====
            Text(
                text = "Documento",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Spruce
            )
            Spacer(Modifier.height(10.dp))

            // Tipo de Documento
            ExposedDropdownMenuBox(
                expanded = state.isDropdownExpanded,
                onExpandedChange = { vm.onDropdownExpandChange(it) },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.tipoSeleccionado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de documento *") },
                    isError = state.tipoError != null,
                    supportingText = {
                        if (state.tipoError != null) {
                            Text(state.tipoError ?: "", color = Red, fontSize = 12.sp)
                        }
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isDropdownExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = state.isDropdownExpanded,
                    onDismissRequest = { vm.onDropdownExpandChange(false) }
                ) {
                    state.tiposDisponibles.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo, fontSize = 13.sp) },
                            onClick = { vm.onTipoChange(tipo) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Nombre del archivo (se autocompleta al seleccionar archivo digital o foto)
            OutlinedTextField(
                value = state.nombreArchivo,
                onValueChange = { vm.onNombreArchivoChange(it) },
                label = { Text("Nombre del archivo con extensión *") },
                placeholder = { Text("Se completa al seleccionar el archivo") },
                readOnly = state.fileUri != null || state.fotoRuta != null,
                singleLine = true,
                isError = state.nombreArchivoError != null,
                supportingText = {
                    if (state.nombreArchivoError != null) {
                        Text(state.nombreArchivoError ?: "", color = Red, fontSize = 12.sp)
                    } else if (state.fileNombre != null || state.fotoRuta != null) {
                        Text("Nombre asignado automáticamente", color = Grey, fontSize = 12.sp)
                    } else {
                        Text("Formatos válidos: .pdf, .jpg, .png", color = Grey, fontSize = 12.sp)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Descripción / Observaciones
            OutlinedTextField(
                value = state.descripcion,
                onValueChange = { vm.onDescripcionChange(it) },
                label = { Text("Descripción / Observaciones *") },
                placeholder = { Text("Ingrese antecedentes o motivo de la carga...") },
                minLines = 3,
                maxLines = 5,
                isError = state.descripcionError != null,
                supportingText = {
                    if (state.descripcionError != null) {
                        Text(state.descripcionError ?: "", color = Red, fontSize = 12.sp)
                    } else {
                        Text(
                            "${state.descripcion.length}/200 caracteres (mínimo 5)",
                            color = Grey,
                            fontSize = 12.sp
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))

            // Origen del Documento
            Text(
                text = "Origen del documento",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Spruce
            )
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.selectableGroup()) {
                OrigenDocumento.entries.forEach { opcion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (state.origen == opcion),
                                onClick = { vm.onOrigenChange(opcion) },
                                role = Role.RadioButton
                            )
                            .background(
                                color = if (state.origen == opcion) SoftSage else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (state.origen == opcion),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Blue,
                                unselectedColor = Grey
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = opcion.label,
                            fontSize = 14.sp,
                            color = Navy,
                            fontWeight = if (state.origen == opcion) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            // RF-07: Archivo digital — selector real del sistema
            if (state.origen == OrigenDocumento.DIGITAL) {
                Spacer(Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Archivo digital (PDF / JPG / PNG)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Navy
                        )
                        Text(
                            text = "Selecciona un archivo del dispositivo usando el selector del sistema.",
                            fontSize = 12.sp,
                            color = Grey
                        )

                        if (state.fileUri != null && state.fileNombre != null) {
                            Spacer(Modifier.height(12.dp))
                            // Muestra el archivo real seleccionado
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = state.fileNombre!!,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Navy
                                    )
                                    Text(
                                        text = state.fileMimeType ?: "Tipo desconocido",
                                        fontSize = 12.sp,
                                        color = Grey
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Mint, shape = RoundedCornerShape(14.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Listo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Blue
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                                SecondaryButton(
                                    text = "Cambiar",
                                    onClick = {
                                        vm.setFilePickerOpen(false)
                                        filePicker.launch(arrayOf("application/pdf", "image/jpeg", "image/png"))
                                    },
                                    modifier = Modifier.height(32.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            SecondaryButton(
                                text = "Quitar archivo",
                                onClick = { vm.clearFileSelected() }
                            )
                        } else {
                            Spacer(Modifier.height(10.dp))
                            PrimaryButton(
                                text = "Seleccionar archivo",
                                onClick = {
                                    vm.setFilePickerOpen(true)
                                    filePicker.launch(arrayOf("application/pdf", "image/jpeg", "image/png"))
                                }
                            )
                        }

                        if (state.digitalError != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(state.digitalError ?: "", color = Red, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Semana 9.3 (RF-09): al elegir "Captura física con cámara" se habilita la captura real
            if (state.origen == OrigenDocumento.CAMARA) {
                Spacer(Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Fotografía del documento físico (RF-09)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Navy
                        )
                        Text(
                            text = "Se usa la cámara del dispositivo. La imagen queda en " +
                                "el almacenamiento local y se asocia al documento.",
                            fontSize = 12.sp,
                            color = Grey
                        )

                        if (state.fotoRuta != null) {
                            Spacer(Modifier.height(10.dp))
                            ImagenLocal(
                                rutaRelativa = state.fotoRuta!!,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                contentDescription = "Fotografía capturada del documento"
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Captura guardada",
                                fontSize = 12.sp,
                                color = Blue,
                                fontWeight = FontWeight.SemiBold
                            )
                            state.fotoHash?.let { hash ->
                                Text(
                                    text = "SHA-256: ${hash.take(20)}… (RF-11)",
                                    fontSize = 12.sp,
                                    color = Grey
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        if (state.fotoRuta == null) {
                            PrimaryButton(
                                text = "Tomar fotografía",
                                onClick = { vm.abrirCamara() }
                            )
                        } else {
                            PrimaryButton(
                                text = "Volver a tomar la fotografía",
                                onClick = { vm.abrirCamara() }
                            )
                            Spacer(Modifier.height(8.dp))
                            SecondaryButton(
                                text = "Descartar fotografía",
                                onClick = { vm.descartarFoto() }
                            )
                        }

                        if (state.fotoError != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(state.fotoError ?: "", color = Red, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ===== 3. Confirmación =====
            Text(
                text = "Confirmación",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Spruce
            )
            Spacer(Modifier.height(10.dp))

            // Derivar a Jefatura (Switch)
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftSage),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Derivar a Jefatura para validación",
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Navy
                        )
                        Text(
                            text = if (state.requiereValidacionUrgente)
                                "Quedará en estado PENDIENTE hasta que un jefe lo apruebe"
                            else
                                "Quedará en estado VIGENTE inmediatamente",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = Grey
                        )
                    }
                    Switch(
                        checked = state.requiereValidacionUrgente,
                        onCheckedChange = { vm.onRequiereValidacionChange(it) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Declaración de autenticidad
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.declaraAutenticidad,
                    onCheckedChange = { vm.onAutenticidadChange(it) }
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Declaro que el documento y metadatos ingresados corresponden a " +
                        "información fidedigna del funcionario.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = if (state.autenticidadError != null) Red else Navy,
                    lineHeight = 21.sp
                )
            }
            if (state.autenticidadError != null) {
                Text(
                    text = state.autenticidadError ?: "",
                    color = Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 28.dp)
                )
            }

            // Meta 4: banner condicional con el resumen de campos a corregir
            if (state.generalError != null) {
                Spacer(Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = BannerErrorBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = state.generalError ?: "",
                            color = Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (state.camposConError.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Text("Campos a revisar:", color = Red, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            state.camposConError.forEach { campo ->
                                Text(
                                    text = "• $campo",
                                    color = Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Botones de acción
            if (state.isSubmitting) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Blue)
                }
            } else {
                PrimaryButton(
                    text = when {
                        state.isSubmitting -> "Guardando..."
                        state.isEditando -> "Guardar cambios"
                        else -> "Guardar y Registrar Documento"
                    },
                    onClick = { vm.guardarDocumento() }
                )
                // En modo edición no se ofrece "limpiar": vaciaría el registro
                // original; para descartar cambios basta volver atrás.
                if (!state.isEditando) {
                    Spacer(Modifier.height(12.dp))
                    // Guía: "Limpiar formulario" → secundaria menos prominente + confirmación
                    SecondaryButton(
                        text = "Limpiar formulario",
                        onClick = { setShowClearDialog(true) },
                        icon = Icons.Default.Delete
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Diálogo de confirmación para limpiar
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { setShowClearDialog(false) },
            title = { Text("Limpiar formulario", fontWeight = FontWeight.Bold, color = Navy) },
            text = {
                Text(
                    "¿Estás seguro de que deseas limpiar el formulario? Se perderán todos los datos ingresados.",
                    fontSize = 13.sp,
                    color = Navy
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.limpiarFormulario()
                        setShowClearDialog(false)
                    }
                ) {
                    Text("Limpiar", color = Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { setShowClearDialog(false) }) {
                    Text("Cancelar", color = Grey)
                }
            }
        )
    }

    // Modal de Retroalimentación de Éxito (Ciclo: Entregar respuesta)
    if (state.showSuccessDialog && state.documentoCreado != null) {
        val doc = state.documentoCreado!!
        AlertDialog(
            onDismissRequest = { vm.dismissSuccessDialog() },
            title = {
                Text(
                    if (state.isEditando) "¡Documento Actualizado!" else "¡Documento Registrado!",
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
            },
            text = {
                Column {
                    Text(
                        if (state.isEditando) {
                            "Los cambios se guardaron correctamente en el almacenamiento local."
                        } else {
                            "El documento se ha procesado y guardado correctamente."
                        },
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("• Folio: #${doc.idDocumento}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("• Archivo: ${doc.nombreArchivo}", fontSize = 12.sp)
                    Text("• Tipo: ${doc.tipo}", fontSize = 12.sp)
                    Text("• Estado: ${doc.estado.etiqueta}", fontSize = 12.sp, color = Blue)
                    Text("• Fecha: ${doc.fechaCarga}", fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.dismissSuccessDialog()
                        onCargaExitosa(doc.idDocumento)
                    }
                ) {
                    Text("Ver en Inicio", color = Navy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                if (state.isEditando) {
                    TextButton(onClick = { vm.dismissSuccessDialog() }) {
                        Text("Seguir editando", color = Grey)
                    }
                } else {
                    TextButton(onClick = { vm.cargarOtro() }) {
                        Text("Cargar otro", color = Grey)
                    }
                }
            }
        )
    }
}
