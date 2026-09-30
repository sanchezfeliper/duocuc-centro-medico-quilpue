package cl.cmq.salud.ui.screens.login.home

data class HomeUiState(
    val rut: String = "",
    val nombreUsuario: String = "Maria Gonzalez",
    val perfil: String = "Personal Administrativo",
    val centroSalud: String = "CESFAM Quilpue",
    val validacionesPendientes: Int = 3,
    val documentosPorVencer: Int = 2,
    val isSessionActive: Boolean = true
)
