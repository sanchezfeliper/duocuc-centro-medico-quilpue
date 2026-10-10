DSY1105 · Meta 7 · Relacionar el modelo de datos con Kotlin — CMQ Salud

# **Meta 7 - Relacionar el Modelo de Datos con Kotlin**

_Revisión del modelo relacional y reflejo de las entidades E1–E9 + catálogos como data classes: los datos diseñados son los mismos datos que usa la aplicación_

DSY1105 - Desarrollo de Aplicaciones Móviles · CMQ Área Salud · 10-10-2026

---

## **1. Objetivo de la Meta**

La Meta 7 exige que las **entidades principales del modelo de datos comiencen a reflejarse en el código**, sin implementar todavía persistencia con Room. Lo que se debe comprobar es que:

> **los datos que diseñamos (Modelo de Datos Preliminar + MER) son los mismos datos que está utilizando la aplicación.**

| **Atributo** | **Valor** |
|---|---|
| **Insumos revisados** | Modelo de Datos Preliminar (E1–E9 + catálogos) y `MER.png` (modelo relacional) |
| **Entidades reflejadas** | 9 principales + 2 catálogos → `data class` en `domain/model/` |
| **Persistencia** | No: sin Room ni Retrofit (consigna explícita); datos en memoria (RN-03) |
| **Arquitectura** | MVVM: las entidades viven en la capa de **dominio**, no en UI ni en los ViewModels |

---

## **2. Revisión del Modelo Relacional**

Antes de codificar se contrastó el modelo diseñado con lo que la app realmente usaba. La auditoría arrojó **dos brechas de coherencia**, corregidas en esta meta:

| **#** | **Brecha detectada** | **Detalle** | **Corrección aplicada** |
|---|---|---|---|
| B-1 | **E2 Funcionario incompleta** | El código solo usaba `idFuncionario, rut, nombreCompleto, cargo, centroSalud`; la entidad diseñada incluye además profesión, tipo de contrato, fecha de ingreso, fecha de nacimiento y situación contractual. | Se extendió `Funcionario` con los atributos faltantes como campos opcionales (el repositorio registra funcionarios "Por registrar" desde el formulario, aún sin mantención RF-05). |
| B-2 | **E4 Documento incompleta** | Faltaban `fechaVencimiento`, `comentarioValidacion`, `hashArchivo` y `urlSharePoint`, todos definidos en la entidad diseñada (RF-11, RN-05, RF-16/17, D-08). | Se extendió `Documento`; los campos son nullable porque se completan en etapas posteriores (hash al subir archivo real, URL al integrar SharePoint). |
| B-3 | **Estado adicional** | RF-17 ("solicitar corrección") requería un estado que el MER no contemplaba explícitamente. | Se documentó la extensión: `PENDIENTE_CORRECCION` se agrega al enum de estados de E4, con etiqueta legible "Pendiente corrección" y color de badge propio. |

**Hallazgo clave de la revisión:** el modelo diseñado y el código ya coincidían en lo esencial (nombres de atributos en español, claves `id*`, estados como enums acotados en dominio), porque las metas 1–6 se construyeron sobre `Documento` y `Funcionario` leyendo el modelo de datos. La Meta 7 cerró las brechas restantes y completó las entidades que aún no tenían representación.

---

## **3. Mapeo Entidad → Kotlin (E1–E9 + catálogos)**

Cada entidad del modelo de datos preliminar vive ahora como `data class` en `domain/model/`, una por archivo. Las fechas se manejan como `String` ISO (coherente con lo ya usado por `Documento.fechaCarga`); los estados como enums acotados, según la decisión de diseño del propio modelo ("estado como VARCHAR acotado por enums en la capa de dominio").

| **Entidad** | **Archivo Kotlin** | **Data class** | **Estado(s) como enum** | **RF asociados** |
|---|---|---|---|---|
| E1 Usuario | `Usuario.kt` | `Usuario` | `EstadoUsuario`, `Perfil` | RF-01, RF-05, RF-07 |
| E2 Funcionario | `Funcionario.kt` | `Funcionario` | — | RF-12, RF-13 |
| E3 TipoDocumento | `TipoDocumento.kt` | `TipoDocumento` | — | RF-10 |
| E4 Documento | `Documento.kt` | `Documento` | `EstadoDocumento` | RF-08–11, RF-14, RF-16/17 |
| E5 Permiso | `Permiso.kt` | `Permiso` | `EstadoPermiso` | RF-15–17 |
| E6 Capacitacion | `Capacitacion.kt` | `Capacitacion` | `EstadoCapacitacion` | RF-10, RF-15 |
| E7 Bitacora | `Bitacora.kt` | `Bitacora` | `AccionBitacora` | RF-18–20 |
| E8 Notificacion | `Notificacion.kt` | `Notificacion` | `TipoNotificacion` | RF-21–23 |
| E9 Sesion | `Sesion.kt` | `Sesion` | `EstadoSesion` | RF-01, RF-03, RF-04 |
| Catálogo Perfil | `Usuario.kt` | `Perfil` (enum con `codigo` y `nombre`) | — | RN-02, RF-07 |
| Catálogo CentroSalud | `CentroSalud.kt` | `CentroSalud` | `TipoCentroSalud` | E2/E1 |

