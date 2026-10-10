package cl.cmq.salud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.cmq.salud.ui.navigation.AppNavHost
import cl.cmq.salud.ui.theme.CMQSaludTheme

/**
 * Meta 5: MainActivity ya no concentra pantallas, reglas ni navegacion.
 * Solo inicializa el tema y delega el grafo de navegacion a AppNavHost
 * (ui/navigation/); el estado y la logica viven en los ViewModels de
 * cada pantalla.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CMQSaludTheme {
                AppNavHost()
            }
        }
    }
}
