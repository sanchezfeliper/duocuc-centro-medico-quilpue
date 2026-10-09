DSY1105 · Meta 3 · Validaciones del formulario P05 Carga de Documento

# **Meta 3 - Implementación de Validaciones**

_P05: Formulario de Carga y Registro de Documentos — Validaciones reactivas con mensajes comprensibles_

DSY1105 - Desarrollo de Aplicaciones Móviles  ·  CMQ Área Salud  ·  08-10-2026

---

## **1. Selección y Justificación**

La Meta 3 exige que el formulario trabajado en la sesión contenga, como mínimo, una **validación de campo obligatorio** y una **validación adicional** (longitud, formato, número, rango, fecha, selección o regla propia), con el principio rector de que **el usuario debe comprender qué está mal y cómo puede corregirlo**, no solo verse impedido de enviar datos incorrectos.

Se aplica al formulario **FORM-P05: Carga de Documento** (`CargaDocumentoScreen` + `CargaViewModel`), por ser el punto de entrada de datos al repositorio centralizado: cada error que estas validaciones evita es un documento mal clasificado, mal asociado o con metadatos incompletos en el expediente de un funcionario (exactamente la dispersión y duplicidad que el caso CMQ busca eliminar).

| **Atributo** | **Valor** |
|---|---|
| **ID Formulario** | FORM-P05 |
| **Pantalla asociada** | P05 - Carga de documento (`CargaDocumentoScreen.kt`) |
| **Requerimientos** | RF-08, RF-10, RF-11, RN-08 |
| **Arquitectura** | MVVM + Jetpack Compose + StateFlow (UDF) |

---

## **2. Inventario de Validaciones Implementadas**

El formulario queda cubierto por **11 reglas de validación** distribuidas en las categorías de la pauta:

| **#** | **Categoría (paleta)** | **Campo** | **Regla** |
|---|---|---|---|
| V-01 | **Campo obligatorio** | RUT Funcionario | No puede estar vacío. |
| V-02 | **Formato** | RUT Funcionario | Patrón chileno `12.345.678-9` (puntos, guión, sin espacios). |
| V-03 | **Regla propia del proyecto** | RUT Funcionario | Dígito verificador correcto según **Módulo 11**. |
| V-04 | **Campo obligatorio** | Tipo de documento | Debe seleccionarse una opción. |
| V-05 | **Selección** | Tipo de documento | El valor debe pertenecer al catálogo institucional de 5 tipos (RN-08). |
| V-06 | **Campo obligatorio** | Nombre del archivo | No puede estar vacío. |
| V-07 | **Formato** | Nombre del archivo | Solo extensiones `.pdf`, `.jpg`, `.jpeg`, `.png` (RF-08/RF-11). |
| V-08 | **Formato** | Nombre del archivo | Sin caracteres prohibidos por SharePoint/Windows: `\ / : * ? " < > \|`. |
| V-09 | **Longitud** | Nombre del archivo | Máximo 150 caracteres (E4 `Documento.nombreArchivo` VARCHAR(150)). |
| V-10 | **Campo obligatorio + Longitud** | Descripción | Obligatoria; mínimo 5 y máximo 200 caracteres (E4 `Documento.descripcion` VARCHAR(200)). |
| V-11 | **Campo obligatorio (booleano)** | Declaración de autenticidad | Checkbox obligatorio (RN-08). |

Con esto, la Meta 3 no solo cumple el mínimo exigido (1 obligatorio + 1 adicional), sino que cubre **cinco categorías distintas**: obligatorio, formato, longitud, selección y regla propia.

---

## **3. Mensajes Comprensibles: Qué está Mal y Cómo Corregirlo**

Principio de diseño de esta meta: **cada mensaje nombra el problema, muestra el valor esperado y sugiere la acción de corrección**. Todos los mensajes se despliegan en `supportingText` rojo bajo el campo correspondiente (y el campo se marca con `isError = true`), sin borrar lo que el usuario ya escribió.

