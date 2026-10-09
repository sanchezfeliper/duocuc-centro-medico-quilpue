DSY1105 · Caso CMQ Area Salud · Levantamiento inicial de requerimientos 

# **Levantamiento Inicial de Requerimientos** 

_Gestion documental movil de funcionarios - Corporacion Municipal de Quilpue (CMQ), Area Salud_ DSY1105 - Desarrollo de Aplicaciones Moviles  ·  Sede CITT Quilpue  ·  28-09-2026 

## **1.  Objetivo del Documento** 

Este documento presenta la primera version de los requerimientos del sistema movil de gestion documental para la CMQ - Area Salud. Cada requerimiento se redacta como una accion concreta y verificable, indicando que debe permitir hacer la aplicacion, quien la ejecuta, que informacion se ingresa, consulta o modifica, y que resultado debe entregar el sistema. La version no esta cerrada y podra refinarse en etapas posteriores del proyecto. 

### **1.1  Actores del Sistema** 

- FUN: Funcionario del Area Salud. 

- ADM: Personal administrativo. 

- JEF: Jefatura o encargado de centro de salud. 

- RRHH: Area de Recursos Humanos. 

- SYS: Administrador del sistema. 

- TIC: Area de Tecnologias de Informacion y Ciberseguridad (control y autorizacion). 

## **2.  Requerimientos Funcionales** 

### **2.1  Autenticación y Sesion** 

|**ID**|**Que permite hacer**|**Actor**|**Informacion**<br>**involucrada**|**Resultado esperado**|
|---|---|---|---|---|
|**RF-01**|Iniciar sesion con RUT y<br>contrasena valida.|Todos|RUT, contrasena,<br>dispositivo.|Sesion activa con<br>permisos segun perfil;<br>acceso denegado si<br>credenciales invalidas.|
|**RF-02**|Cerrar sesion activa.|Todos|Token de sesion.|Sesion finalizada; datos<br>sensibles limpiados del<br>dispositivo.|
|**RF-03**|Bloquear sesion tras 5<br>minutos de inactividad.|Sistema|Temporizador de<br>inactividad.|Pantalla de bloqueo;<br>reautenticacion<br>requerida para<br>continuar.|
|**RF-04**|Registrar intentos<br>fallidos de inicio de<br>sesion.|Sistema|RUT, fecha/hora, IP,<br>dispositivo.|Bitacora de intentos;<br>bloqueo temporal tras<br>5 intentos fallidos.|



Pagina 1 

DSY1105 · Caso CMQ Area Salud · Levantamiento inicial de requerimientos 

### **2.2  Perfiles y Permisos** 

|**ID**|**Que permite hacer**|**Actor**|**Informacion**<br>**involucrada**|**Resultado esperado**|
|---|---|---|---|---|
|**RF-05**|Crear, editar y<br>desactivar usuarios del<br>sistema.|SYS|RUT, nombre, rol,<br>centro de salud,<br>estado.|Usuario registrado con<br>permisos segun rol<br>asignado.|
|**RF-06**|Asignar y revocar<br>permisos por perfil.|SYS|Lista de permisos,<br>perfil.|Permisos efectivos al<br>siguiente inicio de<br>sesion.|
|**RF-07**|Restringir acceso a<br>informacion<br>exclusivamente a la<br>necesaria para cada<br>perfil.|Sistema|Perfil del usuario,<br>recurso solicitado.|Acceso permitido o<br>denegado segun matriz<br>de permisos.|



### **2.3  Carga y Digitalizacion de Documentos** 

|**ID**|**Que permite hacer**|**Actor**|**Informacion**<br>**involucrada**|**Resultado esperado**|
|---|---|---|---|---|
|**RF-08**|Cargar archivo digital<br>existente (PDF, JPG,<br>PNG).|FUN, ADM, RRHH|Archivo, tipo de<br>documento,<br>funcionario asociado.|Documento<br>almacenado en<br>SharePoint con<br>metadatos.|
|**RF-09**|Capturar imagen de<br>documento fisico con<br>camara del dispositivo.|FUN, ADM|Imagen capturada, tipo<br>de documento,<br>funcionario.|Imagen guardada<br>como archivo digital en<br>SharePoint.|
|**RF-10**|Asignar tipo, estado y<br>descripcion al<br>documento cargado.|ADM, RRHH|Tipo, estado,<br>descripcion.|Documento clasificado<br>y visible en el<br>expediente.|
|**RF-11**|Rechazar carga de<br>archivo duplicado para<br>el mismo funcionario y<br>tipo.|Sistema|Hash del archivo, RUT,<br>tipo de documento.|Mensaje de error y<br>bloqueo de carga<br>duplicada.|



### **2.4  Consulta y Busqueda** 

|**ID**|**Que permite hacer**|**Actor**|**Informacion**<br>**involucrada**|**Resultado esperado**|
|---|---|---|---|---|
|**RF-12**|Consultar expediente<br>del funcionario.|FUN (propio), ADM,<br>JEF, RRHH|RUT o nombre del<br>funcionario.|Listado de documentos<br>con tipo, estado, fecha<br>y usuario.|
|**RF-13**|Buscar documentos<br>por filtros (RUT, tipo,<br>estado, fechas).|ADM, JEF, RRHH|Filtros seleccionados.|Listado filtrado en<br>menos de 3 segundos.|



Pagina 2 

DSY1105 · Caso CMQ Area Salud · Levantamiento inicial de requerimientos 

|**RF-14**|Descargar o visualizar<br>documento.|FUN, ADM, JEF, RRHH|ID del documento.|Documento mostrado<br>o descargado; acceso<br>registrado en bitacora.|
|---|---|---|---|---|



