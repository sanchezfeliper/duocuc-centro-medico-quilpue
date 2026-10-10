// data/DocumentRepository.kt
package cl.cmq.salud.data

import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.model.Funcionario
import kotlinx.coroutines.delay

class DocumentRepository {

    companion object {
        // Funcionarios ficticios de demostracion (RN-03, E2)
        private val funcionariosMemoria = mutableListOf(
            Funcionario(101, "18.765.432-7", "Juan Pérez", "Enfermero", "CESFAM Quilpué")
        )
        private var nextFuncionarioId = 102

        private val documentosMemoria = mutableListOf(
            Documento(1, 101, "18.765.432-7", "PDF", "Contrato de trabajo",
                EstadoDocumento.VIGENTE, "2026-03-12", "RRHH"),
            Documento(2, 101, "18.765.432-7", "PDF", "Certificado capacitacion",
                EstadoDocumento.PENDIENTE, "2026-09-20", "J. Perez"),
            Documento(3, 101, "18.765.432-7", "PDF", "Permiso medico",
                EstadoDocumento.APROBADO, "2026-09-18", "J. Perez"),
            Documento(4, 101, "18.765.432-7", "JPG", "Anexo contrato",
                EstadoDocumento.RECHAZADO, "2026-09-05", "M. Gonzalez")
        )
        private var nextId = 5

        /** Asocia (o registra) el funcionario del RUT y devuelve su ID. */
        private fun obtenerOCrearFuncionario(rut: String): Int {
            val normalizado = rut.trim()
            val existente = funcionariosMemoria.firstOrNull { it.rut == normalizado }
            if (existente != null) return existente.idFuncionario
            val nuevo = Funcionario(
                idFuncionario = nextFuncionarioId++,
                rut = normalizado,
                nombreCompleto = "Funcionario $normalizado",
                cargo = "Por registrar",
                centroSalud = "Por registrar"
            )
            funcionariosMemoria.add(nuevo)
            return nuevo.idFuncionario
        }
    }

    /** Busca el funcionario por RUT (E2). Datos ficticios (RN-03). */
    suspend fun buscarFuncionario(rut: String): Funcionario? {
        delay(200)
        return funcionariosMemoria.firstOrNull { it.rut == rut.trim() }
    }

    /** Expediente del funcionario: solo SUS documentos (RF-12). */
    suspend fun buscarPorRut(rut: String): List<Documento> {
        delay(600) // simula latencia de red
        return documentosMemoria.filter { it.rutFuncionario == rut.trim() }
    }

    /** Consulta un documento por su ID (Meta 6 - destino Detalle P04). */
    suspend fun buscarPorId(idDocumento: Int): Documento? {
        delay(400) // simula latencia de red
        return documentosMemoria.firstOrNull { it.idDocumento == idDocumento }
    }

    /**
     * Guarda un nuevo documento cargado (Meta 2 - Formulario funcional).
     * El RUT ingresado en el formulario determina el funcionario asociado
     * (Meta 6: el dato ingresado se usa posteriormente).
     */
    suspend fun guardarDocumento(documento: Documento): Documento {
        delay(800) // simula subida y registro en SharePoint / API
        val idFuncionario = obtenerOCrearFuncionario(documento.rutFuncionario)
        val nuevoDoc = documento.copy(idDocumento = nextId++, idFuncionario = idFuncionario)
        documentosMemoria.add(0, nuevoDoc)
        return nuevoDoc
    }
}
