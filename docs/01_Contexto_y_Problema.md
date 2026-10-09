DSY1105 · Caso CMQ Área Salud · Análisis del Contexto 

# **Análisis del Contexto Asignado** 

_Caso: Gestión documental móvil de funcionarios — Corporación Municipal de Quilpué (CMQ), Área Salud_ 

Asignatura: DSY1105 – Desarrollo de Aplicaciones Móviles  ·  Sede CITT Quilpué  ·  28-09-2026 

## **Descripción General** 

Este documento presenta el análisis del contexto del caso asignado en la asignatura DSY1105. Identifica el problema principal, los actores involucrados, las necesidades detectadas, las reglas de negocio explícitas, la información que el sistema debe manejar y las dificultades que la aplicación móvil ayudará a resolver. El contenido constituye la base técnica para etapas posteriores de requerimientos y diseño de la solución. 

## **1.  Problema Principal** 

La Corporación Municipal de Quilpué (CMQ) – Área Salud carece de un repositorio centralizado para la gestión documental de sus funcionarios. Actualmente los documentos se administran de forma dispersa mediante archivos digitales sueltos, planillas Excel, correos electrónicos y carpetas documentales, lo que provoca dificultad para localizar oportunamente los antecedentes y verificar su vigencia, duplicidad de documentos, ausencia de trazabilidad sobre quién consulta, incorpora o modifica información, riesgos de accesos no autorizados y pérdida de información, así como un tratamiento inseguro de datos personales en un ámbito sensible como el de salud. 

En síntesis, el problema radica en la dispersión documental sin control ni seguridad, lo que impacta la eficiencia administrativa y expone a la organización a riesgos de ciberseguridad y de cumplimiento normativo en protección de datos. 

## **2.  Usuarios o Actores Involucrados** 

Los siguientes perfiles participan en el proceso de gestión documental del Área Salud: 

|**#**|**Perfil**|**Rolprincipal**|
|---|---|---|
|**1**|Funcionarios<br>i|Entregan antecedentes personales y<br>laborales, certificados, permisos y<br>documentos de capacitación.<br>i|
|**2**|Personal administrativo|Recibe, verifica, registra y almacena<br>documentos; localiza y envía<br>antecedentes ajefaturas.|
|**3**|Jefaturas y encargados de centros de<br>salud|Revisan, validan antecedentes,<br>autorizan solicitudes y hacen<br>seguimiento deprocesos.|
|**4**|Área de Recursos Humanos (RRHH)|Administra y consulta los<br>antecedentes laborales de los<br>funcionarios.|
|**5**|Administradores del sistema|Gestionan configuración, perfiles,<br>permisos y mantenimiento de la<br>aplicación.|
|**6**|Área de TI y Ciberseguridad<br>i|Evalúa y autoriza integraciones;<br>controla seguridad, accesos y<br>gestión de incidentes.|
|**7**|Supervisores institucionales|Supervisan el cumplimiento de|



Página 1 

DSY1105 · Caso CMQ Área Salud · Análisis del Contexto 

<u><mark>procesos y normatvas internas.</mark></u> i 

Regla transversal: cada perfil debe contar con permisos diferenciados y acceso únicamente a la información necesaria para sus funciones (principio de mínimo privilegio). 

## **3.  Necesidades Detectadas** 

- Repositorio centralizado accesible desde dispositivos móviles que elimine la dispersión documental. 

- Autenticación segura y control de acceso por perfiles y permisos. 

- Trazabilidad completa de accesos, cargas, modificaciones y descargas de documentos. 

- Digitalización de documentos físicos mediante la cámara del dispositivo. 

- Sincronización con SharePoint para mantener una única fuente de verdad, sin copias adicionales. 

- Notificaciones sobre solicitudes pendientes, documentos rechazados, vencimientos y acciones que requieran atención. 

- Cierre de sesión y mecanismos de protección de la información almacenada y en tránsito. 

- Uso exclusivo de datos ficticios o sintéticos para el desarrollo académico del MVP. 

## **4.  Reglas de Negocio Explícitamente Indicadas** 

Las siguientes reglas están explícitas en el caso y deben ser respetadas por la solución: 

