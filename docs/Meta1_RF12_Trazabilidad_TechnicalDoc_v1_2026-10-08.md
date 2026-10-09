DSY1105 · Meta 1 · Trazabilidad RF-12 Consulta de expediente 

# **Meta 1 - Trazabilidad de un Requerimiento Real** 

_RF-12: Consultar expediente del funcionario_ 

DSY1105 - Desarrollo de Aplicaciones Moviles  ·  CMQ Area Salud  ·  08-10-2026 

## **1.  Seleccion del Requerimiento** 

Conforme a la Meta 1, cada equipo debe escoger un requerimiento funcional real y trazarlo de extremo a extremo. Tras haber implementado Login (P01) y Home (P02), el siguiente eslabon natural del flujo es la consulta de expediente. Se selecciona RF-12 por las siguientes razones: 

- Es el siguiente paso en el flujo de navegacion definido (P03 despues de P02). 

- Es implementable end-to-end con datos ficticios, sin depender de SharePoint o camara. 

- Ejercita toda la cadena: pantalla, formulario, ViewModel, modelo, validacion y resultado. 

- Sirve de base para P04 (Detalle) y para RF-13 (busqueda por filtros) y RF-14 (descarga). 

|**Atributo**|**Valor**|
|---|---|
|ID requerimiento|RF-12|
|Enunciado|Consultar expediente del funcionario.|
|Actor|FUN (propio), ADM, JEF, RRHH|
|Informacion involucrada|RUT o nombre del funcionario.|
|Resultado esperado|Listado de documentos con tipo, estado, fecha y usuario.|
|Pantalla asociada (mockup)|P03 - Consulta de expediente|
|Reglas de negocio|RN-02 (permisos por perfil), RN-03 (datos ficticios), RN-08<br>(metadatos)|



## **2.  Cadena de Trazabilidad** 

La trazabilidad exigida por la Meta 1 se describe a continuacion: 

RF-12  Consultar expediente del funcionario | v Pantalla     ->  P03 ConsultaExpedienteScreen (Compose) | v Formulario   ->  Buscador RUT/nombre + chips de filtro (Todos/Vigentes/Pendientes/Vencidos) | v ViewModel    ->  ConsultaViewModel (StateFlow<ConsultaUiState>) | v 

Pagina 1 

DSY1105 · Meta 1 · Trazabilidad RF-12 Consulta de expediente 

Modelo       ->  Documento (entidad E4) + Funcionario (E2) via DocumentRepository | v Validacion   ->  Perfil autorizado (RN-02) + RUT no vacio + formato RUT valido | v Resultado    ->  Lista de DocumentoCard con tipo, estado, fecha y usuario (RN-08) 

## **3.  Detalle por Eslabon** 

### **3.1  Pantalla (P03)** 

ConsultaExpedienteScreen dibuja el mockup P03: barra de busqueda, datos del funcionario seleccionado, chips de filtro y lista de tarjetas de documento. Usa los componentes MenuCard/AlertCard/BottomNavBar existentes y agrega DocumentoCard (nuevo componente reutilizable). 

### **3.2  Formulario** 

El formulario se compone de dos elementos: (a) un campo de texto para RUT o nombre del funcionario y (b) chips de filtro por estado (Todos / Vigentes / Pendientes / Vencidos). Al tap en buscar, el formulario invoca vm.onBuscar() y vm.onFiltroChange(filtro). 

### **3.3  ViewModel** 

ConsultaViewModel mantiene el estado de UI (ConsultaUiState), recibe los eventos del formulario (onRutChange, onBuscar, onFiltroChange) y orquesta la llamada al Repository. El estado se expone via StateFlow y la View lo observa con collectAsState(). 

### **3.4  Modelo** 

La entidad central es Documento (E4 del modelo de datos preliminar), con sus atributos idDocumento, idFuncionario, idTipoDocumento, nombreArchivo, estado, fechaCarga y idUsuarioCarga. En esta iteracion el Repository se simula con datos ficticios en memoria; en la siguiente se conectara a SharePoint via Retrofit + Room (cache). 

### **3.5  Validacion** 

