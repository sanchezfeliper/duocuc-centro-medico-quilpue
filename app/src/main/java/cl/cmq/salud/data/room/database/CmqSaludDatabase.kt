package cl.cmq.salud.data.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.cmq.salud.data.room.dao.DocumentoDao
import cl.cmq.salud.data.room.entities.DocumentoEntity
import cl.cmq.salud.data.room.entities.FuncionarioEntity
import cl.cmq.salud.data.local.SeedData

/**
 * Base de datos local de Room (RNF-14, documentación v1.1):
 *
 * - Dos entidades: [FuncionarioEntity] y [DocumentoEntity].
 * - La clave primaria de `funcionario` es `rut` (identificador único del
 *   expediente), tal como lo define el documento de requisitos.
 * - `documento.rutFuncionario` declara clave foránea a `funcionario(rut)`
 *   con eliminación restringida y está indexada (RNF-11).
 * - `exportSchema = true` persiste el esquema para versionar las migraciones.
 * - La base se carga con datos de demostración (ficticios) en [onCreate].
 */
@Database(
    entities = [FuncionarioEntity::class, DocumentoEntity::class],
    version = 3,
    exportSchema = true
)
abstract class CmqSaludDatabase : RoomDatabase() {

    abstract val documentoDao: DocumentoDao

    companion object {

        private const val BASIS_NAME = "cmq_salud.db"

        /**
         * La versión 1 corresponde al esquema Room actual anterior a esta
         * migración. La versión 1 heredada puede, en cambio, venir de
         * SQLiteOpenHelper y usar tablas plurales; se detecta por sus tablas.
         */
        private val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (db.hasTable("funcionarios") && db.hasTable("documentos")) {
                    migrateLegacySqliteSchema(db)
                }
            }
        }

        /** Convierte sin borrar datos la BD SQLiteOpenHelper v2 a las tablas Room. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateLegacySqliteSchema(db)
            }
        }

        private fun SupportSQLiteDatabase.hasTable(tableName: String): Boolean =
            query(
                "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?",
                arrayOf(tableName)
            ).use { it.moveToFirst() }

        /**
         * Conserva funcionarios y documentos de SQLiteOpenHelper v2,
         * convirtiendo fechas ISO a epoch (Room) y nombres snake_case a las
         * columnas camelCase de las entidades Room.
         */
        private fun migrateLegacySqliteSchema(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `funcionario` (
                    `rut` TEXT NOT NULL,
                    `nombreCompleto` TEXT NOT NULL,
                    `cargo` TEXT NOT NULL,
                    `profesion` TEXT NOT NULL,
                    `centroSalud` TEXT NOT NULL,
                    `tipoContrato` TEXT NOT NULL,
                    `fechaIngreso` INTEGER NOT NULL,
                    `correo` TEXT NOT NULL,
                    `perfilRol` TEXT NOT NULL,
                    PRIMARY KEY(`rut`)
                )""".trimIndent()
            )
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `documento` (
                    `idDocumento` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `rutFuncionario` TEXT NOT NULL,
                    `rutCargador` TEXT NOT NULL,
                    `tipoDocumento` TEXT NOT NULL,
                    `estado` TEXT NOT NULL,
                    `fechaCarga` INTEGER NOT NULL,
                    `fechaModificacion` INTEGER NOT NULL,
                    `nombreArchivo` TEXT NOT NULL,
                    `uriLocal` TEXT NOT NULL,
                    `hash` TEXT,
                    `descripcion` TEXT,
                    FOREIGN KEY(`rutFuncionario`) REFERENCES `funcionario`(`rut`)
                        ON UPDATE NO ACTION ON DELETE RESTRICT
                )""".trimIndent()
            )

            db.execSQL(
                """INSERT OR IGNORE INTO `funcionario`
                    (`rut`, `nombreCompleto`, `cargo`, `profesion`, `centroSalud`,
                     `tipoContrato`, `fechaIngreso`, `correo`, `perfilRol`)
                    SELECT `rut`, COALESCE(`nombre_completo`, ''), COALESCE(`cargo`, ''),
                        COALESCE(`profesion`, ''), COALESCE(`centro_salud`, 'Por registrar'),
                        COALESCE(`tipo_contrato`, ''),
                        COALESCE(CAST(strftime('%s', `fecha_ingreso`) AS INTEGER), 0), '', ''
                    FROM `funcionarios`""".trimIndent()
            )
            // Los expedientes antiguos podían contener documentos sin una fila
            // de funcionario. Crea un registro mínimo para preservar el archivo.
            db.execSQL(
                """INSERT OR IGNORE INTO `funcionario`
                    (`rut`, `nombreCompleto`, `cargo`, `profesion`, `centroSalud`,
                     `tipoContrato`, `fechaIngreso`, `correo`, `perfilRol`)
                    SELECT DISTINCT d.`rut_funcionario`, 'Funcionario ' || d.`rut_funcionario`,
                        'Por registrar', '', 'Por registrar', '', 0, '', ''
                    FROM `documentos` AS d
                    WHERE d.`rut_funcionario` IS NOT NULL AND TRIM(d.`rut_funcionario`) <> ''""".trimIndent()
            )
            db.execSQL(
                """INSERT OR REPLACE INTO `documento`
                    (`idDocumento`, `rutFuncionario`, `rutCargador`, `tipoDocumento`, `estado`,
                     `fechaCarga`, `fechaModificacion`, `nombreArchivo`, `uriLocal`, `hash`, `descripcion`)
                    SELECT d.`id_documento`, d.`rut_funcionario`, COALESCE(d.`usuario_carga`, ''),
                        COALESCE(d.`tipo`, ''), COALESCE(d.`estado`, 'PENDIENTE'),
                        COALESCE(CAST(strftime('%s', d.`fecha_carga`) AS INTEGER), 0),
                        COALESCE(CAST(strftime('%s', d.`fecha_carga`) AS INTEGER), 0),
                        COALESCE(d.`nombre_archivo`, ''), COALESCE(d.`ruta_local`, ''),
                        d.`hash_archivo`, d.`descripcion`
                    FROM `documentos` AS d
                    WHERE d.`rut_funcionario` IS NOT NULL AND TRIM(d.`rut_funcionario`) <> ''""".trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_documento_rutFuncionario` ON `documento` (`rutFuncionario`)")
            db.execSQL("DROP TABLE `documentos`")
            db.execSQL("DROP TABLE `funcionarios`")
        }

        /**
         * Crea (si no existe) la base de datos Room a partir del contexto.
         *
         * Nota: la versión 1 describe el esquema original del documento;
         * versiones posteriores se añaden con [addMigrations] en el
         * contenedor y se cargan los datos ficticios en [onCreate].
         */
        @Synchronized
        fun getInstance(context: Context): CmqSaludDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                CmqSaludDatabase::class.java,
                BASIS_NAME
            )
                .addMigrations(MIGRATION_1_3, MIGRATION_2_3)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        SeedData.load(db)
                    }
                })
                .build()
        }
    }
}
