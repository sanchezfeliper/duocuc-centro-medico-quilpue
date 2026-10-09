DSY1105 · Caso CMQ Area Salud · Mapa de navegacion y MVVM 

# **Mapa de Navegación y Esquema MVVM** 

_Aplicación móvil de gestión documental - CMQ Área Salud_ 

DSY1105 - Desarrollo de Aplicaciones Móviles  ·  Sede CITT Quilpue  ·  30-09-2026 

## **1.  Objetivo y Alcance** 

Este documento define el mapa de navegación entre las pantallas de la aplicación móvil y presenta un primer esquema de arquitectura MVVM que separa la interfaz (Jetpack Compose), el ViewModel y la capa de datos. Sirve como referencia técnica para el desarrollo del MVP y toma como referente funcional a dynafile.com, solución web de gestión documental orientada a repositorios centralizados, control de versiones, permisos por rol y captura móvil de documentos. La aplicación CMQ replicará conceptos clave de dynafile (repositorio único, trazabilidad, roles) adaptados al contexto municipal de salud y al stack Android. 

## **2.  Mapa de Navegación** 

### **2.1  Pantallas Identificadas** 

|**ID**|**Pantalla**|**Propósito**|**RF asociados**|
|---|---|---|---|
|P01|Login|Autenticación con RUT y<br>contraseña.|RF-01 a RF-04|
|P02|Home|Menú principal y accesos<br>rápidos por perfil.|RF-05 a RF-07|
|P03|Consulta de expediente|Búsqueda y listado de<br>documentos del<br>funcionario.|RF-12 a RF-14|
|P04|Detalle de documento|Visualización, metadatos y<br>descarga.|RF-14, RF-18|
|P05|Carga de documento|Captura con cámara y carga<br>de metadatos.|RF-08 a RF-11|
|P06|Validación|Revisión, aprobación,<br>rechazo o corrección.|RF-15 a RF-17|
|P07|Notificaciones|Bandeja de alertas y<br>vencimientos.|RF-21 a RF-23|
|P08|Bitácora|Consulta de trazabilidad de<br>auditoría.|RF-19, RF-20|



### **2.2  Recorrido Principal del Usuario** 

El recorrido principal corresponde al flujo que cubre la necesidad central del caso: cargar un documento, validarlo y notificar el resultado. Los recorridos secundarios cubren consulta y trazabilidad. 

[Login] ──ok──> [Home] ──> [Carga documento] ──> [Sincroniza SharePoint] ──> [Home] │ (notificación push)▼ 

Pagina 1 

DSY1105 · Caso CMQ Area Salud · Mapa de navegacion y MVVM 

── [Validación] > [Detalle de documento] │ ┌─────────────────────┼─────────────────────┐ ▼ ▼ ▼ [Aprobar]            [Rechazar]           [Corrección] │ │ │ └─────> notificación al funcionario <──────┘ │ ▼ ── [Notificaciones] > [Detalle de documento] 

### **2.3  Tabla de Transiciones** 

|**Origen**|**Destino**|**Acción / Evento**|**Condición**|
|---|---|---|---|
|P01 Login|P02 Home|Credenciales válidas.|RUT y contraseña correctos;<br>perfil activo.|
|P01 Login|P01 Login|Credenciales inválidas.|Bloqueo tras 5 intentos<br>fallidos (RF-04).|
|P02 Home|P03 Consulta|Tap en 'Consultar<br>expediente'.|Perfil con permiso de<br>consulta.|
|P02 Home|P05 Carga|Tap en 'Cargar documento'.|Perfil FUN, ADM o RRHH.|
|P02 Home|P06 Validación|Tap en 'Validaciones<br>pendientes'.|Perfil JEF o RRHH.|
|P02 Home|P08 Bitácora|Tap en 'Bitácora de<br>auditoría'.|Perfil SYS o TIC.|
|P02 Home|P07 Notificaciones|Tap en campana superior.|Sesión activa.|
|P03 Consulta|P04 Detalle|Tap en una tarjeta de<br>documento.|Documento accesible al<br>perfil.|
|P05 Carga|P02 Home|Carga exitosa sincronizada<br>con SharePoint.|Conexión a Internet<br>disponible.|
|P06 Validación|P04 Detalle|Decisión registrada<br>(aprobar/rechazar/corregir).|Comentario obligatorio en<br>rechazo.|
|P07 Notificaciones|P04 Detalle|Tap en una notificación.|Documento aún vigente.|
|Cualquiera|P01 Login|Cerrar sesión o timeout 5<br>min.|Inactividad o acción<br>explícita.|



### **2.4  Reglas de Navegación** 

- Toda pantalla (excepto Login) exige sesión activa; timeout de 5 minutos vuelve a Login (RF-03). 

- El acceso a P06 Validación, P08 Bitácora y la edición en P05 Carga depende del perfil (RF-07). 

Pagina 2 

DSY1105 · Caso CMQ Area Salud · Mapa de navegacion y MVVM 

- La sincronización con SharePoint es obligatoria antes de regresar a Home desde P05 (RF-24). 

- El botón 'Atrás' del sistema lleva a la pantalla anterior sin perder estado de sesión. 

## **3.  Esquema MVVM** 

### **3.1  Capas y Responsabilidades** 

|**Capa**|**Tecnología**|**Responsabilidad**|**No debe**|
|---|---|---|---|
|View (UI)|Jetpack Compose|Dibujar pantallas, capturar<br>eventos del usuario y<br>observar el estado del<br>ViewModel.|Acceder a la capa de datos<br>directamente.|
|ViewModel|AAC ViewModel + StateFlow|Mantener estado de UI,<br>aplicar reglas de<br>presentación y orquestar<br>llamadas al repositorio.|Referenciar objetos de<br>Android (Context, Activity)<br>ni conocer Compose.|
|Model / Data|Repository + Retrofit +<br>Room + DataStore|Proveer datos: SharePoint<br>API (remoto), caché local<br>(Room), sesión y<br>preferencias.|Tener lógica de<br>presentación ni conocer<br>Compose.|



