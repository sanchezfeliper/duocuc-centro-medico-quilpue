DSY1105 · Semana 9 · Visualización de los Datos Guardados — CMQ Salud

# **Semana 9 · Parte 9.2 - Visualización de los Datos Guardados**

_Lo que se guarda en el dispositivo se ve en la pantalla y sigue ahí al reabrir la app_

DSY1105 - Desarrollo de Aplicaciones Móviles  ·  CMQ Área Salud  ·  12-10-2026

---

## **1. Objetivo de la Parte 9.2**

La pauta de la Semana 9 pide, tras la persistencia, la **visualización de los datos guardados**: que el usuario pueda *ver* en la interfaz la información que vive en el dispositivo y comprobar que se mantiene entre sesiones.

En la 9.1 el expediente ya se guardaba en SQLite, pero esa persistencia no era **visible** para el usuario: las pantallas mostraban los datos sin decir de dónde venían. Esta entrega hace explícito el **origen local** en las tres pantallas que leen el almacenamiento (P01 Inicio, P03 Consulta y P04 Detalle) y lo deja verificado con un script que **carga, reinicia y reabre** la aplicación.

---

## **2. Qué cambió**

Se mantiene la separación MVVM (Meta 5): el conteo se resuelve en la capa de datos y se expone por estado; las Views solo lo dibujan.

| Punto de la pauta | Dónde se muestra |
|---|---|
| **Indicador de origen local** | **P01 Inicio**: tarjeta "**N documentos en almacenamiento local** · Base SQLite del dispositivo · persisten al cerrar la app". Es el total real de la tabla `documentos`. |
| **Expediente leído del dispositivo** | **P03 Consulta**: bajo los filtros, "**N documentos del expediente · leídos del almacenamiento local (M en el dispositivo)**". |
| **Metadato de procedencia** | **P04 Detalle**: fila **ORIGEN = "Almacenamiento local del dispositivo (SQLite)"**, como metadato del expediente (RN-08). |

**Cambios de código**:

| Archivo | Rol |
|---|---|
| `data/local/DocumentoLocalDataSource.kt` | Nuevo contrato `contarDocumentos(): Int` (el repositorio sigue sin conocer el motor). |
| `data/local/SqliteDocumentoLocalDataSource.kt` | Implementación con `SELECT COUNT(*)` en `Dispatchers.IO`. |
| `test/.../local/FakeDocumentoLocalDataSource.kt` | Doble de prueba devuelve el tamaño de su lista en memoria. |
| `data/DocumentRepository.kt` | Expone `contarDocumentos()` a la capa de presentación. |
| `ui/screens/home/HomeUiState.kt` · `HomeViewModel.kt` | Nuevo estado `documentosEnAlmacenamiento`, cargado en `init` y en `refrescarContador()`. |
| `ui/screens/home/HomeScreen.kt` | Tarjeta indicadora del almacenamiento local. |
| `ui/screens/consulta/ConsultaUiState.kt` · `ConsultaViewModel.kt` · `ConsultaExpedienteScreen.kt` | Estado `totalEnAlmacenamiento` y rótulo de origen bajo los filtros. |
| `ui/screens/detalle/DetalleDocumentoScreen.kt` | Metadato **ORIGEN**. |

> Nota de alcance: no se agregó ninguna dependencia nueva. El conteo usa la misma base SQLite de la 9.1; solo se hizo **visible** en la interfaz.

---

## **3. Evidencia**

Script reproducible [`scripts/verificar_visualizacion.sh`](scripts/verificar_visualizacion.sh): parte de una app limpia (`pm clear`), carga un documento y **cierra y reabre** la aplicación, comprobando en pantalla el total antes, durante y después. Cada paso usa `exigir …`: si un elemento no aparece, el script **falla de forma ruidosa** en vez de dejar una captura sin contenido.

Salida real:

