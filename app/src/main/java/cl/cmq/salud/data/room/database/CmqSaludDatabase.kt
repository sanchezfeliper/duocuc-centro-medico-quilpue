// data/room/database/CmqSaludDatabase.kt
package cl.cmq.salud.data.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.cmq.salud.data.local.SemillaDocumentos
import cl.cmq.salud.data.room.dao.DocumentoDao
import cl.cmq.salud.data.room.entities.DocumentoEntity
import cl.cmq.salud.data.room.entities.FuncionarioEntity

/**
 * Base de datos local de Room (RNF-14).
 *
 * Reemplaza al SQLiteOpenHelper manual ([cl.cmq.salud.data.local.CmqSqliteHelper])
 * conservando el mismo archivo (`cmq_salud.db`) y los mismos datos: las
 * migraciones convierten el esquema antiguo (tablas plurales, fechas ISO,
 * columnas snake_case) al de Room sin borrar información del usuario.
 *
 * Esquema:
 *  - `funcionario` con PK `rut` (identificador único del expediente).
 *  - `documento` con FK a `funcionario(rut)` y eliminación restringida (RNF-11).
 *
 * `exportSchema = true` deja el esquema versionado en `app/schemas/`, requisito
 * para validar las migraciones en las pruebas.
 */
@Database(
    entities = [FuncionarioEntity::class, DocumentoEntity::class],
    version = 3,
    exportSchema = true
)
abstract class CmqSaludDatabase : RoomDatabase() {

    abstract val documentoDao: DocumentoDao

