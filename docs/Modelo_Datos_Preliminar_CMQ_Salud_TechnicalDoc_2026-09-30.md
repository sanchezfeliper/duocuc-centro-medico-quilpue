DSY1105 · Caso CMQ Area Salud · Modelo de datos preliminar 

# **Modelo de Datos Preliminar** 

_Aplicacion movil de gestion documental - CMQ Area Salud_ 

DSY1105 - Desarrollo de Aplicaciones Moviles  ·  Sede CITT Quilpue  ·  30-09-2026 

## **1.  Objetivo y Alcance** 

Este documento presenta la primera version del modelo de datos de la aplicacion movil de gestion documental de la CMQ - Area Salud. Define las principales entidades, sus atributos, claves primarias, claves foraneas y relaciones. El modelo es preliminar y podra modificarse durante el desarrollo, pero constituye una base coherente para las etapas siguientes. Todas las entidades se trabajaran con datos ficticios o sinteticos conforme a la regla RN-03 del caso. 

## **2.  Listado de Entidades** 

|**#**|**Entidad**|**Proposito**|**RF asociados**|
|---|---|---|---|
|**E1**|Usuario|Cuenta de acceso del<br>sistema con perfil y<br>permisos.|RF-01, RF-05, RF-07|
|**E2**|Funcionario|Persona del Area Salud<br>sobre quien recae la<br>documentacion.|RF-12, RF-13|
|**E3**|TipoDocumento|Catalogo de categorias de<br>documentos (contrato,<br>permiso,etc.).|RF-10|
|**E4**|Documento|Archivo digital cargado,<br>asociado a un funcionario.|RF-08 a RF-11, RF-14|
|**E5**|Permiso|Solicitud de<br>permiso/autorizacion del<br>funcionario.|RF-15 a RF-17|
|**E6**|Capacitacion|Curso o perfeccionamiento<br>del funcionario.|RF-10, RF-15|
|**E7**|Bitacora|Registro de auditoria de<br>accesosymodificaciones.|RF-18 a RF-20|
|**E8**|Notificacion|i<br>Alerta enviada a un usuario<br>sobre un evento.|RF-21 a RF-23|
|**E9**|Sesion|Sesion activa del usuario<br>con tokenydispositivo.|RF-01, RF-03, RF-04|



## **3.  Detalle por Entidad** 

### **3.1  E1 - Usuario** 

|**Atributo**|**Tipo de dato**|**Clave**|**Descripcion**|
|---|---|---|---|
|**idUsuario**|INTEGER (auto)|**PK**|Identificador unico del<br>usuario.|
|**rut**|VARCHAR(12)|UK|RUT del usuario ( formato<br>12345678-9).|
|**nombreCompleto**<br>**i**|VARCHAR(120)|-|Nombre y apellido del<br>usuario.<br>t|
|**correoInstitucional**<br>**i**|VARCHAR(120)|UK<br>**i**|Correo Microsoft 365 del<br>usuario.|
|**idPerfil**|INTEGER|**FK -> Perfil**|Rol asignado(FUN,ADM,JEF,|



Pagina 1 

DSY1105 · Caso CMQ Area Salud · Modelo de datos preliminar 

||||RRHH,SYS,TIC).|
|---|---|---|---|
|**idCentroSalud**|INTEGER|**FK -> CentroSalud**|Centro donde se desempena<br>(opcional segunperfil).|
|**estado**|VARCHAR(15)|-|ACTIVO / INACTIVO /<br>BLOQUEADO.|
|**fechaCreacion**|DATETIME|-|Fecha de alta en el sistema.|
|**ultimaSesion**|DATETIME|-|Timestamp del ultimo inicio<br>de sesion.|



### **3.2  E2 - Funcionario** 

