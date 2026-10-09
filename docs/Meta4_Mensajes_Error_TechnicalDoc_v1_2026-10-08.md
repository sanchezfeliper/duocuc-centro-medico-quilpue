DSY1105 · Meta 4 · Mostrar errores correctamente — Formulario P05 Carga de Documento

# **Meta 4 - Mostrar Errores Correctamente**

_P05: Carga de Documento — Feedback visible garantizado en todo el ciclo del formulario_

DSY1105 - Desarrollo de Aplicaciones Móviles  ·  CMQ Área Salud  ·  08-10-2026

---

## **1. Selección y Justificación**

La Meta 4 exige eliminar los formularios donde el usuario presiona Guardar y **no ocurre nada**: ante cualquier error debe existir una **respuesta visible**, y el flujo completo debe ser siempre:

```
dato incorrecto ──> mensaje específico ──> corrección ──> formulario válido
```

Se aplica nuevamente al formulario **FORM-P05: Carga de Documento**, construyendo sobre las validaciones de la Meta 3. El foco de esta meta no es validar más, sino **comunicar mejor**: cada estado del formulario (error por campo, error global, envío en curso, registro exitoso) tiene ahora una señal visual inequívoca, y ningún intento de guardado queda sin respuesta.

| **Atributo** | **Valor** |
|---|---|
| **ID Formulario** | FORM-P05 |
| **Pantalla asociada** | P05 - Carga de documento (`CargaDocumentoScreen.kt`) |
| **Requerimientos** | RF-08, RF-10, RF-11, RN-08 |
| **Arquitectura** | MVVM + Jetpack Compose + StateFlow (UDF) |

---

## **2. Mecanismos de Feedback Utilizados (Pauta Meta 4)**

La pauta sugiere `isError`, `supportingText`, texto condicional, `Snackbar` y mensajes de éxito o error. **Se implementaron los cinco**, cada uno con un rol distinto dentro del mismo flujo:

| **#** | **Mecanismo** | **Dónde** | **Rol** |
|---|---|---|---|
| F-01 | `isError = true` | Los 5 campos de entrada | Pinta en rojo el borde/indicador del campo con problema: la señal se ve aunque el usuario no lea texto. |
| F-02 | `supportingText` condicional | Los 5 campos de entrada | Mensaje específico **bajo el campo**: qué está mal y cómo corregirlo. Cuando el campo es correcto muestra ayuda preventiva (pista de formato, contador). |
| F-03 | **Texto condicional — banner resumen** | Entre el checkbox y los botones | Tarjeta roja que aparece solo si hubo fallo, con el mensaje general y la **lista nominal de los campos a revisar** (ej: "• RUT Funcionario", "• Descripción"). Resuelve el caso de campos fuera de la vista: el usuario no tiene que buscar cuál falló. |
| F-04 | **Snackbar de error** | Parte inferior de la pantalla | Confirmación adicional e inmediata en cada intento fallido: "No se pudo registrar el documento: revise los campos marcados en rojo", con acción "Entendido". Se dispara **en cada** guardado fallido, aunque los errores se repitan. |
| F-05 | **Mensaje de éxito** | `AlertDialog` | Tras un guardado válido: folio, archivo, tipo, estado y fecha. Nunca hay silencio ante una operación exitosa. |
| F-06 | **Snackbar de éxito** | Parte inferior de la pantalla | Al elegir "Cargar otro": confirma "Documento #N registrado correctamente" mientras el formulario se reinicia. |

### **2.1 Complemento: señal en el checkbox de autenticidad**

El componente `Checkbox` de Material 3 no tiene parámetro `isError`; la retroalimentación equivalente se logró con **texto condicional**: el texto de la declaración y su mensaje de error se pintan de rojo (`color = Red`) cuando `autenticidadError != null`.

---

## **3. El Flujo Correcto, Paso a Paso (Demostración)**

Escenario: el usuario intenta guardar con el RUT mal formateado y sin descripción.

