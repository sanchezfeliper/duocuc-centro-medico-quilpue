DSY1105 · Caso CMQ Area Salud · Pantalla Home (P02) 

# **Pantalla de Inicio (P02) - Implementacion** 

_Continuacion del primer avance en Android Studio con Jetpack Compose_ 

DSY1105 - Desarrollo de Aplicaciones Moviles  ·  CMQ Area Salud  ·  30-09-2026 

## **1.  Objetivo** 

Este documento extiende el primer avance del proyecto Android Studio agregando la pantalla de Inicio (P02) sobre la base del Login (P01) ya implementado. Se incorporan nuevos componentes reutilizables (tarjeta de menu, alerta y barra de navegacion inferior) y se mantiene el patron MVVM definido en el Punto 8. Con esto se cumplen los minimos exigidos por la etapa: proyecto creado, MainActivity, dos pantallas en Compose y mas de dos componentes reutilizables. 

## **2.  Estado de Cumplimiento de la Etapa** 

|**Requisito**|**Estado**|**Evidencia**|
|---|---|---|
|Proyecto creado en Android Studio|OK|Configurado en Punto 7 (CMQSalud,<br>package cl.cmq.salud).|
|MainActivity|OK|Definido en Punto 7; ahora navega<br>entre Login y Home.|
|Primera pantalla Compose|OK|LoginScreen (P01) entregada en Punto<br>7.|
|Segunda pantalla Compose|OK|HomeScreen (P02) entregada en este<br>documento.|
|Componentes reutilizables|OK|PrimaryButton, SecondaryButton (P7)<br>+ MenuCard, AlertCard, BottomNavBar<br>(este doc).|
|Proyecto ejecutable|OK|Verificado en casos CP-01 a CP-04 del<br>Punto 7.|



## **3.  Arquitectura de la Pantalla Home** 

La pantalla Home sigue el patron MVVM: la View observa el StateFlow expuesto por el ViewModel, y este mantiene el estado de UI con los datos del usuario autenticado y los indicadores del menu (pendientes, vencimientos). Por ahora los datos son ficticios (RN-03); el Repository se incorporara en la siguiente iteracion. 

### **3.1  Estado de UI (HomeUiState.kt)** 

_<u>[kotlin]</u>_ 

> // ui/screens/home/HomeUiState.kt package cl.cmq.salud.ui.screens.home 

> data class HomeUiState( val rut: String = "", val nombreUsuario: String = "Maria Gonzalez", val perfil: String = "Personal Administrativo", 

Pagina 1 

DSY1105 · Caso CMQ Area Salud · Pantalla Home (P02) 

val centroSalud: String = "CESFAM Quilpue", val validacionesPendientes: Int = 3, val documentosPorVencer: Int = 2, val isSessionActive: Boolean = true <u>)</u> 

### **3.2  ViewModel (HomeViewModel.kt)** 

_<u>[kotlin]</u>_ 

// ui/screens/home/HomeViewModel.kt package cl.cmq.salud.ui.screens.home 

import androidx.lifecycle.ViewModel import androidx.lifecycle.viewModelScope import kotlinx.coroutines.flow.MutableStateFlow import kotlinx.coroutines.flow.StateFlow import kotlinx.coroutines.flow.asStateFlow import kotlinx.coroutines.launch 

class HomeViewModel : ViewModel() { 

private val _uiState = MutableStateFlow(HomeUiState()) val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow() init { cargarResumen() } /** Carga resumen del usuario. Datos ficticios (RN-03). */ private fun cargarResumen() { viewModelScope.launch { _uiState.value = HomeUiState( rut = "12.345.678-9", nombreUsuario = "Maria Gonzalez", perfil = "Personal Administrativo", centroSalud = "CESFAM Quilpue", validacionesPendientes = 3, documentosPorVencer = 2 ) } } fun cerrarSesion() { _uiState.value = _uiState.value.copy(isSessionActive = false) } <u>}</u> 

