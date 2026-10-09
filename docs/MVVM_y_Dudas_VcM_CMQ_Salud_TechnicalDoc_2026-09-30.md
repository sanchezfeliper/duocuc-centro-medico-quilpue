DSY1105 · Caso CMQ Area Salud · MVVM y Dudas VcM 

# **MVVM y Registro de Dudas** 

_Aplicacion movil de gestion documental - CMQ Area Salud_ 

DSY1105 - Desarrollo de Aplicaciones Moviles  ·  Puntos 8 y 9  ·  30-09-2026 

## **1.  Punto 8 - Esquema MVVM** 

Este apartado presenta un primer esquema simple que identifica la separacion entre la Interfaz (Jetpack Compose), el ViewModel y la capa de Datos para la aplicacion movil de la CMQ - Area Salud. No se busca una implementacion completa, sino delimitar responsabilidades de cada capa para guiar el desarrollo posterior. 

### **1.1  Capas y Responsabilidades** 

|**Capa**|**Tecnologia**|**Responsabilidades**|**No debe hacer**|
|---|---|---|---|
|**Interfaz (View)**|Jetpack Compose + Material<br>3|Dibujar pantallas<br>(@Composable); capturar<br>eventos del usuario<br>(onClick, onTextChange);<br>observar el estado expuesto<br>por el ViewModel mediante<br>collectAsState(); invocar<br>funciones del ViewModel<br>ante interacciones.|Acceder directamente a la<br>capa de datos; contener<br>reglas de negocio;<br>referenciar objetos de base<br>de datos o red.|
|**ViewModel**|AAC ViewModel + StateFlow<br>i|Mantener y exponer el<br>estado de UI (StateFlow);<br>aplicar reglas de<br>presentacion (validaciones<br>simples, formateo);<br>orquestar llamadas al<br>Repository; sobrevivir a<br>cambios de configuracion.|Referenciar objetos de<br>Android (Context, Activity);<br>conocer Compose; realizar<br>llamadas de red directas;<br>contener lógica de<br>persistencia.|
|**Datos (Model)**|Repository + Retrofit +<br>Room + DataStore|Proveer datos al<br>ViewModel; abstraer la<br>fuente (SharePoint remoto<br>via Retrofit, cache local via<br>Room, sesion via<br>DataStore); aplicar<br>trazabilidad (AuditLogger);<br>exponer interfaces.|Conocer Compose ni el<br>ViewModel; contener reglas<br>de presentacion; manipular<br>el estado de UI.|



### **1.2  Esquema Visual de la Separacion** 

+------------------------------------------------------------+ 

|  VIEW  (Jetpack Compose)                                   | 

|    @Composable LoginScreen(vm: LoginViewModel)             | 

|    - Recoge eventos del usuario                            | 

|    - Observa vm.uiState con collectAsState()               | 

+-----------------------------+------------------------------+ 

|  eventos hacia abajo 

|  estado hacia arriba +-----------------------------v------------------------------+ 

|  VIEWMODEL  (AAC ViewModel + StateFlow)                    | 

- |    class LoginViewModel @Inject constructor(repo: AuthRepo) | 

- |    - val uiState: StateFlow<LoginUiState>                  | 

Pagina 1 

DSY1105 · Caso CMQ Area Salud · MVVM y Dudas VcM 

|    - fun onRutChange(s: String)                            | 

|    - fun login()  ->  viewModelScope.launch { repo.login() }| 

+-----------------------------+------------------------------+ |  invocacion +-----------------------------v------------------------------+ 

|  DATOS  (Repository pattern)                               | |    interface AuthRepository                                | |      suspend fun login(rut, pass): Result<Session>         | |    impl: AuthRepositoryImpl                                | |      - SharePointAuthDataSource (Retrofit, TLS 1.2)        | |      - SessionLocalDataSource (DataStore)                  | |      - AuditLogger (trazabilidad RF-18)                    | 

+------------------------------------------------------------+ 

### **1.3  Reglas de Separacion (Reglas de Oro)** 

- La View no conoce la existencia del Repository ni de SharePoint. 

- El ViewModel no conoce la existencia de Compose ni de la Activity. 

- El Repository no conoce la existencia del ViewModel ni de la pantalla. 

- El flujo de datos es unidireccional (UDF): evento -> ViewModel -> Repository -> estado -> View. 

- Las dependencias entre capas se inyectan via Hilt (constructor injection). 

### **1.4  Esquema de Estado y Eventos (Pantalla Login)** 

El siguiente esquema muestra el estado expuesto por el ViewModel y los eventos que la View puede invocar. La implementacion completa se incorporara en iteraciones posteriores. 

