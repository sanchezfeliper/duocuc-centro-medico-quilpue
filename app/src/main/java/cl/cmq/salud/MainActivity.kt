package cl.cmq.salud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cl.cmq.salud.ui.screens.login.home.HomeScreen
import cl.cmq.salud.ui.screens.login.LoginScreen
import cl.cmq.salud.ui.theme.CMQSaludTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CMQSaludTheme {
                var isLoggedIn by remember { mutableStateOf(false) }

                if (!isLoggedIn) {
                    LoginScreen(
                        onLoginSuccess = {
                            isLoggedIn = true
                        }
                    )
                } else {
                    HomeScreen(
                        onNavigateConsulta = { },
                        onNavigateCarga = { },
                        onNavigateValidacion = { },
                        onNavigateBitacora = { },
                        onCerrarSesion = {
                            isLoggedIn = false
                        }
                    )
                }
            }
        }
    }
}

