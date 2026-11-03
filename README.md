# CMQ Salud

**Gestión documental móvil para funcionarios — CMQ Área Salud**

Aplicación Android que centraliza la carga, consulta y validación de documentos de los funcionarios de la Corporación Municipal de Quilpué (CMQ), Área Salud, con control de acceso por perfil y persistencia local en el dispositivo.

> Proyecto académico de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** · Sede CITT Quilpué.
> Todos los datos son ficticios (RN-03): la app no contiene información real de personas.

---

## El problema que resuelve

La CMQ no tiene un repositorio centralizado para la documentación de sus funcionarios. Los documentos viven dispersos en correos, planillas Excel y carpetas, sin trazabilidad ni control de acceso, lo que dificulta verificar su vigencia y expone la organización a riesgos de seguridad y de cumplimiento en protección de datos.

Esta app entrega un MVP que resuelve ese núcleo: cargar un documento, consultarlo por expediente, validarlo (aprobarlo o rechazarlo) y controlar quién puede ver qué según su rol.

---

## Funcionalidades principales

| Área | Qué hace |
|---|---|
| **Autenticación** | Ingreso con RUT y contraseña; la sesión determina el perfil activo. |
| **Home por rol** | El resumen cambia según el perfil: el funcionario ve su expediente, administración ve la bandeja de revisión. |
| **Carga de documentos** | Formulario con validaciones (RUT Módulo 11, campos obligatorios, catálogo de tipos), adjunto desde el dispositivo y captura con cámara. |
| **Consulta de expediente** | Listado y búsqueda de documentos; el funcionario solo ve los suyos, administración puede buscar otros RUT. |
| **Detalle y CRUD** | Ver un documento, editarlo o eliminarlo con confirmación. |
| **Validación (bandeja)** | Aprobar o rechazar con comentario, reservado a los perfiles con permiso de revisión. |
| **Perfil** | Datos de la sesión activa y cierre de sesión. |
| **Persistencia** | Los documentos sobreviven al cierre de la app (Room sobre SQLite). |

---

## Perfiles y cuentas de demostración

La autenticación es simulada (supuesto S-01): mientras no exista backend institucional, la app reconoce dos cuentas ficticias definidas en `domain/validation/CatalogoCuentasDemo.kt`.

| Perfil | RUT | Contraseña | Puede |
|---|---|---|---|
| Personal administrativo (ADM) | `12.345.678-9` | `admin123` | Validar, buscar cualquier expediente, cargar para otro RUT |
| Funcionario (FUN) | `18.765.432-7` | `funcionario123` | Cargar y consultar **solo** su propio expediente |

El MVP activa **ADM** y **FUN**. El modelo reconoce además JEF, RRHH, SYS y TIC (existen en el catálogo para futuras iteraciones), pero hoy se tratan como perfiles sin privilegios.

> Las contraseñas son valores de demostración y viven únicamente en el código. No hay credenciales reales en el repositorio (RN-03).

---

## Arquitectura

El proyecto sigue **MVVM** con separación estricta por capas: cada capa solo conoce a la que está inmediatamente debajo.

```
UI (Jetpack Compose)   →  ui/screens/*, ui/components/*, ui/theme/*
        ↓ eventos / ↑ estado
ViewModel              →  ui/screens/*/*ViewModel.kt      (estado + lógica)
        ↓
Domain                 →  domain/model/*, domain/validation/*, domain/permissions/*
        ↓
Data (Repository)      →  data/DocumentRepository.kt
        ↓
Data source            →  data/local/RoomDocumentoLocalDataSource.kt
        ↓
Room (RNF-14)          →  data/room/database/, data/room/dao/, data/room/entities/
```

**Principios de diseño aplicados:**

- **Estado que baja, eventos que suben.** La UI es "tonta": observa un `StateFlow` y emite eventos al ViewModel. Toda la lógica es testeable sin emulador.
- **Mínimo privilegio (RN-02).** Cada perfil accede solo a lo que necesita, y la regla se evalúa en el dominio (`PermisosPorPerfil`), no solo ocultando botones.
- **Defensa en profundidad.** Ocultar un botón no es seguridad: las rutas de navegación y los ViewModels vuelven a validar el permiso antes de actuar.
- **Datos ficticios (RN-03).** Ninguna información real de personas en el repositorio.