_[kotlin]_ 

// ---------- ESTADO (expuesto al ViewModel) ---------data class LoginUiState( val rut: String = "", val password: String = "", val isAuthenticating: Boolean = false, val errorMessage: String? = null, val isAuthenticated: Boolean = false ) // ---------- VIEWMODEL ---------class LoginViewModel @Inject constructor( private val repo: AuthRepository   // inyectado por Hilt ) : ViewModel() { private val _uiState = MutableStateFlow(LoginUiState()) val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow() // EVENTOS que la View puede invocar fun onRutChange(value: String) { /* actualiza estado */ } fun onPasswordChange(value: String) { /* actualiza estado */ } fun login() { /* valida, llama a repo.login(), actualiza estado */ } } // ---------- DATOS (contrato) ---------interface AuthRepository { suspend fun login(rut: String, password: String): Result<Session> <u>}</u> 

Pagina 2 

DSY1105 · Caso CMQ Area Salud · MVVM y Dudas VcM 

### **1.5  Checklist de Cumplimiento MVVM** 

|**Aspecto**|**Cumple**|**Observacion**|
|---|---|---|
|**Separacion View / ViewModel / Datos**|Si|Tres capas diferenciadas con<br>responsabilidades delimitadas.|
|**Estado reactivo (StateFlow)**|Si|El ViewModel expone StateFlow; la<br>View observa con collectAsState.|
|**Flujo unidireccional (UDF)**|Si|Eventos hacia abajo, estado hacia<br>arriba.|
|**View sin acceso a datos**|Si|La View solo invoca funciones del<br>ViewModel.|
|**ViewModel sin referencias a Android**|Si|No usa Context ni Activity.|
|**Repository abstrae fuentes**|Parcial|Definida la interfaz; implementacion<br>con Retrofit/Room en proxima<br>iteracion.|
|**Inyeccion de dependencias(Hilt)**|Pendiente|Se incorporara al integrar Repository.|



## **2.  Punto 9 - Registro de Dudas (VcM)** 

El siguiente registro documenta las preguntas que requieren validacion externa. Conforme a las reglas del caso, no se deben inventar reglas de negocio que no esten definidas en el contexto. Las dudas se canalizaran mediante la cadena: Estudiantes -> Docente -> CITT -> Empresa/Organizacion (CMQ Area Salud). 

### **2.1  Tabla de Dudas** 

