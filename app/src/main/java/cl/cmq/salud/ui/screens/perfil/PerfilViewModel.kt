// ui/screens/perfil/PerfilViewModel.kt
package cl.cmq.salud.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.di.AppContainer
import cl.cmq.salud.domain.session.SesionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla Perfil.
 *
 * La identidad viene de la sesión activa; los datos laborales (cargo y
 * centro) se completan leyendo E2 Funcionario de la base por RUT, tal como
 * quedó definido en el plan: la pantalla no muestra datos en memoria que
 * puedan quedar desfasados de lo persistido.
 */
class PerfilViewModel(
    private val repo: DocumentRepository = AppContainer.documentRepository,
    private val sesion: SesionActual? = AppContainer.sesionActual.value
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PerfilUiState(
            rut = sesion?.rut.orEmpty(),
            nombreCompleto = sesion?.nombreCompleto.orEmpty(),
            correoInstitucional = sesion?.correoInstitucional.orEmpty(),
            perfil = sesion?.perfil ?: PerfilUiState().perfil
        )
    )
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init { cargarDatosLaborales() }

    /** Completa cargo y centro desde la base por RUT (E2 Funcionario). */
    private fun cargarDatosLaborales() {
        val sesionActiva = sesion ?: return
        _uiState.value = _uiState.value.copy(cargando = true)
        viewModelScope.launch {
            val funcionario = repo.buscarFuncionario(sesionActiva.rut)
            _uiState.value = _uiState.value.copy(
                cargo = funcionario?.cargo.orEmpty(),
                centroSalud = funcionario?.centroSalud.orEmpty(),
                cargando = false
            )
        }
    }
}
