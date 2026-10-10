DSY1105 · Meta 6 · Interacción entre pantallas — CMQ Salud

# **Meta 6 - Lograr Interacción entre Pantallas**

_NavController, NavHost, destinos y paso de información: el flujo Formulario → Listado → Detalle con el dato ingresado_

DSY1105 - Desarrollo de Aplicaciones Móviles  ·  CMQ Área Salud  ·  08-10-2026

---

## **1. Objetivo de la Meta**

La Meta 6 exige **al menos dos pantallas conectadas** (ej: Home → Registrar, o Listado → Detalle) y que **el dato ingresado comience a utilizarse posteriormente** (ej: lo escrito en un formulario aparece después en el detalle). La guía introduce el flujo mediante **NavController, NavHost, destinos y paso de información entre pantallas**.

En CMQ Salud se implementó el eslabón que faltaba en el recorrido principal del mapa de navegación (Punto 8): la pantalla **P04 Detalle de documento**, conectada a P03 Consulta mediante **argumentos de ruta**. Con esto queda cerrado el ciclo completo de datos del caso:

```
P05 Carga (formulario)          P03 Consulta (listado)         P04 Detalle
"contrato_juan_perez.pdf"  ──>  tarjeta del documento #5  ──>  #5 · contrato_juan_perez.pdf
estado: PENDIENTE               tap en la tarjeta              estado: PENDIENTE
descripcion: "..."                                             descripcion: "..."
```

El documento que el usuario registra en el formulario **viaja por el repositorio compartido, aparece en el listado de consulta y se puede abrir en su detalle completo con todos sus metadatos (RN-08)**. Además —corrección aplicada tras la revisión del equipo— **el RUT ingresado en el formulario es el dato que realmente asocia el documento a su funcionario**: `guardarDocumento` registra (o reutiliza) al funcionario del RUT, `buscarPorRut` filtra el expediente solo con los documentos de ese RUT, y P04 muestra el RUT asociado. El dato más importante del caso ya no se valida y se descarta: **determina a qué expediente pertenece todo lo demás**.

| **Atributo** | **Valor** |
|---|---|
| **Pantallas conectadas** | P01→P02→P03→**P04 (nueva)** y P02→P05 |
| **Paso de información** | Argumento de ruta `detalle/{idDocumento}` |
| **Requerimientos** | RF-12, RF-14, RF-08, RN-08 |
| **Arquitectura** | MVVM + Navigation-Compose (NavHost/NavController/destinos) |

---

## **2. Qué se Conectó**

| **Flujo** | **Estado antes** | **Estado después** |
|---|---|---|
| Home → Carga (P02 → P05) | Conectado desde Meta 2, formalizado con NavHost en Meta 5 | Sin cambios (Home → Registrar ya existía como ejemplo de la pauta) |
| **Consulta → Detalle (P03 → P04)** | `onDocumentoClick(id)` mostraba un Toast "P04 próximamente" | **Nueva pantalla P04**: el tap en una `DocumentoCard` navega al detalle del documento con su ID |
| **Dato del formulario usado después** | El documento creado en P05 aparecía en P03 (repositorio en memoria compartido) | Ahora además se **abre en P04** con nombre, tipo, estado, fecha, responsable y descripción exactamente como se ingresaron |

### **2.1 La pantalla P04 Detalle (nueva)**

Implementa el propósito definido en el mapa de navegación ("Visualización, metadatos y descarga", RF-14 y RF-18), con el lenguaje visual del mockup (bloques de revisión estilo P05 y badge de estado compartido con `DocumentoCard`):

* **Vista previa simulada** del documento (tipo + nombre).
* **Metadatos del expediente (RN-08)**: folio, nombre del archivo, tipo, estado con badge de color, fecha de carga, usuario que cargó, funcionario asociado y descripción.
* **RF-14**: botón "Registrar acceso de descarga" — el acceso queda registrado en el estado (simulado; la bitácora real llega con P08). 
* **Estados de carga y error**: indicador de progreso mientras consulta, y mensaje comprensible si el ID no existe ("No se encontró el documento #N…"), con botón de retorno.

---

## **3. Paso de Información entre Pantallas**

### **3.1 Decisión de diseño: se pasa el ID, no el objeto**

Entre las dos alternativas para llevar el dato a P04 se eligió **pasar el ID por la ruta** y que el ViewModel del destino resuelva el documento contra la fuente única (`DocumentRepository`):

* Es el patrón recomendado de Navigation-Compose: la ruta es una **URL** (`detalle/5`), no un contenedor de objetos.
* Mantiene el **repositorio como única fuente de verdad** (RN-05: sin copias — también aplicable a objetos en memoria entre pantallas).
* Si el documento cambia, el detalle siempre muestra la versión actual.
* Añade un caso de prueba real: ID inexistente → estado de error (CP-04).

### **3.2 Destino con argumento (`AppDestinos`)**

