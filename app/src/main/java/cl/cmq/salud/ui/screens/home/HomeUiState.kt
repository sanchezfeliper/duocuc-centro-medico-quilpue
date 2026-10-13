package cl.cmq.salud.ui.screens.home

data class HomeUiState(
    val rut: String = "",
    val nombreUsuario: String = "Maria Gonzalez",
    val perfil: String = "Personal Administrativo",
    val centroSalud: String = "CESFAM Quilpue",
    val validacionesPendientes: Int = 0,
    val documentosPorVencer: Int = 2,
    // Semana 9.2: total de documentos que viven en el almacenamiento local
    // del dispositivo (SQLite). Hace visible que los datos persisten.
    val documentosEnAlmacenamiento: Int = 0,
    val isSessionActive: Boolean = true
)