|**Atributo**|**Tipo de dato**|**Clave**|**Descripcion**|
|---|---|---|---|
|**idFuncionario**|INTEGER (auto)|**PK**|Identificador unico del<br>funcionario.|
|**rut**|VARCHAR(12)|UK|RUT del funcionario.|
|**nombreCompleto**|VARCHAR(120)|-|Nombreyapellido.|
|**fechaNacimiento**|DATE|-|Fecha de nacimiento.|
|**idCentroSalud**|INTEGER|**FK -> CentroSalud**|CESFAM o CECOSF donde<br>trabaja.|
|**cargo**|VARCHAR(60)|-|Cargoque ocupa.|
|**profesion**|VARCHAR(60)|-|Profesion (enfermero,<br>medico,TENS,etc.).|
|**tipoContrato**|VARCHAR(30)|-|Tipo de contrato (planta,<br>contrata,honorarios).|
|**fechaIngreso**|DATE|-|Fecha de ingreso a la CMQ.|
|**situacionContractual**|VARCHAR(30)|-|Vigente / Finiquitado /<br>Licencia.|



### **3.3  E3 - TipoDocumento** 

|**Atributo**|**Tipo de dato**|**Clave**|**Descripcion**|
|---|---|---|---|
|**idTipoDocumento**|INTEGER(auto)|**PK**|Identificador unico del tipo.<br>i|
|**nombre**|VARCHAR(60)|UK|Nombre del tipo (Contrato,<br>Permiso,Capacitacion,etc.).<br>i|
|**descripcion**|VARCHAR(200)|-|Descripcion del tipo.|
|**diasVigencia**|INTEGER|-|Dias de vigencia por defecto<br>(nullable si no aplica).<br>i|
|**requiereValidacion**|BOOLEAN|-|Indica si el tipo requiere<br>aprobacion dejefatura.|



### **3.4  E4 - Documento** 

|**Atributo**|**Tipo de dato**|**Clave**|**Descripcion**|
|---|---|---|---|
|**idDocumento**|INTEGER (auto)|**PK**|Identificador unico del<br>documento.|
|**idFuncionario**|INTEGER|**FK -> Funcionario**|Funcionario al que pertenece<br>el documento.|
|**idTipoDocumento**|INTEGER|**FK -> TipoDocumento**|Categoria del documento.|
|**idUsuarioCarga**|INTEGER|**FK -> Usuario**|Usuarioque cargo el archivo.|
|**nombreArchivo**|VARCHAR(150)|-|Nombre original del archivo.|
|**urlSharePoint**|VARCHAR(300)|UK|URL unica en SharePoint (sin<br>copias adicionales).|
|**hashArchivo**|VARCHAR(64)|UK|Hash SHA-256 para detectar<br>duplicados(RF-11).|
|**descripcion**|VARCHAR(200)|-|Descripcion ingresada al<br>cargar.|
|**estado**|VARCHAR(20)|-|VIGENTE / PENDIENTE /<br>APROBADO / RECHAZADO /<br>VENCIDO.|



Pagina 2 

DSY1105 · Caso CMQ Area Salud · Modelo de datos preliminar 

|**fechaCarga**|DATETIME|-|Fechayhora de carga.|
|---|---|---|---|
|**fechaVencimiento**|DATE|-|Fecha de vencimiento<br>(nullable).|
|**comentarioValidacion**|VARCHAR(300)|-|Comentario del revisor<br>(nullable).|



### **3.5  E5 - Permiso** 

|**Atributo**|**Tipo de dato**|**Clave**|**Descripcion**|
|---|---|---|---|
|**idPermiso**|INTEGER (auto)|**PK**|Identificador unico del<br>permiso.|
|**idFuncionario**|INTEGER|**FK -> Funcionario**|Funcionario solicitante.|
|**idDocumentoRespaldo**|INTEGER|**FK -> Documento**|Documento de respaldo<br>(nullable).|
|**tipoPermiso**|VARCHAR(40)|-|Administrativo, medico,<br>maternidad,etc.|
|**fechaInicio**|DATE|-|Fecha de inicio delpermiso.|
|**fechaFin**|DATE|-|Fecha de termino del<br>permiso.|
|**estado**|VARCHAR(20)|-|PENDIENTE / APROBADO /<br>RECHAZADO.|
|**idUsuarioValida**|INTEGER|**FK -> Usuario**|Jefatura/RRHH que valida<br>(nullable).|
|**fechaValidacion**|DATETIME|-|Fecha de validacion<br>(nullable).|



### **3.6  E6 - Capacitacion** 