    companion object {

        private const val NOMBRE_BD = "cmq_salud.db"

        /**
         * v1 -> v3: base room recién creada (sin tablas) o base heredada del
         * SQLiteOpenHelper. Si detecta las tablas plurales antiguas migra sus
         * filas; si no, no hay nada que convertir.
         */
        private val MIGRACION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (db.tieneTabla("funcionarios") || db.tieneTabla("documentos")) {
                    migrarDesdeSqliteHeredado(db)
                }
            }
        }

        /** v2 -> v3: la base viene del SQLiteOpenHelper v2 (con `ruta_local`). */
        private val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrarDesdeSqliteHeredado(db)
            }
        }

        private fun SupportSQLiteDatabase.tieneTabla(nombre: String): Boolean =
            query(
                "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?",
                arrayOf(nombre)
            ).use { it.moveToFirst() }

        /**
         * Conserva funcionarios y documentos del esquema SQLiteOpenHelper v2,
         * convirtiendo fechas ISO (TEXT) a epoch (INTEGER) y los nombres
         * snake_case a las columnas camelCase de las entidades Room.
         *
         * Se ejecuta dentro de la transacción de la migración: si algo falla,
         * la base queda intacta y no se pierde ningún expediente.
         */
        private fun migrarDesdeSqliteHeredado(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `funcionario` (
                    `rut` TEXT NOT NULL,
                    `nombreCompleto` TEXT NOT NULL,
                    `cargo` TEXT NOT NULL,
                    `centroSalud` TEXT NOT NULL,
                    `profesion` TEXT,
                    `tipoContrato` TEXT,
                    `fechaIngreso` TEXT,
                    `fechaNacimiento` TEXT,
                    `situacionContractual` TEXT,
                    `correo` TEXT,
                    `perfilRol` TEXT,
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
                    `fechaVencimiento` INTEGER,
                    `comentarioValidacion` TEXT,
                    `urlSharePoint` TEXT,
                    FOREIGN KEY(`rutFuncionario`) REFERENCES `funcionario`(`rut`)
                        ON UPDATE NO ACTION ON DELETE RESTRICT
                )""".trimIndent()
            )

            // Funcionarios: se conservan fechas ISO tal cual (el dominio usa
            // String), y los campos nuevos quedan en null.
            db.execSQL(
                """INSERT OR IGNORE INTO `funcionario`
                    (`rut`, `nombreCompleto`, `cargo`, `centroSalud`, `profesion`,
                     `tipoContrato`, `fechaIngreso`, `fechaNacimiento`,
                     `situacionContractual`, `correo`, `perfilRol`)
                    SELECT `rut`, COALESCE(`nombre_completo`, ''), COALESCE(`cargo`, ''),
                        COALESCE(`centro_salud`, 'Por registrar'), `profesion`,
                        `tipo_contrato`, `fecha_ingreso`, `fecha_nacimiento`,
                        `situacion_contractual`, NULL, NULL
                    FROM `funcionarios`
                    WHERE `rut` IS NOT NULL AND TRIM(`rut`) <> ''""".trimIndent()
            )

            // Expedientes con documentos pero sin fila de funcionario: se crea
            // un registro mínimo para no romper la clave foránea ni perder el archivo.
            db.execSQL(
                """INSERT OR IGNORE INTO `funcionario`
                    (`rut`, `nombreCompleto`, `cargo`, `centroSalud`, `profesion`,
                     `tipoContrato`, `fechaIngreso`, `fechaNacimiento`,
                     `situacionContractual`, `correo`, `perfilRol`)
                    SELECT DISTINCT d.`rut_funcionario`, 'Funcionario ' || d.`rut_funcionario`,
                        'Por registrar', 'Por registrar', NULL, NULL, NULL, NULL,
                        NULL, NULL, NULL
                    FROM `documentos` AS d
                    WHERE d.`rut_funcionario` IS NOT NULL AND TRIM(d.`rut_funcionario`) <> ''""".trimIndent()
            )

            // Documentos: fechas ISO -> epoch (segundos) con strftime.
            db.execSQL(
                """INSERT OR REPLACE INTO `documento`
                    (`idDocumento`, `rutFuncionario`, `rutCargador`, `tipoDocumento`, `estado`,
                     `fechaCarga`, `fechaModificacion`, `nombreArchivo`, `uriLocal`, `hash`,
                     `descripcion`, `fechaVencimiento`, `comentarioValidacion`, `urlSharePoint`)
                    SELECT d.`id_documento`, d.`rut_funcionario`, COALESCE(d.`usuario_carga`, ''),
                        COALESCE(d.`tipo`, ''), COALESCE(d.`estado`, 'PENDIENTE'),
                        COALESCE(CAST(strftime('%s', d.`fecha_carga`) AS INTEGER), 0),
                        COALESCE(CAST(strftime('%s', d.`fecha_carga`) AS INTEGER), 0),
                        COALESCE(d.`nombre_archivo`, ''), COALESCE(d.`ruta_local`, ''),
                        d.`hash_archivo`, d.`descripcion`,
                        CAST(strftime('%s', d.`fecha_vencimiento`) AS INTEGER),
                        d.`comentario_validacion`, d.`url_sharepoint`
                    FROM `documentos` AS d
                    WHERE d.`rut_funcionario` IS NOT NULL AND TRIM(d.`rut_funcionario`) <> ''""".trimIndent()
            )

            db.execSQL("CREATE INDEX IF NOT EXISTS `index_documento_rutFuncionario` ON `documento` (`rutFuncionario`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_documento_rutCargador` ON `documento` (`rutCargador`)")
            db.execSQL("DROP TABLE IF EXISTS `documentos`")
            db.execSQL("DROP TABLE IF EXISTS `funcionarios`")
        }

        @Volatile
        private var instancia: CmqSaludDatabase? = null

        /**
         * Instancia única de la base. Si el archivo no existe se crea el
         * esquema Room y se cargan los datos ficticios de demostración
         * (RN-03); si existe una base del esquema anterior, se migra.
         */
        fun getInstance(context: Context): CmqSaludDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    CmqSaludDatabase::class.java,
                    NOMBRE_BD
                )
                    .addMigrations(MIGRACION_1_3, MIGRACION_2_3)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            sembrar(db)
                        }
                    })
                    .build()
                    .also { instancia = it }
            }

        /**
         * Semilla sobre la conexión recién creada. Reutiliza
         * [SemillaDocumentos] para que la base y los dobles de prueba partan
         * exactamente del mismo expediente de ejemplo.
         */
        private fun sembrar(db: SupportSQLiteDatabase) {
            SemillaDocumentos.funcionarios().forEach { f ->
                db.execSQL(
                    """INSERT OR REPLACE INTO `funcionario`
                        (`rut`, `nombreCompleto`, `cargo`, `centroSalud`, `profesion`,
                         `tipoContrato`, `fechaIngreso`, `fechaNacimiento`,
                         `situacionContractual`, `correo`, `perfilRol`)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""".trimIndent(),
                    arrayOf<Any?>(
                        f.rut, f.nombreCompleto, f.cargo, f.centroSalud, f.profesion,
                        f.tipoContrato, f.fechaIngreso, f.fechaNacimiento,
                        f.situacionContractual, f.correo, f.perfilRol
                    )
                )
            }
            SemillaDocumentos.documentos().forEach { d ->
                db.execSQL(
                    """INSERT OR REPLACE INTO `documento`
                        (`idDocumento`, `rutFuncionario`, `rutCargador`, `tipoDocumento`,
                         `estado`, `fechaCarga`, `fechaModificacion`, `nombreArchivo`,
                         `uriLocal`, `hash`, `descripcion`, `fechaVencimiento`,
                         `comentarioValidacion`, `urlSharePoint`)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""".trimIndent(),
                    arrayOf<Any?>(
                        d.idDocumento.toLong(), d.rutFuncionario, d.usuarioCarga, d.tipo,
                        d.estado.name, epochDeFecha(d.fechaCarga), epochDeFecha(d.fechaCarga),
                        d.nombreArchivo, d.rutaLocal, d.hashArchivo, d.descripcion,
                        d.fechaVencimiento?.let { epochDeFecha(it) },
                        d.comentarioValidacion, d.urlSharePoint
                    )
                )
            }
        }

        /** Convierte una fecha ISO `yyyy-MM-dd` a epoch en segundos (0 si es inválida). */
        private fun epochDeFecha(fecha: String): Long =
            runCatching {
                java.time.LocalDate.parse(fecha)
                    .atStartOfDay(java.time.ZoneId.systemDefault())
                    .toEpochSecond()
            }.getOrDefault(0L)
    }
}
