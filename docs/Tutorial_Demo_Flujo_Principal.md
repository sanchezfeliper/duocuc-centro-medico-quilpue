DSY1105 · Semana 9 · Tutorial de demo del flujo principal — CMQ Salud

# **Tutorial: cómo mostrar el flujo principal en la revisión de clase**

_Guion para demostrar en ~5 minutos el flujo completo exigido por la pauta:_
**_usuario ingresa información → formulario valida → ViewModel procesa → cambia el estado → información se guarda → resultado aparece en pantalla_**

DSY1105 - Desarrollo de Aplicaciones Móviles  ·  CMQ Área Salud  ·  10-10-2026

---

## **1. Antes de la clase (5 minutos)**

1. Abrir el emulador (o conectar el teléfono): en Android Studio, **Device Manager → Pixel_7 → Play**.
2. Verificar que la app está instalada y al día:
   ```bash
   gradlew :app:installDebug
   ```
3. Si vienen de una prueba anterior y quieren partir limpio (la base local se borra y se vuelve a cargar la semilla ficticia):
   ```bash
   adb shell pm clear cl.cmq.salud
   ```
4. Datos que usarán (son ficticios, **RN-03**): RUT de login `12.345.678-9` y contraseña cualquiera; RUT de funcionario `18.765.432-7`.

> **Importante**: no usar datos reales de personas. Todo el sistema trabaja con datos ficticios, tal como exige el caso.

---

## **2. Guion de la demostración (paso a paso)**

### Paso 1 — Login (validación) · ~30 s
- Abrir la app → aparece **P01 Login**.
- Tocar **INICIAR SESIÓN** con los campos vacíos.
- **Qué decir**: *"El formulario valida antes de enviar: el error aparece en pantalla y no se procesa nada."*
- Escribir RUT `12.345.678-9` + contraseña, entrar.
- **Punto de código**: `ui/screens/login/LoginViewModel.kt` (valida en el ViewModel, no en la pantalla).

### Paso 2 — Home (estado) · ~30 s
- En **P02 Home** mostrar el saludo y las tarjetas del menú.
- **Qué decir**: *"El contador de 'Validaciones pendientes' no es un número fijo: sale de los documentos que hay guardados."*
- **Punto de código**: `ui/screens/home/HomeViewModel.kt` → `repo.listarPendientes()`.

### Paso 3 — Carga de documento: caso INVÁLIDO · ~40 s
- Tocar **Cargar documento** (P05) y presionar **Guardar y Registrar Documento** con el formulario vacío.
- **Qué mostrar**: los 4 campos en rojo con su mensaje, el banner con la lista de campos a corregir y el Snackbar "No se pudo registrar el documento".
- **Qué decir**: *"Nada se guardó: el ViewModel detuvo el procesamiento. Cada mensaje dice qué corregir."*

### Paso 4 — Carga de documento: caso VÁLIDO · ~40 s
- Llenar: RUT `18.765.432-7`, nombre `demo_clase.pdf`, una descripción breve, **marcar la declaración de autenticidad**.
- Presionar **Guardar y Registrar Documento**.
- **Qué mostrar**: aparece el diálogo **"¡Documento Registrado!"** con el **folio** asignado.
- **Qué decir**: *"El formulario validó, el ViewModel procesó y el documento se guardó en la base local del dispositivo con un folio."*

### Paso 5 — La prueba de la persistencia (el momento clave) · ~40 s
- **En Android Studio, detener la app** (botón rojo Stop) — o desde consola:
  ```bash
  adb shell am force-stop cl.cmq.salud
  ```
- Volver a abrir la app, hacer login y entrar a **Consultar expediente** con el RUT `18.765.432-7`.
- **Qué mostrar**: el documento `demo_clase.pdf` **sigue en el expediente**.
- **Qué decir**: *"Cerré la aplicación por completo. El dato no estaba en memoria: quedó guardado en SQLite y se recuperó al volver a abrirla."*

### Paso 6 — Consulta → Detalle (navegación con datos) · ~30 s
- Tocar la tarjeta del documento.
- **Qué mostrar**: **P04 Detalle** con folio, archivo, tipo, estado, fecha, responsable y la descripción **tal como se escribió en el formulario**.
- **Qué decir**: *"El ID del documento viaja por la navegación y el detalle lo recupera de la base."*

