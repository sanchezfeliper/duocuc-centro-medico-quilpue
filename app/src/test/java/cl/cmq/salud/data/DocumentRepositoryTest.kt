// test/cl/cmq/salud/data/DocumentRepositoryTest.kt
package cl.cmq.salud.data

import cl.cmq.salud.data.local.FakeDocumentoLocalDataSource
import cl.cmq.salud.data.local.SemillaDocumentos
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Meta 8: casos validos e invalidos del repositorio compartido por
 * P03 Consulta, P05 Carga y P06 Validacion. Sin Android ni Compose.
 *
 * El almacenamiento vive en la fuente de datos local (Semana 9: SQLite en la
 * app). Los tests inyectan un doble en memoria con la misma semilla, por lo
 * que cada test parte de un estado conocido y nunca depende de otros.
 */
class DocumentRepositoryTest {

    private val repo = DocumentRepository(FakeDocumentoLocalDataSource())

    private var secuencia = 0
    private suspend fun nuevoDocumento(
        rut: String = "20.111.222-2",
        estado: EstadoDocumento = EstadoDocumento.PENDIENTE
    ): Documento = repo.guardarDocumento(
        Documento(
            idDocumento = 0, idFuncionario = 0, rutFuncionario = rut,
            tipo = "PDF", nombreArchivo = "doc_prueba_${secuencia++}.pdf",
            estado = estado, fechaCarga = "2026-10-10",
            usuarioCarga = "Test", descripcion = "Documento de prueba"
        )
    )

    // ------------------------------------------------------------------
    // Casos VALIDOS
    // ------------------------------------------------------------------

    @Test
    fun `guardar documento lo registra en el expediente de su RUT`() = runBlocking {
        val guardado = nuevoDocumento(rut = "20.111.222-2")

        assertTrue(guardado.idDocumento > 0)
        assertTrue(guardado.idFuncionario > 0) // funcionario resuelto por el RUT

        val expediente = repo.buscarPorRut("20.111.222-2")
        assertTrue(expediente.any { it.idDocumento == guardado.idDocumento })
    }

    @Test
    fun `buscarPorRut devuelve solo los documentos de ese RUT`() = runBlocking {
        val docA = nuevoDocumento(rut = "20.111.222-2")
        nuevoDocumento(rut = "20.999.888-7")

        val expedienteA = repo.buscarPorRut("20.111.222-2")
        assertTrue(expedienteA.isNotEmpty())
        assertTrue(expedienteA.any { it.idDocumento == docA.idDocumento })
        assertTrue(expedienteA.all { it.rutFuncionario == "20.111.222-2" }) // sin documentos cruzados
    }

    @Test
    fun `registrarDecision aproba un documento pendiente y deja el comentario`() = runBlocking {
        val pendiente = nuevoDocumento()
        val actualizado = repo.registrarDecision(
            pendiente.idDocumento, EstadoDocumento.APROBADO, "Antecedentes completos"
        )

        assertNotNull(actualizado)
        assertEquals(EstadoDocumento.APROBADO, actualizado!!.estado)
        assertEquals("Antecedentes completos", actualizado.comentarioValidacion)
    }

    @Test
    fun `listarPendientes incluye pendiente y pendiente de correccion`() = runBlocking {
        // Un PENDIENTE_CORRECCION vuelve a la bandeja del revisor (RF-17)
        val pendiente = nuevoDocumento()
        repo.registrarDecision(pendiente.idDocumento, EstadoDocumento.PENDIENTE_CORRECCION, "Falta anexo")

        val bandeja = repo.listarPendientes()
        assertTrue(bandeja.any {
            it.idDocumento == pendiente.idDocumento && it.estado == EstadoDocumento.PENDIENTE_CORRECCION
        })
    }

    @Test
    fun `buscarPorId devuelve el documento existente`() = runBlocking {
        val encontrado = repo.buscarPorId(1) // documento semilla del repositorio
        assertNotNull(encontrado)
        assertEquals(1, encontrado!!.idDocumento)
    }

    // ------------------------------------------------------------------
    // Semana 9.2: visualizacion de lo guardado (conteo local)
    // ------------------------------------------------------------------

