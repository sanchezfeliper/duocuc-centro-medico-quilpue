// ui/screens/carga/CargaUiState.kt
package cl.cmq.salud.ui.screens.carga

import cl.cmq.salud.domain.model.Documento

enum class OrigenDocumento(val label: String) {
    DIGITAL("Archivo digital (PDF / JPG / PNG)"),
    CAMARA("Captura física con cámara")
}

data class CargaUiState(
    // Campos de entrada
    val rut: String = "",
    val tipoSeleccionado: String = "Contrato de trabajo",
    val nombreArchivo: String = "",
    val descripcion: String = "",
    val origen: OrigenDocumento = OrigenDocumento.DIGITAL,
    val requiereValidacionUrgente: Boolean = false,
    val declaraAutenticidad: Boolean = false,

    // Semana 9.3: captura con la camara del dispositivo (RF-09).
    // La foto se guarda como archivo; aqui solo viaja su ruta relativa y su huella.
    val fotoRuta: String? = null,
    val fotoHash: String? = null,
    val isCamaraAbierta: Boolean = false,

    // RF-07: seleccion real de archivo digital (PDF / JPG / JPEG / PNG).
    // Se persiste el acceso URI (como string, compatible con SQLite y tests JVM)
    // y se almacena el nombre real del archivo.
    val fileUri: String? = null,
    val fileNombre: String? = null,
    val fileMimeType: String? = null,
    val isFilePickerOpen: Boolean = false,

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
    val tipoError: String? = null,
    val nombreArchivoError: String? = null,
    val descripcionError: String? = null,
    val autenticidadError: String? = null,
    val fotoError: String? = null,
    val digitalError: String? = null,
    val generalError: String? = null,

    // Meta 4: resumen de campos a corregir (banner) + eventos únicos para Snackbar
    val camposConError: List<String> = emptyList(),
    val errorEventId: Long = 0,
    val successEventId: Long = 0,
    val ultimoFolioRegistrado: Int? = null,

    // Estado de envío / procesamiento
    val isSubmitting: Boolean = false,

    // Semana 10: modo edición. Si idDocumentoEnEdicion != null, el formulario
    // actualiza un registro existente en vez de crear uno nuevo.
    val idDocumentoEnEdicion: Int? = null,
    val isEditando: Boolean = false,

    // Retroalimentación / Respuesta
    val documentoCreado: Documento? = null,
    val showSuccessDialog: Boolean = false
)
