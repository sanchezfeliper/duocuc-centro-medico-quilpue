// ui/screens/carga/CargaUiState.kt
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
