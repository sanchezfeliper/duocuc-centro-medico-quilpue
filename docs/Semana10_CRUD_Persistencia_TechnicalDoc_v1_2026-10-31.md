DSY1105 · Semana 10 · CRUD y Persistencia Local — CMQ Salud

# **Semana 10 · CRUD completo con persistencia local**

_El flujo exigido por la pauta: formulario → validación → guardar → listar → editar → eliminar → cerrar → reabrir → datos disponibles_

DSY1105 - Desarrollo de Aplicaciones Móviles  ·  CMQ Área Salud  ·  31-10-2026

---

## **1. Objetivo de la Semana 10**

La pauta pide cerrar el **flujo CRUD completo** sobre la persistencia local y demostrar la **prueba de persistencia real** (guardar → cerrar la app → reabrir → los datos continúan). Además revisa que sigan funcionando los contenidos de semanas anteriores (navegación, formularios, validaciones, ViewModel, estado, animaciones, recurso nativo y evidencia en GitHub).

Al iniciar la semana, el proyecto **ya cumplía**: Entity (modelo Kotlin), DAO funcional, base local, Repository, integración con ViewModel, corrutinas, formulario, validaciones, listar y persistencia real. Faltaban dos operaciones del CRUD: **editar** y **eliminar** desde la interfaz.

---

## **2. Estado de la semana: qué ya existía y qué faltaba**

| Punto de la pauta | Estado al iniciar | Acción de esta semana |
|---|---|---|
| Crear una Entity | Existía (modelo Kotlin `Documento` / `Funcionario`) | Ver equivalencia (§3) |
| Crear el DAO | Existía funcional (`DocumentoLocalDataSource`) | Ver equivalencia (§3) |
| Crear y configurar la base local | Existía (`CmqSqliteHelper`, SQLite) | Ver equivalencia (§3) |
| Crear un Repository | Existía (`DocumentRepository`) | Se le agregaron `actualizarDocumento` y `eliminarDocumento` |
| Integrar con el ViewModel | Existía | Se extendió para el modo edición |
| Usar corrutinas | Existía (`suspend`, `withContext(IO)`, `viewModelScope`) | Se mantiene |
| Formulario de registro | Existía (P05) | Se reutiliza en modo edición |
| Validaciones | Existían (12 reglas) | Se conservan; el modo edición las reutiliza |
| Mostrar los registros guardados | Existía (P03 Consulta) | Se mantiene |
| **Editar información** | **Faltaba** | **Implementado** (§4) |
| **Eliminar información** | **Faltaba** | **Implementado** (§4) |
| Comprobar persistencia real | Existía (9.1) | Se re-verifica en el flujo CRUD (§6) |

---

## **3. Equivalencia: Entity / DAO / Room Database**

La pauta nombra **Room** como motor de persistencia. El proyecto usa **SQLite del propio SDK de Android** (`SQLiteOpenHelper`), elegido en la Semana 9 porque Room/KSP no estaban configurados y sus versiones compatibles con este build (AGP 9.4.1 / Kotlin 2.2.10 / compileSdk 37) no eran verificables sin riesgo. La decisión se mantiene: **la capa de datos funciona y los tests pasan**, y la migración futura a Room no afecta al resto de la app porque todo depende de la **interfaz** `DocumentoLocalDataSource`.

Las tres piezas que Room pide son **equivalentes uno a uno** con las que ya existen:

| Concepto Room | Equivalente en CMQ Salud | Archivo |
|---|---|---|
| `@Entity` | Modelo de dominio Kotlin (`Documento`, `Funcionario`) mapeado a tabla | `domain/model/Documento.kt`, `data/local/Mapeadores.kt` |
| `@Dao` | `DocumentoLocalDataSource` (interfaz) + `SqliteDocumentoLocalDataSource` (implementación SQL) | `data/local/` |
| `@Database` (`RoomDatabase`) | `CmqSqliteHelper : SQLiteOpenHelper` | `data/local/CmqSqliteHelper.kt` |
| `@Insert` / `@Update` / `@Delete` / `@Query` | Métodos `insertarDocumento` / `actualizarDocumento` / `eliminarDocumento` / `buscar*` con `db.insert/update/delete/query` | `SqliteDocumentoLocalDataSource.kt` |
| `suspend` + `Dispatchers.IO` | Igual: todas las operaciones son `suspend` y corren en `withContext(Dispatchers.IO)` | `SqliteDocumentoLocalDataSource.kt` |

> **Consecuencia práctica:** el flujo `Interfaz → ViewModel → Repository → DAO → Base` que pide la pauta **existe tal cual**, solo que la "Base" es SQLite en lugar de Room. Si en el futuro se habilita Room, se reemplaza únicamente `SqliteDocumentoLocalDataSource` por un `@Dao`; ni los ViewModels, ni el Repository, ni las pantallas cambian.

---

## **4. Qué se implementó esta semana (Editar y Eliminar)**

**Flujo de edición:** P03 Consulta → P04 Detalle → botón **"Editar información"** → se abre el **formulario P05 en modo edición** (precargado con los datos del registro) → el usuario modifica → **"Guardar cambios"** → se actualiza el mismo registro (mismo folio) en la base local → vuelve al expediente con el dato actualizado.

**Flujo de eliminación:** P04 Detalle → **"Eliminar documento"** → **diálogo de confirmación** ("no se puede deshacer") → **"Eliminar"** → se borra el registro de la base local → vuelve al expediente; el registro ya no aparece.