```kotlin
object Detalle : AppDestinos("detalle/{idDocumento}") {
    fun crearRuta(idDocumento: Int): String = "detalle/$idDocumento"
    const val ARG_ID = "idDocumento"
}
```

### **3.3 Navegación desde el listado (`AppNavHost`)**

```kotlin
composable(AppDestinos.Consulta.route) {
    ConsultaExpedienteScreen(
        onVolver = { navController.popBackStack() },
        onDocumentoClick = { idDocumento ->
            navController.navigate(AppDestinos.Detalle.crearRuta(idDocumento))
        }
    )
}

// Destino que lee el argumento
composable(AppDestinos.Detalle.route) { backStackEntry ->
    val idDocumento = backStackEntry.arguments
        ?.getString(AppDestinos.Detalle.ARG_ID)
        ?.toIntOrNull() ?: 0
    DetalleDocumentoScreen(
        idDocumento = idDocumento,
        onVolver = { navController.popBackStack() }
    )
}
```

### **3.4 ViewModel del destino con argumento**

Como aún no hay Hilt, `DetalleViewModel` recibe el ID mediante un `ViewModelProvider.Factory` y consulta al repositorio:

```kotlin
class DetalleViewModel(
    private val repo: DocumentRepository,
    private val idDocumento: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init { cargarDetalle() }

    fun cargarDetalle() {
        viewModelScope.launch {
            val documento = repo.buscarPorId(idDocumento)   // nueva operación del repo
            _uiState.update { it.copy(isLoading = false, documento = documento,
                error = if (documento == null) "No se encontró el documento #$idDocumento…" else null) }
        }
    }

    fun registrarAccesoDescarga() {
        _uiState.update { it.copy(accesoRegistrado = true) }  // RF-14, simulado
    }

    companion object {
        fun factory(idDocumento: Int, repo: DocumentRepository = DocumentRepository()) =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    DetalleViewModel(repo, idDocumento) as T
            }
    }
}
```

Y en `DocumentRepository` se agregó `buscarPorId(idDocumento: Int): Documento?` (con latencia simulada), sobre el mismo almacenamiento en memoria que ya comparte P05 y P03 — por eso el documento recién cargado ya está disponible en detalle sin copias adicionales.

### **3.5 El RUT también viaja: asociación real del documento (corrección)**

La primera versión tenía una grieta detectada en revisión: el formulario validaba el RUT pero creaba el documento con `idFuncionario = 101` fijo, y `buscarPorRut` devolvía siempre todos los documentos. Se corrigió para que **el RUT ingresado sea el dato que gobierna el flujo**:

* `Documento` (E4) incorporó `rutFuncionario`, y se creó el modelo `Funcionario` (E2, subconjunto) con el funcionario de demostración Juan Pérez · `18.765.432-7`.
* `guardarDocumento` resuelve el funcionario a partir del RUT (`obtenerOCrearFuncionario`): lo reutiliza si existe o lo registra si es la primera carga para ese RUT (con nombre "Por registrar" hasta que exista mantención de funcionarios, RF-05).
* `buscarPorRut(rut)` ahora **filtra**: el expediente muestra únicamente los documentos cuyo `rutFuncionario` coincide — cumplir RF-12 de verdad ("Listado de documentos" **del funcionario**), no un listado global.
* El resumen del funcionario en P03 sale del repositorio (E2), no de datos hardcodeados; y si el RUT no tiene expediente, se muestra un estado vacío comprensible ("No se encontró expediente para el RUT…"), que era el caso **CA-06 de la Meta 1, pendiente desde entonces**.
* P04 Detalle muestra "FUNCIONARIO ASOCIADO: RUT … · ID …", cerrando la trazabilidad del dato en las tres pantallas.

---

## **4. Cadena Completa del Dato Ingresado (Demostración)**

1. En **P05 Carga**, el usuario registra: RUT del funcionario, nombre `permiso_medico_28-09.pdf`, tipo "Licencia médica", descripción "Permiso médico del 28-09-2026", switch activo → estado `PENDIENTE`. AlertDialog confirma con **folio #5**.
2. En **P03 Consulta**, el usuario busca por RUT: la tarjeta "permiso_medico_28-09.pdf" aparece **con los datos exactos que ingresó** (fecha, responsable, badge "Pendiente").
3. Al **tocar la tarjeta**, el NavController navega a `detalle/5`: el **P04 Detalle** muestra folio #5, nombre del archivo, tipo, estado PENDIENTE con su badge, fecha, responsable, funcionario y **la descripción tal como se escribió en el formulario**.
4. Desde P04, "Registrar acceso de descarga" simula RF-14, y "Volver al expediente" regresa a P03 conservando el filtro y la búsqueda.

Cumple el ejemplo de la guía (Formulario → Detalle con el dato ingresado) usando las pantallas del caso: **Formulario (P05) → Listado (P03) → Detalle (P04)**.

---

## **5. Casos de Prueba**

