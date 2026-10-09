// ui/screens/carga/CargaViewModel.kt
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

    // Manejo de eventos de entrada y modificación (Ciclo: Ingresar -> Modificar)
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

    /**
     * Valida y procesa el formulario (Ciclo: Validar -> Procesar -> Entregar respuesta).
     */
    fun guardarDocumento() {
        val state = _uiState.value

        var hayError = false
        var rutErr: String? = null
        var nombreErr: String? = null
        var descErr: String? = null
        var autErr: String? = null

        // 1. Validar RUT (formato chileno con dígito verificador sintáctico)
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

        // 3. Validar Descripción / Observaciones
        if (state.descripcion.isBlank()) {
            descErr = "La descripción es obligatoria"
            hayError = true
        } else if (state.descripcion.trim().length < 5) {
            descErr = "Mínimo 5 caracteres para la descripción"
            hayError = true
        }

        // 4. Validar Declaración de autenticidad
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

        // 5. Procesamiento asíncrono
        _uiState.update { it.copy(isSubmitting = true, generalError = null) }

        viewModelScope.launch {
            val tipoFormato = if (state.nombreArchivo.lowercase().endsWith(".pdf")) "PDF" else "JPG"
            val estadoInicial = if (state.requiereValidacionUrgente) {
                EstadoDocumento.PENDIENTE
            } else {
                EstadoDocumento.VIGENTE
            }

            val nuevoDoc = Documento(
                idDocumento = 0, // El repositorio asignará el ID correlativo
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