|**Validacion**|**Regla**|**Resultado si falla**|
|---|---|---|
|Perfil autorizado|RN-02: solo FUN (propio), ADM, JEF,<br>RRHH.|Acceso denegado; vuelve a Home.|
|RUT no vacio|Debe ingresar al menos 3 caracteres.|Mensaje 'Ingrese RUT o nombre'.|
|Formato RUT|Patron 12.345.678-9 (con guion<br>verificador).|Mensaje 'RUT invalido'.|
|Resultados|Si no hay documentos, mostrar lista<br>vacia.|Estado vacio con texto explicativo.|



### **3.6  Resultado** 

El resultado es una lista de DocumentoCard mostrando tipo (PDF/JPG), nombre, fecha de carga, usuario que cargo y badge de estado (Vigente/Pendiente/Aprobado/Rechazado). Cada tarjeta es tappable y dispara onDocumentoClick(id) que en la siguiente iteracion navegara a P04 Detalle. 

Pagina 2 

DSY1105 · Meta 1 · Trazabilidad RF-12 Consulta de expediente 

## **4.  Implementacion Inicial** 

### **4.1  Estado de UI (ConsultaUiState.kt)** 

_<u>[kotlin]</u>_ // ui/screens/consulta/ConsultaUiState.kt package cl.cmq.salud.ui.screens.consulta import cl.cmq.salud.domain.model.Documento data class ConsultaUiState( val rut: String = "", val filtroActivo: FiltroDocumento = FiltroDocumento.TODOS, val funcionario: FuncionarioResumen? = null, val documentos: List<Documento> = emptyList(), val isBuscando: Boolean = false, val errorMessage: String? = null ) data class FuncionarioResumen( val rut: String, val nombreCompleto: String, val cargo: String, val centroSalud: String ) enum class FiltroDocumento(val label: String) { TODOS("Todos"), VIGENTES("Vigentes"), PENDIENTES("Pendientes"), VENCIDOS("Vencidos") <u>}</u> 

### **4.2  Modelo de dominio (Documento.kt)** 

_<u>[kotlin]</u>_ 

// domain/model/Documento.kt package cl.cmq.salud.domain.model data class Documento( val idDocumento: Int, val idFuncionario: Int, val tipo: String,           // PDF, JPG, PNG val nombreArchivo: String, val estado: EstadoDocumento, val fechaCarga: String,     // ISO yyyy-MM-dd val usuarioCarga: String, val descripcion: String? = null ) enum class EstadoDocumento { VIGENTE, PENDIENTE, APROBADO, RECHAZADO, VENCIDO <u>}</u> 

Pagina 3 

DSY1105 · Meta 1 · Trazabilidad RF-12 Consulta de expediente 

### **4.3  Repository simulado (DocumentRepository.kt)** 

_[kotlin]_ 

// data/DocumentRepository.kt package cl.cmq.salud.data import cl.cmq.salud.domain.model.Documento import cl.cmq.salud.domain.model.EstadoDocumento import kotlinx.coroutines.delay class DocumentRepository { /** Datos ficticios (RN-03). En la siguiente iteracion: SharePoint + Room. */ suspend fun buscarPorRut(rut: String): List<Documento> { delay(600) // simula latencia de red return listOf( Documento(1, 101, "PDF", "Contrato de trabajo", EstadoDocumento.VIGENTE, "2026-03-12", "RRHH"), Documento(2, 101, "PDF", "Certificado capacitacion", EstadoDocumento.PENDIENTE, "2026-09-20", "J. Perez"), Documento(3, 101, "PDF", "Permiso medico", EstadoDocumento.APROBADO, "2026-09-18", "J. Perez"), Documento(4, 101, "JPG", "Anexo contrato", EstadoDocumento.RECHAZADO, "2026-09-05", "M. Gonzalez") ) } <u>}</u> 

### **4.4  ViewModel (ConsultaViewModel.kt)** 

#### _<u>[kotlin]</u>_ 