### **2.5  Validacion y Aprobacion** 

|**ID**|**Que permite hacer**|**Actor**|**Informacion**<br>**involucrada**|**Resultado esperado**|
|---|---|---|---|---|
|**RF-15**|Revisar documentos<br>pendientes de<br>validacion.|JEF, RRHH|Lista de documentos<br>en estado pendiente.|Detalle del documento<br>y antecedentes del<br>funcionario.|
|**RF-16**|Aprobar o rechazar<br>documento con<br>comentario.|JEF, RRHH|ID documento,<br>decision, comentario.|Estado actualizado;<br>notificacion enviada al<br>funcionario.|
|**RF-17**|Solicitar correccion de<br>documento.|JEF, RRHH|ID documento, motivo.|Documento marcado<br>como 'pendiente<br>correccion';<br>notificacion al<br>funcionario.|



### **2.6  Trazabilidad y Auditoria** 

|**ID**|**Que permite hacer**|**Actor**|**Informacion**<br>**involucrada**|**Resultado esperado**|
|---|---|---|---|---|
|**RF-18**|Registrar<br>automaticamente cada<br>acceso, carga,<br>modificacion y<br>descarga.|Sistema|Usuario, accion,<br>recurso, fecha/hora,<br>dispositivo.|Entrada en bitacora de<br>auditoria no<br>modificable.|
|**RF-19**|Consultar bitacora de<br>auditoria por<br>funcionario o<br>documento.|SYS, TIC|Filtros de busqueda.|Listado cronologico de<br>acciones sobre el<br>recurso.|
|**RF-20**|Registrar y notificar<br>incidentes de<br>seguridad.|Sistema, TIC|Tipo de incidente,<br>usuario, recurso.|Incidente registrado;<br>alerta al area TIC.|



### **2.7  Notificaciones** 

|**ID**|**Que permite hacer**|**Actor**|**Informacion**<br>**involucrada**|**Resultado esperado**|
|---|---|---|---|---|
|**RF-21**|Notificar al recibir<br>documento pendiente<br>de validacion.|Sistema -> JEF, RRHH|Documento,<br>funcionario.|Notificacion push en<br>dispositivo del<br>destinatario.|
|**RF-22**|Notificar al funcionario<br>ante aprobacion o|Sistema -> FUN|Documento, estado,|Notificacion push con<br>resultado de la|



Pagina 3 

DSY1105 · Caso CMQ Area Salud · Levantamiento inicial de requerimientos 

||rechazo.||comentario.|validacion.|
|---|---|---|---|---|
|**RF-23**|Notificar vencimiento|Sistema -> FUN, ADM|Documento, fecha de|Alerta 15 dias antes del|
||de documento.||vencimiento.|vencimiento.|



### **2.8  Integracion con SharePoint y Microsoft 365** 

|**ID**|**Que permite hacer**|**Actor**|**Informacion**<br>**involucrada**|**Resultado esperado**|
|---|---|---|---|---|
|**RF-24**|Sincronizar carga de<br>documentos con<br>SharePoint sin copias<br>adicionales.|Sistema|Archivo, metadatos.|Documento unico en<br>SharePoint; referencia<br>en la app.|
|**RF-25**|Consultar y actualizar<br>documentos<br>directamente desde<br>SharePoint.|Sistema|ID documento,<br>cambios.|Cambios reflejados en<br>SharePoint y en la app.|
|**RF-26**|Enviar notificaciones<br>por correo institucional<br>(Microsoft 365).|Sistema|Destinatario, asunto,<br>mensaje.|Correo enviado al<br>buzon institucional.|



## **3.  Requerimientos No Funcionales Criticos** 

|**ID**|**Categoria**|**Descripcion**|
|---|---|---|
|**RNF-01**|Seguridad - Autenticacion|Contrasena cifrada y bloqueo tras 5<br>intentos fallidos.|
|**RNF-02**|Seguridad - Cifrado|TLS 1.2 o superior en transito;<br>cifrado en reposo en dispositivo.|
|**RNF-03**|Seguridad - Trazabilidad|Toda accion sobre un documento<br>debe quedar en bitacora no<br>modificable.|
|**RNF-04**|Seguridad - Minimo privilegio|Cada perfil accede solo a la<br>informacion necesaria.|
|**RNF-05**|Disponibilidad|Operacion con Wi-Fi o datos<br>moviles; offline solo consulta<br>cacheada.|
|**RNF-06**|Desempeno|Consultas y busquedas en menos de<br>3 segundos.|
|**RNF-07**|Compatibilidad|Android 8.0 o superior; iOS en etapa<br>posterior.|
|**RNF-08**|Usabilidad|Interfaz en espanol;maximo 3|



Pagina 4 

DSY1105 · Caso CMQ Area Salud · Levantamiento inicial de requerimientos 

|||toques para acciones frecuentes.|
|---|---|---|
|**RNF-09**|Privacidad|Uso exclusivo de datos ficticios o<br>sinteticos durante el MVP.|
|**RNF-10**|Mantenibilidad|Codigo modular y documentado,<br>separacion de capas.|



## **4.  Cierre del Levantamiento** 

Esta primera version identifica 26 requerimientos funcionales distribuidos en 8 modulos y 10 requerimientos no funcionales criticos. La mayoria de las acciones se concentra en los modulos de carga, consulta y validacion de documentos, coherente con la necesidad de centralizar y dar trazabilidad a la gestion documental. Los modulos de seguridad, trazabilidad e integracion con SharePoint reflejan las reglas de negocio explicitas del caso. La version queda abierta a refinamientos posteriores conforme avancen las etapas de diseno y validacion con el equipo docente y el coordinador CITT. 

Pagina 5 

