DSY1105 · Semana 9 · Recurso Nativo: Cámara — CMQ Salud

# **Semana 9 · Parte 9.3 - Recurso Nativo: Cámara del Dispositivo**

_La captura física deja de ser un botón inerte: se digitaliza el documento con CameraX (RF-09)_

DSY1105 - Desarrollo de Aplicaciones Móviles  ·  CMQ Área Salud  ·  13-10-2026

---

## **1. Objetivo de la Parte 9.3**

La pauta de la Semana 9 pide usar **al menos un recurso nativo del dispositivo y justificarlo**. En la 9.1 y 9.2 la aplicación ya persistía y mostraba los datos guardados, pero el formulario de carga (P05) tenía una opción que **no hacía nada**: el `RadioButton` **"Captura física con cámara"** solo cambiaba un valor de estado. Esta entrega convierte esa opción en una **captura real con la cámara del dispositivo** (CameraX), guarda la fotografía en el almacenamiento local, la asocia al documento en SQLite y la muestra en P04 Detalle.

---

## **2. Por qué la cámara (justificación del recurso nativo)**

| Criterio | Cámara (elegida) |
|---|---|
| **Requerimiento del caso** | **RF-09** pide capturar el documento físico. La necesidad "digitalizar documentos físicos" está en el alcance del proyecto. |
| **Evidencia en el diseño** | El mockup **P05** ya incluía la zona de captura con la opción "Captura física con cámara"; el modelo E4 ya tenía los campos `hashArchivo` (RF-11) y ahora `rutaLocal`. |
| **Por qué no GPS u otro** | El caso es **gestión documental** (SharePoint como fuente única, RN-05): la ubicación no aporta a ningún requerimiento, la cámara sí. |

> **Alcance acotado:** el recurso nativo se usa donde el caso lo pide. No se agregó GPS ni biometría porque están **fuera de alcance** (el enunciado los excluye explícitamente).

---

## **3. Qué cambió**

Se mantiene el MVVM (Meta 5): la captura vive en la capa de UI/datos y el **ViewModel sigue sin conocer el sistema de archivos** — solo recibe la **ruta** y la **huella** de la foto como datos.

**Flujo implementado:** el usuario elige "Captura física con cámara" → se habilita la zona de captura → se solicita el permiso de cámara → se abre **CameraX** (vista previa en vivo) → obturador → el JPEG se guarda en `filesDir/fotos/` → la ruta y el SHA-256 vuelven al formulario → al registrar, se persisten en SQLite junto al documento → P04 Detalle muestra la fotografía y declara su origen.

| Archivo | Rol |
|---|---|
| `app/build.gradle.kts` · `gradle/libs.versions.toml` | Dependencias **CameraX 1.5.3** (core, camera2, lifecycle, view) y `lifecycle-runtime-compose` (para `LocalLifecycleOwner`). |
| `domain/model/Documento.kt` | Nuevo campo `rutaLocal` (extensión de E4: ruta de la captura en el dispositivo). |
| `data/local/AlmacenFotosLocales.kt` *(nuevo)* | Guarda los JPEG en el almacenamiento interno privado (`filesDir/fotos`), sin permisos de almacenamiento; calcula la huella. |
| `data/local/HuellaArchivo.kt` *(nuevo)* | SHA-256 del contenido (RF-11), función pura y testeable. |
| `data/local/Mapeadores.kt` · `CmqSqliteHelper.kt` | Columna `ruta_local` en la tabla `documentos` (versión de BD **2**). |
| `di/AppContainer.kt` | Expone `almacenFotos` (misma inyección manual de la 9.1). |
| `ui/screens/carga/CamaraCaptura.kt` *(nuevo)* | Pantalla de captura con **CameraX** (Preview + ImageCapture), permiso en tiempo de ejecución y obturador. |
| `ui/screens/carga/CargaUiState.kt` · `CargaViewModel.kt` | Estado de la captura (`fotoRuta`, `fotoHash`, `fotoError`) y **validación nueva**: si el origen es cámara, la foto es obligatoria. |
| `ui/screens/carga/CargaDocumentoScreen.kt` | Tarjeta de captura (miniatura + ruta + huella) y apertura de la pantalla de cámara. |
| `ui/components/ImagenLocal.kt` *(nuevo)* | Dibuja una imagen del almacenamiento interno sin librerías externas. |
| `ui/screens/detalle/DetalleDocumentoScreen.kt` | Muestra la fotografía real y los metadatos **ORIGEN DE CAPTURA**, **ARCHIVO LOCAL** y **HASH SHA-256 (RF-11)**. |

> **Nota de alcance:** la única dependencia nueva es CameraX (biblioteca oficial de Android para cámara). No se agregó ninguna librería de imágenes ni de terceros.

---

## **4. Evidencia**

Script reproducible [`scripts/verificar_camara.sh`](scripts/verificar_camara.sh): parte de una app limpia (`pm clear`), recorre el flujo completo y **falla de forma ruidosa** (`exigir`) si un paso no ocurre, en vez de dejar una captura sin contenido.

Salida real (emulador Pixel_7, Android 14):

