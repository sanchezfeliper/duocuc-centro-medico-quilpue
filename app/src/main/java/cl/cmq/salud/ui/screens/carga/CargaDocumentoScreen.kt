// ui/screens/carga/CargaDocumentoScreen.kt
package cl.cmq.salud.ui.screens.carga

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.cmq.salud.ui.components.PrimaryButton
import cl.cmq.salud.ui.components.SecondaryButton
import cl.cmq.salud.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargaDocumentoScreen(
    onVolver: () -> Unit,
    onCargaExitosa: (Int) -> Unit = {},
    vm: CargaViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cargar documento", color = Color.White, fontSize = 15.sp) },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("<", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Título de la sección
            Text(
                text = "Registro de Nuevo Documento",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )
            Text(
                text = "Metadatos obligatorios para repositorio centralizado (RN-08)",
                fontSize = 10.sp,
                color = Grey
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Campo RUT Funcionario (OutlinedTextField)
            // Meta 3: validación en vivo de obligatorio, formato y dígito verificador
            OutlinedTextField(
                value = state.rut,
                onValueChange = { vm.onRutChange(it) },
                label = { Text("RUT Funcionario *") },
                placeholder = { Text("Ej: 18.765.432-7") },
                singleLine = true,
                isError = state.rutError != null,
                supportingText = {
                    if (state.rutError != null) {
                        Text(state.rutError ?: "", color = Red, fontSize = 10.sp)
                    } else {
                        Text("Formato 12.345.678-9; se verifica el dígito verificador", color = Grey, fontSize = 10.sp)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Tipo de Documento (DropdownMenu con ExposedDropdownMenuBox)
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
                            Text(state.tipoError ?: "", color = Red, fontSize = 10.sp)
                        }
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
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

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Nombre del archivo / Título (OutlinedTextField)
            OutlinedTextField(
                value = state.nombreArchivo,
                onValueChange = { vm.onNombreArchivoChange(it) },
                label = { Text("Nombre del archivo con extensión *") },
                placeholder = { Text("Ej: contrato_juan_perez.pdf") },
                singleLine = true,
                isError = state.nombreArchivoError != null,
                supportingText = {
                    if (state.nombreArchivoError != null) {
                        Text(state.nombreArchivoError ?: "", color = Red, fontSize = 10.sp)
                    } else {
                        Text("Formatos válidos: .pdf, .jpg, .png", color = Grey, fontSize = 10.sp)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Descripción / Observaciones (OutlinedTextField multilínea)
            // Meta 3: contador de caracteres visible para prevenir el máximo de 200 (E4)
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
                        Text(state.descripcionError ?: "", color = Red, fontSize = 10.sp)
                    } else {
                        Text("${state.descripcion.length}/200 caracteres (mínimo 5)", color = Grey, fontSize = 10.sp)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Origen del Documento (RadioButtons)
            Text(
                text = "Origen del documento",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Navy
            )
            Spacer(modifier = Modifier.height(4.dp))
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
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (state.origen == opcion),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = opcion.label, fontSize = 12.sp, color = Navy)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Requiere validación de Jefatura (Switch)
            Card(
                colors = CardDefaults.cardColors(containerColor = Light),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Derivar a Jefatura para validación",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Navy
                        )
                        Text(
                            text = if (state.requiereValidacionUrgente) "Quedará en estado PENDIENTE" else "Quedará en estado VIGENTE",
                            fontSize = 10.sp,
                            color = Grey
                        )
                    }
                    Switch(
                        checked = state.requiereValidacionUrgente,
                        onCheckedChange = { vm.onRequiereValidacionChange(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7. Declaración de autenticidad (Checkbox)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.declaraAutenticidad,
                    onCheckedChange = { vm.onAutenticidadChange(it) }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Declaro que el documento y metadatos ingresados corresponden a información fidedigna del funcionario.",
                    fontSize = 11.sp,
                    color = if (state.autenticidadError != null) Red else Navy,
                    lineHeight = 14.sp
                )
            }
            if (state.autenticidadError != null) {
                Text(
                    text = state.autenticidadError ?: "",
                    color = Red,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            // Mensaje de error general si falló validación
            if (state.generalError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.generalError ?: "",
                    color = Red,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 8. Botones de acción (Button / PrimaryButton / SecondaryButton)
            if (state.isSubmitting) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green)
                }
            } else {
                PrimaryButton(
                    text = "Guardar y Registrar Documento",
                    onClick = { vm.guardarDocumento() }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryButton(
                    text = "Limpiar Formulario",
                    onClick = { vm.limpiarFormulario() }
                )
            }
        }
    }

    // Modal de Retroalimentación de Éxito (Ciclo: Entregar respuesta)
    if (state.showSuccessDialog && state.documentoCreado != null) {
        val doc = state.documentoCreado!!
        AlertDialog(
            onDismissRequest = { vm.dismissSuccessDialog() },
            title = {
                Text("¡Documento Registrado!", fontWeight = FontWeight.Bold, color = Navy)
            },
            text = {
                Column {
                    Text("El documento se ha procesado y guardado correctamente.", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Folio: #${doc.idDocumento}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("• Archivo: ${doc.nombreArchivo}", fontSize = 11.sp)
                    Text("• Tipo: ${doc.tipo}", fontSize = 11.sp)
                    Text("• Estado: ${doc.estado.name}", fontSize = 11.sp, color = Green)
                    Text("• Fecha: ${doc.fechaCarga}", fontSize = 11.sp)
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
                TextButton(
                    onClick = {
                        vm.dismissSuccessDialog()
                        vm.limpiarFormulario()
                    }
                ) {
                    Text("Cargar otro", color = Grey)
                }
            }
        )
    }
}
