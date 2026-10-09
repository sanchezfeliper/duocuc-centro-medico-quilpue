DSY1105 · Caso CMQ Area Salud · Primer avance Android Studio 

# **Primer Avance en Android Studio** 

_Configuracion del proyecto Jetpack Compose e implementacion de la pantalla Login_ 

DSY1105 - Desarrollo de Aplicaciones Moviles  ·  CMQ Area Salud  ·  30-09-2026 

## **1.  Objetivo** 

Este documento registra el primer avance del proyecto Android: la creacion del proyecto en Android Studio con Jetpack Compose, la verificacion de su ejecucion y la implementacion inicial de la pantalla Login (P01) junto con componentes reutilizables basados en los mockups del Punto 5. El avance cumple el requisito de la etapa 7 del caso DSY1105. 

## **2.  Configuracion del Proyecto** 

### **2.1  Especificaciones del Proyecto** 

|**Parametro**|**Valor**|
|---|---|
|IDE|Android Studio Hedgehog (2023.1.1) o superior|
|Plantilla|Empty Activity (Jetpack Compose)|
|Name|CMQSalud|
|Package name|cl.cmq.salud|
|Save location|C:\Users\<usuario>\AndroidStudioProjects\CMQSalud|
|Language|Kotlin|
|Minimum SDK|API 26 (Android 8.0)|
|Build configuration language|Kotlin DSL (build.gradle.kts)|



### **2.2  Pasos de Creacion** 

1. Abrir Android Studio y seleccionar New Project. 

2. Elegir la plantilla Empty Activity (Compose). 

3. Completar nombre, paquete y ubicacion segun la tabla 2.1. 

4. Establecer Minimum SDK en API 26. 

5. Confirmar con Finish y esperar la sincronizacion de Gradle. 

### **2.3  Dependencias Clave (build.gradle.kts)** 

Verificar que el modulo app incluya las siguientes dependencias en el bloque dependencies: 

_<u>[kotlin]</u>_ 

