package cl.cmq.salud.data.room

import android.content.Context
import androidx.room.Room
import cl.cmq.salud.data.room.dao.DocumentoDao
import cl.cmq.salud.data.room.database.CmqSaludDatabase
import cl.cmq.salud.data.room.entities.DocumentoEntity
import cl.cmq.salud.domain.model.EstadoDocumento
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias del DAO Room con base de datos en memoria.
 *
 * La base se abre con `Room.inMemoryDatabaseBuilder` y se cierrca tras cada
 * test. Los datos son sintéticos (RNF-09): nunca representan antecedentes
 * reales de funcionarios.
 */
class DocumentoDaoTest {

    private lateinit var dao: DocumentoDao
    private lateinit var db: CmqSaludDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            getContext(),
            CmqSaludDatabase::class.java
        ).build()
        dao = db.documentoDao
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `getDocumentosPorRut devuelve los documentos de un RUT`() = runTest {
        // given
        val entity = DocumentoEntity(
            idDocumento = 1L,
            rutFuncionario = "18.765.432-7",
            rutCargador = "18.765.432-7",
            tipoDocumento = "PDF",
            estado = "PENDIENTE",
            fechaCarga = 1_700_000_000L,
            fechaModificacion = 1_700_000_000L,
            nombreArchivo = "test.pdf",
            uriLocal = "",
            hash = null,
            descripcion = null
        )
        dao.insertar(entity)

        // when
        val result = dao.getDocumentosPorRut("18.765.432-7")

        // then
        assertEquals(1, result.size)
        assertEquals("PDF", result[0].tipoDocumento)
        assertEquals("test.pdf", result[0].nombreArchivo)
    }

    @Test
    fun `getDocumentosPorRut ordena por fechaCarga descendente`() = runTest {
        // given
        val e1 = DocumentoEntity(1L, "r", "r", "PDF", "PENDIENTE", 100L, 100L, "a.pdf", "", null, null)
        val e2 = DocumentoEntity(2L, "r", "r", "PDF", "PENDIENTE", 200L, 200L, "b.pdf", "", null, null)
        dao.insertar(e1)
        dao.insertar(e2)

        // when
        val result = dao.getDocumentosPorRut("r")

        // then
        assertEquals(2, result.size)
        assertEquals(2L, result[0].idDocumento)
        assertEquals(1L, result[1].idDocumento)
    }

    @Test
    fun `getDocumentosPorEstado filtra por estado`() = runTest {
        // given
        val pendiente = DocumentoEntity(1L, "r", "r", "PDF", "PENDIENTE", 100L, 100L, "a.pdf", "", null, null)
        val aprobado = DocumentoEntity(2L, "r", "r", "PDF", "APROBADO", 200L, 200L, "b.pdf", "", null, null)
        dao.insertar(pendiente)
        dao.insertar(aprobado)

        // when
        val result = dao.getDocumentosPorEstado("PENDIENTE")

        // then
        assertEquals(1, result.size)
        assertEquals("PENDIENTE", result[0].estado)
    }

    @Test
    fun `insertar devuelve el id de fila generado`() = runTest {
        // when
        val id = dao.insertar(
            DocumentoEntity(
                idDocumento = 0L, rutFuncionario = "r", rutCargador = "r",
                tipoDocumento = "PDF", estado = "VIGENTE", fechaCarga = 100L,
                fechaModificacion = 100L, nombreArchivo = "x.pdf", uriLocal = "",
                hash = null, descripcion = null
            )
        )

        // then
        assertTrue(id > 0)
    }

    @Test
    fun `getDocumentoById devuelve null si no existe`() = runTest {
        // when
        val result = dao.getDocumentoById(999L)

        // then
        assertTrue(result == null)
    }

    @Test
    fun `getFuncionarioByRut devuelve el funcionario si existe`() = runTest {
        // given
        val funcionario = FuncionarioEntity(
            rut = "r",
            nombreCompleto = "Test",
            cargo = "Test",
            profesion = "Test",
            centroSalud = "Test",
            tipoContrato = "Test",
            fechaIngreso = 100L,
            correo = "test@test.com",
            perfilRol = "FUN"
        )
        dao.insertarFuncionario(funcionario)

        // when / then
        val encontrado = dao.getFuncionarioByRut("r")
        assertTrue(encontrado != null)
        assertEquals("r", encontrado!!.rut)
    }

    @Test
    fun `getFuncionarioByRut devuelve null si no existe`() = runTest {
        val encontrado = dao.getFuncionarioByRut("no_existe")
        assertTrue(encontrado == null)
    }
}

/* ------------------------------------------------------------------ */
/* Context mínimo para pruebas en JVM (solo lo necesario para Room)   */
/* ------------------------------------------------------------------ */

private fun getContext(): Context = TestContext()
    private val m = mutableMapOf<String, Any?>()

    override fun getPackageName(): String = "cl.cmq.salud"

    @Synchronized
    override fun getCacheDir(): java.io.File = throw UnsupportedOperationException()

    @Synchronized
    override fun getFilesDir(): java.io.File = throw UnsupportedOperationException()

    @Synchronized
    override fun openFileInput(name: String): java.io.InputStream? = null

    @Synchronized
    override fun openFileOutput(name: String, mode: Int): java.io.OutputStream? =
        throw UnsupportedOperationException()

    @Synchronized
    override fun deleteFile(name: String): Boolean = false

    @Synchronized
    override fun fileList(): Array<String> = emptyArray()

    @Synchronized
    override fun getDir(name: String, mode: Int): java.io.File =
        throw UnsupportedOperationException()

    @Synchronized
    override fun getExternalFilesDir(type: String): java.io.File? = null

    @Synchronized
    override fun getExternalCacheDir(): java.io.File? = null

    @Synchronized
    override fun getObbDir(): java.io.File? = null

    @Synchronized
    override fun getDatabasePath(name: String): java.io.File = throw UnsupportedOperationException()

    @Synchronized
    override fun getDir(name: String, mode: Int): java.io.File = throw UnsupportedOperationException()

    override fun getSharedPreferences(name: String?, mode: Int): android.content.SharedPreferences? =
        throw UnsupportedOperationException()
}
