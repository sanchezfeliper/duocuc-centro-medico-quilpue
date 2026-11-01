// ui/screens/carga/CargaViewModel.kt
package cl.cmq.salud.ui.screens.carga

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.di.AppContainer
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.validation.ValidadorRut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class CargaViewModel(
    private val repo: DocumentRepository = AppContainer.documentRepository,
    // Semana 10: si viene un ID, el formulario opera en modo edición sobre ese
    // documento existente; si es null, crea uno nuevo (comportamiento original).
    private val idDocumentoEdicion: Int? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CargaUiState(idDocumentoEnEdicion = idDocumentoEdicion, isEditando = idDocumentoEdicion != null)
    )
    val uiState: StateFlow<CargaUiState> = _uiState.asStateFlow()

    init {
        // Precarga los datos del documento a editar en el formulario.
        idDocumentoEdicion?.let { id ->
            viewModelScope.launch {
                repo.buscarPorId(id)?.let { doc -> precargar(doc) }
            }
        }
    }

    companion object {
        // Extensiones aceptadas por RF-08
        private val EXTENSIONES_VALIDAS = listOf(".pdf", ".jpg", ".jpeg", ".png")

        // Caracteres no permitidos en nombres de archivo (SharePoint/Windows)
        private val CARACTERES_PROHIBIDOS = listOf('\\', '/', ':', '*', '?', '"', '<', '>', '|')

        // Límites coherentes con el modelo de datos E4 Documento
        private const val LARGO_MAX_NOMBRE_ARCHIVO = 150
        private const val LARGO_MIN_DESCRIPCION = 5
        private const val LARGO_MAX_DESCRIPCION = 200

        /**
         * Semana 10: factory para crear el ViewModel en modo edición con el ID
         * del documento como argumento (patrón de DetalleViewModel.factory).
         */
        fun factory(
            idDocumento: Int,
            repo: DocumentRepository = AppContainer.documentRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CargaViewModel(repo, idDocumento) as T
        }
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
        _uiState.update { state ->
            when (value) {
                // Al volver a archivo digital se descarta la captura y, si el
                // nombre provenia de la foto, se limpia para que el usuario lo escriba.
                OrigenDocumento.DIGITAL -> state.copy(
                    origen = value,
                    fotoRuta = null,
                    fotoHash = null,
                    fotoError = null,
                    nombreArchivo = if (state.fotoRuta != null &&
                        state.nombreArchivo == nombreDeRuta(state.fotoRuta)
                    ) "" else state.nombreArchivo
                )

                // Al cambiar a cámara se descarta la selección digital
                OrigenDocumento.CAMARA -> state.copy(
                    origen = value,
                    fotoError = null,
                    fileUri = null,
                    fileNombre = null,
                    fileMimeType = null,
                    digitalError = null,
                    nombreArchivo = if (state.fileUri != null &&
                        state.nombreArchivo == state.fileNombre
                    ) "" else state.nombreArchivo
                )
            }
        }
    }

    // ------------------------------------------------------------------
    // Semana 9.3: captura con la camara del dispositivo (RF-09).
    // El archivo y su huella los produce la capa de datos; el ViewModel solo
    // guarda los datos resultantes (ruta y hash), sin conocer el sistema de archivos.
    // ------------------------------------------------------------------

    fun abrirCamara() {
        _uiState.update { it.copy(isCamaraAbierta = true) }
    }

    fun cerrarCamara() {
        _uiState.update { it.copy(isCamaraAbierta = false) }
    }

    /** Recibe la fotografia ya guardada por la camara y la asocia al formulario. */
    fun onFotoCapturada(rutaRelativa: String, huellaSha256: String) {
        _uiState.update {
            it.copy(
                fotoRuta = rutaRelativa,
                fotoHash = huellaSha256.ifBlank { null },
                // El nombre del archivo pasa a ser el de la captura (RF-09)
                nombreArchivo = nombreDeRuta(rutaRelativa),
                nombreArchivoError = null,
                fotoError = null,
                isCamaraAbierta = false
            )
        }
    }

    /** Descarta la captura actual para volver a tomarla. */
    fun descartarFoto() {
        _uiState.update {
            it.copy(
                fotoRuta = null,
                fotoHash = null,
                nombreArchivo = if (it.nombreArchivo == nombreDeRuta(it.fotoRuta)) "" else it.nombreArchivo
            )
        }
    }

    // ------------------------------------------------------------------
    // RF-07: seleccion real de archivo digital (PDF / JPG / JPEG / PNG).
    // El Composable lanza el selector del sistema; el ViewModel valida
    // el tipo por extensión y almacena el URI con su nombre real.
    // ------------------------------------------------------------------

    /**
     * Procesa el archivo seleccionado por el usuario mediante el selector
     * del sistema. Valida la extensión y, si no es aceptable, deja un error
     * que el Composable comunica de inmediato.
     */
    fun onFileSelected(uriString: String, nombre: String, mimeType: String?) {
        val extension = nombre.substringAfterLast('.', "").lowercase()
        val esValido = extension in listOf("pdf", "jpg", "jpeg", "png")

        if (!esValido) {
            _uiState.update {
                it.copy(
                    digitalError = "Tipo de archivo no admitido: solo se aceptan .pdf, .jpg, .jpeg o .png.",
                    isFilePickerOpen = false
                )
            }
            return
        }
        _uiState.update {
            it.copy(
                fileUri = uriString,
                fileNombre = nombre,
                fileMimeType = mimeType,
                // El nombre del archivo pasa a ser el del archivo seleccionado
                nombreArchivo = nombre,
                nombreArchivoError = null,
                digitalError = null,
                isFilePickerOpen = false
            )
        }
    }

    /** Quita el archivo digital seleccionado para volver a elegir otro. */
    fun clearFileSelected() {
        _uiState.update { state ->
            val nombre = if (state.nombreArchivo == state.fileNombre) "" else state.nombreArchivo
            state.copy(
                fileUri = null,
                fileNombre = null,
                fileMimeType = null,
                digitalError = null,
                nombreArchivo = nombre
            )
        }
    }

    /** Alterna el estado visual del selector de archivos. */
    fun setFilePickerOpen(open: Boolean) {
        _uiState.update { it.copy(isFilePickerOpen = open) }
    }

    fun onFileSelectionFailed(message: String) {
        _uiState.update {
            it.copy(
                digitalError = message,
                isFilePickerOpen = false
            )
        }
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

    /**
     * Semana 10 (editar): vuelca el documento existente en los campos del
     * formulario. El origen se infiere de la ruta guardada: una ruta de cámara
     * (filesDir/fotos) mantiene el modo cámara; una URI content:// o ausencia de
     * adjunto se trata como archivo digital.
     */
    private fun precargar(doc: Documento) {
        val ruta = doc.rutaLocal
        val esCamara = ruta != null && !ruta.startsWith("content://")
        _uiState.update {
            it.copy(
                rut = doc.rutFuncionario,
                // El campo de la UI es la CATEGORÍA del documento (catálogo RN-08),
                // mientras que Documento.tipo guarda el FORMATO de archivo (PDF/JPG).
                // La categoría se recupera del nombre/descripción del registro.
                tipoSeleccionado = tipoCatalogoDesde(doc.nombreArchivo, doc.descripcion),
                nombreArchivo = doc.nombreArchivo,
                descripcion = doc.descripcion.orEmpty(),
                origen = if (esCamara) OrigenDocumento.CAMARA else OrigenDocumento.DIGITAL,
                requiereValidacionUrgente = doc.estado == EstadoDocumento.PENDIENTE,
                fotoRuta = if (esCamara) ruta else null,
                fotoHash = if (esCamara) doc.hashArchivo else null,
                fileUri = if (!esCamara) ruta else null,
                fileNombre = if (!esCamara) doc.nombreArchivo else null,
                // El formulario exige la declaración de autenticidad para guardar;
                // un registro ya existente la cumple por haber sido cargado antes (RN-08).
                declaraAutenticidad = true
            )
        }
    }

    /**
     * Semana 10: reconstruye la categoría del catálogo a partir del nombre de
     * archivo y la descripción del registro, ya que la base guarda el formato
     * (PDF/JPG) en `tipo` y no la categoría. Si nada coincide, deja el catálogo
     * en su valor por defecto para que el usuario lo confirme al guardar.
     */
    private fun tipoCatalogoDesde(nombre: String, descripcion: String?): String {
        val texto = "$nombre ${descripcion.orEmpty()}".lowercase()
        return when {
            "anexo" in texto -> "Anexo de contrato"
            "certificado" in texto || "capacitacion" in texto || "capacitación" in texto ->
                "Certificado de capacitación"
            "licencia" in texto || "medico" in texto || "médico" in texto -> "Licencia médica"
            "contrato" in texto -> "Contrato de trabajo"
            "permiso" in texto -> "Permiso administrativo"
            else -> _uiState.value.tiposDisponibles.first()
        }
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
        // Semana 9.3 (RF-09): si el origen es captura fisica, la foto es obligatoria
        val fotoErr = if (state.origen == OrigenDocumento.CAMARA && state.fotoRuta == null) {
            "Debe capturar la fotografía del documento físico con la cámara del dispositivo antes de registrar (RF-09)."
        } else {
            null
        }
        // RF-07: si el origen es archivo digital, el archivo seleccionado es obligatorio
        val digitalErr = if (state.origen == OrigenDocumento.DIGITAL && state.fileUri == null) {
            "Debe seleccionar un archivo digital (PDF, JPG o PNG) antes de registrar, usando el selector del sistema."
        } else {
            null
        }
        val autErr = if (!state.declaraAutenticidad) {
            "Debe marcar la declaración de autenticidad: confirme que el documento y sus metadatos son fidedignos (RN-08)."
        } else {
            null
        }

        val errores = listOfNotNull(rutErr, tipoErr, nombreErr, descErr, fotoErr, digitalErr, autErr)
        if (errores.isNotEmpty()) {
            // Meta 4: respuesta visible garantizada en tres niveles: Snackbar
            // (evento único), banner con el resumen y errores por campo.
            val campos = buildList {
                if (rutErr != null) add("RUT Funcionario")
                if (tipoErr != null) add("Tipo de documento")
                if (nombreErr != null) add("Nombre del archivo")
                if (descErr != null) add("Descripción / Observaciones")
                if (fotoErr != null) add("Fotografía del documento")
                if (digitalErr != null) add("Archivo digital")
                if (autErr != null) add("Declaración de autenticidad")
            }
            _uiState.update {
                it.copy(
                    rutError = rutErr,
                    tipoError = tipoErr,
                    nombreArchivoError = nombreErr,
                    descripcionError = descErr,
                    fotoError = fotoErr,
                    digitalError = digitalErr,
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
            val tipoFormato = when (state.nombreArchivo.substringAfterLast('.', "").lowercase()) {
                "pdf" -> "PDF"
                "png" -> "PNG"
                "jpg", "jpeg" -> "JPG"
                else -> "Archivo"
            }
            val estadoInicial = if (state.requiereValidacionUrgente) {
                EstadoDocumento.PENDIENTE
            } else {
                EstadoDocumento.VIGENTE
            }

            val nuevoDoc = Documento(
                idDocumento = 0, // El repositorio asignará el ID correlativo
                idFuncionario = 0, // El repositorio lo resuelve a partir del RUT ingresado
                rutFuncionario = state.rut.trim(), // Meta 6: el RUT del formulario asocia el documento
                tipo = tipoFormato,
                nombreArchivo = state.nombreArchivo.trim(),
                estado = estadoInicial,
                fechaCarga = LocalDate.now().toString(),
                usuarioCarga = "M. Gonzalez (Admin)",
                descripcion = state.descripcion.trim(),
                // Semana 9.3 (RF-09/RF-11): la captura viaja con el documento
                hashArchivo = state.fotoHash,
                // RF-07: el archivo digital seleccionado también viaja asociado (URI persistido)
                rutaLocal = state.fotoRuta ?: state.fileUri?.toString()
            )

            // Semana 10: en modo edición se conservan los datos de gestión del
            // registro original (id, folio, estado, fecha y responsable de carga,
            // comentario del revisor) y solo se actualizan los campos del formulario.
            val guardado: Documento? = if (state.idDocumentoEnEdicion != null) {
                val original = repo.buscarPorId(state.idDocumentoEnEdicion)
                if (original == null) {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            generalError = "No se encontró el documento #${state.idDocumentoEnEdicion}. " +
                                "Puede haber sido eliminado; no se aplicaron cambios."
                        )
                    }
                    return@launch
                }
                repo.actualizarDocumento(
                    original.copy(
                        tipo = tipoFormato,
                        nombreArchivo = state.nombreArchivo.trim(),
                        descripcion = state.descripcion.trim(),
                        // El RUT puede cambiar el funcionario asociado
                        rutFuncionario = state.rut.trim(),
                        hashArchivo = state.fotoHash ?: original.hashArchivo,
                        rutaLocal = state.fotoRuta ?: state.fileUri?.toString() ?: original.rutaLocal
                    )
                )
            } else {
                repo.guardarDocumento(nuevoDoc)
            }

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
                    fotoError = null,
                    digitalError = null,
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

    /** Nombre de archivo a partir de su ruta (ultimo segmento). */
    private fun nombreDeRuta(ruta: String?): String = ruta?.substringAfterLast('/') ?: ""

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