### Estructura de carpetas

```
app/src/main/java/cl/cmq/salud/
├── data/               # Repositorio y fuentes de datos
│   ├── room/           # Room: database, dao, entities
│   └── local/          # Data sources, semilla, almacén de fotos
├── di/                 # AppContainer (inyección manual de dependencias)
├── domain/             # Modelo, validaciones, permisos, sesión
└── ui/                 # Compose: screens, components, navigation, theme
```

---

## Navegación

El grafo vive en `ui/navigation/AppNavHost.kt` y las rutas en `AppDestinos.kt`.

- **Barra inferior compartida** (`ui/components/BottomNavBar.kt`): los botones cambian según el perfil.

  | Perfil | Barra |
  |---|---|
  | Funcionario | Inicio · Expediente · Cargar · **Perfil** |
  | Administración | Inicio · Expediente · Cargar · **Revisión** · **Perfil** |

- **Un solo mapeo índice → ruta**, centralizado en `BottomNavItem.destinoDe(...)`. Las pantallas reenvían el índice tocado al grafo; ninguna mantiene su propia tabla de rutas.
- **Guardias:** sin sesión no se entra a ninguna pantalla interna y los destinos reservados (Revisión) exigen el permiso del perfil.

---

## Tecnologías

| Componente | Versión / detalle |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Navegación | Navigation Compose |
| Persistencia | Room (RNF-14) sobre SQLite |
| Cámara | CameraX (RF-09) |
| Concurrencia | Corrutinas + `StateFlow` |
| Compilación | Gradle (Kotlin DSL) + KSP para Room |
| `minSdk` / `targetSdk` / `compileSdk` | 26 / 37 / 37 |

---

## Cómo ejecutar

### Requisitos

- Android Studio (versión reciente, compatible con AGP 9 y KSP)
- JDK 11 o superior
- Un emulador o dispositivo con Android 8.0 (API 26) o superior

### Pasos

```bash
# 1. Clonar
git clone https://github.com/sanchezfeliper/duocuc-centro-medico-quilpue.git
cd duocuc-centro-medico-quilpue

# 2. Compilar
./gradlew :app:compileDebugKotlin        # En Windows: gradlew.bat

# 3. Instalar y ejecutar en el emulador
./gradlew :app:installDebug

# 4. (Opcional) Ejecutar las pruebas unitarias
./gradlew :app:testDebugUnitTest
```

Luego abre la app e inicia sesión con una de las cuentas de demostración de la tabla de arriba.

> **Nota:** `local.properties` apunta al SDK local y no se versiona. Android Studio lo regenera al abrir el proyecto.

---

## Pruebas

- **121 pruebas unitarias** (JVM, sin emulador) en `app/src/test/`. Cubren validaciones (RUT Módulo 11), el repositorio, los ViewModels y la composición de la barra de navegación.
- Las pruebas de **DAO sobre Room** viven en `app/src/androidTest/` y requieren un emulador o dispositivo.

```bash
./gradlew :app:testDebugUnitTest                          # unitarias (JVM)
./gradlew :app:connectedDebugAndroidTest                   # instrumentadas (requiere emulador)
```

El informe HTML de las unitarias queda en `app/build/reports/tests/testDebugUnitTest/index.html`.

---

## Documentación del proyecto

En `docs/` hay documentación técnica por etapa (contexto, requerimientos, alcance, metas, semana 9-10, guías de estudio). Los documentos de análisis base son:

- `docs/01_Contexto_y_Problema.md` — problema, actores y reglas de negocio.
- `docs/02_Requerimientos.md` — requisitos funcionales (RF) y no funcionales (RNF).
- `docs/03_Alcance.md` — qué entra y qué queda fuera del MVP.

---

## Reglas de negocio y alcance

- **RN-02 – Mínimo privilegio:** cada perfil accede solo a lo necesario.
- **RN-03 – Datos ficticios:** el repositorio no contiene información real de personas.
- **RF-02 – Cierre de sesión:** los datos sensibles se limpian de la memoria al salir.

**Fuera del MVP:** backend institucional real, autenticación con proveedor de identidad, bitácora de auditoría (P08), sincronización con SharePoint y cálculo de documentos vencidos por fecha.