| **Caso** | **Pasos** | **Resultado esperado** | **Estado** |
|---|---|---|---|
| **CP-01** | Consulta → buscar RUT válido → tap "Contrato de trabajo" | Navega al detalle: folio #1, nombre, tipo PDF, estado Vigente (badge azul), fecha y responsable correctos. | Aprobado |
| **CP-02** | Cargar documento en P05 (RUT `18.765.432-7`, estado PENDIENTE, folio #N) → Consulta con ese RUT → tap en la nueva tarjeta | El detalle muestra **exactamente** los datos ingresados en el formulario, incluida la descripción, el estado PENDIENTE y el RUT asociado. | Aprobado |
| **CP-03** | Detalle → botón Atrás del sistema o "Volver al expediente" | Regresa a P03 conservando la búsqueda y el filtro activo. | Aprobado |
| **CP-04** | Abrir ruta con ID inexistente (`detalle/999` vía flujo forzado) | Estado de error comprensible: "No se encontró el documento #999…" + botón de retorno. | Aprobado |
| **CP-05** | Detalle → tap "Registrar acceso de descarga" | Aparece confirmación verde "Acceso registrado en la bitácora (RF-18, simulado)"; el botón cambia a "Registrar otro acceso". | Aprobado |
| **CP-06** | P03 → P04 → P03 → tap en otro documento | Navega al detalle del nuevo documento (la instancia del ViewModel es única por ID, sin datos cruzados). | Aprobado |
| **CP-07** | Cargar documento en P05 para un RUT nuevo (ej: `12.345.678-5`) → Consultar ese RUT | El expediente muestra **solo** el documento recién cargado, con resumen "Funcionario 12.345.678-5 · Por registrar" (asociación real por RUT). | Aprobado |
| **CP-08** | Consultar un RUT válido sin expediente (ej: `9.876.543-2`) | Estado vacío comprensible: "No se encontró expediente para el RUT 9.876.543-2. Verifique el RUT o cargue un documento…" (CA-06 de la Meta 1, ahora implementado). | Aprobado |
| **CP-09** | Cargar en P05 para RUT nuevo → volver a consultar `18.765.432-7` | El documento del RUT nuevo **no aparece** en el expediente de Juan Pérez: cada expediente contiene solo sus documentos. | Aprobado |

---

## **6. Respuestas a la Guía Meta 6**

| **Pregunta de la Guía** | **Respuesta del Equipo CMQ Salud** |
|---|---|
| **¿Hay al menos dos pantallas conectadas?** | Sí: cuatro pares conectados con NavHost — Login→Home, Home→Consulta, Home→Carga y ahora **Consulta→Detalle (P03→P04, la nueva pantalla del mapa de navegación)**. |
| **¿El dato ingresado se usa posteriormente?** | Sí, ahora en sentido estricto: **el RUT del formulario determina el funcionario asociado** (el repositorio lo registra o reutiliza), el expediente de P03 **filtra solo los documentos de ese RUT**, y P04 muestra el RUT asociado. Además nombre, tipo, estado y descripción viajan intactos del formulario al detalle (CP-02, CP-07, CP-09). Es el ejemplo Formulario→Detalle de la pauta, materializado con las pantallas del caso. |
| **¿Cómo se pasa información entre pantallas?** | Por **argumento de ruta** (`detalle/{idDocumento}`): el listado navega con `Detalle.crearRuta(id)`, el destino lo lee de `backStackEntry.arguments` y su ViewModel resuelve el documento contra la fuente única. Se pasa el ID, no el objeto, para no duplicar estado. |
| **¿Dónde quedó la responsabilidad de cada pieza?** | MVVM intacto: la navegación vive en `AppNavHost` (único cableador), P04 tiene su `DetalleUiState` + `DetalleViewModel` (carga, error, acceso), y la pantalla solo dibuja y emite eventos. |

---

## **7. Conclusión y Próximos Pasos**

La **Meta 6 queda cumplida**: la app ya no es una secuencia de pantallas aisladas — ahora el dato que entra por el formulario viaja hasta su detalle, con navegación formal por NavController/NavHost, destinos tipados y paso de información por argumento de ruta. Se cerró además el primer eslabón de trazabilidad del expediente: un documento se puede seguir desde su carga hasta su consulta individual.

**Próximos pasos para siguientes entregas:**

1. **P06 Validación** conectada al mismo repositorio: los documentos en estado PENDIENTE (creados por el switch de P05) alimentan la bandeja de jefatura, y su decisión (aprobar/rechazar/corregir, RF-15 a RF-17) actualiza el estado que P03 y P04 muestran.
2. **Notificaciones P07** navegando al detalle: la notificación tap lleva a `detalle/{idDocumento}` (misma ruta, otro origen).
3. Guardar el **RUT del usuario autenticado** en un estado de sesión compartido para que Home muestre datos reales del perfil y P03 restrinja al expediente propio (FUN).
4. Reemplazar el `factory()` de `DetalleViewModel` por **Hilt** cuando se integre la inyección de dependencias (pendiente desde el Punto 8).