---

## **4. Implementación**

### **4.1 Ejemplo de reflejo directo (E1 Usuario)**

Tal como lo pide la pauta de la meta, la tabla del modelo pasa a ser una data class:

```kotlin
// domain/model/Usuario.kt
data class Usuario(
    val idUsuario: Int,
    val rut: String,                  // UK, formato 12.345.678-9
    val nombreCompleto: String,
    val correoInstitucional: String,  // UK, correo Microsoft 365
    val idPerfil: Int,                // FK -> Perfil
    val idCentroSalud: Int? = null,   // FK -> CentroSalud (opcional según perfil)
    val estado: EstadoUsuario = EstadoUsuario.ACTIVO,
    val fechaCreacion: String? = null,
    val ultimaSesion: String? = null
)

enum class EstadoUsuario { ACTIVO, INACTIVO, BLOQUEADO }

/** Catálogo de perfiles del caso (RN-02): mínimo privilegio por rol. */
enum class Perfil(val codigo: String, val nombre: String) {
    FUN("FUN", "Funcionario"),
    ADM("ADM", "Personal administrativo"),
    JEF("JEF", "Jefatura de centro de salud"),
    RRHH("RRHH", "Recursos Humanos"),
    SYS("SYS", "Administrador del sistema"),
    TIC("TIC", "TI y Ciberseguridad")
}
```

Los comentarios de cada campo conservan la semántica del modelo: clave (PK/UK/FK), tipo diseñado y regla de negocio asociada, de modo que el mapeo sea verificable línea a línea contra el documento.

### **4.2 E4 Documento: la entidad que la app ya usa, ahora completa**

```kotlin
// domain/model/Documento.kt
data class Documento(
    val idDocumento: Int,
    val idFuncionario: Int,             // FK -> Funcionario (E2)
    val rutFuncionario: String,         // dato del formulario (Meta 6)
    val tipo: String,                   // PDF, JPG, PNG
    val nombreArchivo: String,
    val estado: EstadoDocumento,
    val fechaCarga: String,             // ISO yyyy-MM-dd
    val usuarioCarga: String,           // responsable de la carga (RN-08)
    val descripcion: String? = null,
    // Atributos E4 incorporados en Meta 7
    val fechaVencimiento: String? = null,      // política pendiente (D-08)
    val comentarioValidacion: String? = null,  // comentario del revisor (RF-16/RF-17)
    val hashArchivo: String? = null,           // SHA-256 duplicados (RF-11)
    val urlSharePoint: String? = null          // referencia única (RN-05, simulada)
)

enum class EstadoDocumento {
    VIGENTE, PENDIENTE, APROBADO, RECHAZADO, PENDIENTE_CORRECCION, VENCIDO;

    val etiqueta: String
        get() = when (this) {
            PENDIENTE_CORRECCION -> "Pendiente corrección"
            else -> name.lowercase().replaceFirstChar { it.uppercase() }
        }
}
```

### **4.3 E7 Bitácora: trazabilidad lista para P08**

```kotlin
// domain/model/Bitacora.kt
data class Bitacora(
    val idBitacora: Int,
    val idUsuario: Int,            // FK -> Usuario (quien realizó la acción)
    val idDocumento: Int? = null,  // FK -> Documento (nullable)
    val accion: AccionBitacora,
    val recurso: String? = null,
    val ipOrigen: String? = null,
    val dispositivo: String? = null,
    val fechaHora: String          // no modificable (RNF-03)
)

enum class AccionBitacora { LOGIN, CARGA, CONSULTA, MODIFICACION, DESCARGA, RECHAZO }
```

### **4.4 Regla de diseño aplicada**

- **Sin persistencia**: ninguna entidad tiene DAO ni tabla Room; el repositorio sigue en memoria con datos ficticios (RN-03), según la consigna.
- **FK como `Int`/`Int?`**: igual que en el modelo (claves enteras autoincrementales).
- **Enums para estados**: impide estados inválidos y centraliza etiquetas legibles (`etiqueta`) usadas por los badges de P03, P04, P05 y P06.
- **Campos opcionales = datos aún no gestionados**: lo nullable marca exactamente lo que llegará con Room/SharePoint (hash, URL, vigencias, datos laborales), sin inventar valores (D-08, D-10).

---

## **5. Uso real en la aplicación (la comprobación de la meta)**

Las entidades del dominio **no son decorativas**: son el tipo de dato que circula por toda la app. En esta misma iteración se implementó además la pantalla **P06 Validación (RF-15 a RF-17)** sobre el repositorio, cerrando el circuito del modelo:

```
P05 Carga (guardarDocumento: Documento)          -> E4 con estado PENDIENTE
P06 Validacion (listarPendientes: List<Documento>)  -> consulta por estado
P06 Validacion (registrarDecision: Documento)       -> PENDIENTE -> APROBADO / RECHAZADO / PENDIENTE_CORRECCION + comentarioValidacion
P03 Consulta / P04 Detalle                          -> muestran el documento ya validado con su comentario
```

El dato diseñado como `Documento.comentarioValidacion` (E4) existe en el modelo **y** lo que escribe la jefatura en P06 **y** lo que muestra P04: un solo tipo, `Documento`, en las tres pantallas. Lo mismo ocurre con `Funcionario` (E2): el RUT ingresado en P05 registra o reutiliza al funcionario del repositorio, y P03 muestra su resumen desde esa entidad.

---

## **6. Casos de Prueba**

| **Caso** | **Pasos** | **Resultado esperado** | **Estado** |
|---|---|---|---|
| **CP-01** | Compilar el proyecto (`compileDebugKotlin`) | Las 12 entidades/enums del dominio compilan sin errores. | **Aprobado** |
| **CP-02** | Verificar el mapeo E1–E9 contra el modelo de datos | Cada atributo del modelo tiene su campo Kotlin con el mismo nombre y tipo; los comentados conservan la semántica (UK/FK/nullable). | **Aprobado** |
| **CP-03** | Cargar documento en P05 con derivación → abrir P06 | El documento (E4) aparece en la bandeja de pendientes del repositorio compartido. | **Aprobado** |
| **CP-04** | En P06: aprobar / rechazar / solicitar corrección | `EstadoDocumento` cambia a `APROBADO` / `RECHAZADO` / `PENDIENTE_CORRECCION`; `comentarioValidacion` queda registrado (obligatorio en rechazo y corrección). | **Aprobado** |
| **CP-05** | Abrir P04 Detalle de un documento validado | Muestra el bloque "COMENTARIO DEL REVISOR" y el badge del nuevo estado con etiqueta legible. | **Aprobado** |
| **CP-06** | Volver a Home tras decidir en P06 | El contador "Validaciones pendientes" se refresca desde `listarPendientes()` (datos reales del repositorio, ya no un valor fijo). | **Aprobado** |
| **CP-07** | Consultar expediente en P03 con filtro "Pendientes" | Incluye tanto `PENDIENTE` como `PENDIENTE_CORRECCION` (ambos esperan gestión). | **Aprobado** |

---

## **7. Respuestas a la Guía Meta 7**

| **Pregunta** | **Respuesta del Equipo CMQ Salud** |
|---|---|
| **¿Las entidades del modelo están reflejadas en el código?** | Sí: E1–E9 + los catálogos Perfil y CentroSalud son `data class`/`enum class` en `domain/model/`, una por archivo, con claves y reglas documentadas en cada campo. |
| **¿Se revisó el modelo relacional (MER)?** | Sí: la revisión detectó 2 entidades incompletas en código (E2 y E4) y 1 estado faltante (RF-17), todas corregidas y documentadas en la sección 2. |
| **¿Los datos diseñados son los mismos que usa la aplicación?** | Sí: `Documento` y `Funcionario` son los tipos concretos que circulan entre P03, P04, P05 y P06 vía `DocumentRepository`; no hay objetos paralelos ni duplicados de estado. |
| **¿Se implementó persistencia?** | No, conforme a la consigna: sin Room. Las entidades son el contrato del dominio y están listas para mapearse a Room (y a Retrofit) en la siguiente iteración. |
| **¿Dónde vive cada entidad?** | En la capa de **dominio** (`domain/model/`), consumida por `data/` (repositorio) y `ui/` (ViewModels). Ningún ViewModel ni pantalla define su propio "documento" o "usuario": un solo modelo para toda la app. |

---

## **8. Conclusión y Próximos Pasos**

La **Meta 7 queda cumplida**: el modelo de datos diseñado (9 entidades + 2 catálogos) está ahora íntegramente reflejado en Kotlin como data classes del dominio, la revisión del MER corrigió las brechas entre diseño y código (E2 y E4 ampliadas, estado RF-17 documentado), y la comprobación es verificable en ejecución: el mismo `Documento` que se crea en el formulario es el que la bandeja de jefatura valida y el detalle muestra. En paralelo se implementó la pantalla P06 Validación (RF-15 a RF-17), que es el primer consumidor completo del ciclo de estados de E4.

**Próximos pasos para siguientes entregas:**

1. **Room**: mapear `Documento` y `Funcionario` a tablas + DAO (la data class ya coincide con la estructura diseñada, el mapeo será directo).
2. **Sesión (E9) y Usuario (E1)**: conectar el login a `AuthRepository` usando las entidades, reemplazando la autenticación simulada.
3. **Bitácora (E7)**: registrar automáticamente las acciones de P05/P06 (CARGA, CONSULTA, MODIFICACION) para alimentar P08 Bitácora (RF-18).
4. **Notificación (E8)**: generar la alerta al funcionario cuando su documento sea aprobado/rechazado en P06 (RF-22), antes de integrar push real.