dependencies { 

> // Compose BOM (Bill of Materials) val composeBom = platform("androidx.compose:compose-bom:2024.02.00") implementation(composeBom) 

> // Core Compose implementation("androidx.compose.ui:ui") 

Pagina 1 

DSY1105 · Caso CMQ Area Salud · Primer avance Android Studio 

implementation("androidx.compose.ui:ui-graphics") implementation("androidx.compose.ui:ui-tooling-preview") implementation("androidx.compose.material3:material3") // Activity + Lifecycle implementation("androidx.activity:activity-compose:1.8.2") implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0") // Navegacion implementation("androidx.navigation:navigation-compose:2.7.7") // Testing testImplementation("junit:junit:4.13.2") androidTestImplementation(composeBom) debugImplementation("androidx.compose.ui:ui-tooling") <u>}</u> 

### **2.4  Permisos en AndroidManifest.xml** 

Para soportar la camara (RF-09) y el acceso a Internet (RF-24), incluir: 

_<u>[xml]</u>_ 

<uses-permission android:name="android.permission.INTERNET" /> <uses-permission android:name="android.permission.CAMERA" /> <uses-feature android:name="android.hardware.camera" android:required="false" /> 

## **3.  Verificacion de Ejecucion** 

Para confirmar que el proyecto compila y ejecuta correctamente: 

6. Ejecutar Make Project (Ctrl+F9) para verificar la compilacion. 

7. Seleccionar un emulador API 26 o superior (o dispositivo fisico). 

8. Ejecutar Run 'app' (Shift+F10). 

9. Confirmar que la app inicia y muestra la pantalla por defecto de la plantilla. 

Resultado esperado: la aplicacion compila sin errores y se ejecuta mostrando el texto predeterminado 'Hello Android!' en pantalla. Esto valida el entorno antes de incorporar la pantalla Login. 

## **4.  Implementacion: Pantalla Login (P01)** 

Se implementa la primera pantalla segun el mockup P01 del Punto 5. Se aplica la arquitectura MVVM definida en el Punto 6: View en Compose, ViewModel con StateFlow y, en esta etapa, datos en memoria (el Repository se integrara en la siguiente iteracion). 

### **4.1  Tema y Colores (ui/theme/Color.kt)** 

_<u>[kotlin]</u>_ // Paleta institucional CMQ Salud val Navy = Color(0xFF1F3A5F) val Blue = Color(0xFF2E5E8C) val Green = Color(0xFF2E8C5E) val Light = Color(0xFFF4F7FA) val Grey = Color(0xFF7A8794) 

Pagina 2 

DSY1105 · Caso CMQ Area Salud · Primer avance Android Studio 

### **4.2  Estado de UI (LoginUiState.kt)** 

_<u>[kotlin]</u>_ 

data class LoginUiState( val rut: String = "", val password: String = "", val isAuthenticating: Boolean = false, val errorMessage: String? = null, val isAuthenticated: Boolean = false <u>)</u> 

### **4.3  ViewModel (LoginViewModel.kt)** 

#### _<u>[kotlin]</u>_ 

class LoginViewModel : ViewModel() { private val _uiState = MutableStateFlow(LoginUiState()) val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow() fun onRutChange(value: String) { _uiState.update { it.copy(rut = value, errorMessage = null) } } fun onPasswordChange(value: String) { _uiState.update { it.copy(password = value, errorMessage = null) } } fun login() { val state = _uiState.value if (state.rut.isBlank() || state.password.isBlank()) { _uiState.update { it.copy(errorMessage = "Ingrese RUT y contrasena") } return } _uiState.update { it.copy(isAuthenticating = true, errorMessage = null) } viewModelScope.launch { delay(800) // simulacion de llamada al backend (RN-03: datos ficticios) // En etapa posterior: AuthRepository.login(rut, password) _uiState.update { it.copy(isAuthenticating = false, isAuthenticated = true) } } } <u>}</u> 

### **4.4  Componentes Reutilizables (ui/components/Buttons.kt)** 

Se crean componentes basados en el mockup, reutilizables en otras pantallas: 

_<u>[kotlin]</u>_ 

@Composable fun PrimaryButton( text: String, onClick: () -> Unit, modifier: Modifier = Modifier, 

Pagina 3 

DSY1105 · Caso CMQ Area Salud · Primer avance Android Studio 

enabled: Boolean = true ) { Button( onClick = onClick, enabled = enabled, colors = ButtonDefaults.buttonColors( containerColor = Green, contentColor = Color.White ), shape = RoundedCornerShape(6.dp), modifier = modifier.fillMaxWidth() ) { Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) } } @Composable fun SecondaryButton( text: String, onClick: () -> Unit, modifier: Modifier = Modifier ) { OutlinedButton( onClick = onClick, colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy), border = BorderStroke(1.dp, Navy), shape = RoundedCornerShape(6.dp), modifier = modifier.fillMaxWidth() ) { Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) } <u>}</u> 

### **4.5  Pantalla Login (ui/screens/LoginScreen.kt)** 

_<u>[kotlin]</u>_ 

@Composable fun LoginScreen( vm: LoginViewModel = viewModel(), onLoginSuccess: () -> Unit ) { val state by vm.uiState.collectAsState() LaunchedEffect(state.isAuthenticated) { if (state.isAuthenticated) onLoginSuccess() } Column( modifier = Modifier .fillMaxSize() .background(Navy) .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center 

Pagina 4 

DSY1105 · Caso CMQ Area Salud · Primer avance Android Studio 

) { // Logo CMQ Box( modifier = Modifier .size(72.dp) .clip(CircleShape) .background(Color.White), contentAlignment = Alignment.Center ) { Text("+", color = Navy, fontSize = 32.sp, fontWeight = FontWeight.Bold) } Spacer(Modifier.height(8.dp)) Text("CMQ Salud", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) Text("Gestion documental", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp) Spacer(Modifier.height(32.dp)) // Campos OutlinedTextField( value = state.rut, onValueChange = vm::onRutChange, label = { Text("RUT", color = Color.White) }, singleLine = true, colors = fieldColors(), modifier = Modifier.fillMaxWidth() ) Spacer(Modifier.height(12.dp)) OutlinedTextField( value = state.password, onValueChange = vm::onPasswordChange, label = { Text("Contrasena", color = Color.White) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), colors = fieldColors(), modifier = Modifier.fillMaxWidth() ) state.errorMessage?.let { Spacer(Modifier.height(8.dp)) Text(it, color = Color(0xFFE74C3C), fontSize = 12.sp) } Spacer(Modifier.height(16.dp)) if (state.isAuthenticating) { CircularProgressIndicator(color = Green) } else { PrimaryButton("INICIAR SESION", vm::login) } Spacer(Modifier.height(8.dp)) SecondaryButton("¿Primera vez? Solicitar acceso", onClick = { /* TODO */ }) } } <u>@Composable</u> 

Pagina 5 

DSY1105 · Caso CMQ Area Salud · Primer avance Android Studio 

private fun fieldColors() = OutlinedTextFieldDefaults.colors( focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Green, unfocusedBorderColor = Color.White.copy(alpha = 0.5f), focusedLabelColor = Green, unfocusedLabelColor = Color.White.copy(alpha = 0.7f), cursorColor = Green <u>)</u> 

### **4.6  Punto de Entrada (MainActivity.kt)** 

_<u>[kotlin]</u>_ class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState) setContent { CMQSaludTheme { Surface( modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background ) { LoginScreen(onLoginSuccess = { /* TODO: navegar a Home */ }) } } } } <u>}</u> 