|**ID**|**Categoria**|**Pregunta**|**Origen /**<br>**Justificacion**|**Estado**|**Canal**|
|---|---|---|---|---|---|
|**D-01**|Acceso y<br>autenticacion|¿La autenticacion<br>de la app debe<br>integrarse con el<br>Active Directory /<br>Microsoft 365 de la<br>CMQ, o se maneja<br>con credenciales<br>propias del<br>sistema?|El caso menciona<br>correo institucional<br>y Microsoft 365,<br>pero no especifica<br>el metodo de<br>autenticacion.|Abierta|Estudiantes -><br>Docente -> CITT -><br>CMQ|
|**D-02**|Integracion<br>SharePoint|¿Se utilizara<br>Microsoft Graph API<br>o SharePoint REST<br>API para la<br>integracion? ¿Que<br>permisos de<br>aplicacion se<br>requieren?|El caso exige<br>integracion con<br>SharePoint pero no<br>define el<br>mecanismo tecnico<br>ni las credenciales.|Abierta|Estudiantes -><br>Docente -> CITT -><br>CMQ|
|**D-03**|Repositorio de<br>pruebas|¿Se proveera un<br>sitio de SharePoint<br>academico<br>(sandbox) con datos<br>ficticios, o el equipo<br>debe simular la<br>integracion con un<br>mock?|El caso proscribe el<br>acceso a sistemas<br>internos y el uso de<br>datos reales<br>(RN-03), pero no<br>indica como probar<br>la integracion.|Abierta|Estudiantes -><br>Docente -> CITT -><br>CMQ|
|**D-04**|Notificaciones push|¿Que servicio de<br>notificaciones push<br>se utilizara<br>(Firebase Cloud<br>Messaging, Azure<br>Notification Hubs)?|El caso menciona<br>notificaciones pero<br>no especifica el<br>proveedor ni las<br>restricciones de TI.|Abierta|Estudiantes -><br>Docente -> CITT -><br>CMQ|



Pagina 3 

DSY1105 · Caso CMQ Area Salud · MVVM y Dudas VcM 

|||¿Esta autorizado<br>por TI?||||
|---|---|---|---|---|---|
|**D-05**|Almacenamiento<br>local|¿Esta permitido<br>cachear<br>documentos en el<br>dispositivo (Room)?<br>¿Que documentos<br>pueden<br>almacenarse offline<br>y por cuanto<br>tiempo?|El caso exige<br>proteccion de datos<br>personales; el cache<br>local podria<br>vulnerar la regla si<br>no se acota.|Abierta|Estudiantes -><br>Docente -> CITT -><br>CMQ|
|**D-06**|Plataforma iOS|¿Se exigira una<br>version iOS en<br>alguna evaluacion<br>posterior, o Android<br>es suficiente para el<br>MVP?|El caso menciona<br>iOS como<br>'eventual'; el<br>equipo necesita<br>confirmar si<br>quedara fuera de<br>toda evaluacion.|Abierta|Estudiantes -><br>Docente|
|**D-07**|Perfiles de usuario|¿La matriz de<br>perfiles (FUN, ADM,<br>JEF, RRHH, SYS, TIC)<br>es completa o<br>existen mas roles<br>que no aparecen en<br>el caso?|El caso enumera 7<br>perfiles; se requiere<br>confirmar que no<br>hay roles<br>adicionales.|Abierta|Estudiantes -><br>Docente -> CITT -><br>CMQ|
|**D-08**|Vencimiento de<br>documentos|¿Existe una politica<br>institucional de<br>vigencia por tipo de<br>documento<br>(contrato, permiso,<br>capacitacion)?|El caso menciona<br>vencimientos pero<br>no define plazos; el<br>equipo no debe<br>inventarlos.|Abierta|Estudiantes -><br>Docente -> CITT -><br>CMQ|
|**D-09**|Trazabilidad|¿La bitacora de<br>auditoria debe<br>almacenarse en<br>SharePoint, en base<br>de datos propia o<br>en ambos?|El caso exige<br>trazabilidad pero no<br>indica el repositorio<br>de la bitacora.|Abierta|Estudiantes -><br>Docente -> CITT -><br>CMQ|
|**D-10**|Datos ficticios|¿El equipo puede<br>generar libremente<br>datos sinteticos o<br>debe solicitar un set<br>de datos de prueba<br>al CITT?|El caso exige datos<br>ficticios pero no<br>indica si deben ser<br>provistos por la<br>organizacion.|Abierta|Estudiantes -><br>Docente -> CITT|



### **2.2  Reglas para el Manejo de Dudas** 

- No incorporar al diseno ninguna regla de negocio que no este explicita en el caso. 

- Toda duda con impacto en el alcance o la arquitectura debe registrarse antes de continuar. 

- Las decisiones tomadas sin validacion externa se marcan como supuestos y se revisan al recibir respuesta. 

- El registro se actualiza en cada reunion de equipo y se entrega junto a los avances del caso. 

- Canal oficial: Estudiantes -> Docente -> CITT -> Empresa/Organizacion (CMQ Area Salud). 

### **2.3  Supuestos Asumidos (mientras se valida)** 

|**ID**|**Supuesto asumido**|**Duda asociada**|**Riesgo si el supuesto es**<br>**falso**|
|---|---|---|---|
|**S-01**|La autenticacion sera local|D-01|Bajo - se puede ajustar en la|
||(RUT + contrasena) sin<br>integracion con Active<br>Directory.||capa de datos.|



Pagina 4 

DSY1105 · Caso CMQ Area Salud · MVVM y Dudas VcM 

|**S-02**|La integracion con<br>SharePoint se simulara con<br>un mock hasta contar con<br>sandbox.|D-03|Medio - requiere refactor al<br>recibir credenciales reales.|
|---|---|---|---|
|**S-03**|Se utilizara Firebase Cloud<br>Messaging para<br>notificacionespush.|D-04|Bajo - el cambio a Azure<br>Notification Hubs es aislado.|
|**S-04**|El cache local (Room)<br>almacenara solo metadatos,<br>no documentos sensibles.|D-05|Alto - podria requerir<br>eliminacion del cacheo.|



## **3.  Cierre** 

El esquema MVVM delimita responsabilidades para View (Compose), ViewModel (estado y reglas de presentacion) y Datos (Repository con SharePoint, Room y AuditLogger), con flujo unidireccional y separacion estricta. El registro de dudas identifica 10 preguntas abiertas que requieren validacion externa via la cadena Estudiantes -> Docente -> CITT -> CMQ, junto a 4 supuestos transitorios. Ambos entregables se actualizaran en cada iteracion del proyecto. 

Pagina 5 