| **Condición detectada** | **Mensaje mostrado al usuario (texto exacto)** |
|---|---|
| RUT vacío | "El RUT es obligatorio: ingrese el identificador del funcionario con puntos y guión, ej: 18.765.432-7." |
| RUT mal formateado | "Formato de RUT inválido: escríbalo como 18.765.432-7 (puntos miles, guión y dígito verificador, sin espacios)." |
| Dígito verificador incorrecto | "El dígito verificador no coincide: para este RUT el carácter tras el guión debiera ser '7' y se ingresó '1'. Verifique el RUT del funcionario." |
| Tipo fuera de catálogo | "Seleccione un tipo de documento del catálogo institucional: Contrato, Certificado de capacitación, Licencia médica, Permiso administrativo o Anexo." |
| Nombre de archivo vacío | "El nombre del archivo es obligatorio: escriba un nombre descriptivo con su extensión, ej: contrato_juan_perez.pdf." |
| Extensión no permitida | "Extensión no permitida: solo se aceptan archivos .pdf, .jpg, .jpeg o .png. Renombre el archivo con una de esas extensiones antes de subirlo." |
| Carácter prohibido | "El nombre contiene el carácter '\*', no permitido en nombres de archivo. Reemplácelo por guion bajo (_) o guion medio (-)." |
| Nombre demasiado largo | "El nombre supera el máximo de 150 caracteres (lleva 163). Acórtelo manteniendo la extensión (.pdf, .jpg o .png)." |
| Descripción vacía | "La descripción es obligatoria: indique el motivo o contexto del documento, ej: Permiso médico del 28-09-2026." |
| Descripción demasiado corta | "La descripción es demasiado corta: escriba al menos 5 caracteres (lleva 3). Agregue el contexto del documento." |
| Descripción demasiado larga | "La descripción supera el máximo de 200 caracteres (lleva 215). Resuma la información esencial." |
| Autenticidad sin marcar | "Debe marcar la declaración de autenticidad: confirme que el documento y sus metadatos son fidedignos (RN-08)." |
| Resumen al intentar guardar con errores | "No se pudo registrar el documento: N campo(s) requieren corrección. Cada mensaje en rojo indica qué está mal y cómo corregirlo." |

Nótese que los mensajes de longitud y de dígito verificador **incluyen el conteo o el valor correcto calculado**, de modo que el usuario sabe exactamente cuánto le sobra o qué carácter debiera ingresar.

---

## **4. Validación Reactiva "en Vivo"**

Además de la validación al presionar **Guardar y Registrar Documento**, cada campo se **re-valida en el mismo instante en que el usuario escribe** (`onRutChange`, `onNombreArchivoChange`, `onDescripcionChange`):

* Mientras el campo está vacío no se muestra error (no se molesta al usuario antes de que escriba; la regla de obligatorio se aplica al guardar).
* En cuanto hay contenido, cada corrección del usuario vuelve a evaluarse: **el error desaparece apenas el valor se vuelve válido**, sin perder el resto de los datos del formulario.
* La descripción muestra un **contador preventivo permanente** (`123/200 caracteres (mínimo 5)`) para que el usuario nunca llegue a exceder el máximo sin saberlo.
* El RUT muestra una **pista de formato** cuando no hay error ("Formato 12.345.678-9; se verifica el dígito verificador").

Esto implementa el ciclo exigido: la validación no solo **impide** el ingreso incorrecto (bloqueo del envío), sino que ** guía la corrección** mensaje a mensaje.

---

## **5. Implementación (Código Clave)**

### **5.1 Validadores por campo en `CargaViewModel`**

Cada validador devuelve `null` si el valor es correcto, o el mensaje comprensible si falla:

```kotlin
companion object {
    private val REGEX_RUT = Regex("^\\d{1,2}\\.\\d{3}\\.\\d{3}-[\\dkK]$")
    private val EXTENSIONES_VALIDAS = listOf(".pdf", ".jpg", ".jpeg", ".png")
    private val CARACTERES_PROHIBIDOS = listOf('\\', '/', ':', '*', '?', '"', '<', '>', '|')
    private const val LARGO_MAX_NOMBRE_ARCHIVO = 150
    private const val LARGO_MIN_DESCRIPCION = 5
    private const val LARGO_MAX_DESCRIPCION = 200
}

/** Campo obligatorio + formato + regla propia: dígito verificador (Módulo 11). */
private fun validarRut(valor: String): String? {
    val rut = valor.trim()
    if (rut.isEmpty())
        return "El RUT es obligatorio: ingrese el identificador del funcionario con puntos y guión, ej: 18.765.432-7."
    if (!REGEX_RUT.matches(rut))
        return "Formato de RUT inválido: escríbalo como 18.765.432-7 (puntos miles, guión y dígito verificador, sin espacios)."
    val cuerpo = rut.dropLast(2).filter { it.isDigit() }
    val dvIngresado = rut.last().uppercaseChar()
    val dvEsperado = calcularDigitoVerificador(cuerpo)
    if (dvIngresado != dvEsperado)
        return "El dígito verificador no coincide: para este RUT el carácter tras el guión debiera ser " +
               "'$dvEsperado' y se ingresó '$dvIngresado'. Verifique el RUT del funcionario."
    return null
}

/** Regla propia: cálculo del dígito verificador del RUT chileno (Módulo 11). */
private fun calcularDigitoVerificador(cuerpo: String): Char {
    var suma = 0
    var multiplicador = 2
    for (digito in cuerpo.reversed()) {
        suma += digito.digitToInt() * multiplicador
        multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
    }
    val resto = suma % 11
    return when (val dv = 11 - resto) {
        11 -> '0'
        10 -> 'K'
        else -> ('0' + dv)
    }
}
```

### **5.2 Validación en vivo dentro del flujo UDF**

```kotlin
fun onRutChange(value: String) {
    // Mientras el campo está vacío no se marca error; la regla de
    // obligatorio se aplica al guardar. Con contenido, se valida en vivo.
    val error = if (value.isBlank()) null else validarRut(value)
    _uiState.update { it.copy(rut = value, rutError = error, generalError = null) }
}
```

### **5.3 Orquestación al guardar**

`guardarDocumento()` ejecuta los cinco validadores, y si alguno falla **no envía nada al repositorio**: marca cada campo con su error y muestra el mensaje general con la cantidad de campos a corregir:

```kotlin
val errores = listOfNotNull(rutErr, tipoErr, nombreErr, descErr, autErr)
if (errores.isNotEmpty()) {
    _uiState.update {
        it.copy(
            rutError = rutErr, tipoError = tipoErr,
            nombreArchivoError = nombreErr, descripcionError = descErr,
            autenticidadError = autErr,
            generalError = "No se pudo registrar el documento: ${errores.size} campo(s) " +
                "requieren corrección. Cada mensaje en rojo indica qué está mal y cómo corregirlo."
        )
    }
    return
}
```

### **5.4 Cambios en la UI (`CargaDocumentoScreen`)**

* El selector de **Tipo de documento** ahora muestra `isError` + `supportingText` (cubre la validación de selección).
* El campo **RUT** muestra pista de formato cuando está correcto.
* La **Descripción** muestra contador `n/200 caracteres (mínimo 5)` como prevención activa.

### **5.5 Hallazgo derivado de la validación**

La nueva regla V-03 detectó de inmediato un dato de prueba inconsistente en nuestro propio formulario: el RUT de ejemplo pre-cargado `18.765.432-1` **no pasa el Módulo 11** (el dígito verificador correcto para ese cuerpo es **7**; igual pasa con `12.345.678-9`, cuyo DV correcto es **5**). Por ello:

1. El campo RUT ahora inicia **vacío** (buena práctica: el dato debe ingresarse, no sugerirse precargado).
2. Los ejemplos de mensajes y placeholder se actualizaron a `18.765.432-7`.

Este episodio se documenta como evidencia de que la validación atrapa errores reales y no solo casos teóricos.

---

## **6. Casos de Prueba**

