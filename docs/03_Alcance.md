DSY1105 · Caso CMQ Area Salud · Definicion inicial del alcance 

# **Definicion Inicial del Alcance** 

_Gestion documental movil de funcionarios - Corporacion Municipal de Quilpue (CMQ), Area Salud_ 

DSY1105 - Desarrollo de Aplicaciones Moviles  ·  Sede CITT Quilpue  ·  28-09-2026 

## **1.  Objetivo del Documento** 

Este documento define el alcance inicial del MVP del sistema movil de gestion documental de la CMQ - Area Salud. Establece que funcionalidades SI se construiran durante el semestre y cuales NO se incluiran en esta version. La definicion responde al principio de priorizar una solucion funcional, coherente y terminada, antes que una aplicacion con muchas funcionalidades incompletas. El alcance se basa en los requerimientos levantados en el Punto 2 y en las reglas de negocio explicitas del caso. 

## **2.  Criterios de Priorizacion** 

El alcance se definio aplicando los siguientes criterios, en orden de importancia: 

- Critico para el negocio: funcionalidades que resuelven el problema central (dispersion documental y falta de trazabilidad). 

- Viable en el semestre: funcionalidades desarrollables por un equipo estudiantil en el plazo disponible. 

- Coherencia con reglas de negocio: seguridad desde el diseño, control de accesos y uso de datos ficticios. 

- Integracion minima: solo SharePoint y Microsoft 365, sin sistemas institucionales adicionales. 

- Experiencia completa: pocas funcionalidades, pero terminadas y verificables de extremo a extremo. 

## **3.  Que SI Realizara la Aplicacion** 

La aplicacion movil construira las siguientes funcionalidades, agrupadas por modulo. Cada modulo se entregara completo y verificable. 

|**Modulo**|**Funcionalidades incluidas**|**Cobertura**|
|---|---|---|
|**Autenticacion y Sesion**<br>**i**|Inicio de sesion con RUT y<br>contrasena; cierre de sesion;<br>bloqueo por inactividad; registro de<br>intentos fallidos.|RF-01 a RF-04|
|**Perfiles y Permisos**|Creacion y edicion de usuarios;<br>asignacion de permisos por perfil;<br>restriccion de acceso por minimo<br>privilegio.|RF-05 a RF-07|
|**Carga y Digitalizacion**|Carga de archivos PDF, JPG y PNG;<br>captura de documentos fisicos con<br>camara; asignacion de tipo y estado;<br>rechazo de duplicados.|RF-08 a RF-11|
|**Consulta y Busqueda**|Consulta de expediente por<br>funcionario; busqueda por filtros;<br>visualizacion y descarga con registro<br>de acceso.|RF-12 a RF-14|
|**Validacion y Aprobacion**|Revision de documentos pendientes;<br>aprobacion o rechazo con<br>comentario;solicitud de correccion.<br>i|RF-15 a RF-17|
|**Trazabilidad y Auditoria**|Registro automatico de accesos,<br>cargas,modificacionesydescargas;|RF-18 a RF-20|



Pagina 1 

DSY1105 · Caso CMQ Area Salud · Definicion inicial del alcance 

||consulta de bitacora; registro de<br>incidentes.||
|---|---|---|
|**Notificaciones**|Notificaciones push para<br>documentos pendientes,<br>aprobaciones, rechazos y<br>vencimientos.|RF-21 a RF-23|
|**Integracion con SharePoint**|Sincronizacion de documentos sin<br>copias adicionales; consulta y<br>actualizacion desde SharePoint;<br>notificaciones por correo Microsoft<br>365.|RF-24 a RF-26|



## **4.  Que No Realizara la Aplicacion en Esta Version** 

Para asegurar la viabilidad del proyecto en el semestre, las siguientes funcionalidades quedan explicitamente fuera del alcance de esta version: 

|**Categoria**|**Funcionalidades excluidas**|**Justificacion**|
|---|---|---|
|**Plataforma**|Aplicacion nativa para iOS.<br>i   i|Se prioriza Android por mayor<br>cobertura en el entorno<br>institucional; iOS queda para etapa<br>posterior.|
|**Atencion clinica**|Gestion de citas, fichas clinicas,<br>recetas electronicas y atencion de<br>pacientes.|El caso se acota a documentacion de<br>funcionarios, no a atencion clinica.|
|**Pagos y facturacion**|Pasarelas de pago, cobros, copagos y<br>facturacion electronica.|No aplica al proceso de gestion<br>documental interna.|
|**Telesalud**|Videollamadas, consultas remotas y<br>mensajeria clinica.|Fuera del foco del caso.|
|**Integraciones adicionales**|Integracion con sistemas<br>institucionales de gestion de<br>funcionarios, RRHH externos y otras<br>plataformas.|Sujetas a evaluacion de TI y<br>Ciberseguridad; no se desarrollaran<br>en el MVP.|
|**Biometria**|Inicio de sesion con huella,<br>reconocimiento facial u otros<br>metodos biometricos.|Complejidad tecnica fuera del<br>alcance semestral.|
|**Modo offline completo**|Edicion y carga de documentos sin<br>conexion a Internet.|Solo se permite consulta cacheada;<br>las cargas requieren conexion.|
|**Reportes avanzados**<br>**i**|Tableros analiticos, metricas<br>predictivas y exportacion masiva a<br>BI.<br>i|Se prioriza la operacion basica; los<br>reportes quedan para una etapa<br>posterior.|
|**Despliegue productivo**|Publicacion en tiendas de<br>aplicaciones y operacion en el<br>entorno real de la CMQ.|El entregable es un MVP academico,<br>no productivo.|
|**Datos reales**|Uso de antecedentes reales de<br>funcionarios, listados o credenciales<br>institucionales.|Prohibido por las reglas del caso;<br>solo datos ficticios o sinteticos.|



## **5.  Limites de la Version y Supuestos** 

- El MVP se evaluara con datos ficticios o sinteticos provistos o generados por el equipo. 

- La integracion con SharePoint se simulara o usara una cuenta de prueba academica, no el entorno institucional real. 

Pagina 2 

DSY1105 · Caso CMQ Area Salud · Definicion inicial del alcance 

- Las notificaciones push y el correo Microsoft 365 se probaran con un entorno de desarrollo (sandbox). 

- La aplicacion se probacuten en dispositivos Android 8.0 o superior disponibles para el equipo. 

- El rendimiento y la escalabilidad se validaran con un volumen acotado de documentos (miles, no millones). 

## **6.  Criterios de Aceptacion del MVP** 

El MVP se considerara terminado cuando cumpla los siguientes criterios: 

- Los 8 modulos del alcance SI estan implementados y operativos. 

- Todos los requerimientos RF-01 a RF-26 son verificables mediante pruebas funcionales. 

- La integracion con SharePoint funciona para cargar, consultar y actualizar documentos. 

- La bitacora de auditoria registra correctamente todas las acciones criticas. 

- El control de acceso por perfil impide accesos no autorizados en los casos de prueba. 

- La aplicacion se ejecuta sin errores bloqueantes en Android 8.0 o superior. 

- La documentacion tecnica y el manual de uso estan completos. 

## **7.  Cierre** 

El alcance definido privilegia una solucion movil funcional, coherente y terminada por sobre una aplicacion extensa e incompleta. Las funcionalidades excluidas quedan documentadas para futuras etapas del proyecto. Esta definicion se revisara al final del Punto 4 (casos de uso) para confirmar su consistencia antes de iniciar el desarrollo. 

Pagina 3 

