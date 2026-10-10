DSY1105 · Meta 5 · Aplicación correcta de MVVM — CMQ Salud

# **Meta 5 - Empezar a Aplicar MVVM Correctamente**

_Separación estricta View → ViewModel → Datos: navegación fuera de MainActivity, lógica fuera de las pantallas y reglas compartidas en el dominio_

DSY1105 - Desarrollo de Aplicaciones Móviles  ·  CMQ Área Salud  ·  08-10-2026

---

## **1. Objetivo de la Meta**

La Meta 5 exige que a esta altura del semestre **toda la lógica no viva dentro de las pantallas**. El ciclo que debe cumplirse en cada flujo es:

```
SCREEN / VIEW
      ↓  evento
VIEWMODEL
      ↓  validación / lógica
nuevo estado
      ↓  estado expuesto (StateFlow)
SCREEN actualizada
```

| **Debe vivir en el ViewModel** | **Debe vivir en la pantalla (View)** |
|---|---|
| Estado del formulario | Mostrar información |
| Validaciones | Recibir acciones |
| Procesamiento de eventos | Enviar eventos |
| Creación o actualización de datos | |

El objetivo explícito de la guía es **evitar que `MainActivity` termine concentrando pantallas, reglas, navegación y datos en un único archivo**.

---

## **2. Diagnóstico: Violaciones Encontradas ("Antes")**

Auditoría del código tal como quedó tras la Meta 4. Se detectaron **4 violaciones** al principio de la meta:

| **#** | **Dónde** | **Violación** | **Gravedad** |
|---|---|---|---|
| V-1 | `MainActivity.kt` | Concentraba el enum `AppPantalla`, el `when` de navegación entre 4 pantallas, la creación de cada pantalla y 4 `Toast` de feedback: navegación + reglas + pantallas en un solo archivo (exactamente el anti-patrón que la guía advierte). | **Alta** |
| V-2 | `ConsultaExpedienteScreen.kt` | La **lógica de filtrado** de documentos vivía en la View (un bloque `remember(documentos, filtroActivo)` con `when` y `filter`), mientras el `aplicarFiltro()` del ViewModel era **código muerto**: calculaba la lista filtrada y nunca la exponía. Regla de negocio dibujando en la capa equivocada. | **Alta** |
| V-3 | `ConsultaViewModel` vs `CargaViewModel` | El validador de RUT estaba **duplicado y desincronizado**: Consulta solo validaba formato con un regex local, mientras Carga validaba formato + dígito verificador (Módulo 11). La misma regla de dominio vivía dos veces, distinta, en dos ViewModels. | Media |
| V-4 | `HomeScreen.kt` | El botón "Cerrar sesión" ejecutaba **dos acciones imperativas** (`vm.cerrarSesion()` y `onCerrarSesion()`) en el onClick: la navegación no era consecuencia del estado sino de la llamada directa desde la View. | Baja |

---

## **3. Solución Implementada ("Después")**

### **3.1 Navegación formal con Navigation-Compose (V-1)**

Se creó el paquete `ui/navigation/` con dos archivos, y `MainActivity` quedó reducido a **solo inicializar tema y grafo** (de 86 líneas a 25):

**`ui/navigation/AppDestinos.kt`** — rutas tipadas de la app:

```kotlin
sealed class AppDestinos(val route: String) {
    object Login : AppDestinos("login")
    object Home : AppDestinos("home")
    object Consulta : AppDestinos("consulta")
    object Carga : AppDestinos("carga")
}
```

**`ui/navigation/AppNavHost.kt`** — único lugar donde se cablean pantallas y transiciones. Las pantallas solo exponen eventos (lambdas); el grafo decide a dónde navegar:

```kotlin
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = AppDestinos.Login.route) {
        composable(AppDestinos.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppDestinos.Home.route) {
                        popUpTo(AppDestinos.Login.route) { inclusive = true } // Atras no vuelve al Login
                    }
                }
            )
        }
        composable(AppDestinos.Home.route) {
            HomeScreen(
                onNavigateConsulta = { navController.navigate(AppDestinos.Consulta.route) },
                onNavigateCarga    = { navController.navigate(AppDestinos.Carga.route) },
                onNavigateValidacion = { /* P06 futura */ },
                onNavigateBitacora   = { /* P08 futura */ },
                onCerrarSesion = {
                    navController.navigate(AppDestinos.Login.route) {
                        popUpTo(0) { inclusive = true } // limpia toda la pila
                    }
                }
            )
        }
        // ... Consulta y Carga, con popBackStack() para volver y
        // reinicio de pila en Home tras una carga exitosa
    }
}
```

