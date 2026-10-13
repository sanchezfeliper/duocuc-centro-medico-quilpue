// test/cl/cmq/salud/data/local/HuellaArchivoTest.kt
package cl.cmq.salud.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Semana 9.3: la huella SHA-256 (RF-11) se calcula sobre el contenido capturado.
 * Se verifica contra vectores conocidos del algoritmo, sin Android.
 */
class HuellaArchivoTest {

    @Test
    fun `sha256 de abc coincide con el vector conocido`() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            HuellaArchivo.sha256Hex("abc".toByteArray())
        )
    }

    @Test
    fun `sha256 de contenido vacio coincide con el vector conocido`() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            HuellaArchivo.sha256Hex(ByteArray(0))
        )
    }

    @Test
    fun `huellas de contenidos distintos difieren`() {
        assertNotEquals(
            HuellaArchivo.sha256Hex("documento A".toByteArray()),
            HuellaArchivo.sha256Hex("documento B".toByteArray())
        )
    }
}
