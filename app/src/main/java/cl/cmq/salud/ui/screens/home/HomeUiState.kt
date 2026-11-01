package cl.cmq.salud.ui.screens.home

import cl.cmq.salud.domain.model.Perfil
import cl.cmq.salud.domain.permissions.PermisosPorPerfil

data class HomeUiState(
    val rut: String = "",
    val nombreUsuario: String = "",
    val perfil: Perfil = Perfil.FUN,
    val centroSalud: String = "",
    // Contadores de la bandeja de revisión: solo tienen sentido para jefatura
    // y administración (RF-15). Para el funcionario quedan en 0.
    val validacionesPendientes: Int = 0,
    val documentosPorVencer: Int = 0,
    // Semana 9.2: total de documentos que viven en el almacenamiento local
    // del dispositivo (SQLite). Hace visible que los datos persisten.
    val documentosEnAlmacenamiento: Int = 0,
    // Separación de perfiles: el funcionario ve el estado de SU expediente,
    // no la bandeja de revisión (que es de administración).
    val misDocumentos: Int = 0,
    val isSessionActive: Boolean = true
) {
    /** Etiqueta legible del perfil para mostrar en pantalla. */
    val perfilNombre: String get() = perfil.nombre

    /** ¿Puede este perfil abrir la bandeja de revisión? (RF-15 / mínimo privilegio) */
    val puedeRevisar: Boolean get() = PermisosPorPerfil.veRevision(perfil)

    /** ¿Puede buscar el expediente de cualquier funcionario? (RF-12/RF-13) */
    val puedeBuscarOtrosRuts: Boolean get() = PermisosPorPerfil.puedeBuscarOtrosRuts(perfil)

    /**
     * RF-08: el funcionario solo carga para su propio RUT; administración
     * puede cargar a nombre de cualquier funcionario.
     */
    val puedeElegirRutDeCarga: Boolean get() = PermisosPorPerfil.puedeCargarParaOtroRut(perfil)
}