### **3.2  Diagrama de Capas** 

- ┌─────────────────────────────────────────────────────────┐ │  VIEW  (Jetpack Compose)                                │ │   · @Composable LoginScreen(homeViewModel)              │ │   · Recoge eventos: onClick, onTextChange               │ │   · Observa: viewModel.uiState (StateFlow)              │ └──────────────────────────┬──────────────────────────────┘ collect / send event│ ┌──────────────────────────▼──────────────────────────────┐ │  VIEWMODEL  (AAC ViewModel + StateFlow)                 │ │   · LoginViewModel @HiltViewModel                       │ │   · expone: uiState: StateFlow<LoginUiState>            │ │   · fun login(rut, pass) { viewModelScope.launch { …} } │ └──────────────────────────┬──────────────────────────────┘ call│ ┌──────────────────────────▼──────────────────────────────┐ │  MODEL / DATA                                           │ │   · AuthRepository (interface)                          │ │ └─ SharePointAuthDataSource (Retrofit)            │ │ └─ SessionLocalDataSource (DataStore)             │ │   · DocumentRepository                                  │ │ └─ SharePointDocsDataSource + Room (cache)        │ └─────────────────────────────────────────────────────────┘ 

### **3.3  Ejemplo: Pantalla de Carga de Documento (P05)** 

El siguiente esquema muestra la separación de capas para una pantalla concreta. 

_[kotlin]_ 

Pagina 3 

DSY1105 · Caso CMQ Area Salud · Mapa de navegacion y MVVM 

|// ---------- VIEW ----------<br>@Composable|
|---|
|fun UploadDocumentScreen(<br>vm: UploadViewModel = hiltViewModel(),<br>onUploaded: () -> Unit|
|) {|
|val state by vm.uiState.collectAsState()      // observa estado|
|UploadDocumentView(|
|state = state,|
|onTypeChange = vm::onTypeChange,          // evento -> VM<br>onCapture = vm::onCaptureImage,<br>onSubmit = { vm.upload(); onUploaded() }<br>)|
|}<br>// ---------- VIEWMODEL ----------|
|@HiltViewModel<br>class UploadViewModel @Inject constructor(<br>private val repo: DocumentRepository|
|) : ViewModel() {|
|data class UploadUiState(<br>val rut: String = "",<br>val type: DocType = DocType.PERMISO,<br>val previewUri: String? = null,<br>val isUploading: Boolean = false,<br>val error: String? = null|
|)|
|<br>private val _uiState = MutableStateFlow(UploadUiState())<br>val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()<br>fun onTypeChange(t: DocType) { _uiState.update { it.copy(type = t) } }<br>fun onCaptureImage(uri: String) { _uiState.update { it.copy(previewUri = uri) } }<br>fun upload() = viewModelScope.launch {<br>_uiState.update { it.copy(isUploading = true, error = null) }<br>runCatching { repo.uploadDocument(_uiState.value.toCommand()) }<br>.onSuccess { _uiState.update { it.copy(isUploading = false) } }<br>.onFailure { e -> _uiState.update { it.copy(isUploading = false, error = e.message) } }<br>|
|}<br>|
|}<br>// ---------- MODEL / DATA ----------|
|interface DocumentRepository {<br>suspend fun uploadDocument(cmd: UploadCommand): Result<Unit><br>suspend fun listByRut(rut: String): List<Document><br>}|
|class DocumentRepositoryImpl @Inject constructor(<br>private val remote: SharePointDocsDataSource,   // Retrofit|
|i<br>private val cache: DocumentDao,                 // Room|
|<br>private val audit: AuditLogger                  // trazabilidad (RF-18)|
|<br>) : DocumentRepository {|
|override suspend fun uploadDocument(cmd: UploadCommand) = runCatching {<br>audit.log(action = "UPLOAD", rut = cmd.rut)         // antes de la llamada<br>remote.upload(cmd)                                   // SharePoint sin copias<br>cache.insert(cmd.toEntity())                         // caché local<br>}<br>}|



Pagina 4 

DSY1105 · Caso CMQ Area Salud · Mapa de navegacion y MVVM 

### **3.4  Convenciones del Stack** 

|**Concern**|**Decisión**|**Justificación**|
|---|---|---|
|Lenguaje|Kotlin 1.9+|Estandar Android; corrutinas y null<br>safety.|
|UI|Jetpack Compose + Material 3|Declarativo, menos código, fácil de<br>testear.|
|Estado|StateFlow + collectAsState|Estado reactivo, sobrevive a rotación.|
|DI|Hilt|Inyección estándar; reduce<br>boilerplate.|
|Async|Coroutines + Flow|Operaciones de red y Room no<br>bloqueantes.|
|Networking|Retrofit + OkHttp + TLS 1.2|Conexión segura a SharePoint<br>(RNF-02).|
|Persistencia|Room (caché) + DataStore (sesión)|Modo offline de consulta (RNF-05).|
|Trazabilidad|AuditLogger (interceptor)|Registro no modificable (RF-18,<br>RNF-03).|



## **5.  Cierre** 

El mapa de navegación define 8 pantallas y un recorrido principal centrado en la carga, validación y notificación de documentos. El esquema MVVM separa claramente Compose (View), ViewModel (estado y reglas de presentación) y Repository (datos SharePoint + Room). El referente dynafile.com valida conceptualmente la propuesta. Esta versión queda abierta a refinamiento al avanzar el diseño detallado y la implementación del MVP. 

Pagina 5 

