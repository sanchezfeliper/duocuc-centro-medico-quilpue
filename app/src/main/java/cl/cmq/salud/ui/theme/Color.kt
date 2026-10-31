package cl.cmq.salud.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Sistema visual institucional CMQ Salud — rediseño UI.
 *
 * Los colores se centralizan como roles semánticos; las pantallas deben
 * consumirlos por su nombre semántico, no por valores literales.
 *
 * Paleta de referencia (guía de rediseño UI):
 *   Tinta spruce  #173F38  → texto principal, títulos y marca CMQ
 *   Teal de acción #1D6B5A → acción primaria, selección y enlaces
 *   Fondo papel   #F5F7F4  → fondo general de pantalla
 *   Superficie    #FFFFFF  → formularios, paneles y contenido
 *   Sage suave    #E8F0EB  → selección, foco suave y agrupaciones
 *   Texto secund. #536A62  → ayuda, descripción y metadatos
 *   Borde         #D5E0DA  → separadores y contornos de control
 *   Atención      #82580A / #FFF1CE → vencimientos y advertencias
 *   Error         #A8343F / #FDEBEC → errores y rechazo
 *   Éxito         #216A49 / #E7F3EB → guardado y aprobación
 */

// --- Tinta principal / marca ---
val Spruce = Color(0xFF173F38)            // texto principal, títulos, marca CMQ

// --- Color de acción ---
val Teal = Color(0xFF1D6B5A)              // acción primaria, selección, enlaces

// --- Fondos ---
val Paper = Color(0xFFF5F7F4)             // fondo general de pantalla
val Surface = Color(0xFFFFFFFF)           // formularios, paneles, contenido

// --- Acento suave ---
val SoftSage = Color(0xFFE8F0EB)          // selección, foco suave, agrupaciones

// --- Texto ---
val TextSecondary = Color(0xFF536A62)     // ayuda, descripción, metadatos

// --- Contornos ---
val Divider = Color(0xFFD5E0DA)           // separadores y contornos de control

// --- Atención (advertencias, vencimientos) ---
val AttentionInk = Color(0xFF82580A)      // tinta de advertencia
val AttentionBg = Color(0xFFFFF1CE)       // fondo de advertencia

// --- Error / rechazo ---
val Error = Color(0xFFA8343F)             // tinta de error
val ErrorBg = Color(0xFFFDEBEC)           // fondo de error

// --- Éxito / aprobación ---
val Success = Color(0xFF216A49)           // tinta de éxito
val SuccessBg = Color(0xFFE7F3EB)         // fondo de éxito

// --- Aliases de compatibilidad con el código existente ---
// Nuevas pantallas deben usar los nombres semánticos de arriba.
// Estos aliases preservan las referencias ya establecidas sin romper el build.
val Navy = Spruce
val Blue = Teal
val Green = Teal
val Light = SoftSage        // alias: fondo de selección y agrupaciones suaves
val Grey = TextSecondary
val Red = Error
val Border = Divider
val Background = Paper
val SurfaceWhite = Surface
val Mint = SuccessBg        // alias: fondo de estados de éxito
val Warm = AttentionBg
val WarmInk = AttentionInk
val Accent = Color(0xFFE6B15B)            // resaltado de badges/acento secundario
