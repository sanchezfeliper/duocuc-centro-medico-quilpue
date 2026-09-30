package cl.cmq.salud.ui.screens.login

data class LoginUiState(
    val rut: String = "",
    val password: String = "",
    val isAuthenticating: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)