1. **Dato incorrecto** → el usuario toca [Guardar y Registrar Documento].
2. **Respuesta inmediata en 4 niveles simultáneos**:
   * El campo RUT se pinta rojo y debajo muestra: *"Formato de RUT inválido: escríbalo como 18.765.432-7 (puntos miles, guión y dígito verificador, sin espacios)."*
   * El campo Descripción se pinta rojo con su mensaje específico.
   * Aparece el **banner resumen** encima de los botones: *"No se pudo registrar el documento: 2 campo(s) requieren corrección…"* + *"Campos a revisar: • RUT Funcionario • Descripción / Observaciones"*.
   * Se despliega el **Snackbar**: *"No se pudo registrar el documento: revise los campos marcados en rojo [Entendido]"*.
   * **Nada se envía al repositorio** (el `return` en el ViewModel lo garantiza).
3. **Corrección** → mientras el usuario escribe, cada campo se revalida en vivo: el error del RUT **desaparece solo** al quedar válido, sin borrar el resto del formulario (los datos ya ingresados nunca se pierden).
4. **Formulario válido** → al guardar de nuevo no hay errores; se muestra `CircularProgressIndicator` en el botón, se procesa y aparece el **AlertDialog de éxito** con el folio. Desde "Cargar otro", el Snackbar confirma el registro y el formulario queda limpio.

El ciclo queda cerrado y observable en cada una de sus etapas: no existe ningún camino desde "Guardar" que termine sin respuesta visible.

### **3.1 Detalle técnico que evita el snackbar "fantasma"**

Un `LaunchedEffect` común quedaría keyeado al mensaje de error y **no se dispararía dos veces si el usuario vuelve a guardar con los mismos errores** (mismo string, misma key). Para garantizar respuesta en **cada** intento, el estado lleva un contador de eventos:

```kotlin
// CargaUiState — eventos únicos, no mensajes
val errorEventId: Long = 0,
val successEventId: Long = 0,
val ultimoFolioRegistrado: Int? = null,
```

```kotlin
// CargaViewModel — cada fallo incrementa el contador
_uiState.update {
    it.copy(
        ..., camposConError = campos,
        generalError = "No se pudo registrar el documento: ${errores.size} campo(s) requieren corrección. " +
            "Cada mensaje en rojo indica qué está mal y cómo corregirlo.",
        errorEventId = it.errorEventId + 1
    )
}
```

```kotlin
// CargaDocumentoScreen — la UI reacciona al evento, no al texto
val snackbarHostState = remember { SnackbarHostState() }

LaunchedEffect(state.errorEventId) {
    if (state.errorEventId > 0) {
        snackbarHostState.showSnackbar(
            message = "No se pudo registrar el documento: revise los campos marcados en rojo",
            actionLabel = "Entendido",
            duration = SnackbarDuration.Short
        )
    }
}

LaunchedEffect(state.successEventId) {
    if (state.successEventId > 0) {
        snackbarHostState.showSnackbar(
            message = "Documento #${state.ultimoFolioRegistrado ?: "-"} registrado correctamente",
            duration = SnackbarDuration.Short
        )
    }
}
```

Este patrón separa **estado** (lo que la pantalla dibuja: errores por campo, banner) de **eventos** (lo que ocurre una sola vez: snackbar), práctica recomendada en arquitectura UDF.

### **3.2 Banner resumen de campos (texto condicional)**

```kotlin
if (state.generalError != null) {
    Card(colors = CardDefaults.cardColors(containerColor = BannerErrorBg),
         shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(state.generalError ?: "", color = Red, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            if (state.camposConError.isNotEmpty()) {
                Text("Campos a revisar:", color = Red, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                state.camposConError.forEach { campo ->
                    Text("• $campo", color = Red, fontSize = 10.sp, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
```

La lista `camposConError` la construye el ViewModel en el fallo (ej: `["RUT Funcionario", "Descripción / Observaciones"]`), de modo que el usuario sepa **qué campos buscar** aunque estén fuera de la pantalla. El ViewModel también limpia los errores del estado al registrar con éxito, evitando que la señal roja sobreviva al guardado.

---

## **4. Mapa Completo de Respuestas Visibles del Formulario**

| **Situación del usuario** | **Respuesta visible** |
|---|---|
| Guarda con N campos inválidos | Snackbar de error + banner con lista de campos + cada campo en rojo con mensaje específico |
| Escribe un valor inválido | El campo se pinta rojo con mensaje en vivo (sin tocar Guardar) |
| Corrige el valor | El error desaparece en vivo; si la descripción queda válida vuelve el contador preventivo |
| Guarda con todo válido | Indicador de progreso → AlertDialog de éxito con folio/estado/fecha |
| Elige "Cargar otro" | Snackbar de éxito "Documento #N registrado correctamente" + formulario limpio |
| Elige "Ver en Inicio" | Navegación al Home + confirmación "Documento #N registrado exitosamente" |
| Sin conexión de red simulada durante el guardado | (Cobertura futura, ver §7) el patrón de eventos ya soporta snackbar de fallo de red |