**`MainActivity.kt`** final:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CMQSaludTheme {
                AppNavHost()   // MainActivity ya no conoce pantallas ni rutas
            }
        }
    }
}
```

**Mejora funcional adicional**: al usar un `NavController` real (la dependencia `navigation-compose:2.7.7` ya estaba declarada desde el primer avance pero sin usar), la app ahora tiene **back stack correcto**: el botón "Atrás" del sistema lleva a la pantalla anterior y no vuelve al Login tras autenticar (cumple la regla de navegación del Punto 8), el logout limpia toda la pila, y tras una carga exitosa la pantalla de carga sale de la pila. Los `Toast` para destinos no implementados (P04, P06, P08) quedaron como feedback temporal en el grafo; al existir cada pantalla, tendrá su propia ruta.

### **3.2 Filtrado de vuelta al ViewModel (V-2)**

El estado de Consulta ahora incluye el listado ya filtrado, y `aplicarFiltro()` es funcional:

```kotlin
// ConsultaUiState — nuevo campo
val documentosFiltrados: List<Documento> = emptyList()

// ConsultaViewModel — el filtro es lógica del ViewModel
private fun aplicarFiltro() {
    _uiState.update { state ->
        val filtrada = when (state.filtroActivo) {
            FiltroDocumento.TODOS     -> state.documentos
            FiltroDocumento.VIGENTES  -> state.documentos.filter { it.estado == EstadoDocumento.VIGENTE }
            FiltroDocumento.PENDIENTES-> state.documentos.filter { it.estado == EstadoDocumento.PENDIENTE }
            FiltroDocumento.VENCIDOS  -> state.documentos.filter { it.estado == EstadoDocumento.VENCIDO }
        }
        state.copy(documentosFiltrados = filtrada)
    }
}
```

La View quedó reducida a `items(state.documentosFiltrados)`: ya no contiene `when`, ni `filter`, ni `remember` de lógica de negocio. El comportamiento visible es idéntico al de la Meta 1 (mismos chips, mismos resultados), pero la regla vive donde corresponde.

### **3.3 Validador de RUT compartido en el dominio (V-3)**

Se creó `domain/validation/ValidadorRut.kt`: la regla de RUT vive ahora en la **capa de dominio** y es consumida por tres ViewModels:

| **Consumidor** | **Función usada** | **Alcance** |
|---|---|---|
| `CargaViewModel` (P05) | `ValidadorRut.validar()` | Formato + dígito verificador (Módulo 11). Mensajes idénticos a los documentados en la Meta 3. |
| `ConsultaViewModel` (P03) | `ValidadorRut.validar()` | Ahora **también valida dígito verificador** (la promesa "siguiente iteración" de la Meta 1, y el paso 1 de próximos pasos de la Meta 3). |
| `LoginViewModel` (P01) | `ValidadorRut.esFormatoValido()` | Solo formato: la autenticación es simulada (supuesto S-01) y la identidad real vendrá del backend, por eso aquí no se exige DV (así la credencial ficticia `12.345.678-9` de los mockups sigue funcionando). |

`CargaViewModel` eliminó su regex y su `calcularDigitoVerificador` privados; la obligatoriedad del campo sigue siendo regla de cada formulario (mensaje contextual), y el formato+DV es regla de dominio única.

### **3.4 Navegación por estado en Home (V-4)**

El botón "Cerrar sesión" ahora solo emite el evento al ViewModel, y la navegación ocurre como **consecuencia del estado**:

```kotlin
// HomeScreen — la View reacciona al estado, no orquesta
LaunchedEffect(state.isSessionActive) {
    if (!state.isSessionActive) onCerrarSesion()
}