## **4.  Componentes Reutilizables Nuevos** 

Se incorporan tres componentes reutilizables basados en el mockup P02: MenuCard (tarjeta de menu con icono, titulo y subtitulo), AlertCard (alerta de vencimiento) y BottomNavBar (barra inferior de navegacion). Se suman a PrimaryButton y SecondaryButton ya existentes. 

Pagina 2 

DSY1105 · Caso CMQ Area Salud · Pantalla Home (P02) 

### **4.1  MenuCard (ui/components/MenuCard.kt)** 

#### _<u>[kotlin]</u>_ 

// ui/components/MenuCard.kt package cl.cmq.salud.ui.components import androidx.compose.foundation.background import androidx.compose.foundation.border import androidx.compose.foundation.clickable import androidx.compose.foundation.layout.* import androidx.compose.foundation.shape.RoundedCornerShape import androidx.compose.material3.Icon import androidx.compose.material3.Text import androidx.compose.runtime.Composable import androidx.compose.ui.Alignment import androidx.compose.ui.Modifier import androidx.compose.ui.graphics.Color import androidx.compose.ui.text.font.FontWeight import androidx.compose.ui.unit.dp import androidx.compose.ui.unit.sp import cl.cmq.salud.ui.theme.Green import cl.cmq.salud.ui.theme.Light import cl.cmq.salud.ui.theme.Navy import cl.cmq.salud.ui.theme.Grey @Composable fun MenuCard( icon: String, title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier ) { Row( modifier = modifier .fillMaxWidth() .background(Light, RoundedCornerShape(6.dp)) .border(width = 2.dp, color = Green, shape = RoundedCornerShape(6.dp)) .clickable(onClick = onClick) .padding(12.dp), verticalAlignment = Alignment.CenterVertically ) { Box( modifier = Modifier .size(28.dp) .background(Navy, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center ) { Text(icon, color = Color.White, fontSize = 13.sp) } Spacer(Modifier.width(10.dp)) Column(modifier = Modifier.weight(1f)) { Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Navy) Text(subtitle, fontSize = 9.sp, color = Grey) <u>}</u> 

Pagina 3 

DSY1105 · Caso CMQ Area Salud · Pantalla Home (P02) 

Text(">", color = Grey, fontSize = 16.sp) } <u>}</u> 

### **4.2  AlertCard (ui/components/AlertCard.kt)** 

_<u>[kotlin]</u>_ // ui/components/AlertCard.kt package cl.cmq.salud.ui.components import androidx.compose.foundation.background import androidx.compose.foundation.layout.* import androidx.compose.foundation.shape.RoundedCornerShape import androidx.compose.material3.Text import androidx.compose.runtime.Composable import androidx.compose.ui.Alignment import androidx.compose.ui.Modifier import androidx.compose.ui.text.font.FontWeight import androidx.compose.ui.unit.dp import androidx.compose.ui.unit.sp import androidx.compose.ui.graphics.Color private val AlertBg = Color(0xFFFFF3CD) private val AlertFg = Color(0xFF856404) @Composable fun AlertCard( text: String, modifier: Modifier = Modifier ) { Row( modifier = modifier .fillMaxWidth() .background(AlertBg, RoundedCornerShape(5.dp)) .padding(10.dp), verticalAlignment = Alignment.CenterVertically ) { Text(text, color = AlertFg, fontSize = 10.sp, fontWeight = FontWeight.Medium) } <u>}</u> 

### **4.3  BottomNavBar (ui/components/BottomNavBar.kt)** 

_<u>[kotlin]</u>_ // ui/components/BottomNavBar.kt package cl.cmq.salud.ui.components import androidx.compose.foundation.layout.* import androidx.compose.material3.Text import androidx.compose.runtime.Composable import androidx.compose.ui.Alignment import androidx.compose.ui.Modifier import androidx.compose.ui.text.font.FontWeight import androidx.compose.ui.unit.dp 

Pagina 4 

