// domain/model/Funcionario.kt
package cl.cmq.salud.domain.model

/**
 * E2 Funcionario del modelo de datos preliminar: persona del Area Salud sobre
 * quien recae la documentacion (RF-12, RF-13). Datos ficticios (RN-03).
 *
 * Meta 7: se incorporan los atributos faltantes de la entidad disenada;
 * los campos de informacion laboral son opcionales porque el repositorio
 * actual registra funcionarios "Por registrar" desde el formulario de carga.
 */
data class Funcionario(
    val idFuncionario: Int,
    val rut: String,                    // UK, formato 12.345.678-9
    val nombreCompleto: String,
    val cargo: String,
    val centroSalud: String,            // FK -> CentroSalud (nombre de referencia)
    val profesion: String? = null,
    val tipoContrato: String? = null,   // planta / contrata / honorarios
    val fechaIngreso: String? = null,   // DATE
    val fechaNacimiento: String? = null, // DATE
    val situacionContractual: String? = null, // Vigente / Finiquitado / Licencia
    // Atributos de contacto y rol del funcionario (RNF-14: persistidos en Room).
    // El correo institucional y el codigo de perfil permiten reconstruir la
    // identidad al leer el expediente desde la base, sin depender de datos en memoria.
    val correo: String? = null,
    val perfilRol: String? = null
)