|**Atributo**|**Tipo de dato**|**Clave**|**Descripcion**|
|---|---|---|---|
|**idCapacitacion**|INTEGER (auto)|**PK**|Identificador unico de la<br>capacitacion.|
|**idFuncionario**|INTEGER|**FK -> Funcionario**|Funcionario capacitado.|
|**idDocumentoCertificado**|INTEGER|**FK -> Documento**|Certificado asociado<br>(nullable).|
|**nombreCurso**|VARCHAR(120)|-|Nombre del curso.|
|**institucion**|VARCHAR(120)|-|Institucion que imparte el<br>curso.|
|**horas**|INTEGER|-|Duracion en horas.|
|**fechaRealizacion**|DATE|-|Fecha de realizacion.|
|**estado**|VARCHAR(20)|-|PENDIENTE / VALIDADO /<br>RECHAZADO.|



### **3.7  E7 - Bitacora** 

|**Atributo**|**Tipo de dato**|**Clave**|**Descripcion**|
|---|---|---|---|
|**idBitacora**|INTEGER (auto)|**PK**|Identificador unico del<br>registro de auditoria.|
|**idUsuario**|INTEGER|**FK -> Usuario**|Usuarioque realizo la accion.|
|**idDocumento**|INTEGER|**FK -> Documento**|Documento afectado<br>(nullable).|
|**accion**|VARCHAR(30)|-|LOGIN, CARGA, CONSULTA,<br>MODIFICACION, DESCARGA,<br>RECHAZO.|
|**recurso**|VARCHAR(120)|-|Recurso afectado.<br>i|
|**ipOrigen**<br>**i**|VARCHAR(45)|-|IP del dispositivo.<br>iii|
|**dispositivo**|VARCHAR(120)|-|Identificacion del dispositivo.|
|**fechaHora**|DATETIME|-|Timestamp de la accion (no<br>modificable).|



Pagina 3 

DSY1105 · Caso CMQ Area Salud · Modelo de datos preliminar 

### **3.8  E8 - Notificacion** 

|**Atributo**|**Tipo de dato**|**Clave**|**Descripcion**|
|---|---|---|---|
|**idNotificacion**|INTEGER (auto)|**PK**|Identificador unico de la<br>notificacion.|
|**idUsuarioDestino**|INTEGER|**FK -> Usuario**|Usuario destinatario.|
|**idDocumento**|INTEGER|**FK -> Documento**|Documento relacionado<br>(nullable).|
|**tipo**|VARCHAR(30)|-|PENDIENTE, APROBACION,<br>RECHAZO,VENCIMIENTO.|
|**titulo**|VARCHAR(120)|-|Titulo breve de la<br>notificacion.|
|**mensaje**|VARCHAR(300)|-|Cuerpo del mensaje.|
|**leida**|BOOLEAN|-|Indica si fue leida.|
|**fechaEnvio**|DATETIME|-|Fechayhora de envio.|



### **3.9  E9 - Sesion** 

|**Atributo**|**Tipo de dato**|**Clave**|**Descripcion**|
|---|---|---|---|
|**idSesion**|INTEGER (auto)|**PK**|Identificador unico de la<br>sesion.|
|**idUsuario**|INTEGER|**FK -> Usuario**|Usuario propietario de la<br>sesion.|
|**token**|VARCHAR(200)|UK|Token de sesion cifrado.|
|**dispositivo**|VARCHAR(120)|-|Identificador del dispositivo.|
|**ipInicio**|VARCHAR(45)|-|IP al iniciar sesion.|
|**fechaInicio**|DATETIME|-|Timestampde inicio.|
|**fechaCierre**|DATETIME|-|Timestamp de cierre (nullable<br>mientras activa).|
|**estado**|VARCHAR(15)|-|ACTIVA / CERRADA /<br>EXPIRADA.|



## **4.  Entidades de Apoyo (Catalogos)** 

Adicionalmente, el modelo incluye dos tablas catalogo referenciadas por las entidades principales. Se incluyen aqui de forma sintetica. 

|**Entidad**|**Atributosprincipales**|**Utilidad**|
|---|---|---|
|**Perfil**|idPerfil (PK), codigo (UK:|Soporta el control de acceso por perfil|
|**i**|i<br>FUN/ADM/JEF/RRHH/SYS/TIC),<br>nombre,descripcion.|i<br>(RN-02, RF-07).|
|**CentroSalud**|idCentroSalud (PK), nombre, tipo<br>(CESFAM/CECOSF),direccion,comuna.|Identifica el establecimiento al que<br>pertenece el funcionario o usuario.|



