// domain/validation/ValidadorRut.kt
package cl.cmq.salud.domain.validation

/**
 * Validador de RUT chileno compartido por toda la app (Meta 5).
 * La regla vive en la capa de dominio y es consumida por los ViewModels;
 * las Views no la conocen.
 */
object ValidadorRut {

    // Formato chileno con puntos, guion y digito verificador
    private val REGEX = Regex("^\\d{1,2}\\.\\d{3}\\.\\d{3}-[\\dkK]$")

    /** Solo formato (sin dígito verificador): usado donde el RUT es credencial simulada. */
    fun esFormatoValido(valor: String): Boolean = REGEX.matches(valor.trim())

    /**
     * Valida formato y dígito verificador de un RUT NO vacío (Meta 3).
     * La regla de obligatoriedad es de cada formulario: aqui se asume
     * que el valor ya tiene contenido y se devuelve el mensaje de error
     * exacto, o null si es valido.
     */
    fun validar(valor: String): String? {
        val rut = valor.trim()
        if (!REGEX.matches(rut)) {
            return "Formato de RUT inválido: escríbalo como 18.765.432-7 (puntos miles, guión y dígito verificador, sin espacios)."
        }
        val cuerpo = rut.dropLast(2).filter { it.isDigit() }
        val dvIngresado = rut.last().uppercaseChar()
        val dvEsperado = calcularDigitoVerificador(cuerpo)
        if (dvIngresado != dvEsperado) {
            return "El dígito verificador no coincide: para este RUT el carácter tras el guión debiera ser '$dvEsperado' y se ingresó '$dvIngresado'. Verifique el RUT del funcionario."
        }
        return null
    }

    /**
     * Regla propia del proyecto (Meta 3): cálculo del dígito verificador
     * del RUT chileno mediante Módulo 11. Devuelve un dígito '0' a '9'
     * o la letra 'K' cuando el resultado es 10.
     */
    fun calcularDigitoVerificador(cuerpo: String): Char {
        var suma = 0
        var multiplicador = 2
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        val resto = suma % 11
        return when (val dv = 11 - resto) {
            11 -> '0'
            10 -> 'K'
            else -> ('0' + dv)
        }
    }
}