| **Caso** | **Condición inicial** | **Acción** | **Resultado esperado** | **Estado** |
|---|---|---|---|---|
| **CA-01** | Formulario en blanco | Tap [Guardar y Registrar] | 4 errores en rojo (RUT, nombre, descripción, autenticidad) + mensaje general "4 campo(s) requieren corrección". No se envía. | Aprobado |
| **CA-02** | RUT "187654327" (sin puntos/guión) | Escribir / guardar | "Formato de RUT inválido: escríbalo como 18.765.432-7…". | Aprobado |
| **CA-03** | RUT "18.765.432-1" (DV incorrecto) | Escribir / guardar | "…el carácter tras el guión debiera ser '7' y se ingresó '1'…". Error visible en vivo al terminar de escribir. | Aprobado |
| **CA-04** | RUT corregido a "18.765.432-7" | Escribir | El error desaparece en vivo sin tocar los demás campos. | Aprobado |
| **CA-05** | Nombre "contrato" (sin extensión) | Escribir | "Extensión no permitida: solo se aceptan archivos .pdf, .jpg, .jpeg o .png…". | Aprobado |
| **CA-06** | Nombre "informe*2026?.pdf" | Escribir | "El nombre contiene el carácter '*', no permitido… Reemplácelo por guion bajo (_) o guion medio (-)." | Aprobado |
| **CA-07** | Nombre de 163 caracteres con ".pdf" | Escribir | "El nombre supera el máximo de 150 caracteres (lleva 163). Acórtelo manteniendo la extensión…". | Aprobado |
| **CA-08** | Descripción "abc" | Escribir | "La descripción es demasiado corta: escriba al menos 5 caracteres (lleva 3)…". | Aprobado |
| **CA-09** | Descripción de 215 caracteres | Escribir / guardar | Contador supera 200 y al validar: "…supera el máximo de 200 caracteres (lleva 215). Resuma la información esencial." | Aprobado |
| **CA-10** | Tipo fuera de catálogo (solo alcanzable por defecto de datos) | Guardar | "Seleccione un tipo de documento del catálogo institucional…". Defensa en profundidad aunque el dropdown es read-only. | Aprobado |
| **CA-11** | Formulario completo válido (RUT 18.765.432-7, PDF válido, descripción ≥5, checkbox marcado) | Tap [Guardar y Registrar] | Procesa (indicador de carga) y despliega `AlertDialog` con folio y estado. | Aprobado |
| **CA-12** | Formulario con 2 errores (RUT y descripción) | Corregir solo el RUT | El error del RUT desaparece en vivo; el de la descripción persiste hasta corregirse; los datos corregidos no se pierden. | Aprobado |

---

## **7. Respuestas a la Guía Meta 3**

| **Pregunta de la Guía** | **Respuesta del Equipo CMQ Salud** |
|---|---|
| **¿El formulario tiene validación de campo obligatorio?** | Sí: RUT, tipo de documento, nombre del archivo, descripción y declaración de autenticidad (V-01, V-04, V-06, V-10, V-11). |
| **¿Tiene al menos una validación adicional?** | Sí, diez: formato de RUT, extensión de archivo, caracteres prohibidos, longitud máxima de nombre (150) y de descripción (mín. 5 / máx. 200), selección dentro del catálogo, y la regla propia de **dígito verificador de RUT por Módulo 11** (V-02 a V-10). |
| **¿El usuario comprende qué está mal y cómo corregirlo?** | Sí. Cada mensaje nombra el problema, muestra el formato o valor esperado (incluso el DV correcto calculado y los conteos de caracteres) y sugiere la acción de corrección. Además la validación es **en vivo**: el error desaparece apenas el valor se corrige, y el contador de descripción previene el error antes de que ocurra. |
| **¿Dónde se implementaron las validaciones?** | En `CargaViewModel` (reglas y orquestación, siguiendo MVVM: la lógica no vive en la View), mostradas por `CargaDocumentoScreen` mediante `isError` + `supportingText`. |

---

## **8. Conclusión y Próximos Pasos**

La **Meta 3 queda cumplida** sobre el formulario P05 Carga de Documento: 11 reglas de validación en cinco categorías (obligatorio, formato, longitud, selección y regla propia), ejecutadas tanto al guardar como de forma reactiva durante la escritura, siempre con mensajes que explican el problema y su corrección. Como beneficio adicional, la validación de dígito verificador detectó y permitió corregir un RUT de ejemplo inválido en nuestros propios datos de prueba.

**Próximos pasos para siguientes entregas:**

1. Migrar `validarRut`/`calcularDigitoVerificador` a un utilitario compartido (`domain/validation/`) y reutilizarlo en P03 Consulta de Expediente (hoy solo valida formato).
2. Agregar **validación de fecha** (fecha de vencimiento ≥ fecha de carga) al incorporar `fechaVencimiento` de la entidad E4.
3. Mantener trazabilidad: registrar los rechazos de validación en la bitácora (RF-18) cuando exista la capa de auditoría.
4. Pruebas unitarias automatizadas de los validadores (JUnit) para cubrir los casos CA-01 a CA-12 sin depender del emulador.
