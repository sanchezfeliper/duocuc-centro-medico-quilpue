DSY1105 · Semana 9 · Persistencia Local en el dispositivo — CMQ Salud

# **Semana 9 · Parte 9.1 - Persistencia Local**

_El expediente de funcionarios se guarda en el dispositivo y sobrevive al cierre de la aplicación_

DSY1105 - Desarrollo de Aplicaciones Móviles  ·  CMQ Área Salud  ·  10-10-2026

---

## **1. Objetivo de la Parte 9.1**

La pauta de la Semana 9 pide "**persistencia local**: comenzar a guardar información relevante en el dispositivo, de manera que los datos puedan recuperarse posteriormente". Hasta la Meta 8 el repositorio guardaba todo **en memoria**: al cerrar la app, lo cargado se perdía.

Esta entrega hace que el expediente de funcionarios (documentos E4 y funcionarios E2 del modelo de datos del proyecto) se almacene en una **base de datos SQLite en el dispositivo** y se recupere en cada apertura.

---

## **2. Decisión técnica: SQLite del SDK (y por qué no Room todavía)**

| **Opción** | **Evaluación** |
|---|---|
| **Room** | Es la opción documentada como plan en el Punto 8 del proyecto, pero requiere un **procesador de anotaciones** (KSP/kapt). El proyecto usa AGP 9.3.0 / Kotlin 2.2.10 / compileSdk 37 y **no tiene KSP configurado**; las versiones compatibles de KSP/Room para este toolchain no pudieron verificarse, y un fallo de configuración bloquearía el build completo. |
| **DataStore** | Requiere sumar una dependencia cuya versión compatible tampoco es verificable sin descargarla. |
| **SQLite (`SQLiteOpenHelper`)** ✅ | Viene **incluido en el SDK de Android**: cero dependencias nuevas, cero procesadores de anotaciones, imposible que rompa el build, y es una base **relacional real** — la misma familia que Room (que es una capa sobre SQLite). |

**Decisión**: se implementa con `SQLiteOpenHelper`, coherente con la regla de la Semana 9 de privilegiar una solución **acotada y completamente funcional**. Room queda como siguiente paso documentado: al configurar KSP, la migración es directa porque la app ya depende de la **interfaz** `DocumentoLocalDataSource`, no de SQLite.

> Nota de alcance: la meta no pide un motor específico, pide que la información persista y se recupere. El caso CMQ (documentos, expedientes, bandejas) es de naturaleza relacional, por eso se eligió una base de datos relacional y no un archivo plano.

---

## **3. Arquitectura Implementada**

Se mantiene la separación MVVM (Meta 5): la persistencia vive en la capa de datos y los ViewModels no cambian.

```
ViewModel (P03/P04/P05/P06/Home)
        │  usa la misma API de siempre
        ▼
DocumentRepository                 ← no sabe si los datos son SQLite, Room o red
        │
        ▼
DocumentoLocalDataSource  (interfaz)
        ├── SqliteDocumentoLocalDataSource   → CMQ Salud en el dispositivo
        └── FakeDocumentoLocalDataSource     → doble de prueba (tests JVM)
                 │
                 ▼
        CmqSqliteHelper (SQLiteOpenHelper) → tablas `funcionarios` y `documentos`
```

**Archivos nuevos** (`data/local/`, `di/`):

| Archivo | Rol |
|---|---|
| `DocumentoLocalDataSource.kt` | Interfaz de persistencia: desacopla el repositorio del motor de almacenamiento. |
| `SemillaDocumentos.kt` | Datos ficticios de demostración (RN-03) en un solo lugar, compartidos por SQLite y los tests. |
| `CmqSqliteHelper.kt` | `SQLiteOpenHelper`: crea las tablas y carga la semilla la primera vez. |
| `SqliteDocumentoLocalDataSource.kt` | Implementación real sobre SQLite (consultas e inserciones en `Dispatchers.IO`). |
| `Mapeadores.kt` | Conversión entre entidades de dominio y filas de tabla. |
| `di/AppContainer.kt` | Contenedor de dependencias manual: entrega el repositorio único respaldado por SQLite (antesala de Hilt). |
| `CmqSaludApp.kt` | `Application` que inicializa el almacenamiento antes de cualquier pantalla (registrado en el manifest). |

**Cambios**: `DocumentRepository` ahora delega en la fuente local (su API pública es idéntica, por lo que P03, P04, P05, P06 y Home siguen funcionando igual); los ViewModels obtienen el repositorio desde `AppContainer`; la latencia simulada de red se eliminó porque ahora la escritura es local y real.

---

## **4. Evidencia**

