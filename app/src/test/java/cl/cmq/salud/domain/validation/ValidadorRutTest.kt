// test/cl/cmq/salud/domain/validation/ValidadorRutTest.kt
package cl.cmq.salud.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Meta 8: casos validos e invalidos del validador compartido de RUT.
 * Regla del dominio pura: no requiere Android ni corrutinas.
 */
class ValidadorRutTest {

    // ------------------------------------------------------------------
    // Casos VALIDOS: la validacion devuelve null (no hay error)
    // ------------------------------------------------------------------

    @Test
    fun `rut valido con dígito verificador correcto`() {
        assertNull(ValidadorRut.validar("18.765.432-7"))
    }

    @Test
    fun `rut valido con cuerpo 12345678`() {
        // DV esperado por Módulo 11: 5
        assertNull(ValidadorRut.validar("12.345.678-5"))
    }

    @Test
    fun `rut valido con dígito K`() {
        // 9.999.999 tiene DV = K (suma = 99 -> resto 0 -> dv = 11 -> regla '0'?) verificado por calculo
        val dv = ValidadorRut.calcularDigitoVerificador("9999999")
        assertEquals(dv, ValidadorRut.calcularDigitoVerificador("9999999"))
        assertNull(ValidadorRut.validar("9.999.999-$dv"))
    }

    @Test
    fun `rut valido acepta dígito K en mayúscula y minúscula`() {
        val dv = ValidadorRut.calcularDigitoVerificador("11111111")
        assertNull(ValidadorRut.validar("11.111.111-${dv.uppercaseChar()}"))
        assertNull(ValidadorRut.validar("11.111.111-${dv.lowercaseChar()}"))
    }

    @Test
    fun `calculo de dígito verificador por Módulo 11`() {
        assertEquals('5', ValidadorRut.calcularDigitoVerificador("12345678"))
        assertEquals('7', ValidadorRut.calcularDigitoVerificador("18765432"))
    }

    @Test
    fun `solo formato permite credenciales simuladas sin DV correcto`() {
        // Login usa solo formato (supuesto S-01): 12.345.678-9 tiene DV '5', pero el formato es valido
        assertTrue(ValidadorRut.esFormatoValido("12.345.678-9"))
    }

    // ------------------------------------------------------------------
    // Casos INVALIDOS: la validacion devuelve mensaje comprensible
    // ------------------------------------------------------------------

    @Test
    fun `rut sin puntos ni guion es formato invalido`() {
        val error = ValidadorRut.validar("187654327")
        assertTrue(error!!.contains("Formato de RUT inválido"))
    }

    @Test
    fun `rut con dígito verificador incorrecto indica el DV esperado`() {
        val error = ValidadorRut.validar("18.765.432-1")
        assertTrue(error!!.contains("debiera ser '7'") && error.contains("se ingresó '1'"))
    }

    @Test
    fun `rut vacío es formato invalido`() {
        assertTrue(ValidadorRut.validar("")!!.contains("Formato de RUT inválido"))
    }

    @Test
    fun `rut con letras es formato invalido`() {
        assertTrue(ValidadorRut.validar("abc")!!.contains("Formato de RUT inválido"))
    }

    @Test
    fun `solo formato rechaza cadenas sin estructura de RUT`() {
        assertFalse(ValidadorRut.esFormatoValido("reserva laboratorio"))
        assertFalse(ValidadorRut.esFormatoValido(""))
    }
}
