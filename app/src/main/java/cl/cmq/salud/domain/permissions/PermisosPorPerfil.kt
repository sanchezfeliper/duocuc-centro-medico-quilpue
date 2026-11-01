// domain/permissions/PermisosPorPerfil.kt
package cl.cmq.salud.domain.permissions

import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.model.Perfil

/**
 * Matriz de permisos por perfil (RF-07 / RNF-04: mínimo privilegio).
 *
 * Las reglas viven en el dominio, no en las pantallas: los ViewModels y la
 * navegación las consultan, y cada operación sensible vuelve a validarlas
 * antes de ejecutarse (defensa en profundidad: ocultar un botón no es una
 * medida de seguridad).
 *
 * El MVP implementa dos perfiles operativos:
 *  - [Perfil.ADM] (administración): valida documentos y opera sobre cualquier
 *    expediente.
 *  - [Perfil.FUN] (funcionario): solo carga y consulta su propia información,
 *    y puede corregir sus documentos mientras no exista una decisión de
 *    jefatura sobre ellos.
 *
 * Los perfiles JEF, RRHH, SYS y TIC existen en el catálogo (E1 Usuario) pero
 * quedan fuera del alcance de esta iteración; todas las reglas los tratan como
 * sin privilegios, para que el día que se activen se deban definir de forma
 * explícita.
 */
object PermisosPorPerfil {

    /** Estados en los que el propio funcionario aún puede corregir o retirar su documento. */
    private val ESTADOS_CORREGIBLES_POR_FUNCIONARIO = setOf(
        EstadoDocumento.PENDIENTE,
        EstadoDocumento.PENDIENTE_CORRECCION
    )

    /** RF-15/RF-16/RF-17: solo jefatura y administración revisan y deciden. */
    fun puedeValidar(perfil: Perfil): Boolean = perfil == Perfil.ADM

    /** RF-12/RF-13: el funcionario consulta su expediente; no busca el de terceros. */
    fun puedeBuscarOtrosRuts(perfil: Perfil): Boolean = perfil == Perfil.ADM

    /**
     * RF-08/RF-09: quién puede cargar un documento a nombre de OTRO funcionario.
     * El funcionario solo carga para sí mismo (el formulario fija su RUT).
     */
    fun puedeCargarParaOtroRut(perfil: Perfil): Boolean = perfil == Perfil.ADM

    /** Acceso a la bitácora de auditoría (RF-19): SYS y TIC en el diseño; fuera del MVP. */
    fun veBitacora(perfil: Perfil): Boolean = false

    /** Acceso a la bandeja de revisión (P06): mismo criterio que [puedeValidar]. */
    fun veRevision(perfil: Perfil): Boolean = puedeValidar(perfil)

    /**
     * ¿Puede [perfil] editar el documento [rutDocumento] que hoy está en [estado]?
     *
     * La administración edita cualquier registro. El funcionario solo los
     * suyos, y únicamente mientras el documento siga en un estado corregible:
     * una vez aprobado, rechazado o vencido, la decisión de jefatura manda.
     */
    fun puedeEditarDocumento(
        perfil: Perfil,
        estado: EstadoDocumento,
        rutDocumento: String,
        rutSesion: String
    ): Boolean = when (perfil) {
        Perfil.ADM -> true
        Perfil.FUN -> esPropio(rutDocumento, rutSesion) && estado in ESTADOS_CORREGIBLES_POR_FUNCIONARIO
        else -> false
    }

    /** Mismo criterio que la edición: borrar es igual de destructivo que modificar. */
    fun puedeEliminarDocumento(
        perfil: Perfil,
        estado: EstadoDocumento,
        rutDocumento: String,
        rutSesion: String
    ): Boolean = puedeEditarDocumento(perfil, estado, rutDocumento, rutSesion)

    /** Normaliza antes de comparar para que espacios accidentales no den un falso "ajeno". */
    private fun esPropio(rutDocumento: String, rutSesion: String): Boolean =
        rutDocumento.trim() == rutSesion.trim()
}