DSY1105 · Caso CMQ Area Salud · Pantalla Home (P02) 

import androidx.compose.ui.unit.sp import cl.cmq.salud.ui.theme.Green import cl.cmq.salud.ui.theme.Grey data class NavItem(val icon: String, val label: String) @Composable fun BottomNavBar( items: List<NavItem>, activeIndex: Int = 0, onItemSelected: (Int) -> Unit, modifier: Modifier = Modifier ) { Row( modifier = modifier .fillMaxWidth() .padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceAround ) { items.forEachIndexed { index, item -> Column( horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier .padding(horizontal = 4.dp) .padding(top = 2.dp) ) { val color = if (index == activeIndex) Green else Grey Text(item.icon, color = color, fontSize = 16.sp) Spacer(Modifier.height(2.dp)) Text( item.label, color = color, fontSize = 9.sp, fontWeight = if (index == activeIndex) FontWeight.SemiBold else FontWeight.Normal ) } } } <u>}</u> 

## **5.  Pantalla Home (ui/screens/home/HomeScreen.kt)** 

_<u>[kotlin]</u>_ // ui/screens/home/HomeScreen.kt package cl.cmq.salud.ui.screens.home import androidx.compose.foundation.background import androidx.compose.foundation.layout.* import androidx.compose.foundation.rememberScrollState import androidx.compose.foundation.verticalScroll import androidx.compose.material3.* import androidx.compose.runtime.* import androidx.compose.ui.Alignment import androidx.compose.ui.Modifier 

Pagina 5 

DSY1105 · Caso CMQ Area Salud · Pantalla Home (P02) 

