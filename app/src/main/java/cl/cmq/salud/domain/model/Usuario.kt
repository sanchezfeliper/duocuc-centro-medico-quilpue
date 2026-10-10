// domain/model/Usuario.kt
package cl.cmq.salud.domain.model

/**
 * E1 Usuario del modelo de datos preliminar: cuenta de acceso del sistema con
 * perfil y permisos (RF-01, RF-05, RF-07). Meta 7: entidad disenada reflejada
 * en Kotlin; la persistencia con Room llega en una iteracion posterior.
 */
data class Usuario(
    val idUsuario: Int,
    val rut: String,                  // UK, formato 12.345.678-9
    val nombreCompleto: String,
    val correoInstitucional: String,  // UK, correo Microsoft 365
    val idPerfil: Int,                // FK -> Perfil
    val idCentroSalud: Int? = null,   // FK -> CentroSalud (opcional segun perfil)
    val estado: EstadoUsuario = EstadoUsuario.ACTIVO,
    val fechaCreacion: String? = null, // DATETIME de alta en el sistema
    val ultimaSesion: String? = null   // DATETIME del ultimo inicio de sesion
)

enum class EstadoUsuario { ACTIVO, INACTIVO, BLOQUEADO }

/**
 * Catalogo de perfiles del caso (RN-02, RF-07): cada rol accede solo a la
 * informacion necesaria para sus funciones (minimo privilegio).
 */
enum class Perfil(val codigo: String, val nombre: String) {
    FUN("FUN", "Funcionario"),
    ADM("ADM", "Personal administrativo"),
    JEF("JEF", "Jefatura de centro de salud"),
    RRHH("RRHH", "Recursos Humanos"),
    SYS("SYS", "Administrador del sistema"),
    TIC("TIC", "TI y Ciberseguridad")
}
