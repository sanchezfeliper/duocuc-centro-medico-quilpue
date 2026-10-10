// ui/screens/carga/CargaViewModel.kt
package cl.cmq.salud.ui.screens.carga

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.validation.ValidadorRut
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

    companion object {
        // Extensiones aceptadas por RF-08
        private val EXTENSIONES_VALIDAS = listOf(".pdf", ".jpg", ".jpeg", ".png")

        // Caracteres no permitidos en nombres de archivo (SharePoint/Windows)
        private val CARACTERES_PROHIBIDOS = listOf('\\', '/', ':', '*', '?', '"', '<', '>', '|')

        // Límites coherentes con el modelo de datos E4 Documento
        private const val LARGO_MAX_NOMBRE_ARCHIVO = 150
        private const val LARGO_MIN_DESCRIPCION = 5
        private const val LARGO_MAX_DESCRIPCION = 200
    }

    // ------------------------------------------------------------------
    // Eventos de entrada (Ciclo: Ingresar -> Modificar)
    // Cada cambio re-valida el campo en vivo: el usuario ve el error
    // mientras escribe y el mensaje le indica cómo corregirlo.
    // ------------------------------------------------------------------

    fun onRutChange(value: String) {
        val error = if (value.isBlank()) null else validarRut(value)
        _uiState.update { it.copy(rut = value, rutError = error, generalError = null) }
    }

    fun onTipoChange(value: String) {
        _uiState.update { it.copy(tipoSeleccionado = value, isDropdownExpanded = false, tipoError = null) }
    }

    fun onDropdownExpandChange(expanded: Boolean) {
        _uiState.update { it.copy(isDropdownExpanded = expanded) }
    }

    fun onNombreArchivoChange(value: String) {
        val error = if (value.isBlank()) null else validarNombreArchivo(value)
        _uiState.update { it.copy(nombreArchivo = value, nombreArchivoError = error, generalError = null) }
    }

    fun onDescripcionChange(value: String) {
        val error = if (value.isBlank()) null else validarDescripcion(value)
        _uiState.update { it.copy(descripcion = value, descripcionError = error, generalError = null) }
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
     * Meta 4: cierre del ciclo al elegir "Cargar otro". Reinicia el formulario
     * y confirma el registro con un evento de éxito (Snackbar), de modo que el
     * usuario siempre recibe respuesta visible tras procesar.
     */
    fun cargarOtro() {
        val folio = _uiState.value.documentoCreado?.idDocumento
        _uiState.value = CargaUiState()
        _uiState.update {
            it.copy(successEventId = it.successEventId + 1, ultimoFolioRegistrado = folio)
        }
    }

    // ------------------------------------------------------------------
    // Validación y procesamiento (Ciclo: Validar -> Procesar -> Entregar respuesta)
    // ------------------------------------------------------------------

    /**
     * Valida todos los campos del formulario. Si algún campo falla, marca
     * los errores por campo y un mensaje general con la cantidad de campos
     * a corregir; no se envía nada al repositorio.
     */
    fun guardarDocumento() {
        val state = _uiState.value

        val rutErr = validarRut(state.rut)
        val tipoErr = validarTipo(state.tipoSeleccionado)
        val nombreErr = validarNombreArchivo(state.nombreArchivo)
        val descErr = validarDescripcion(state.descripcion)
        val autErr = if (!state.declaraAutenticidad) {
            "Debe marcar la declaración de autenticidad: confirme que el documento y sus metadatos son fidedignos (RN-08)."
        } else {
            null
        }

        val errores = listOfNotNull(rutErr, tipoErr, nombreErr, descErr, autErr)
        if (errores.isNotEmpty()) {
            // Meta 4: respuesta visible garantizada en tres niveles: Snackbar
            // (evento único), banner con el resumen y errores por campo.
            val campos = buildList {
                if (rutErr != null) add("RUT Funcionario")
                if (tipoErr != null) add("Tipo de documento")
                if (nombreErr != null) add("Nombre del archivo")
                if (descErr != null) add("Descripción / Observaciones")
                if (autErr != null) add("Declaración de autenticidad")
            }
            _uiState.update {
                it.copy(
                    rutError = rutErr,
                    tipoError = tipoErr,
                    nombreArchivoError = nombreErr,
                    descripcionError = descErr,
                    autenticidadError = autErr,
                    camposConError = campos,
                    generalError = "No se pudo registrar el documento: ${errores.size} campo(s) requieren corrección. " +
                        "Cada mensaje en rojo indica qué está mal y cómo corregirlo.",
                    errorEventId = it.errorEventId + 1
                )
            }
            return
        }

        // Procesamiento asíncrono
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
                    showSuccessDialog = true,
                    // Meta 4: al registrar, se limpian los errores previos
                    rutError = null,
                    tipoError = null,
                    nombreArchivoError = null,
                    descripcionError = null,
                    autenticidadError = null,
                    generalError = null,
                    camposConError = emptyList()
                )
            }
        }
    }

    // ------------------------------------------------------------------
    // Reglas de validación (Meta 3)
    // Cada función devuelve null si el valor es válido, o un mensaje que
    // explica QUÉ está mal y CÓMO corregirlo.
    // ------------------------------------------------------------------

    /** Campo obligatorio + formato + regla propia: dígito verificador (Módulo 11). */
    private fun validarRut(valor: String): String? {
        if (valor.trim().isEmpty()) {
            return "El RUT es obligatorio: ingrese el identificador del funcionario con puntos y guión, ej: 18.765.432-7."
        }
        // Formato y dígito verificador viven en la capa de dominio (Meta 5)
        return ValidadorRut.validar(valor)
    }

    /** Validación de selección: el tipo debe pertenecer al catálogo institucional (RN-08). */
    private fun validarTipo(valor: String): String? {
        if (valor !in _uiState.value.tiposDisponibles) {
            return "Seleccione un tipo de documento del catálogo institucional: Contrato, Certificado de capacitación, Licencia médica, Permiso administrativo o Anexo."
        }
        return null
    }

    /** Campo obligatorio + formato (extensión y caracteres) + longitud máxima E4. */
    private fun validarNombreArchivo(valor: String): String? {
        val nombre = valor.trim()
        if (nombre.isEmpty()) {
            return "El nombre del archivo es obligatorio: escriba un nombre descriptivo con su extensión, ej: contrato_juan_perez.pdf."
        }
        val prohibido = nombre.firstOrNull { it in CARACTERES_PROHIBIDOS }
        if (prohibido != null) {
            return "El nombre contiene el carácter '$prohibido', no permitido en nombres de archivo. Reemplácelo por guion bajo (_) o guion medio (-)."
        }
        if (nombre.length > LARGO_MAX_NOMBRE_ARCHIVO) {
            return "El nombre supera el máximo de $LARGO_MAX_NOMBRE_ARCHIVO caracteres (lleva ${nombre.length}). Acórtelo manteniendo la extensión (.pdf, .jpg o .png)."
        }
        val extensionValida = EXTENSIONES_VALIDAS.any { nombre.lowercase().endsWith(it) }
        if (!extensionValida) {
            return "Extensión no permitida: solo se aceptan archivos .pdf, .jpg, .jpeg o .png. Renombre el archivo con una de esas extensiones antes de subirlo."
        }
        return null
    }

    /** Campo obligatorio + longitud mínima y máxima (E4 Documento.descripcion). */
    private fun validarDescripcion(valor: String): String? {
        val descripcion = valor.trim()
        if (descripcion.isEmpty()) {
            return "La descripción es obligatoria: indique el motivo o contexto del documento, ej: Permiso médico del 28-09-2026."
        }
        if (descripcion.length < LARGO_MIN_DESCRIPCION) {
            return "La descripción es demasiado corta: escriba al menos $LARGO_MIN_DESCRIPCION caracteres (lleva ${descripcion.length}). Agregue el contexto del documento."
        }
        if (descripcion.length > LARGO_MAX_DESCRIPCION) {
            return "La descripción supera el máximo de $LARGO_MAX_DESCRIPCION caracteres (lleva ${descripcion.length}). Resuma la información esencial."
        }
        return null
    }
}