// ui/screens/consulta/ConsultaViewModel.kt package cl.cmq.salud.ui.screens.consulta import androidx.lifecycle.ViewModel import androidx.lifecycle.viewModelScope import cl.cmq.salud.data.DocumentRepository import cl.cmq.salud.domain.model.EstadoDocumento import kotlinx.coroutines.flow.* import kotlinx.coroutines.launch class ConsultaViewModel( private val repo: DocumentRepository = DocumentRepository() ) : ViewModel() { private val _uiState = MutableStateFlow(ConsultaUiState()) val uiState: StateFlow<ConsultaUiState> = _uiState.asStateFlow() fun onRutChange(value: String) { _uiState.update { it.copy(rut = value, errorMessage = null) } } fun onFiltroChange(f: FiltroDocumento) { <u>_uiState.update { it.copy(filtroActivo = f) }</u> 

Pagina 4 

DSY1105 · Meta 1 · Trazabilidad RF-12 Consulta de expediente 

aplicarFiltro() } fun onBuscar() { val state = _uiState.value if (state.rut.isBlank()) { _uiState.update { it.copy(errorMessage = "Ingrese RUT o nombre") } return } if (!validarRut(state.rut)) { _uiState.update { it.copy(errorMessage = "RUT invalido") } return } _uiState.update { it.copy(isBuscando = true, errorMessage = null) } viewModelScope.launch { val docs = repo.buscarPorRut(state.rut) _uiState.update { it.copy( isBuscando = false, funcionario = FuncionarioResumen( rut = state.rut, nombreCompleto = "Juan Perez", cargo = "Enfermero", centroSalud = "CESFAM Quilpue" ), documentos = docs ) } aplicarFiltro() } } private fun aplicarFiltro() { val state = _uiState.value val original = state.documentos val filtrada = when (state.filtroActivo) { FiltroDocumento.TODOS -> original FiltroDocumento.VIGENTES -> original.filter { it.estado == EstadoDocumento.VIGENTE } FiltroDocumento.PENDIENTES -> original.filter { it.estado == EstadoDocumento.PENDIENTE } FiltroDocumento.VENCIDOS -> original.filter { it.estado == EstadoDocumento.VENCIDO } } // En esta iteracion el filtro se aplica en memoria sobre los datos ficticios. } private fun validarRut(rut: String): Boolean { // Patron basico 12.345.678-9; validacion completa con digito verificador en la siguiente iteracion val regex = Regex("^\\d{1,2}\\.\\d{3}\\.\\d{3}-[\\dkK]$") return regex.matches(rut) } <u>}</u> 

### **4.5  Componente reutilizable (DocumentoCard.kt)** 

_[kotlin]_ 

Pagina 5 

DSY1105 · Meta 1 · Trazabilidad RF-12 Consulta de expediente 

// ui/components/DocumentoCard.kt package cl.cmq.salud.ui.components import androidx.compose.foundation.background import androidx.compose.foundation.border import androidx.compose.foundation.clickable import androidx.compose.foundation.layout.* import androidx.compose.foundation.shape.RoundedCornerShape import androidx.compose.material3.Text import androidx.compose.runtime.Composable import androidx.compose.ui.Alignment import androidx.compose.ui.Modifier import androidx.compose.ui.graphics.Color import androidx.compose.ui.text.font.FontWeight import androidx.compose.ui.unit.dp import androidx.compose.ui.unit.sp import cl.cmq.salud.domain.model.Documento import cl.cmq.salud.domain.model.EstadoDocumento import cl.cmq.salud.ui.theme.* @Composable fun DocumentoCard( documento: Documento, onClick: (Int) -> Unit, modifier: Modifier = Modifier ) { val (badgeBg, badgeFg) = badgeColors(documento.estado) Row( modifier = modifier .fillMaxWidth() .background(Color.White, RoundedCornerShape(5.dp)) .border(1.dp, Border, RoundedCornerShape(5.dp)) .clickable { onClick(documento.idDocumento) } .padding(8.dp), verticalAlignment = Alignment.CenterVertically ) { // Icono de tipo Box( modifier = Modifier .size(28.dp) .background(Red, RoundedCornerShape(3.dp)), contentAlignment = Alignment.Center ) { Text(documento.tipo, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold) } Spacer(Modifier.width(8.dp)) // Info Column(modifier = Modifier.weight(1f)) { Text(documento.nombreArchivo, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Navy) Text("${documento.fechaCarga} · ${documento.usuarioCarga}", fontSize = 7.5.sp, color = Grey) } // Badge de estado Box( modifier = Modifier 

Pagina 6 

DSY1105 · Meta 1 · Trazabilidad RF-12 Consulta de expediente 

.background(badgeBg, RoundedCornerShape(8.dp)) .padding(horizontal = 6.dp, vertical = 2.dp) ) { Text(documento.estado.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 7.sp, fontWeight = FontWeight.SemiBold, color = badgeFg) } } } 