```
== 1) App desde cero: Home debe mostrar los 4 documentos de la semilla ==
aparecio: '4 documentos en almacenamiento local'
== 3) Home debe reflejar el nuevo total (5) =============================
aparecio: '5 documentos en almacenamiento local'
== 4) Cierre total de la app y reapertura ===============================
aparecio: '5 documentos en almacenamiento local'
== 6) P03 Consulta rotula el origen local ===============================
aparecio: '5 documentos del expediente · leídos del almacenamiento local (5 en el dispositivo)'
== 7) P04 Detalle declara que el documento viene del dispositivo ========
aparecio: 'Almacenamiento local del dispositivo (SQLite)'
== OK: los datos guardados se ven en la interfaz y persisten tras reiniciar ==
```

Capturas en [`docs/evidencia_semana9/`](docs/evidencia_semana9/):

| Archivo | Contenido |
|---|---|
| `21_home_almacenamiento_inicial.png` | App recién instalada: Inicio muestra **4 documentos** (semilla ficticia). |
| `22_home_tras_cargar.png` | Tras cargar `viz_semana9.pdf` en P05: Inicio muestra **5 documentos**. |
| `23_home_persistencia_tras_reinicio.png` | **Tras cerrar y reabrir la app**: Inicio sigue mostrando **5 documentos**, leídos de SQLite. |
| `24_consulta_origen_local.png` | P03 rotula "…leídos del almacenamiento local (5 en el dispositivo)". |
| `25_detalle_origen_local.png` | P04 muestra el metadato **ORIGEN** y el folio **#5** del documento cargado. |

### **Los tests siguen en verde (37)**

Se agregaron 5 casos nuevos: `contarDocumentos` en el repositorio y cuatro en el nuevo `ConsultaViewModelTest` (expediente completo, filtro sin perder el total, RUT válido sin expediente y RUT con DV incorrecto que no consulta la base).

```bash
./gradlew :app:testDebugUnitTest     # 37 tests, 0 fallos
./scripts/verificar_visualizacion.sh # evidencia en el emulador
```

---

## **4. Corrección de comportamiento (ciclo de la Meta 8)**

La primera ejecución de la verificación **falló en el paso 6**: el texto aparecía en pantalla pero el helper no lo hallaba. Causa raíz en `scripts/adb_ui.sh`: al escapar el patrón de búsqueda se convertían `(` y `)` en `\(` y `\)`, que en el grep básico (BRE) de GNU son operadores de **grupo**, no paréntesis literales — el patrón dejaba de coincidir con el texto real. Se corrigió la lista de caracteres escapados (solo lo que es especial en BRE: `. * [ ^ $`), de modo que cualquier texto con paréntesis se localiza correctamente. Es el mismo criterio que la 9.1: **el script debe fallar, no generar evidencia engañosa**, y un fallo debe investigarse hasta la causa.

---

## **5. Estado frente a los puntos de la Semana 9**

| **Punto de la pauta** | **Estado** |
|---|---|
| Navegación funcional | ✅ NavHost con P01–P06 y paso de datos (Metas 5 y 6) |
| Formularios y validaciones | ✅ P05 (11 reglas) y P06 (comentario obligatorio) |
| Uso de ViewModel | ✅ Un ViewModel por pantalla; la lógica no vive en las Views |
| Gestión de estado | ✅ `StateFlow` + Snackbar/banner |
| Persistencia local | ✅ 9.1: SQLite en el dispositivo |
| **Visualización de lo guardado** | ✅ **Esta entrega**: origen local visible en P01, P03 y P04 |
| Animaciones | 🔜 9.4 |
| Recurso nativo | 🔜 9.3 (cámara, RF-09) |
| Prueba del flujo principal | 🔜 se realiza en clase (tutorial en `docs/Tutorial_Demo_Flujo_Principal.md`) |

---

## **6. Conclusión**

La parte **9.2 queda cumplida**: la información que vive en la base SQLite ya no solo persiste, también **se ve** en la interfaz. Inicio informa cuántos documentos hay en el dispositivo, Consulta rotula que el expediente se lee del almacenamiento local y Detalle declara su procedencia; un script reproducible demuestra que el número **crece al cargar** y **se mantiene tras cerrar y reabrir** la aplicación. Los 37 tests unitarios pasan.

**Próximos pasos:** 9.3 cámara como recurso nativo (RF-09), 9.4 animaciones con propósito, 9.5 prueba del flujo principal extremo a extremo.
