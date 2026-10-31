package cl.cmq.salud.ui.screens.home

data class HomeUiState(
    val rut: String = "",
    val nombreUsuario: String = "Maria Gonzalez",
    val perfil: String = "Personal Administrativo",
    val centroSalud: String = "CESFAM Quilpué",
    val validacionesPendientes: Int = 0,
    val documentosPorVencer: Int = 2,
    val isSessionActive: Boolean = true
)