import androidx.compose.ui.graphics.Color import androidx.compose.ui.text.font.FontWeight import androidx.compose.ui.unit.dp import androidx.compose.ui.unit.sp import androidx.lifecycle.viewmodel.compose.viewModel import cl.cmq.salud.ui.components.AlertCard import cl.cmq.salud.ui.components.BottomNavBar import cl.cmq.salud.ui.components.MenuCard import cl.cmq.salud.ui.components.NavItem import cl.cmq.salud.ui.theme.* @Composable fun HomeScreen( vm: HomeViewModel = viewModel(), onNavigateConsulta: () -> Unit, onNavigateCarga: () -> Unit, onNavigateValidacion: () -> Unit, onNavigateBitacora: () -> Unit, onCerrarSesion: () -> Unit ) { val state by vm.uiState.collectAsState() Scaffold( topBar = { TopAppBar( title = { Text("Inicio", color = Color.White, fontSize = 14.sp) }, navigationIcon = { Text("=", color = Color.White) }, actions = { Text("O", color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue) ) }, bottomBar = { BottomNavBar( items = listOf( NavItem("H", "Inicio"), NavItem("L", "Expediente"), NavItem("+", "Cargar"), NavItem("U", "Perfil") ), activeIndex = 0, onItemSelected = { /* TODO navegacion inferior */ } ) } ) { padding -> Column( modifier = Modifier .fillMaxSize() .background(Color.White) .padding(padding) .padding(horizontal = 16.dp, vertical = 12.dp) .verticalScroll(rememberScrollState()) ) { // Saludo al usuario Text( "Hola, <u>${state.nombreUsuario}",</u> 

Pagina 6 

DSY1105 · Caso CMQ Area Salud · Pantalla Home (P02) 

fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy ) Text( "${state.perfil} · ${state.centroSalud}", fontSize = 10.sp, color = Grey ) Spacer(Modifier.height(14.dp)) // Menu principal MenuCard( icon = "L", title = "Consultar expediente", subtitle = "Buscar documentos por funcionario", onClick = onNavigateConsulta ) Spacer(Modifier.height(8.dp)) MenuCard( icon = "U", title = "Cargar documento", subtitle = "Archivo o captura con camara", onClick = onNavigateCarga ) Spacer(Modifier.height(8.dp)) MenuCard( icon = "V", title = "Validaciones pendientes", subtitle = "${state.validacionesPendientes} documentos en espera", onClick = onNavigateValidacion ) Spacer(Modifier.height(8.dp)) MenuCard( icon = "B", title = "Bitacora de auditoria", subtitle = "Trazabilidad de accesos", onClick = onNavigateBitacora ) Spacer(Modifier.height(14.dp)) // Alerta de vencimientos if (state.documentosPorVencer > 0) { AlertCard( text = "! ${state.documentosPorVencer} documentos proximos a vencer" ) } Spacer(Modifier.weight(1f)) 

Pagina 7 

DSY1105 · Caso CMQ Area Salud · Pantalla Home (P02) 

// Cierre de sesion (accion administrativa) TextButton(onClick = { vm.cerrarSesion() onCerrarSesion() }) { Text("Cerrar sesion", color = Navy, fontSize = 11.sp) } } } <u>}</u> 

## **6.  Conexion Login -> Home en MainActivity** 

Para conectar la pantalla Login con Home sin Navigation-Compose aun, se usa un estado simple en MainActivity que cambia de pantalla cuando el Login dispara onLoginSuccess. En la siguiente iteracion se integrara Navigation-Compose con un NavHost formal. 

_<u>[kotlin]</u>_ // MainActivity.kt package cl.cmq.salud import android.os.Bundle import androidx.activity.ComponentActivity import androidx.activity.compose.setContent import androidx.compose.runtime.* import cl.cmq.salud.ui.screens.home.HomeScreen import cl.cmq.salud.ui.screens.login.LoginScreen import cl.cmq.salud.ui.theme.CMQSaludTheme class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState) setContent { CMQSaludTheme { var isLoggedIn by remember { mutableStateOf(false) } if (!isLoggedIn) { LoginScreen(onLoginSuccess = { isLoggedIn = true }) } else { HomeScreen( onNavigateConsulta = { /* TODO */ }, onNavigateCarga = { /* TODO */ }, onNavigateValidacion = { /* TODO */ }, onNavigateBitacora = { /* TODO */ }, onCerrarSesion = { isLoggedIn = false } ) } } } } <u>}</u> 

Pagina 8 

DSY1105 · Caso CMQ Area Salud · Pantalla Home (P02) 

## **7.  Pruebas del Avance** 

|**Caso**|**Pasos**|**Resultado esperado**|**Estado**|
|---|---|---|---|
|CP-05 Login -> Home|Login valido desde P01.|Pasa a Home (P02) y<br>muestra saludo.|OK|
|CP-06 Menu completo|Verificar 4 MenuCard en<br>Home.|Consulta, Carga,<br>Validaciones y Bitacora<br>visibles.|OK|
|CP-07 Alerta vencimiento|Revisar estado con<br>documentosPorVencer = 2.|Se muestra AlertCard<br>amarilla.|OK|
|CP-08 Bottom nav|Verificar barra inferior con 4<br>items.|Inicio activo en verde, resto<br>en gris.|OK|
|CP-09 Cerrar sesion|Tap en 'Cerrar sesion'.|Vuelve a Login (P01).|OK|



## **8.  Conclusion y Proximos Pasos** 

Con la pantalla Home (P02) se cumplen todos los minimos exigidos por la etapa: proyecto creado, MainActivity, dos pantallas en Compose (Login y Home), cinco componentes reutilizables (PrimaryButton, SecondaryButton, MenuCard, AlertCard, BottomNavBar) y proyecto ejecutable. Se mantiene el patron MVVM y la paleta institucional CMQ. 

Proximos pasos: 

- Integrar Navigation-Compose con NavHost formal para reemplazar el estado de MainActivity. 

- Implementar la pantalla de Consulta de expediente (P03) con su ViewModel. 

- Conectar HomeViewModel a un Repository cuando se integre Hilt y Retrofit. 

- Agregar validacion de timeout de sesion (RF-03) al volver de background. 

Pagina 9 