TextButton(onClick = { vm.cerrarSesion() }) { ... }  // solo emite el evento
```

---

## **4. Mapa de Capas Resultante**

| **Pantalla** | **View (solo muestra / emite)** | **ViewModel (estado + lógica)** | **Datos** |
|---|---|---|---|
| P01 Login | Campos, errores, progreso | `LoginViewModel`: obligatoriedad, formato (dominio), flujo de autenticación simulada | — (simulado) |
| P02 Home | Saludo, 4 MenuCard, alerta, botón sesión | `HomeViewModel`: resumen ficticio, `isSessionActive` | — (ficticio) |
| P03 Consulta | Buscador, chips, listado | `ConsultaViewModel`: RUT (dominio), búsqueda, **filtrado** | `DocumentRepository` |
| P05 Carga | Formulario, errores, banner, snackbar, diálogo | `CargaViewModel`: 11 validaciones (Meta 3), eventos, procesamiento | `DocumentRepository` |
| Navegación | `AppNavHost` (grafo) + `MainActivity` (tema) | — | — |

**Reglas de oro verificadas** (Punto 8 del proyecto):

- La View no conoce el Repository ni SharePoint — ✓ (solo lambdas y `collectAsState`).
- El ViewModel no conoce Compose ni Activity — ✓ (ningún import de Compose/Context en los 4 ViewModel).
- El Repository no conoce ViewModel ni pantallas — ✓.
- Flujo unidirecional: evento → ViewModel → estado → View — ✓ (incluida la navegación por estado de Home).
- Dependencias entre capas: inyección por constructor con valor por defecto; Hilt queda como siguiente paso.

---

## **5. Casos de Prueba**

| **Caso** | **Pasos** | **Resultado esperado** | **Estado** |
|---|---|---|---|
| **CP-01** | Login válido → Home → botón Atrás del sistema | Sale de la app; **no** vuelve al Login (Login fue removido de la pila). | Aprobado |
| **CP-02** | Home → Consulta → botón Atrás del sistema | Vuelve a Home conservando su estado (resumen intacto). | Aprobado |
| **CP-03** | Home → Cargar → carga exitosa → "Ver en Inicio" | Aterriza en Home y la pantalla de Carga sale del back stack. | Aprobado |
| **CP-04** | Home → Cerrar sesión → intentar Atrás | Vuelve al Login y la pila quedó limpia (Atras desde Login no navega a Home). | Aprobado |
| **CP-05** | Consulta: buscar con RUT `18.765.432-1` | Ahora rechaza con mensaje del DV: "…debiera ser '7' y se ingresó '1'…" (validación compartida activa). | Aprobado |
| **CP-06** | Consulta: buscar `18.765.432-7` y aplicar filtros | Muestra funcionario + 4 documentos; los chips filtran igual que en Meta 1 (comportamiento preservado, lógica reubicada). | Aprobado |
| **CP-07** | Carga (P05): caso CA-01 de la Meta 3 (formulario vacío) | Mismos mensajes y mismas 11 validaciones que en Meta 3/4 (el validador compartido no cambió los mensajes). | Aprobado |
| **CP-08** | Login con RUT `12.345.678-9` (formato ok, DV incorrecto) | **Permite** autenticar: en Login solo se exige formato (credencial simulada, supuesto S-01). | Aprobado |

---

## **6. Respuestas a la Guía Meta 5**

| **Pregunta de la Guía** | **Respuesta del Equipo CMQ Salud** |
|---|---|
| **¿Dónde está el estado de cada formulario?** | En el ViewModel de cada pantalla, expuesto como `StateFlow<UiState>` inmutable: `LoginUiState`, `HomeUiState`, `ConsultaUiState` (con `documentosFiltrados` derivado), `CargaUiState` (con errores por campo, banner y eventos de snackbar). |
| **¿Dónde están las validaciones?** | Formato + dígito verificador en el dominio compartido (`domain/validation/ValidadorRut.kt`); obligatoriedad, longitudes, extensiones, selección y autenticidad en los ViewModels. Ninguna validación vive en las Views. |
| **¿Quién procesa los eventos y crea/actualiza datos?** | Los ViewModels: reciben eventos de la View (onChange, onBuscar, guardarDocumento, cerrarSesion), validan, llaman al `DocumentRepository` y publican el nuevo estado. La pantalla P05 crea documentos; P03 los consulta y filtra. |
| **¿Qué quedó en las pantallas?** | Solo dibujar el estado (`collectAsState`), recibir acciones del usuario y emitir eventos (lambdas). Incluso la navegación de cierre de sesión es consecuencia del estado (`LaunchedEffect(isSessionActive)`). |
| **¿MainActivity sigue concentrando todo?** | No: quedó reducido a 25 líneas que solo inicializan el tema y delegan a `AppNavHost` (paquete `ui/navigation`), que es el único lugar donde se cablean rutas y transiciones. |

---

## **7. Conclusión y Próximos Pasos**

La **Meta 5 queda cumplida**: se eliminaron las 4 violaciones detectadas — navegación formal con Navigation-Compose fuera de `MainActivity`, filtrado de Consulta devuelto al ViewModel, validador de RUT unificado en el dominio con tres consumidores, y navegación por estado en Home — sin cambiar el comportamiento visible ya probado en las metas anteriores (los mismos mensajes y resultados se conservan; la validación de Consulta solo se hizo más estricta, al agregar el dígito verificador).

**Próximos pasos para siguientes entregas:**

1. **Hilt**: reemplazar los valores por defecto (`= DocumentRepository()`) por inyección real (`@HiltViewModel` + `@Inject`), definida como pendiente desde el Punto 8.
2. **Interfaz de Repository** (`DocumentRepository` como interface + `DocumentRepositoryImpl`) para dejar listo el cambio a Retrofit + Room cuando se resuelvan las dudas D-02/D-03 (sandbox SharePoint).
3. Pantalla **P06 Validación** conectada al flujo de documentos `PENDIENTE` generados por el switch de P05.
4. Tests unitarios de ViewModels (JUnit + coroutines-test) para consolidar la separación: si la lógica está en el VM, debe ser testeable sin Compose.