| Archivo | Cambio |
|---|---|
| `data/local/DocumentoLocalDataSource.kt` | Nuevo método `eliminarDocumento(id): Boolean` en la interfaz. |
| `data/local/SqliteDocumentoLocalDataSource.kt` | Implementación con `db.delete(...)`; devuelve `false` si el ID no existía. |
| `data/DocumentRepository.kt` | Nuevos `actualizarDocumento(documento)` y `eliminarDocumento(id)` (la UI nunca toca la base directamente). |
| `ui/screens/carga/CargaUiState.kt` | Campos `idDocumentoEnEdicion` e `isEditando`. |
| `ui/screens/carga/CargaViewModel.kt` | Constructor con `idDocumentoEdicion` opcional; `precargar()` vuelca el registro en el formulario; `guardarDocumento()` **actualiza** si está en edición o **crea** si no; `factory()` para la navegación. |
| `ui/screens/carga/CargaDocumentoScreen.kt` | Título y botón según el modo ("Editar documento" / "Guardar cambios"); el diálogo de éxito cambia a "¡Documento Actualizado!". |
| `ui/screens/detalle/DetalleUiState.kt` · `DetalleViewModel.kt` | Estado y acciones de borrado (`abrirConfirmacionEliminar`, `cancelarEliminar`, `eliminarDocumento`). |
| `ui/screens/detalle/DetalleDocumentoScreen.kt` | Botones **Editar información** y **Eliminar documento** + diálogo de confirmación; al borrar, vuelve al expediente. |
| `ui/navigation/AppDestinos.kt` · `AppNavHost.kt` | Nueva ruta `carga/editar/{idDocumento}` y cableado de Editar/Eliminar. |
| `data/local/SemillaDocumentos.kt` | Los documentos ficticios de demostración ahora tienen nombre de archivo con extensión, descripción y ruta local coherentes (necesario para que un registro existente pueda editarse sin re-adjuntar el archivo). |

### Detalle de diseño (modo edición)
- **Se conserva la identidad del registro:** en edición se mantienen `idDocumento` (folio), `estado`, `fechaCarga`, `usuarioCarga` y el comentario del revisor; solo se actualizan los campos del formulario. Así editar no "reinicia" el ciclo de vida del documento.
- **La validación se reutiliza:** el modo edición pasa por las mismas reglas que el alta (RUT Módulo 11, extensión de archivo, descripción, autenticidad). No hay una segunda ruta de validación que mantener.
- **La categoría se reconstruye:** el campo de UI "Tipo de documento" es la **categoría** del catálogo (RN-08), mientras que `Documento.tipo` guarda el **formato** (PDF/JPG). Al precargar, la categoría se deduce del nombre/descripción del registro.
- **Delete con confirmación:** el borrado nunca ocurre en un solo toque; siempre pasa por un diálogo explícito (el dato vive en el dispositivo y no es recuperable en el MVP).

---

## **5. Tests unitarios**

Se sumaron **11 pruebas** (de 44 a **55**, todas en verde) sin emulador:

| Test | Qué verifica |
|---|---|
| `DocumentRepositoryTest` (+4) | Actualizar conserva el folio; eliminar borra y baja el conteo; eliminar/actualizar con ID inexistente devuelve `false`/`null`. |
| `CargaViewModelTest` (+3) | El modo edición precarga el registro; guardar en edición actualiza **el mismo folio** sin crear uno nuevo; sin modo edición se sigue creando un documento nuevo. |
| `DetalleViewModelTest` (+4) | El detalle carga; el borrado exige confirmación; confirmar borra de la fuente local; borrar un ID inexistente reporta error. |

Ejecución:

```bash
./gradlew :app:testDebugUnitTest
# 55 tests, 0 failures
```

---

## **6. Evidencia: flujo CRUD con persistencia real**

Script reproducible [`scripts/verificar_crud.sh`](scripts/verificar_crud.sh): parte de una app limpia (`pm clear`), recorre el flujo completo y **falla de forma ruidosa** (`exigir`) si un paso no ocurre, en vez de dejar una captura sin contenido.

Pasos que verifica:

1. **Home desde cero** → 4 documentos de la semilla.
2. **CREATE** → cargar en P05 → Home pasa a 5.
3. **READ** → P03 lista el documento → P04 Detalle lo abre.
4. **UPDATE** → P04 → Editar → cambiar la descripción → "Guardar cambios" → "¡Documento Actualizado!".
5. **Persistencia** → `force-stop` + reabrir → el cambio sigue visible.
6. **DELETE** → P04 → Eliminar → confirmar → vuelve al expediente.
7. **Base local** → conteo SQLite de vuelta a 4.

Capturas en `docs/evidencia_semana10/` (`40_` a `47_`).

---

## **7. Conclusión**

Con esta entrega el flujo exigido por la pauta queda **completo y verificable**:

```
Formulario → Validación → Repository → DataSource (DAO) → SQLite en el dispositivo
   ↑                                                              │
   └──────── pantalla ← ViewModel ← Repository ← DataSource ←────┘
                    (crear · listar · editar · eliminar · persistir)
```

Las operaciones que faltaban (editar y eliminar) están integradas con el ViewModel, usan corrutinas y sobreviven al cierre de la aplicación. La capa de datos mantiene una equivalencia directa con Entity/DAO/Room, de modo que la posible migración a Room queda acotada a un único archivo.