---

## **5. Casos de Prueba**

| **Caso** | **Condición inicial** | **Acción** | **Resultado esperado** | **Estado** |
|---|---|---|---|---|
| **CA-01** | Formulario vacío | Tap [Guardar] | Snackbar de error + banner con 4 campos listados + 4 campos en rojo. No se envía. | Aprobado |
| **CA-02** | Solo el RUT inválido | Tap [Guardar] | Banner lista únicamente "RUT Funcionario"; los demás campos sin error. | Aprobado |
| **CA-03** | Guardar fallido 2 veces seguidas (mismos errores) | Tap [Guardar] x2 | El Snackbar se muestra **las dos veces** (contador de eventos). | Aprobado |
| **CA-04** | Campos con error | Corregir el RUT en vivo | El rojo y el mensaje del RUT desaparecen solos; el banner y los demás errores persisten. | Aprobado |
| **CA-05** | Formulario completo válido | Tap [Guardar] | Progreso → AlertDialog de éxito con folio; cero elementos rojos en pantalla. | Aprobado |
| **CA-06** | AlertDialog de éxito visible | Tap "Cargar otro" | Formulario limpio + Snackbar "Documento #N registrado correctamente". | Aprobado |
| **CA-07** | AlertDialog de éxito visible | Tap "Ver en Inicio" | Regresa a Home con confirmación del registro. | Aprobado |
| **CA-08** | Formulario con datos parciales + errores | Corregir y re-guardar | Ningún dato ingresado se pierde durante el ciclo corrección→reenvío. | Aprobado |

---

## **6. Respuestas a la Guía Meta 4**

| **Pregunta de la Guía** | **Respuesta del Equipo CMQ Salud** |
|---|---|
| **¿Puede pasar que el usuario presione Guardar y no ocurra nada?** | No. Toda validación fallida produce simultáneamente Snackbar, banner resumen y mensajes por campo; todo guardado exitoso produce diálogo de éxito con folio. Cada intento dispara respuesta por diseño (contador de eventos). |
| **¿Qué mecanismos de la pauta se usaron?** | Los cinco: `isError`, `supportingText`, texto condicional (banner resumen + checkbox de autenticidad), `Snackbar` (error y éxito) y mensajes de éxito/error (AlertDialog). |
| **¿Cómo se cumple el flujo dato incorrecto → mensaje → corrección → válido?** | Mensaje específico por campo con instrucción de corrección (Meta 3) + validación en vivo que limpia el error apenas se corrige + bloqueo de envío hasta validez + confirmación de éxito al procesar. Casos CA-01 a CA-08. |
| **¿Dónde vive la decisión de qué mostrar?** | En el ViewModel (MVVM): calcula errores, lista de campos y eventos; la View (Compose) solo los dibuja con `isError`, `supportingText`, banner y `SnackbarHost`. |

---

## **7. Conclusión y Próximos Pasos**

La **Meta 4 queda cumplida**: el formulario P05 garantiza respuesta visible ante cualquier acción — error o éxito — mediante la combinación de señales por campo (`isError`/`supportingText`), señales globales (banner de texto condicional y Snackbar) y confirmaciones de éxito (AlertDialog + Snackbar), cerrando el ciclo completo dato incorrecto → mensaje específico → corrección → formulario válido.

**Próximos pasos para siguientes entregas:**

1. **Snackbar de fallo de red**: cuando el repositorio deje de ser simulado (Retrofit + SharePoint), usar el mismo patrón de eventos (`errorEventId`) para diferenciar errores de validación vs. errores de conexión, con botón "Reintentar" en el snackbar.
2. **Desplazamiento automático al primer campo con error** (`scrollTo` sobre `LazyColumn` indexado) para formularios más largos.
3. Registrar los intentos fallidos de guardado en la bitácora de auditoría (RF-18) cuando exista la capa de trazabilidad.
4. Pruebas unitarias del ViewModel que verifiquen la construcción de `camposConError` y el incremento de `errorEventId` en cada fallo.
