package cl.cmq.salud

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cl.cmq.salud.ui.screens.consulta.ConsultaExpedienteScreen
import cl.cmq.salud.ui.screens.home.HomeScreen
import cl.cmq.salud.ui.screens.login.LoginScreen
import cl.cmq.salud.ui.theme.CMQSaludTheme

enum class AppPantalla {
    LOGIN,
    HOME,
    CONSULTA
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CMQSaludTheme {
                var pantallaActual by remember { mutableStateOf(AppPantalla.LOGIN) }

                when (pantallaActual) {
                    AppPantalla.LOGIN -> {
                        LoginScreen(
                            onLoginSuccess = {
                                pantallaActual = AppPantalla.HOME
                            }
                        )
                    }
                    AppPantalla.HOME -> {
                        HomeScreen(
                            onNavigateConsulta = {
                                pantallaActual = AppPantalla.CONSULTA
                            },
                            onNavigateCarga = {
                                Toast.makeText(this, "Próximamente: Cargar documento", Toast.LENGTH_SHORT).show()
                            },
                            onNavigateValidacion = {
                                Toast.makeText(this, "Próximamente: Validaciones pendientes", Toast.LENGTH_SHORT).show()
                            },
                            onNavigateBitacora = {
                                Toast.makeText(this, "Próximamente: Bitácora de auditoría", Toast.LENGTH_SHORT).show()
                            },
                            onCerrarSesion = {
                                pantallaActual = AppPantalla.LOGIN
                            }
                        )
                    }
                    AppPantalla.CONSULTA -> {
                        ConsultaExpedienteScreen(
                            onVolver = {
                                pantallaActual = AppPantalla.HOME
                            },
                            onDocumentoClick = { idDocumento ->
                                Toast.makeText(this, "Documento seleccionado: #$idDocumento", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}