```
== 2) P05: origen = Captura fisica con camara ==
aparecio: 'Fotografía del documento físico (RF-09)'
== 3) Permiso de camara (dialogo del sistema) ==
tap 'While using the app' -> tap (540,1229)
== 4) Camara activa: captura de la fotografia ==
aparecio(desc): 'Botón capturar fotografía'
aparecio(contiene+scroll): 'Captura guardada:'
== 6) SQLite guarda ruta_local + hash SHA-256 (RF-09/RF-11) ==
  folio #5  archivo=captura_20261013_030159.jpg
  ruta_local=fotos/captura_20261013_030159.jpg
  hash_sha256=f8c17302e2e49b3e4a9358c090315ac929f812340c3ea0d0088782e45ffec8c7
OK: ruta_local y hash SHA-256 persistidos en la base local
  archivos en files/fotos del dispositivo:
-rw------- 1 u0_a190 u0_a190 190246 2026-10-13 03:02 captura_20261013_030159.jpg
== 7) P04 Detalle muestra la captura y declara el origen (RF-09) ==
aparecio(scroll): 'ORIGEN DE CAPTURA'
aparecio(scroll): 'Cámara del dispositivo (RF-09)'
aparecio(scroll): 'HASH SHA-256 (RF-11)'
== OK: la camara del dispositivo captura, guarda y persiste el documento (RF-09) ==
```

Capturas en [`docs/evidencia_semana9/`](docs/evidencia_semana9/):

| Archivo | Contenido |
|---|---|
| `30_p05_zona_captura.png` | P05 con la opción **"Captura física con cámara"** seleccionada y la zona de captura habilitada. |
| `31_permiso_camara.png` | Diálogo del sistema: **"Allow CMQSalud to take pictures and record video?"** (permiso en tiempo de ejecución). |
| `32_camara_preview.png` | **CameraX activo**: vista previa en vivo de la cámara del dispositivo, guía de encuadre y obturador. |
| `33_captura_asociada.png` | La captura queda asociada: miniatura de la foto, `fotos/captura_….jpg` y huella SHA-256. |
| `34_detalle_captura_camara.png` | P04 Detalle con la fotografía real, **ORIGEN DE CAPTURA = Cámara del dispositivo (RF-09)**, **ARCHIVO LOCAL** y **HASH SHA-256 (RF-11)**. |

### **Los tests siguen en verde (44)**

Se agregaron 8 casos: cuatro en `CargaViewModelTest` (cámara sin captura no registra; la captura asocia ruta/huella y tipo JPG; volver a digital descarta la captura), tres en el nuevo `HuellaArchivoTest` (SHA-256 contra vectores conocidos) y uno en `DocumentRepositoryTest` (la ruta y la huella se conservan).

```bash
./gradlew :app:testDebugUnitTest     # 44 tests, 0 fallos
./scripts/verificar_camara.sh        # evidencia en el emulador
```

---

## **5. Correcciones durante la verificación (ciclo de la Meta 8)**

Dos fallos reales de la automatización, corregidos en el camino:

1. **`adb shell input text` con paréntesis:** el texto `... (RF-09)` rompía el `sh` del dispositivo (`syntax error: unexpected '('`). Se quitó el paréntesis del texto tecleado.
2. **`uiautomator` solo dumpea lo visible:** el texto "Captura guardada: …" quedaba bajo el pliegue y la espera fallaba. Se agregaron helpers que **desplazan** la pantalla hasta hallar el texto (`wait_for_text_scroll` / `wait_for_text_contains_scroll`) y uno que localiza por `content-desc` (el obturador no tiene texto). Es el mismo criterio de las partes anteriores: **el script debe fallar, no inventar evidencia**, y todo fallo se investiga hasta la causa.

---

## **6. Estado frente a los puntos de la Semana 9**

| **Punto de la pauta** | **Estado** |
|---|---|
| Navegación funcional | ✅ NavHost con P01–P06 y paso de datos (Metas 5 y 6) |
| Formularios y validaciones | ✅ P05 (12 reglas, +foto obligatoria) y P06 |
| Uso de ViewModel | ✅ Un ViewModel por pantalla; el VM no conoce archivos ni Context |
| Gestión de estado | ✅ `StateFlow` + Snackbar/banner |
| Persistencia local | ✅ 9.1: SQLite en el dispositivo |
| Visualización de lo guardado | ✅ 9.2: origen local visible en P01, P03 y P04 |
| **Recurso nativo** | ✅ **Esta entrega**: cámara del dispositivo (RF-09), justificada |
| Animaciones | 🔜 9.4 |
| Prueba del flujo principal | 🔜 se realiza en clase (tutorial en `docs/Tutorial_Demo_Flujo_Principal.md`) |

---

## **7. Conclusión**

La parte **9.3 queda cumplida**: la aplicación usa la **cámara del dispositivo** para digitalizar un documento físico, la fotografía se guarda en el almacenamiento local y su ruta y huella SHA-256 se persisten en SQLite, de modo que la captura sobrevive al cierre de la app y se puede ver en P04 Detalle. El recurso nativo está **justificado en el caso** (RF-09, alcance de digitalización documental). Los 44 tests unitarios pasan.

**Próximos pasos:** 9.4 animaciones con propósito, 9.5 prueba del flujo principal extremo a extremo.