|**ID**|**Regla de negocio**|
|---|---|
|**RN-01**|Seguridad desde el diseño: la solución debe incorporar<br>control de acceso, autenticación, respaldo, trazabilidad y<br>gestión de incidentes.<br>i|
|**RN-02**|Permisos diferenciados por perfil: cada actor accede<br>únicamente a la información necesaria para sus<br>funciones.<br>ii  i|
|**RN-03**|Uso exclusivo de datos ficticios o sintéticos: prohibido<br>usar datos reales, listados de personas, credenciales,<br>llaves,tokens o accesos a sistemas internos.|
|**RN-04**|Naturaleza de MVP: el entregable es una propuesta de<br>solución; no corresponde al desarrollo definitivo ni a<br>publicaciónproductiva.|
|**RN-05**|Integración con SharePoint como repositorio central: la<br>app debe consultar, cargar y actualizar documentos sin<br>generar copias adicionales en otros medios.|
|**RN-06**|Autorización de TI y Ciberseguridad: toda integración<br>con sistemas institucionales queda sujeta a evaluación y<br>autorización del área correspondiente.<br>i     i|
|**RN-07**|Uso de dispositivos autorizados: la app se utiliza desde<br>teléfonos institucionales o personales autorizados, con<br>conexión a Internet(Wi-Fi o datos móviles).|
|**RN-08**|Registro de metadatos de gestión documental: cada<br>documento debe registrar fecha de carga, usuario que<br>incorpora el archivo, tipo de documento, estado,<br>modificacionesyaccesos realizados.|



Página 2 

DSY1105 · Caso CMQ Área Salud · Análisis del Contexto 

## **5.  Información que el Sistema Debería Manejar** 

### **5.1  Datos del funcionario** 

|**Categoría**|**Atributos**|
|---|---|
|**Identificación básica**|RUT, nombre completo, fecha de nacimiento, datos de<br>contacto.|
|**Información laboral**|Centro de salud, cargo, profesión, tipo de contrato,<br>fecha de ingreso,situación contractual.|
|**Documentación laboral**|Contratos, anexos, decretos, resoluciones y otros<br>antecedentes.|
|**Permisos y autorizaciones**|Solicitudes, respaldos, fechas, períodos y estado de<br>aprobación.<br>ii|
|**Capacitaciones**|Cursos,certificados,horasyestado de validación.|
|**Otros antecedentes**|Documentos administrativos complementarios del<br>expediente.|



### **5.2  Metadatos de gestión documental** 

|**Metadato**|**Descripción**|
|---|---|
|**Fecha de carga**|Fecha y hora en que el archivo fue incorporado al<br>sistema.<br>iii|
|**Usuarioque incorpora el archivo**|Identificación delperfilque realiza la carga.<br>iii|
|**Tipo de documento**|Clasificación: contrato, permiso,certificado,etc.|
|**Estado**|Vigente, pendiente,rechazado,vencido.|
|**Modificaciones**|Registro de cambios realizados sobre el documento.|
|**Accesos realizados**|Trazabilidad de consultasydescargaspor usuario.|



### **5.3  Información de seguridad y sesión** 

- Credenciales y mecanismos de autenticación de cada perfil. 

- Permisos y roles asignados. 

- Registro de sesión: inicio, cierre, dispositivo, IP. 

- Bitácora de incidentes y accesos no autorizados. 

## **6.  Dificultades o Situaciones que la Aplicación Podría Ayudar a Resolver** 

|**#**|**Dificultad actual**|**Cómo la aplicación contribuye**|
|---|---|---|
|**1**|Búsqueda manual de documentos<br>lentay poco eficiente.|Consulta centralizada y filtrada<br>desde el móvil.|
|**2**|Duplicidad de documentos.|Repositorio único en SharePoint;<br>evita registrar el mismo antecedente<br>más de una vez.|
|**3**|Falta de trazabilidad.|Bitácora de accesos y modificaciones<br>identifica responsable y fecha de<br>cada acción.|
|**4**|Pérdida de información.|Concentración en SharePoint con<br>respaldo institucional; reduce el<br>riesgo frente a medios informales.<br>i  i|
|**5**|Accesos no autorizados.|Control por perfiles y autenticación<br>segura limitan el acceso a<br>información sensible.|
|**6**|Retrasos en la gestión<br>administrativa.|Notificaciones y flujo de validación<br>reducen tiempos de respuesta.|



Página 3 

DSY1105 · Caso CMQ Área Salud · Análisis del Contexto 

|**7**|Dificultad para verificar vigencia.|El sistema marca estados y<br>vencimientos, facilitando la<br>identificación de antecedentes<br>caducos.|
|---|---|---|
|**8**|Asociación incorrecta de<br>documentos.|Vinculación de cada archivo a un<br>funcionario y registro de metadatos<br>minimiza errores.|
|**9**|Trabajo en terreno limitado.|Jefaturas y encargados pueden<br>validar y consultar documentos<br>desde los CESFAM/CECOSF.|
|**10**|Cumplimiento normativo.|Seguridad desde el diseño; fortalece<br>el tratamiento de datos personales y<br>lagestión de incidentes.|



Página 4 

