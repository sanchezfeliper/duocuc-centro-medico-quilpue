// domain/session/SesionActual.kt
package cl.cmq.salud.domain.session

import cl.cmq.salud.domain.model.Perfil

/**
 * Sesión activa del usuario logueado (E9 Sesion, versión simplificada del MVP).
 *
 * Guarda en memoria quién entró y con qué perfil, de modo que Home, Carga,
 * Consulta, Detalle y Validación puedan decidir qué mostrar sin volver a
 * repetir datos ficticios dispersos (RN-03).
 *
 * No se persiste en disco: RF-02 exige limpiar los datos sensibles del
 * dispositivo al cerrar sesión, por lo que al reiniciar la app el usuario
 * vuelve a autenticarse.
 */
data class SesionActual(
    val rut: String,
    val nombreCompleto: String,
    val perfil: Perfil,
    val correoInstitucional: String
)
