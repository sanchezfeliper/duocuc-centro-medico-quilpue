// data/local/CmqSqliteHelper.kt
package cl.cmq.salud.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Base de datos local del dispositivo (Meta Semana 9 - Persistencia local).
 *
 * Se usa SQLite a través de SQLiteOpenHelper, incluido en el SDK de Android:
 * almacenamiento relacional real sin agregar dependencias ni procesadores de
 * anotaciones al build. La primera vez que se abre la base se cargan los datos
 * ficticios de demostración (RN-03).
 *
 * Tablas: funcionarios (E2) y documentos (E4), coherentes con el modelo de
 * datos preliminar del proyecto.
 */
class CmqSqliteHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    NOMBRE_BD,
    null,
    VERSION_BD
) {

    companion object {
        const val NOMBRE_BD = "cmq_salud.db"
        // v2 (Semana 9.3): agrega la columna ruta_local (fotografia capturada).
        const val VERSION_BD = 2

        const val TABLA_FUNCIONARIOS = "funcionarios"
        const val TABLA_DOCUMENTOS = "documentos"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLA_FUNCIONARIOS (
                id_funcionario INTEGER PRIMARY KEY AUTOINCREMENT,
                rut TEXT NOT NULL UNIQUE,
                nombre_completo TEXT NOT NULL,
                cargo TEXT NOT NULL,
                centro_salud TEXT NOT NULL,
                profesion TEXT,
                tipo_contrato TEXT,
                fecha_ingreso TEXT,
                fecha_nacimiento TEXT,
                situacion_contractual TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLA_DOCUMENTOS (
                id_documento INTEGER PRIMARY KEY AUTOINCREMENT,
                id_funcionario INTEGER NOT NULL,
                rut_funcionario TEXT NOT NULL,
                tipo TEXT NOT NULL,
                nombre_archivo TEXT NOT NULL,
                estado TEXT NOT NULL,
                fecha_carga TEXT NOT NULL,
                usuario_carga TEXT NOT NULL,
                descripcion TEXT,
                fecha_vencimiento TEXT,
                comentario_validacion TEXT,
                hash_archivo TEXT,
                url_sharepoint TEXT,
                ruta_local TEXT
            )
            """.trimIndent()
        )

        cargarSemilla(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // MVP académico: la base se recrea al cambiar de versión (datos ficticios)
        db.execSQL("DROP TABLE IF EXISTS $TABLA_DOCUMENTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_FUNCIONARIOS")
        onCreate(db)
    }

    /** Inserta el expediente ficticio de demostración con IDs explícitos (1..4). */
    private fun cargarSemilla(db: SQLiteDatabase) {
        SemillaDocumentos.funcionarios().forEach { f ->
            db.insert(TABLA_FUNCIONARIOS, null, f.toContentValues(conId = true))
        }

        SemillaDocumentos.documentos().forEach { d ->
            db.insert(TABLA_DOCUMENTOS, null, d.toContentValues(conId = true))
        }
    }
}
