// domain/model/CentroSalud.kt
package cl.cmq.salud.domain.model

/**
 * Catalogo CentroSalud del modelo de datos: establecimiento de la red APS al
 * que pertenece el funcionario o usuario (CESFAM / CECOSF de Quilpue).
 */
data class CentroSalud(
    val idCentroSalud: Int,
    val nombre: String,
    val tipo: TipoCentroSalud = TipoCentroSalud.CESFAM,
    val direccion: String? = null,
    val comuna: String = "Quilpué"
)

enum class TipoCentroSalud { CESFAM, CECOSF }
