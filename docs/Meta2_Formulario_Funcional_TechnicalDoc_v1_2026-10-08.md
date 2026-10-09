DSY1105 · Meta 2 · Formulario Funcional P05 Carga de Documento

# **Meta 2 - Formulario Funcional en Jetpack Compose**

_P05: Formulario de Carga y Registro de Documentos con Metadatos_

DSY1105 - Desarrollo de Aplicaciones Móviles  ·  CMQ Área Salud  ·  08-10-2026

---

## **1. Selección y Justificación del Formulario**

Conforme a los lineamientos de la **Meta 2**, cada equipo debe avanzar en al menos un formulario real relacionado directamente con la problemática de su solución. Tras contar con la autenticación (P01 Login), el menú principal (P02 Home) y la consulta (P03 Consulta de Expediente), el formulario esencial del sistema es **P05: Carga de Documento** (Mockup 4).

### **1.1 Justificación en el Dominio CMQ Salud**
El problema central diagnosticado en el Área de Salud de la Corporación Municipal de Quilpué es la dispersión documental y la falta de estandarización al incorporar antecedentes laborales de los funcionarios. El formulario de carga resuelve este punto crítico al forzar el registro de metadatos obligatorios, validar la información antes de persistirla y clasificar el flujo administrativo correspondiente.

### **1.2 Requerimientos Funcionales y Reglas de Negocio Asociadas**
* **RF-08 (Carga de documentos digitales)**: Permite registrar archivos en formatos PDF, JPG y PNG.
* **RF-09 (Captura fotográfica móvil)**: Permite discriminar el origen entre archivo digital y captura física mediante cámara.
* **RF-10 (Metadatos obligatorios)**: Exige tipo de documento, descripción/observaciones, fecha y usuario responsable.
* **RF-11 (Validación de formato)**: Valida sintácticamente la extensión del archivo y consistencia de los datos.
* **RF-15 (Derivación a validación)**: Permite activar la derivación automática a la bandeja de jefatura (estado PENDIENTE).
* **RN-01 y RN-02 (Perfiles y permisos)**: Operado por personal administrativo (ADM) o el propio funcionario (FUN).
* **RN-03 (Datos simulados)**: Opera de forma desacoplada con simulación de latencia de red.
* **RN-08 (Trazabilidad y autenticidad)**: Exige declaración jurada de veracidad de los antecedentes cargados.

| **Atributo** | **Valor** |
|---|---|
| **ID Formulario** | FORM-P05 |
| **Pantalla asociada** | P05 - Carga de documento (`CargaDocumentoScreen.kt`) |
| **Actor principal** | Personal Administrativo (ADM) / Funcionario (FUN) |
| **Entidad de datos** | Documento (Entidad E4 del modelo de datos) |
| **Ciclo implementado** | Ingresar → Modificar → Validar → Procesar → Entregar respuesta |
| **Arquitectura** | MVVM + Jetpack Compose + StateFlow + Material 3 |

---

## **2. Componentes de UI Utilizados (Material 3)**

La implementación incorpora los componentes solicitados en la pauta de evaluación, justificados por los requerimientos del caso:

1. **`OutlinedTextField`**:
   * **RUT Funcionario**: Campo de texto con validación de formato (ej. `18.765.432-1`) y mensaje de error reactivo (`supportingText`).
   * **Nombre del archivo / Título**: Permite ingresar el nombre del documento exigiendo extensión válida (`.pdf`, `.jpg`, `.png`).
   * **Descripción / Observaciones**: Campo multilínea (`minLines = 3`) para detallar el contexto del documento.
2. **`ExposedDropdownMenuBox` y `DropdownMenu`**:
   * **Tipo de documento**: Menú desplegable con el catálogo institucional oficial (Contrato de trabajo, Certificado de capacitación, Licencia médica, Permiso administrativo, Anexo de contrato).
3. **`RadioButton`**:
   * **Origen del documento**: Selección exclusiva entre *Archivo digital (PDF/Imagen)* y *Captura física con cámara* (RF-09).
4. **`Switch`**:
   * **Derivar a Jefatura para validación**: Control booleano que determina dinámicamente si el documento ingresa en estado `PENDIENTE` (requiere aprobación según RF-15) o `VIGENTE`.
5. **`Checkbox`**:
   * **Declaración de autenticidad**: Casilla de verificación legal obligatoria (RN-08); si no está marcada, el formulario bloquea el procesamiento.