### Paso 7 — Validación de jefatura: caso INVÁLIDO y VÁLIDO · ~60 s
- Volver a Home → **Validaciones pendientes** (P06).
- Seleccionar el documento pendiente y tocar **RECHAZAR** *sin* comentario.
- **Qué mostrar**: "El comentario es obligatorio para rechazar…" y el documento sigue pendiente.
- Escribir un comentario (ej: "Documento no legible") y tocar **RECHAZAR** de nuevo.
- **Qué mostrar**: el documento sale de la bandeja y aparece el Snackbar **"Documento #N rechazado"**.
- Al volver a Home, el contador de pendientes **bajó**.
- **Qué decir**: *"La decisión cambió el estado, se guardó y la interfaz lo refleja; el contador de Home se actualiza porque lee los datos reales."*

---

## **3. El flujo que pide la pauta, aterrizado al proyecto**

| **Etapa de la pauta** | **Dónde se ve en CMQ Salud** |
|---|---|
| usuario ingresa información | P05 Carga de documento (RUT, tipo, archivo, descripción) |
| el formulario valida | 11 reglas: obligatorio, formato, longitud, selección, dígito verificador del RUT |
| el ViewModel procesa | `CargaViewModel.guardarDocumento()` → arma el `Documento` y llama al repositorio |
| cambia el estado | `CargaUiState` pasa a `showSuccessDialog` / errores; Home y P06 recalculan sus contadores |
| la información se guarda | `DocumentRepository` → `SqliteDocumentoLocalDataSource` → tabla `documentos` |
| el resultado aparece en pantalla | Diálogo de éxito con folio; el documento aparece en P03 y en su P04 Detalle |

---

## **4. Explicar el recurso nativo (lo pide el profe)**

**Recurso elegido: la cámara del dispositivo.**

- **Por qué tiene sentido en este caso**: el problema central de la CMQ es la **dispersión documental**; gran parte de los antecedentes de los funcionarios existe **en papel** (contratos firmados, licencias, certificados). La cámara es lo que permite **digitalizarlos desde el propio teléfono**, sin escáner ni computador.
- **Está exigido por el caso**: el requerimiento **RF-09** del levantamiento pide literalmente *"capturar imagen de documento físico con la cámara del dispositivo"*, y el permiso `CAMERA` ya está declarado en el manifiesto desde el primer avance.
- **Dónde se integra**: en **P05**, la opción "Captura física con cámara" del formulario (hoy es una selección; en la parte 9.3 se conecta la cámara real y la foto queda asociada al documento guardado).
- **Por qué no GPS/ubicación**: no aporta al problema — saber dónde se cargó un documento no mejora la trazabilidad documental. Se elige el recurso que el caso justifica, no uno por cumplir.

---

## **5. Si algo falla en vivo**

| **Síntoma** | **Solución rápida** |
|---|---|
| La app muestra datos viejos de una demo anterior | `adb shell pm clear cl.cmq.salud` y volver a abrir (recarga la semilla ficticia) |
| No aparece el documento tras reabrir | Verificar que se **detuvo la app por completo** antes de reabrirla |
| El emulador no responde | `adb devices`; si no aparece, reiniciar el emulador desde Device Manager |
| Se quiere repetir la demo de persistencia sin hacerla a mano | `./scripts/verificar_persistencia.sh` (lo hace todo y deja capturas) |

---

## **6. Preguntas probables del docente y respuesta corta**

| **Pregunta** | **Respuesta** |
|---|---|
| ¿Dónde está la lógica del formulario? | En el ViewModel (`CargaViewModel`), no en la pantalla: la View solo dibuja y emite eventos (MVVM, Meta 5). |
| ¿Qué motor de persistencia usan? | SQLite del propio SDK, a través de una interfaz `DocumentoLocalDataSource`; Room es el siguiente paso cuando se configure el procesador de anotaciones. |
| ¿Cómo prueban que guarda de verdad? | Script `verificar_persistencia.sh`: guarda, cierra la app por completo, reabre y verifica; además se vuelca la tabla desde el dispositivo. |
| ¿Y si el usuario carga un documento de otro funcionario? | El **RUT del formulario** determina el funcionario (E2): cada expediente muestra solo sus documentos. |
| ¿Está listo para producción? | No: es un **MVP académico** (RN-04) con datos ficticios; la sincronización real con SharePoint espera autorización de TI (dudas D-02/D-03). |
