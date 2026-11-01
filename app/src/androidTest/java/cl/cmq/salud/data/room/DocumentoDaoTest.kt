// androidTest/cl/cmq/salud/data/room/DocumentoDaoTest.kt
package cl.cmq.salud.data.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.cmq.salud.data.local.RoomDocumentoLocalDataSource
import cl.cmq.salud.data.room.dao.DocumentoDao
import cl.cmq.salud.data.room.database.CmqSaludDatabase
import cl.cmq.salud.data.room.entities.DocumentoEntity
import cl.cmq.salud.data.room.entities.FuncionarioEntity
import cl.cmq.salud.domain.model.EstadoDocumento
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas del DAO Room y del data source sobre una base en memoria.
 *
 * Se ejecutan como prueba instrumentada porque Room necesita el motor SQLite
 * de Android. Los datos son sintéticos (RNF-09): no representan antecedentes
 * reales de funcionarios.
 */
@RunWith(AndroidJUnit4::class)
class DocumentoDaoTest {

    private lateinit var db: CmqSaludDatabase
    private lateinit var dao: DocumentoDao

    private val rutFuncionario = "18.765.432-7"

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, CmqSaludDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.documentoDao

        runTest {
            dao.insertarFuncionario(
                FuncionarioEntity(
                    rut = rutFuncionario,
                    nombreCompleto = "Juan Pérez",
                    cargo = "Enfermero",
                    centroSalud = "CESFAM Quilpué",
                    profesion = "Enfermería",
                    tipoContrato = "Planta",
                    fechaIngreso = "2026-02-01",
                    fechaNacimiento = null,
                    situacionContractual = null,
                    correo = "juan.perez@cmq.cl",
                    perfilRol = "FUN"
                )
            )
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun documento(
        id: Long,
        estado: String = "PENDIENTE",
        fechaCarga: Long = 1_700_000_000L,
        rutAsociado: String = rutFuncionario,
        cargador: String = rutFuncionario
    ) = DocumentoEntity(
        idDocumento = id,
        rutFuncionario = rutAsociado,
        rutCargador = cargador,
        tipoDocumento = "PDF",
        estado = estado,
        fechaCarga = fechaCarga,
        fechaModificacion = fechaCarga,
        nombreArchivo = "doc$id.pdf",
        uriLocal = "",
        hash = null,
        descripcion = null,
        fechaVencimiento = null,
        comentarioValidacion = null,
        urlSharePoint = null
    )

    @Test
    fun getDocumentosPorRut_devuelve_los_del_expediente() = runTest {
        dao.insertar(documento(1))

        val resultado = dao.getDocumentosPorRut(rutFuncionario)

        assertEquals(1, resultado.size)
        assertEquals("PDF", resultado[0].tipoDocumento)
    }

    @Test
    fun getDocumentosPorRut_ordena_por_fecha_descendente() = runTest {
        dao.insertar(documento(1, fechaCarga = 100L))
        dao.insertar(documento(2, fechaCarga = 300L))
        dao.insertar(documento(3, fechaCarga = 200L))

        val resultado = dao.getDocumentosPorRut(rutFuncionario)

        assertEquals(listOf(2L, 3L, 1L), resultado.map { it.idDocumento })
    }

    @Test
    fun getDocumentosPorCargador_filtra_por_rut_cargador() = runTest {
        dao.insertar(documento(1, cargador = rutFuncionario))
        dao.insertar(documento(2, cargador = "99.999.999-9"))

        val resultado = dao.getDocumentosPorCargador(rutFuncionario)

        assertEquals(1, resultado.size)
        assertEquals(1L, resultado[0].idDocumento)
    }

    @Test
    fun getDocumentosPorEstado_solo_devuelve_ese_estado() = runTest {
        dao.insertar(documento(1, estado = "PENDIENTE"))
        dao.insertar(documento(2, estado = "APROBADO"))

        val resultado = dao.getDocumentosPorEstado("PENDIENTE")

        assertEquals(1, resultado.size)
        assertEquals(1L, resultado[0].idDocumento)
    }

    @Test
    fun contarDocumentos_refleja_las_inserciones() = runTest {
        assertEquals(0, dao.contarDocumentos())
        dao.insertar(documento(1))
        dao.insertar(documento(2))

        assertEquals(2, dao.contarDocumentos())
    }

    @Test
    fun actualizar_cambia_el_estado_del_documento() = runTest {
        val original = documento(1, estado = "PENDIENTE")
        dao.insertar(original)

        dao.actualizar(original.copy(estado = "APROBADO"))

        assertEquals("APROBADO", dao.getDocumentoById(1L)?.estado)
    }

    @Test
    fun eliminar_quita_el_documento() = runTest {
        val original = documento(1)
        dao.insertar(original)

        dao.eliminar(original)

        assertNull(dao.getDocumentoById(1L))
    }

    /* ------------------------------------------------------------------ */
    /* Data source sobre Room                                              */
    /* ------------------------------------------------------------------ */

    @Test
    fun dataSource_mapea_el_dominio_de_ida_y_vuelta() = runTest {
        val dataSource = RoomDocumentoLocalDataSource(dao)
        val original = documento(1, estado = "PENDIENTE")

        val insertado = dataSource.insertarDocumento(
            cl.cmq.salud.domain.model.Documento(
                idDocumento = 0,
                idFuncionario = 0,
                rutFuncionario = rutFuncionario,
                tipo = "PDF",
                nombreArchivo = "certificado.pdf",
                estado = EstadoDocumento.PENDIENTE,
                fechaCarga = "2026-09-20",
                usuarioCarga = rutFuncionario,
                descripcion = "Certificado"
            )
        )

        val recuperado = dataSource.buscarDocumentoPorId(insertado.idDocumento)
        assertEquals("2026-09-20", recuperado?.fechaCarga)
        assertEquals(EstadoDocumento.PENDIENTE, recuperado?.estado)
        assertTrue(insertado.idDocumento > 0)
    }

    @Test
    fun dataSource_listarPendientes_incluye_pendiente_y_correccion() = runTest {
        val dataSource = RoomDocumentoLocalDataSource(dao)
        dao.insertar(documento(1, estado = "PENDIENTE"))
        dao.insertar(documento(2, estado = "PENDIENTE_CORRECCION"))
        dao.insertar(documento(3, estado = "APROBADO"))

        val pendientes = dataSource.listarPendientes()

        assertEquals(2, pendientes.size)
    }
}
