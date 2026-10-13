DSY1105 · Meta 8 · Compilar y probar durante la clase — CMQ Salud

# **Meta 8 - Compilar y Probar Durante la Clase**

_Ciclo incremental (implementar → build → corregir → run → probar → commit) aplicado a P05 Carga de Documento y P06 Validación, con evidencia automatizada de casos válidos e inválidos_

DSY1105 - Desarrollo de Aplicaciones Móviles  ·  CMQ Área Salud  ·  10-10-2026

---

## **1. Objetivo de la Meta**

A partir de esta meta rige una **regla permanente de trabajo**: no se programa todo para compilar al final. Cada funcionalidad avanza con el ciclo:

```
1. Implementar una parte pequeña     5. Probar
2. Build                             6. Corregir comportamiento
3. Corregir errores                  7. Commit
4. Run                               8. Continuar
```

Y antes de considerar terminada una funcionalidad deben comprobarse **dos casos**:

| **Tipo** | **Ejemplo de la pauta** | **Resultado esperado** |
|---|---|---|
| **Caso válido** | `Nombre = "Reserva laboratorio"` | Registro correcto |
| **Caso inválido** | `Nombre = ""` | Mensaje "El nombre es obligatorio" |

Este documento registra cómo se aplicó el ciclo en CMQ Salud, **qué se probó**, **con qué evidencia** y **qué se corrigió**.

---

## **2. Cómo se Aplicó el Ciclo Incremental**

| **Paso** | **Cómo se ejecutó en el proyecto** |
|---|---|
| **1. Implementar** | Se trabajó módulo a módulo (Login → Home → Carga → Consulta → Detalle → Validación), un avance pequeño por iteración (Metas 1 a 7). |
| **2. Build** | `gradlew :app:compileDebugKotlin` tras cada cambio. |
| **3. Corregir errores** | Cada compilación se validó antes de seguir; se corrigieron errores de tipos, imports y firmas en el momento. |
| **4. Run** | APK instalado y ejecutado en emulador **Pixel_7 (Android 14, 1080x2400)**: `gradlew :app:installDebug`. |
| **5. Probar** | Casos válidos e inválidos en **P05** y **P06**, automatizados (ver §4). |
| **6. Corregir comportamiento** | Se corrigió un comportamiento real de la automatización y se verificó el flujo completo de datos (ver §5). |
| **7. Commit** | Cada meta cerrada con su commit (historial de `RamaCarlosAcunia`). |

---

## **3. Matriz de Pruebas: Casos Válidos e Inválidos**

Se probaron ambos tipos de caso en las dos pantallas de formulario del sistema (P05 y P06), en **dos niveles**: unitario (JVM, sin emulador) y UI (emulador, con captura).

| **Pantalla** | **Caso** | **Entrada** | **Resultado esperado** | **Nivel** | **Estado** |
|---|---|---|---|---|---|
| **P05 Carga** | **Válido** | RUT `18.765.432-7`, archivo `constancia_meta8_ok.pdf`, descripción ≥5 car., declaración marcada | Registro correcto: diálogo con **Folio #5**, estado Vigente | Unitario + UI | ✅ Aprobado |
| **P05 Carga** | **Inválido** | Formulario vacío (RUT, archivo, descripción y declaración vacíos) | 4 mensajes específicos por campo + Snackbar; **nada se guarda** | Unitario + UI | ✅ Aprobado |
| **P06 Validación** | **Válido** | Documento pendiente + comentario `"Documento no legible"` → RECHAZAR | Estado `RECHAZADO` guardado, sale de la bandeja, Snackbar "Documento #2 rechazado" | Unitario + UI | ✅ Aprobado |
| **P06 Validación** | **Inválido** | Documento pendiente → RECHAZAR **sin comentario** | "El comentario es obligatorio para rechazar…"; el documento **sigue pendiente** | Unitario + UI | ✅ Aprobado |

Casos adicionales cubiertos por los tests unitarios: switch de derivación → estado `PENDIENTE`; solicitar corrección con comentario válido → `PENDIENTE_CORRECCION`; comentario demasiado corto → error; selección/deselección de documento; RUT con dígito verificador incorrecto → rechazo.

---

## **4. Pruebas Automatizadas**

### **4.1 Nivel unitario (JVM, sin emulador)**

`gradlew :app:testDebugUnitTest` — **32 pruebas, 0 fallos**:

| **Clase de prueba** | **Pruebas** | **Qué cubre** |
|---|---|---|
| `CargaViewModelTest` | 5 | P05: 2 casos válidos (registro + estado PENDIENTE) y 3 inválidos (formulario vacío, extensión inválida, DV incorrecto) |
| `ValidacionViewModelTest` | 6 | P06: 4 casos válidos (aprobar, rechazar c/ comentario, solicitar corrección, deseleccionar) y 2 inválidos (sin comentario, comentario corto) |
| `ValidadorRutTest` | 11 | Regla propia Módulo 11 (formato, DV, casos borde) |
| `DocumentRepositoryTest` | 9 | Asociación por RUT, expediente filtrado, bandeja y decisión |
| `ExampleUnitTest` | 1 | Prueba base del proyecto |

Los ViewModels usan `delay()` simulado (latencia de red ficticia); con `runTest` + `advanceUntilIdle` el tiempo virtual avanza y las corrutinas completan **sin esperas reales ni emulador** — razón por la cual la lógica vive en el ViewModel (separación MVVM de la Meta 5).

### **4.2 Nivel UI (emulador, con capturas)**

Se creó el script reproducible [scripts/meta8_casos.sh](scripts/meta8_casos.sh), que:

1. Reinicia la app desde cero (`pm clear` + lanzar MainActivity).
2. Hace login con datos ficticios.
3. Recorre los 4 casos (P05 válido/inválido, P06 válido/inválido) tocando la UI real vía `adb input` (los elementos se localizan en la jerarquía de `uiautomator`, no por coordenadas fijas).
4. Captura una pantalla por caso con `adb exec-out screencap`.

```bash
# Reproducir la evidencia completa:
emulator -avd Pixel_7 -no-snapshot -no-audio -no-boot-anim -no-window &
adb wait-for-device
gradlew :app:installDebug
./scripts/meta8_casos.sh          # genera docs/evidencia_meta8/08..11
```

---

## **5. Correcciones de Comportamiento Encontradas**

### **5.1 Bug de evidencia fantasma (automatización)**

La primera versión del helper UI leía el archivo `ui.xml` del dispositivo **sin borrarlo antes**: si `uiautomator dump` fallaba, se descargaba el archivo de una sesión anterior y la automatización "veía" una pantalla que ya no existía (incluso mostraba los textos que otro integrante había dejado en el emulador). **Corrección**: el helper ahora borra `/sdcard/ui.xml` antes de cada dump, de modo que un fallo no reutiliza evidencia vieja. Sin este arreglo, las capturas podían no corresponder al estado real.

### **5.2 Verificación del flujo de datos (Meta 6) en pantalla**

Al probar P06 se confirmó visualmente que la **asociación por RUT** funciona de extremo a extremo: el panel de revisión muestra **"Funcionario · Juan Pérez · 18.765.432-7"** para el documento pendiente, es decir el RUT que entra por el formulario P05 determina el expediente y se refleja en la bandeja de jefatura.

### **5.3 Caso inválido que no ensucia los datos**

El caso inválido de P06 se probó **antes** del válido sobre el mismo documento: al rechazar sin comentario, el documento **permanece** `PENDIENTE` en el repositorio (verificado también en el test unitario), por lo que la corrección del usuario no requiere recargar datos.

---

## **6. Inventario de Evidencia**

Carpeta [`docs/evidencia_meta8/`](docs/evidencia_meta8/):

| **Archivo** | **Contenido** |
|---|---|
| `01_login.png` | Pantalla de login |
| `02_login_invalido_vacio.png` | **Caso inválido P01**: login con campos vacíos + mensaje |
| `03_home.png` | Home con menú por perfil |
| `04_validacion_bandeja.png` · `10a_validacion_bandeja.png` | Bandeja de P06 con documento pendiente |
| `05_validacion_error_comentario.png` · **`10_validacion_invalido_sin_comentario.png`** | **Caso inválido P06**: comentario obligatorio + mensaje |
| `06_validacion_rechazo_ok.png` · **`11_validacion_valido_rechazo.png`** | **Caso válido P06**: decisión registrada + Snackbar "Documento #2 rechazado" |
| `07_carga_formulario_vacio.png` · **`08_carga_invalido_vacio.png`** | **Caso inválido P05**: errores por campo + Snackbar |
| **`09_carga_valido_exito.png`** | **Caso válido P05**: diálogo de éxito con Folio #5 |
| `debug_checkbox.png` | Diagnóstico de la declaración de autenticidad |

Las capturas **08, 09, 10, 11** fueron generadas automáticamente por `scripts/meta8_casos.sh` (las numeradas 01–07 corresponden a la primera ronda manual del equipo).

---

## **7. Respuestas a la Guía Meta 8**

| **Pregunta de la Guía** | **Respuesta del Equipo CMQ Salud** |
|---|---|
| **¿Se compiló durante la clase o solo al final?** | Durante todo el desarrollo: cada avance se compiló con `:app:compileDebugKotlin` antes de continuar; las 8 metas tienen su propio commit con build verificado. |
| **¿Se probaron casos válidos e inválidos?** | Sí, en P05 y P06, en dos niveles (unitario y UI con captura). Matriz completa en §3; las capturas 08–11 son la evidencia. |
| **¿El caso inválido muestra mensaje y no registra nada?** | Sí: en P05 se muestran 4 mensajes por campo + Snackbar y el repositorio no recibe nada; en P06 el mensaje de comentario obligatorio aparece y el documento sigue `PENDIENTE`. |
| **¿Se corrigió comportamiento tras probar?** | Sí: se corrigió el bug de evidencia fantasma (§5.1) y se verificó en pantalla el flujo de datos RUT → expediente → bandeja (§5.2). |
| **¿Cuántas pruebas automatizadas hay?** | 32 unitarias (0 fallos) más los 4 flujos UI del script reproducible. |

---

## **8. Conclusión**

La **Meta 8 queda finiquitada**: el trabajo se desarrolló con el ciclo incremental exigido, y las dos pantallas de formulario del sistema (**P05 Carga** y **P06 Validación**) quedan probadas en sus **casos válidos e inválidos**, con doble respaldo:

* **32 pruebas unitarias** que verifican estado y reglas sin emulador (rápidas y repetibles).
* **Automatización UI reproducible** (`scripts/meta8_casos.sh`) con **4 capturas** que muestran el resultado real en pantalla, incluidos los mensajes de error exigidos por la pauta.

Queda documentado además un hallazgo de calidad de proceso (§5.1) que mejora la fiabilidad de la evidencia para futuras metas.

**Próximos pasos:**

1. Extender la automatización UI a los casos válidos/inválidos de **P03 Consulta** (RUT sin expediente, filtros) y **P04 Detalle**.
2. Integrar la ejecución de `testDebugUnitTest` en el flujo de trabajo (o CI) para que el paso "Build + Probar" sea automático en cada push.
3. Sumar *instrumented tests* de Compose (`androidTest`) que ejerciten los flujos sin depender de `adb input`.