## **5.  Relaciones entre Entidades** 

|**Origen**|**Destino**|**Cardinalidad**|**Descripcion**|
|---|---|---|---|
|**Usuario**|Perfil|N:1|Muchos usuarios pueden<br>tener el mismoperfil.|
|**Usuario**|CentroSalud|N:1|Un usuario pertenece a un<br>centro de salud.|
|**Funcionario**|CentroSalud|N:1|Un funcionario trabaja en<br>un centro.|
|**Documento**|Funcionario|N:1|Un funcionario tiene<br>muchos documentos.|
|**Documento**|TipoDocumento|N:1|Cada documento tiene un<br>tipo.|



Pagina 4 

DSY1105 · Caso CMQ Area Salud · Modelo de datos preliminar 

|**Documento**|Usuario (carga)|N:1|Cada documento es cargado<br>por un usuario.|
|---|---|---|---|
|**Permiso**|Funcionario|N:1|Un funcionario puede tener<br>multiplespermisos.|
|**Permiso**|Documento|1:1|Un permiso puede tener un<br>documento de respaldo.|
|**Permiso**|Usuario (valida)|N:1|Un permiso es validado por<br>un usuario.|
|**Capacitacion**|Funcionario|N:1|Un funcionario puede tener<br>multiples capacitaciones.<br>i|
|**Capacitacion**|Documento|1:1|Una capacitacion tiene un<br>certificado asociado.|
|**Bitacora**|Usuario|N:1|Cada registro de auditoria<br>corresponde a un usuario.|
|**Bitacora**<br>**ii**|Documento|N:1|Un registro puede<br>referenciar un documento.<br>ii i|
|**Notificacion**<br>**ii**|Usuario|N:1|Una notificacion tiene un<br>destinatario.<br>ii|
|**Notificacion**|Documento|N:1|Una notificacion puede<br>referenciar un documento.|
|**Sesion**|Usuario|N:1|Un usuario puede tener<br>multiples sesiones<br>historicas.|



### **5.1  Diagrama ER Simplificado** 



<!-- Start of picture text -->
  +----------+        +-----------+        +---------------+<br>  | Perfil   |1------N| Usuario   |N------1| CentroSalud   |<br>  +----------+        +-----------+        +---------------+<br>                          | 1<br>                          |<br>                          | N<br>                      +-----------+        +---------------+<br>                      | Sesion    |        | Bitacora      |<br>                      +-----------+        +---------------+<br>  +-----------+        +---------------+        +---------------+<br>  | Funcionario|1----N| Documento     |N------1| TipoDocumento |<br>  +-----------+        +---------------+        +---------------+<br>       | 1                    | 1<br>       |                      |<br>       | N                    | N<br>  +-----------+          +-----------+<br>  | Permiso   |          | Notificacion |<br>  +-----------+          +-----------+<br>       | 1<br>       |<br>       | N<br>  +-----------+<br>  | Capacitacion |<br>  +-----------+<br><!-- End of picture text -->

## **6.  Consideraciones de Diseno** 

- Todas las claves primarias son enteros autoincrementales para simplicidad. 

- Los campos hashArchivo y urlSharePoint son unique para evitar duplicados (RF-11, RF-24). 

Pagina 5 

DSY1105 · Caso CMQ Area Salud · Modelo de datos preliminar 

- La bitacora es solo insercion: ningun registro puede modificarse ni eliminarse (RNF-03). 

- Las fechas usan DATETIME para trazabilidad y DATE para vencimientos. 

- El campo estado se maneja como VARCHAR acotado por enums en la capa de dominio. 

- Las relaciones 1:1 (Permiso-Documento, Capacitacion-Certificado) se modelan con FK unique. 

## **7.  Cierre** 

El modelo preliminar define 9 entidades principales y 2 catalogos, con claves primarias, foraneas y relaciones claramente identificadas. La estructura cubre los requerimientos funcionales RF-01 a RF-26 y refleja las reglas de negocio RN-01, RN-02, RN-05 y RN-08. El modelo podra refinarse durante el desarrollo conforme se validen las dudas D-05 (cache local), D-08 (vigencias) y D-09 (repositorio de bitacora) registradas en el Punto 9. 

Pagina 6 

