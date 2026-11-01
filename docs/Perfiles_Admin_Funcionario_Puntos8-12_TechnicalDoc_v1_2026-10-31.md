DSY1105 · Separación de perfiles admin/funcionario · Puntos 8–12 — CMQ Salud

# **Separación de perfiles · Puntos 8 a 12 (cierre)**

_Las reglas de mínimo privilegio (RN-02 / RF-07) aplicadas de extremo a extremo: expediente propio, gestión acotada de documentos y validación reservada a administración._

DSY1105 - Desarrollo de Aplicaciones Móviles · CMQ Área Salud · 31-10-2026

---

## **1. Objetivo de estos puntos**

La serie de 12 puntos dividió la app en **dos perfiles operativos**: **ADM** (personal administrativo) y **FUN** (funcionario). Los puntos **1 a 7** (ya fusionados en `main` vía PR #23) construyeron la capa de dominio, la sesión, el login con perfil, la guardia de navegación, el Home por rol, la pantalla Perfil y el RUT fijo en el formulario de carga.

Estos puntos cierran la separación **aplicando los permisos en las tres pantallas que faltaban** (Expediente, Detalle y Validación) y **probándolos**. El principio transversal es la **defensa en profundidad**: ocultar un botón no es una medida de seguridad; cada ViewModel vuelve a exigir el permiso antes de ejecutar la acción sensible.

Puntos cubiertos aquí:

| Punto | Contenido | Estado |
|---|---|---|
| 8 | Expediente con RUT fijo para el funcionario (RF-12) | Implementado |
| 9 | Detalle: editar/eliminar según permiso (RF-07) | Implementado |
| 10 | Validación reservada a administración (RF-15) | Implementado |
| 11 | Tests de los puntos 8, 9 y 10 | +12 tests (110 totales) |
| 12 | Documentación técnica | Este documento |

---

## **2. Regla de negocio que se aplica**

La matriz de permisos ya vivía en el dominio desde el punto 1 (`domain/permissions/PermisosPorPerfil.kt`). Estos puntos la consumen:

| Regla | ADM | FUN |
|---|---|---|
| Buscar el expediente de cualquier RUT (`puedeBuscarOtrosRuts`, RF-12/RF-13) | Sí | **No** (solo el suyo) |
| Editar/eliminar un documento (`puedeEditarDocumento`) | Cualquiera | **Solo propio y en estado PENDIENTE / PENDIENTE_CORRECCION** |
| Validar / revisar documentos (`puedeValidar`, RF-15) | Sí | **No** |

Los perfiles JEF, RRHH, SYS y TIC siguen sin privilegios (fuera del alcance del MVP, tal como se declaró en el punto 1): el día que se activen, sus reglas se definen explícitamente.

---

## **3. Punto 8 — Expediente con RUT fijo para el funcionario (RF-12)**

**Problema:** `ConsultaViewModel.onBuscar()` aceptaba cualquier RUT escrito por el usuario. Un funcionario podía consultar el expediente de un tercero, contradiciendo RF-12 ("el funcionario consulta el suyo") y el mínimo privilegio (RN-02).

**Solución:**

- `ConsultaUiState` gana el campo `rutFijo: Boolean`.
- `ConsultaViewModel` recibe la sesión por constructor y deriva `rutFijo = !PermisosPorPerfil.puedeBuscarOtrosRuts(perfil) && rut.isNotBlank()`.
- Con RUT fijo: el estado arranca con el RUT de la sesión, se hace **una búsqueda automática en el `init`** (el funcionario entra directo a su expediente) y `onRutChange()` **ignora** cualquier intento de cambio.
- `onBuscar()` revalida (defensa en profundidad): resuelve siempre contra el RUT de la sesión, nunca contra el del estado, de modo que un `onBuscar()` forzado sigue apuntando al expediente propio.
- En `ConsultaExpedienteScreen`, el campo RUT queda `readOnly = state.rutFijo` y el encabezado cambia a **"Mi expediente"**.
- Administración conserva el comportamiento original: campo editable, sin búsqueda automática, puede consultar cualquier RUT (RF-13).

---

## **4. Punto 9 — Detalle: editar/eliminar según permiso (RF-07)**

**Problema:** los botones **"Editar información"** y **"Eliminar documento"** de P04 se mostraban siempre, a cualquier perfil y sobre cualquier estado.

**Solución:**

- `DetalleUiState` gana `puedeEditar` / `puedeEliminar: Boolean`.
- `DetalleViewModel` recibe la sesión; al cargar el documento calcula ambos permisos con `PermisosPorPerfil.puedeEditarDocumento` / `puedeEliminarDocumento` (estado del documento + propiedad + perfil).
- **Doble revalidación del borrado:** `abrirConfirmacionEliminar()` exige el permiso sobre el documento cargado; `eliminarDocumento()` **vuelve a leer el documento del repositorio y revalida** antes de borrar — así, si entre abrir el diálogo y confirmar otro perfil aprobó el documento, el permiso se retira y el borrado se rechaza con un mensaje.
- En `DetalleDocumentoScreen`, los botones se renderizan **solo si el permiso lo permite**; si no hay ninguna acción disponible, se muestra el aviso "Este documento no admite cambios: ya fue revisado o no pertenece a tu expediente."
- Sin sesión (`sesion = null`) no se ofrece ninguna acción de gestión.

Resultado por perfil:

| Documento | ADM | FUN (dueño) | FUN (ajeno) |
|---|---|---|---|
| PENDIENTE / PENDIENTE_CORRECCION propio | Edita y elimina | Edita y elimina | No |
| APROBADO / RECHAZADO / VENCIDO | Edita y elimina | No | No |
| VIGENTE | Edita y elimina | No | No |

---

## **5. Punto 10 — Validación reservada a administración (RF-15)**

**Problema:** la ruta `revision` ya estaba protegida en el grafo de navegación (punto 4), pero el `ValidacionViewModel` no revalidaba el permiso: si el destino se alcanzaba por otra vía, la bandeja y las decisiones seguían operativas.

**Solución:**

- `ValidacionViewModel` recibe la sesión y expone `puedeValidar = PermisosPorPerfil.puedeValidar(perfil)`.
- `cargarPendientes()`: sin permiso **no carga datos de la bandeja** y expone `errorPermiso`.
- `decidir()`: sin permiso **no registra ninguna decisión** y devuelve un mensaje.
- `ValidacionUiState` gana `errorPermiso: String?`; `ValidacionScreen` lo muestra como estado **"Acceso restringido"**.

Esto complementa la guardia del grafo (`AppNavHost`) y el `AccesoDenegado` de la ruta: tres capas (navegación, pantalla, ViewModel) para una misma regla.

---

## **6. Punto 11 — Pruebas (110 tests, 0 fallos)**

Se sumaron **12 casos** a la suite JVM (sin emulador), que pasó de 98 a **110 tests**:

| Archivo | Casos nuevos | Qué verifican |
|---|---|---|
| `ConsultaViewModelTest` | 4 | FUN entra con RUT fijo y expediente cargado; no puede cambiar el RUT; no puede buscar a un tercero; ADM mantiene el RUT editable |
| `DetalleViewModelTest` | 7 | ADM edita/elimina cualquiera; FUN edita/elimina lo propio pendiente; FUN **no** puede editar un aprobado ni eliminar un vigente; abrir confirmación sin permiso no habilita el diálogo; `eliminarDocumento()` forzado no borra; sin sesión no hay acciones |
| `ValidacionViewModelTest` | 1 | Un perfil sin facultad no carga la bandeja ni registra decisiones (+ se adaptaron los 6 casos previos a una sesión ADM) |

**Comando:** `gradlew :app:testDebugUnitTest` → `BUILD SUCCESSFUL`, 110 tests, 0 fallos, 0 errores.
**Compilación de la app:** `gradlew :app:assembleDebug` → `BUILD SUCCESSFUL`.

---

## **7. Archivos tocados**

| Archivo | Cambio |
|---|---|
| `ui/screens/consulta/ConsultaUiState.kt` | + `rutFijo` |
| `ui/screens/consulta/ConsultaViewModel.kt` | Sesión; RUT fijo, búsqueda automática y revalidación |
| `ui/screens/consulta/ConsultaExpedienteScreen.kt` | Campo `readOnly`; encabezado "Mi expediente" |
| `ui/screens/detalle/DetalleUiState.kt` | + `puedeEditar` / `puedeEliminar` |
| `ui/screens/detalle/DetalleViewModel.kt` | Sesión; cálculo y doble revalidación de permisos |
| `ui/screens/detalle/DetalleDocumentoScreen.kt` | Botones condicionados al permiso; aviso cuando no hay acciones || `ui/screens/validacion/ValidacionUiState.kt` | + `errorPermiso` |
| `ui/screens/validacion/ValidacionViewModel.kt` | Sesión; bandeja y decisión condicionadas al permiso |
| `ui/screens/validacion/ValidacionScreen.kt` | Estado "Acceso restringido" |
| `test/…/ConsultaViewModelTest.kt`, `DetalleViewModelTest.kt`, `ValidacionViewModelTest.kt` | +12 casos |

---

## **8. Cómo probarlo en el emulador**

| Escenario | Cuenta demo | Resultado esperado |
|---|---|---|
| Funcionario consulta su expediente | `18.765.432-7` / `funcionario123` | Expediente se abre solo; RUT de solo lectura; encabezado "Mi expediente" |
| Funcionario abre su documento pendiente | misma cuenta | Botones Editar/Eliminar visibles |
| Funcionario abre su documento aprobado | misma cuenta | Sin botones de gestión; aviso "no admite cambios" |
| Funcionario intenta entrar a Revisión | misma cuenta | Toast / "Acceso no autorizado" (sin bandeja) |
| Admin revisa documentos | `12.345.678-9` / `admin123` | Bandeja de revisión operativa; aprueba/rechaza |
| Admin consulta cualquier RUT | misma cuenta | Campo editable; expediente del RUT buscado |

---

## **9. Alcance declarado (fuera de estos puntos)**

No implementado en el MVP y sin impacto en lo anterior: perfiles JEF/RRHH/SYS/TIC reales, aviso de vencimiento próximo (RF-23), notificaciones push (P07), bitácora de auditoría (P08) y persistencia de la sesión entre reinicios (RF-02 exige limpiarla al cerrar).
