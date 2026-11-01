// domain/validation/CatalogoCuentasDemo.kt
package cl.cmq.salud.domain.validation

import cl.cmq.salud.domain.model.Perfil
import cl.cmq.salud.domain.session.SesionActual

/**
 * Cuentas de demostración del MVP (supuesto S-01: autenticación simulada).
 *
 * La identidad real vendrá del backend institucional; mientras no exista,
 * la app reconoce dos cuentas ficticias (RN-03) que determinan el perfil con
 * el que se entra. El RUT del funcionario coincide con el de la semilla
 * (SemillaDocumentos), para que al entrar como funcionario su expediente de
 * ejemplo ya exista en la base local.
 *
 * Las contraseñas son valores de demostración y viven solo aquí; ninguna
 * credencial real se almacena en el repositorio (RN-03).
 */
object CatalogoCuentasDemo {

    /** Cuenta de administración: puede validar y operar sobre cualquier expediente. */
    val ADMIN = CuentaDemo(
        sesion = SesionActual(
            rut = "12.345.678-9",
            nombreCompleto = "Maria Gonzalez",
            perfil = Perfil.ADM,
            correoInstitucional = "maria.gonzalez@cmq.cl"
        ),
        password = "admin123"
    )

    /** Cuenta de funcionario: solo carga y consulta su propio expediente. */
    val FUNCIONARIO = CuentaDemo(
        sesion = SesionActual(
            rut = "18.765.432-7",
            nombreCompleto = "Juan Pérez",
            perfil = Perfil.FUN,
            correoInstitucional = "juan.perez@cmq.cl"
        ),
        password = "funcionario123"
    )

    val cuentas: List<CuentaDemo> = listOf(ADMIN, FUNCIONARIO)

    /**
     * Resuelve las credenciales contra el catálogo de demostración.
     * El RUT se compara ya normalizado (sin espacios) y la contraseña de forma
     * literal. Devuelve la sesión lista para usar, o null si no hay coincidencia.
     */
    fun resolver(rut: String, password: String): SesionActual? {
        val rutNormalizado = rut.trim()
        return cuentas
            .firstOrNull { it.sesion.rut == rutNormalizado && it.password == password }
            ?.sesion
    }

    /**
     * Cuenta de demostración: la sesión que produce y su contraseña ficticia.
     * `descripcion` existe para mostrar una ayuda en pantalla sin exponer la clave.
     */
    data class CuentaDemo(
        val sesion: SesionActual,
        val password: String
    ) {
        val descripcion: String
            get() = "${sesion.perfil.nombre} · RUT ${sesion.rut}"
    }
}