### **4.1 La información queda en la base del dispositivo**

Volcado de la tabla `documentos` tras cargar un documento nuevo (la base se descargó del dispositivo y se consultó con `sqlite3`):

```
documentos:
   (1, 'VIGENTE',   'Contrato de trabajo')
   (2, 'PENDIENTE', 'Certificado capacitacion')
   (3, 'APROBADO',  'Permiso medico')
   (4, 'RECHAZADO', 'Anexo contrato')
   (5, 'VIGENTE',   'persistencia_semana9.pdf')     ← cargado en P05, folio asignado por SQLite
funcionarios:
   (101, '18.765.432-7', 'Juan Pérez')              ← asociado por el RUT del formulario
```

El archivo vive en el almacenamiento interno de la app: `databases/cmq_salud.db`.

### **4.2 La información se recupera tras cerrar la app**

Script reproducible [`scripts/verificar_persistencia.sh`](scripts/verificar_persistencia.sh): carga un documento, **cierra la app por completo** (`force-stop`), la vuelve a abrir y consulta el expediente. Salida real:

```
== 3) Cierre total de la app y reapertura ==
(app cerrada por completo)
== 4) El documento debe seguir en el expediente tras reabrir ==
aparecio: 'persistencia_semana9.pdf'
== OK: el documento 'persistencia_semana9.pdf' persistio tras cerrar y reabrir la app ==
```

Capturas en [`docs/evidencia_semana9/`](docs/evidencia_semana9/):

| Archivo | Contenido |
|---|---|
| `11_carga_guardada.png` | Diálogo de éxito al cargar el documento (folio #5). |
| `12_persistencia_tras_reinicio.png` | **Tras reabrir la app**, el expediente de `18.765.432-7` muestra `persistencia_semana9.pdf` junto a los 4 documentos semilla, leídos de SQLite. |

### **4.3 Los tests siguen en verde**

Los 32 tests unitarios siguen pasando (0 fallos). El repositorio y los ViewModels ahora se prueban inyectando un **doble de prueba en memoria** con el mismo contrato, sin Android ni base de datos real:

```bash
gradlew :app:testDebugUnitTest     # 32 tests, 0 fallos
./scripts/verificar_persistencia.sh   # evidencia en el emulador
```

---

## **5. Corrección de comportamiento (ciclo de la Meta 8)**

La primera ejecución de la verificación dio un resultado **falso**: el dump de la interfaz fallaba justo después de abrir la app (la UI aún no estaba lista) y un campo quedaba sin llenar, pero el script **continuaba en silencio** y capturaba una pantalla sin el documento nuevo. Correcciones aplicadas:

1. El helper `dump()` ahora **reintenta hasta 3 veces** y valida que el archivo descargado sea una jerarquía real.
2. El script usa `exigir …` en cada paso: si un elemento no aparece, **falla de forma ruidosa** en lugar de generar evidencia engañosa.
3. Se agregó una aserción explícita del estado del `CheckBox` de la declaración de autenticidad.

---

## **6. Estado frente a los puntos de la Semana 9**

| **Punto de la pauta** | **Estado** |
|---|---|
| Navegación funcional | ✅ NavHost con P01–P06 y paso de datos (Metas 5 y 6) |
| Formularios y validaciones | ✅ P05 (11 reglas) y P06 (comentario obligatorio) |
| Uso de ViewModel | ✅ Un ViewModel por pantalla; la lógica no vive en las Views |
| Gestión de estado | ✅ `StateFlow` + Snackbar/banner; el contador de pendientes de Home sale de datos persistidos |
| **Persistencia local** | ✅ **Esta entrega**: SQLite en el dispositivo |
| Visualización de lo guardado | 🔜 9.2 (P03/P04 ya leen de la base; falta el indicador de origen local) |
| Animaciones | 🔜 9.4 |
| Recurso nativo | 🔜 9.3 (cámara, RF-09) |
| Prueba del flujo principal | 🔜 se realiza en clase (tutorial en `docs/Tutorial_Demo_Flujo_Principal.md`) |

---

## **7. Conclusión**

La parte **9.1 queda cumplida**: el expediente de funcionarios se guarda en una base SQLite del dispositivo y se recupera tras cerrar y reabrir la aplicación, verificado tanto en la tabla como en pantalla mediante un script reproducible. La arquitectura mantiene la separación por capas, los 32 tests unitarios siguen pasando y el camino hacia Room queda abierto sin refactor del resto de la app.

**Próximos pasos:** 9.2 indicador/visualización de los datos guardados, 9.3 cámara como recurso nativo (RF-09), 9.4 animaciones con propósito.