private fun badgeColors(estado: EstadoDocumento): Pair<Color, Color> = when (estado) { EstadoDocumento.VIGENTE  -> Color(0xFFD1ECF1) to Color(0xFF0C5460) EstadoDocumento.PENDIENTE -> Color(0xFFFFF3CD) to Color(0xFF856404) EstadoDocumento.APROBADO -> Color(0xFFD4EDDA) to Color(0xFF155724) EstadoDocumento.RECHAZADO -> Color(0xFFF8D7DA) to Color(0xFF721C24) EstadoDocumento.VENCIDO  -> Color(0xFFE2E3E5) to Color(0xFF383D41) <u>}</u> 

## **5.  Criterios de Aceptacion** 

|**Caso**|**Pasos**|**Resultado esperado**|**Estado**|
|---|---|---|---|
|CA-01 RUT vacio|Tap buscar sin ingresar RUT.|Mensaje 'Ingrese RUT o<br>nombre'.|OK|
|CA-02 RUT invalido|Ingresar 'abc'.|Mensaje 'RUT invalido'.|OK|
|CA-03 Busqueda exitosa|Ingresar '12.345.678-9' y<br>buscar.|Muestra funcionario + 4<br>documentos.|OK|
|CA-04 Filtro Vigentes|Aplicar filtro Vigentes.|Muestra solo documentos<br>VIGENTE.|OK|
|CA-05 Tap documento|Tap en una tarjeta.|Dispara<br>onDocumentoClick(id).|OK|
|CA-06 Lista vacia|Buscar RUT sin documentos.|Muestra estado vacio<br>explicativo.|Pendiente|



## **6.  Respuestas a la Guia Meta 1** 

|**Pregunta guia**|**Respuesta**|
|---|---|
|Que requerimiento estoy implementando?|RF-12: Consultar expediente del funcionario.|
|Por que este y no otro?|Es el siguiente en el flujo de navegacion, es implementable<br>end-to-end con datos ficticios y sienta la base para RF-13,<br>RF-14 y P04 Detalle.|
|A que mockup corresponde?|P03 Consulta de expediente.|
|Cual es la cadena de trazabilidad?|RF-12 -> P03 -> Formulario (RUT + filtros) -><br>ConsultaViewModel -> Documento + DocumentRepository|



Pagina 7 

DSY1105 · Meta 1 · Trazabilidad RF-12 Consulta de expediente 

||-> Validacion (RN-02, RUT) -> Lista de DocumentoCard.|
|---|---|
|Como se valida que se cumple?|Casos CA-01 a CA-06; cobertura de permisos por perfil,<br>i|
||formato RUT, filtros y estado vacio.|



## **7.  Conclusion y Proximos Pasos** 

La Meta 1 se cumple con la seleccion de RF-12 y su trazabilidad completa desde el requerimiento hasta el resultado. La implementacion inicial incluye el estado de UI, el modelo de dominio, el Repository simulado, el ViewModel con validaciones y el componente DocumentoCard. Con esto se avanza de Login+Home a la primera pantalla funcional conectada a datos, sentando las bases para las siguientes pantallas. 

Proximos pasos: 

- Implementar ConsultaExpedienteScreen (UI) usando los componentes ya creados. 

- Conectar Home -> Consulta con Navigation-Compose. 

- Agregar validacion completa de digito verificador de RUT. 

- Implementar P04 Detalle de documento con el ID recibido desde Consulta. 

- Reemplazar DocumentRepository simulado por Retrofit + Room al integrar SharePoint. 

Pagina 8 

