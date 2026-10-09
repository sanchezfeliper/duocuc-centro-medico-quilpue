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
import cl.cmq.salud.ui.screens.carga.CargaDocumentoScreen
import cl.cmq.salud.ui.screens.consulta.ConsultaExpedienteScreen
import cl.cmq.salud.ui.screens.home.HomeScreen
import cl.cmq.salud.ui.screens.login.LoginScreen
import cl.cmq.salud.ui.theme.CMQSaludTheme

enum class AppPantalla {
    LOGIN,
    HOME,
    CONSULTA,
    CARGA
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
                                pantallaActual = AppPantalla.CARGA
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
                    AppPantalla.CARGA -> {
                        CargaDocumentoScreen(
                            onVolver = {
                                pantallaActual = AppPantalla.HOME
                            },
                            onCargaExitosa = { idDocumento ->
                                pantallaActual = AppPantalla.HOME
                                Toast.makeText(this, "Documento #$idDocumento registrado exitosamente", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}