    @Test
    fun `contarDocumentos parte con la semilla y crece al guardar`() = runBlocking {
        val inicial = repo.contarDocumentos()
        assertEquals(SemillaDocumentos.documentos().size, inicial)

        nuevoDocumento(rut = "20.111.222-2")

        assertEquals(inicial + 1, repo.contarDocumentos())
    }

    // ------------------------------------------------------------------
    // Semana 9.3: captura con camara (RF-09/RF-11)
    // ------------------------------------------------------------------

    @Test
    fun `guardar documento con captura conserva la ruta local y la huella`() = runBlocking {
        val guardado = repo.guardarDocumento(
            Documento(
                idDocumento = 0, idFuncionario = 0, rutFuncionario = "20.111.222-2",
                tipo = "JPG", nombreArchivo = "captura_x.jpg",
                estado = EstadoDocumento.VIGENTE, fechaCarga = "2026-10-12",
                usuarioCarga = "Test", descripcion = "Documento capturado con camara",
                hashArchivo = "hash123", rutaLocal = "fotos/captura_x.jpg"
            )
        )

        val recuperado = repo.buscarPorId(guardado.idDocumento)
        assertNotNull(recuperado)
        assertEquals("fotos/captura_x.jpg", recuperado!!.rutaLocal)
        assertEquals("hash123", recuperado.hashArchivo)
    }

    // ------------------------------------------------------------------
    // Semana 10: CRUD completo (editar / eliminar)
    // ------------------------------------------------------------------

    @Test
    fun `actualizarDocumento cambia los campos y conserva el folio`() = runBlocking {
        val original = nuevoDocumento(rut = "20.111.222-2")

        val editado = repo.actualizarDocumento(
            original.copy(nombreArchivo = "renombrado.pdf", descripcion = "Descripción corregida")
        )

        assertNotNull(editado)
        assertEquals(original.idDocumento, editado!!.idDocumento) // mismo folio
        val recuperado = repo.buscarPorId(original.idDocumento)!!
        assertEquals("renombrado.pdf", recuperado.nombreArchivo)
        assertEquals("Descripción corregida", recuperado.descripcion)
    }

    @Test
    fun `eliminarDocumento borra el registro del almacenamiento`() = runBlocking {
        val doc = nuevoDocumento(rut = "20.111.222-2")
        val antes = repo.contarDocumentos()

        assertTrue(repo.eliminarDocumento(doc.idDocumento))

        assertEquals(antes - 1, repo.contarDocumentos())
        assertNull(repo.buscarPorId(doc.idDocumento))
    }

    @Test
    fun `eliminarDocumento con ID inexistente devuelve false`() = runBlocking {
        assertTrue(!repo.eliminarDocumento(99999))
    }

    @Test
    fun `actualizarDocumento con ID inexistente devuelve null`() = runBlocking {
        val fantasma = Documento(
            idDocumento = 99999, idFuncionario = 1, rutFuncionario = "20.111.222-2",
            tipo = "PDF", nombreArchivo = "x.pdf", estado = EstadoDocumento.VIGENTE,
            fechaCarga = "2026-10-31", usuarioCarga = "Test", descripcion = "No existe"
        )
        assertNull(repo.actualizarDocumento(fantasma))
    }

    // ------------------------------------------------------------------
    // Casos INVALIDOS
    // ------------------------------------------------------------------

    @Test
    fun `registrarDecision con ID inexistente devuelve null`() = runBlocking {
        assertNull(repo.registrarDecision(99999, EstadoDocumento.APROBADO, "Comentario"))
    }

    @Test
    fun `buscarPorId inexistente devuelve null`() = runBlocking {
        assertNull(repo.buscarPorId(99999))
    }

    @Test
    fun `expediente de RUT sin documentos esta vacio`() = runBlocking {
        assertTrue(repo.buscarPorRut("3.333.333-3").isEmpty())
    }

    @Test
    fun `decision aprobatoria con comentario en blanco no guarda comentario`() = runBlocking {
        val pendiente = nuevoDocumento()
        val actualizado = repo.registrarDecision(pendiente.idDocumento, EstadoDocumento.APROBADO, "   ")
        assertTrue(actualizado!!.comentarioValidacion == null) // "   ".ifBlank -> null
    }
}