## **5.  Prueba y Resultados** 

|**Caso de prueba**|**Pasos**|**Resultado esperado**|**Estado**|
|---|---|---|---|
|CP-01 Compilacion|Make Project (Ctrl+F9).|Build exitoso sin errores.|OK|
|CP-02 Pantalla Login|Ejecutar app en emulador<br>API 26.|Muestra logo, titulo y<br>campos RUT/contrasena.|OK|
|CP-03 Validacion vacia|Tap 'INICIAR SESION' sin<br>datos.|Mensaje 'Ingrese RUT y<br>contrasena'.|OK|
|CP-04 Login exitoso|Ingresar datos ficticios y tap<br>'INICIAR SESION'.|ProgressIndicator y luego<br>disparo onLoginSuccess.|OK|



## **6.  Conclusion y Proximos Pasos** 

El proyecto Android Studio esta creado con Jetpack Compose, compila y ejecuta correctamente. Se implemento la pantalla Login (P01) con arquitectura MVVM, componentes reutilizables (PrimaryButton, SecondaryButton) y la paleta institucional CMQ. La pantalla cumple los RF-01 y RF-04 a nivel de UI; la integracion con AuthRepository y la navegacion a Home quedan para la siguiente iteracion. 

Proximos pasos: 

Pagina 6 

DSY1105 · Caso CMQ Area Salud · Primer avance Android Studio 

- Integrar Hilt para inyeccion de dependencias y AuthRepository con Retrofit. 

- Implementar la pantalla Home (P02) y la navegacion con Navigation-Compose. 

- Anadir validacion de formato de RUT y enmascarado de contrasena. 

- Incorporar el modulo de camara para la pantalla P05 Carga de Documento. 



<!-- Start of picture text -->
‘oma Salud<br>can s00<br>HE bce PaovemseSeSGrvarvex«t192Be@ “ Co at<br><!-- End of picture text -->



<!-- Start of picture text -->
‘cma Salud<br>HE 8 bar Waocemeoeocrar «398280 - BO cena<br><!-- End of picture text -->

Pagina 7 