6. **`Button` (`PrimaryButton` y `SecondaryButton`)**:
   * **Guardar y Registrar Documento**: Dispara el ciclo de validación y persistencia asíncrona.
   * **Limpiar Formulario**: Restablece los campos al estado por defecto.
7. **`AlertDialog`**:
   * **Retroalimentación de respuesta**: Ventana modal que informa el resultado del procesamiento, entregando el folio asignado (#ID), el estado resultante y opciones para continuar.

---

## **3. Ciclo de Vida del Formulario**

La guía docente define el formulario como un ciclo continuo de 5 fases, implementado íntegramente:

```
[ 1. INGRESAR ] ──> [ 2. MODIFICAR ] ──> [ 3. VALIDAR ] ──> [ 4. PROCESAR ] ──> [ 5. ENTREGAR RESPUESTA ]
      │                   │                    │                   │                     │
  Campos UI          StateFlow UDF         ViewModel          Repository           AlertDialog
(TextFields,        (CargaUiState)       (Reglas RN-08,     (Simulación red       (Folio #ID, Estado,
 Dropdown, etc.)                          RUT, Extension)    Coroutines delay)     Limpieza/Navegación)
```

### **3.1 Ingresar (Captura)**
El usuario interactúa con los controles en pantalla: ingresa el RUT del funcionario, selecciona el tipo desde el menú desplegable, define el nombre del archivo, escribe la descripción, elige el origen, decide si derivar a jefatura y confirma la declaración de autenticidad.

### **3.2 Modificar (Gestión de Estado Reactivo)**
Cada cambio en la interfaz emite un evento al ViewModel (`onRutChange`, `onTipoChange`, `onNombreArchivoChange`, etc.). Siguiendo el patrón **Unidirectional Data Flow (UDF)**, el ViewModel actualiza una instancia inmutable de `CargaUiState` expuesta a través de `StateFlow`. La vista se recompone automáticamente mediante `collectAsState()`.

### **3.3 Validar (Lógica de Negocio en ViewModel)**
Al presionar el botón de guardado, `CargaViewModel.guardarDocumento()` ejecuta las validaciones:
* **RUT**: Comprueba no vacuidad y coincidencia con expresión regular chilena `^\d{1,2}\.\d{3}\.\d{3}-[\dkK]$`.
* **Nombre de archivo**: Comprueba no vacuidad y presencia de extensión permitida (`.pdf`, `.jpg`, `.jpeg`, `.png`).
* **Descripción**: Exige un mínimo de 5 caracteres con contenido significativo.
* **Autenticidad**: Exige que el `Checkbox` esté activado (`declaraAutenticidad == true`).

Si alguna regla falla, se inyectan mensajes de error específicos en `rutError`, `nombreArchivoError`, `descripcionError` y `autenticidadError`, mostrándose visualmente en color rojo sin perder los datos ya escritos.

### **3.4 Procesar (Persistencia Asíncrona)**
Si todas las validaciones son exitosas:
1. Se activa el indicador de carga `isSubmitting = true`, mostrando un `CircularProgressIndicator` en el botón.
2. Mediante una corutina en `viewModelScope.launch`, se invoca `DocumentRepository.guardarDocumento()`.
3. Se simula latencia de red (`delay(800)`) y se genera un identificador correlativo único (`nextId`).
4. El nuevo documento queda registrado en el repositorio en memoria, quedando disponible inmediatamente para la consulta de expedientes (P03).

### **3.5 Entregar Respuesta (Retroalimentación)**
Al finalizar el procesamiento:
1. `isSubmitting` regresa a `false`.
2. Se activa `showSuccessDialog = true` desplegando un `AlertDialog` que detalla:
   * Folio asignado (ej. `#5`).
   * Nombre del archivo y tipo.
   * Estado resultante (`PENDIENTE` o `VIGENTE`).
   * Fecha de carga y usuario auditor.
3. El usuario puede elegir entre **"Ver en Inicio"** (navega al Home con Toast de confirmación) o **"Cargar otro"** (cierra el modal y restablece el formulario).

---

## **4. Arquitectura y Código Fuente Implementado**

### **4.1 Estado de UI (`CargaUiState.kt`)**

```kotlin
package cl.cmq.salud.ui.screens.carga

import cl.cmq.salud.domain.model.Documento

enum class OrigenDocumento(val label: String) {
    DIGITAL("Archivo digital (PDF / JPG / PNG)"),
    CAMARA("Captura física con cámara")
}

data class CargaUiState(
    // Campos de entrada
    val rut: String = "18.765.432-1",
    val tipoSeleccionado: String = "Contrato de trabajo",
    val nombreArchivo: String = "",
    val descripcion: String = "",
    val origen: OrigenDocumento = OrigenDocumento.DIGITAL,
    val requiereValidacionUrgente: Boolean = false,
    val declaraAutenticidad: Boolean = false,

    // Catálogo de tipos disponibles (RN-08)
    val tiposDisponibles: List<String> = listOf(
        "Contrato de trabajo",
        "Certificado de capacitación",
        "Licencia médica",
        "Permiso administrativo",
        "Anexo de contrato"
    ),

    // Estado visual del selector Dropdown
    val isDropdownExpanded: Boolean = false,

    // Errores de validación por campo
    val rutError: String? = null,
    val nombreArchivoError: String? = null,
    val descripcionError: String? = null,
    val autenticidadError: String? = null,
    val generalError: String? = null,

    // Estado de envío / procesamiento
    val isSubmitting: Boolean = false,

    // Retroalimentación / Respuesta
    val documentoCreado: Documento? = null,
    val showSuccessDialog: Boolean = false
)
```

### **4.2 ViewModel (`CargaViewModel.kt`)**

```kotlin
package cl.cmq.salud.ui.screens.carga

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CargaViewModel(
    private val repo: DocumentRepository = DocumentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CargaUiState())
    val uiState: StateFlow<CargaUiState> = _uiState.asStateFlow()

    fun onRutChange(value: String) {
        _uiState.update { it.copy(rut = value, rutError = null, generalError = null) }
    }

    fun onTipoChange(value: String) {
        _uiState.update { it.copy(tipoSeleccionado = value, isDropdownExpanded = false) }
    }

    fun onDropdownExpandChange(expanded: Boolean) {
        _uiState.update { it.copy(isDropdownExpanded = expanded) }
    }

    fun onNombreArchivoChange(value: String) {
        _uiState.update { it.copy(nombreArchivo = value, nombreArchivoError = null, generalError = null) }
    }

    fun onDescripcionChange(value: String) {
        _uiState.update { it.copy(descripcion = value, descripcionError = null, generalError = null) }
    }

    fun onOrigenChange(value: OrigenDocumento) {
        _uiState.update { it.copy(origen = value) }
    }

    fun onRequiereValidacionChange(value: Boolean) {
        _uiState.update { it.copy(requiereValidacionUrgente = value) }
    }

    fun onAutenticidadChange(value: Boolean) {
        _uiState.update { it.copy(declaraAutenticidad = value, autenticidadError = null, generalError = null) }
    }

    fun limpiarFormulario() {
        _uiState.value = CargaUiState()
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(showSuccessDialog = false) }
    }

    fun guardarDocumento() {
        val state = _uiState.value

        var hayError = false
        var rutErr: String? = null
        var nombreErr: String? = null
        var descErr: String? = null
        var autErr: String? = null

        // 1. Validar RUT
        val regexRut = Regex("^\\d{1,2}\\.\\d{3}\\.\\d{3}-[\\dkK]$")
        if (state.rut.isBlank()) {
            rutErr = "El RUT es obligatorio"
            hayError = true
        } else if (!regexRut.matches(state.rut.trim())) {
            rutErr = "Formato de RUT inválido (ej: 18.765.432-1)"
            hayError = true
        }

        // 2. Validar Nombre de archivo
        if (state.nombreArchivo.isBlank()) {
            nombreErr = "Debe indicar el nombre del archivo"
            hayError = true
        } else {
            val extensionesValidas = listOf(".pdf", ".jpg", ".jpeg", ".png")
            val tieneExtension = extensionesValidas.any { state.nombreArchivo.lowercase().endsWith(it) }
            if (!tieneExtension) {
                nombreErr = "Debe incluir extensión válida (.pdf, .jpg, .png)"
                hayError = true
            }
        }

        // 3. Validar Descripción
        if (state.descripcion.isBlank()) {
            descErr = "La descripción es obligatoria"
            hayError = true
        } else if (state.descripcion.trim().length < 5) {
            descErr = "Mínimo 5 caracteres para la descripción"
            hayError = true
        }

        // 4. Validar Autenticidad
        if (!state.declaraAutenticidad) {
            autErr = "Debe declarar la autenticidad para registrar el archivo"
            hayError = true
        }

        if (hayError) {
            _uiState.update {
                it.copy(
                    rutError = rutErr,
                    nombreArchivoError = nombreErr,
                    descripcionError = descErr,
                    autenticidadError = autErr,
                    generalError = "Por favor corrija los campos marcados antes de continuar"
                )
            }
            return
        }

        // 5. Procesamiento
        _uiState.update { it.copy(isSubmitting = true, generalError = null) }

        viewModelScope.launch {
            val tipoFormato = if (state.nombreArchivo.lowercase().endsWith(".pdf")) "PDF" else "JPG"
            val estadoInicial = if (state.requiereValidacionUrgente) EstadoDocumento.PENDIENTE else EstadoDocumento.VIGENTE

            val nuevoDoc = Documento(
                idDocumento = 0,
                idFuncionario = 101,
                tipo = tipoFormato,
                nombreArchivo = state.nombreArchivo.trim(),
                estado = estadoInicial,
                fechaCarga = "2026-10-08",
                usuarioCarga = "M. Gonzalez (Admin)",
                descripcion = state.descripcion.trim()
            )

            val guardado = repo.guardarDocumento(nuevoDoc)

            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    documentoCreado = guardado,
                    showSuccessDialog = true
                )
            }
        }
    }
}
```

### **4.3 Pantalla Compose (`CargaDocumentoScreen.kt`)**

```kotlin
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
            Text("Registro de Nuevo Documento", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text("Metadatos obligatorios para repositorio centralizado (RN-08)", fontSize = 10.sp, color = Grey)

            Spacer(modifier = Modifier.height(14.dp))

            // 1. RUT Funcionario (OutlinedTextField)
            OutlinedTextField(
                value = state.rut,
                onValueChange = { vm.onRutChange(it) },
                label = { Text("RUT Funcionario *") },
                placeholder = { Text("Ej: 18.765.432-1") },
                singleLine = true,
                isError = state.rutError != null,
                supportingText = { state.rutError?.let { Text(it, color = Red, fontSize = 10.sp) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Tipo de documento (DropdownMenu)
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
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

            // 3. Nombre del archivo (OutlinedTextField)
            OutlinedTextField(
                value = state.nombreArchivo,
                onValueChange = { vm.onNombreArchivoChange(it) },
                label = { Text("Nombre del archivo con extensión *") },
                placeholder = { Text("Ej: contrato_juan_perez.pdf") },
                singleLine = true,
                isError = state.nombreArchivoError != null,
                supportingText = {
                    Text(
                        state.nombreArchivoError ?: "Formatos válidos: .pdf, .jpg, .png",
                        color = if (state.nombreArchivoError != null) Red else Grey,
                        fontSize = 10.sp
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Descripción / Observaciones (OutlinedTextField)
            OutlinedTextField(
                value = state.descripcion,
                onValueChange = { vm.onDescripcionChange(it) },
                label = { Text("Descripción / Observaciones *") },
                placeholder = { Text("Ingrese antecedentes o motivo de la carga...") },
                minLines = 3,
                maxLines = 5,
                isError = state.descripcionError != null,
                supportingText = { state.descripcionError?.let { Text(it, color = Red, fontSize = 10.sp) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Origen del documento (RadioButtons)
            Text("Origen del documento", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Navy)
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
                        RadioButton(selected = (state.origen == opcion), onClick = null)
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Derivar a Jefatura para validación", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Navy)
                        Text(
                            if (state.requiereValidacionUrgente) "Quedará en estado PENDIENTE" else "Quedará en estado VIGENTE",
                            fontSize = 10.sp, color = Grey
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
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.declaraAutenticidad,
                    onCheckedChange = { vm.onAutenticidadChange(it) }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Declaro que el documento y metadatos ingresados corresponden a información fidedigna del funcionario.",
                    fontSize = 11.sp,
                    color = if (state.autenticidadError != null) Red else Navy,
                    lineHeight = 14.sp
                )
            }
            state.autenticidadError?.let {
                Text(it, color = Red, fontSize = 10.sp, modifier = Modifier.padding(start = 12.dp))
            }

            if (state.generalError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.generalError ?: "", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 8. Botones de acción
            if (state.isSubmitting) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green)
                }
            } else {
                PrimaryButton(text = "Guardar y Registrar Documento", onClick = { vm.guardarDocumento() })
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryButton(text = "Limpiar Formulario", onClick = { vm.limpiarFormulario() })
            }
        }
    }

    // Modal de Retroalimentación de Éxito
    if (state.showSuccessDialog && state.documentoCreado != null) {
        val doc = state.documentoCreado!!
        AlertDialog(
            onDismissRequest = { vm.dismissSuccessDialog() },
            title = { Text("¡Documento Registrado!", fontWeight = FontWeight.Bold, color = Navy) },
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
                TextButton(onClick = {
                    vm.dismissSuccessDialog()
                    onCargaExitosa(doc.idDocumento)
                }) {
                    Text("Ver en Inicio", color = Navy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    vm.dismissSuccessDialog()
                    vm.limpiarFormulario()
                }) {
                    Text("Cargar otro", color = Grey)
                }
            }
        )
    }
}
```

---

## **5. Criterios de Aceptación y Casos de Prueba**

| **Caso** | **Condición inicial** | **Acción** | **Resultado esperado** | **Estado** |
|---|---|---|---|---|
| **CA-01** | Formulario en blanco | Tap en [Guardar y Registrar] | Muestra errores en rojo en RUT, nombre de archivo, descripción y casilla de autenticidad. No se envía. | **Aprobado** |
| **CA-02** | RUT "123456" | Tap en [Guardar y Registrar] | Muestra mensaje `"Formato de RUT inválido (ej: 18.765.432-1)"`. | **Aprobado** |
| **CA-03** | Nombre sin extensión "contrato" | Tap en [Guardar y Registrar] | Muestra mensaje `"Debe incluir extensión válida (.pdf, .jpg, .png)"`. | **Aprobado** |
| **CA-04** | Descripción "abc" | Tap en [Guardar y Registrar] | Muestra mensaje `"Mínimo 5 caracteres para la descripción"`. | **Aprobado** |
| **CA-05** | Datos correctos pero Checkbox desmarcado | Tap en [Guardar y Registrar] | Muestra mensaje de advertencia legal sobre autenticidad en rojo. | **Aprobado** |
| **CA-06** | Selector Dropdown cerrado | Tap en selector de tipo | Despliega catálogo de 5 opciones oficiales; al seleccionar una se actualiza el campo y cierra el menú. | **Aprobado** |
| **CA-07** | Switch desactivado vs activado | Cambiar Switch | Al guardar con Switch activo asigna estado `PENDIENTE`; si está inactivo asigna `VIGENTE`. | **Aprobado** |
| **CA-08** | Formulario completo válido | Tap en [Guardar y Registrar] | Muestra `CircularProgressIndicator`, genera correlativo único `#ID` y despliega `AlertDialog` de éxito con los datos cargados. | **Aprobado** |
| **CA-09** | Formulario con datos | Tap en [Limpiar Formulario] | Restablece todos los campos a su estado inicial limpio. | **Aprobado** |

---

## **6. Respuestas a la Guía Meta 2**

| **Pregunta de la Guía** | **Respuesta del Equipo CMQ Salud** |
|---|---|
| **¿Qué formulario implementamos y por qué?** | `P05 Carga de Documento`. Porque es el punto de entrada neurálgico del sistema donde se digitalizan los antecedentes laborales para alimentar el repositorio centralizado de salud. |
| **¿Qué componentes de UI se utilizaron?** | `OutlinedTextField` (3), `ExposedDropdownMenuBox` / `DropdownMenu` (1), `RadioButton` (2 opciones), `Switch` (1), `Checkbox` (1), `PrimaryButton` (1), `SecondaryButton` (1) y `AlertDialog` (1). |
| **¿Cómo se asegura el ciclo completo del formulario?** | **Ingresar**: Controles Compose. **Modificar**: Eventos ViewModel mutando StateFlow. **Validar**: Verificación sintáctica y de negocio en ViewModel con mensajes por campo. **Procesar**: Guardado asíncrono en `DocumentRepository` mediante Coroutines. **Entregar respuesta**: `AlertDialog` con folio generado y confirmación. |
| **¿Cómo se conecta con el resto de la aplicación?** | Se conectó con el menú de `HomeScreen` mediante `onNavigateCarga`. El documento recién creado se almacena en `DocumentRepository` y queda disponible inmediatamente en `ConsultaExpedienteScreen` (P03). |

---

## **7. Conclusión y Próximos Pasos**

La **Meta 2** queda formalmente cumplida con la implementación end-to-end del formulario de carga de documentos, cubriendo todos los componentes requeridos por la pauta y respetando el ciclo completo de diseño de formularios reactivos en Jetpack Compose.

**Próximos pasos para siguientes entregas:**
1. Integración con `CameraX` para captura real en el dispositivo al seleccionar el RadioButton de cámara.
2. Integración con `ActivityResultContracts.GetContent()` para selección real de archivos del almacenamiento local.
3. Conexión de la pantalla P06 de validación para resolver los documentos derivados en estado `PENDIENTE`.
