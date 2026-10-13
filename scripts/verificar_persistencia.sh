#!/bin/bash
# ============================================================================
# Semana 9 - Verificacion de PERSISTENCIA LOCAL (SQLite)
# ----------------------------------------------------------------------------
# Prueba que un documento cargado en P05 sobrevive al cierre de la aplicacion:
#
#   1. Parte de cero y carga un documento nuevo
#   2. Verifica la fila directamente en la base local (se descarga el .db y
#      se consulta con sqlite3; el emulador no trae el binario sqlite3)
#   3. Cierra la app por completo (force-stop) y la vuelve a abrir
#   4. Consulta el expediente: el documento sigue ahi  -> evidencia en captura
#
# Los pasos fallan de forma ruidosa (|| exit 1): nada de evidencia fantasma.
#
# Requisitos: emulador arriba + app instalada (gradlew :app:installDebug)
# Uso:  ./scripts/verificar_persistencia.sh
# ============================================================================
set -u

DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/adb_ui.sh"

OUT="docs/evidencia_semana9"
RUT="18.765.432-7"
ARCHIVO="persistencia_semana9.pdf"
mkdir -p "$OUT"

exigir() { "$@" || { echo "FALLO: $*"; exit 1; }; }

# ---------------------------------------------------------------------------
echo "== 1) App desde cero y carga de un documento nuevo =="
"$ADB" shell pm clear "$PKG" >/dev/null 2>&1
lanzar_app
exigir login

exigir tap_text "Cargar documento"; sleep 2
exigir tap_text "RUT Funcionario *"; type_text "$RUT"; hide_keyboard
exigir tap_text "Nombre del archivo con extensión *"; type_text "$ARCHIVO"; hide_keyboard
exigir tap_text "Descripción / Observaciones *"; type_text "Prueba%sde%spersistencia%slocal"; hide_keyboard

exigir tap_checkbox
[ "$(checkbox_estado)" = 'checked="true"' ] || { echo "FALLO: la declaracion de autenticidad no quedo marcada"; exit 1; }

swipe_up; swipe_up
exigir tap_text "Guardar y Registrar Documento"
exigir wait_for_text "¡Documento Registrado!"
shot "$OUT/11_carga_guardada.png"
exigir tap_text "Ver en Inicio"; sleep 3

# ---------------------------------------------------------------------------
echo ""
echo "== 2) Verificacion directa en la base local (SQLite) =="
echo "-- archivos de bases de datos --"
"$ADB" shell run-as "$PKG" ls databases 2>&1
echo "-- contenido descargado de cmq_salud.db --"
"$ADB" exec-out run-as "$PKG" cat databases/cmq_salud.db > "$OUT/cmq_salud.db" 2>/dev/null
python - "$OUT/cmq_salud.db" <<'PY'
import sqlite3, sys
con = sqlite3.connect(sys.argv[1])
print("documentos:")
for fila in con.execute("SELECT id_documento, estado, nombre_archivo FROM documentos ORDER BY id_documento"):
    print("  ", fila)
print("funcionarios:")
for fila in con.execute("SELECT id_funcionario, rut, nombre_completo FROM funcionarios ORDER BY id_funcionario"):
    print("  ", fila)
PY

# ---------------------------------------------------------------------------
echo ""
echo "== 3) Cierre total de la app y reapertura =="
"$ADB" shell am force-stop "$PKG"
sleep 2
echo "(app cerrada por completo)"
lanzar_app
exigir login

# ---------------------------------------------------------------------------
echo ""
echo "== 4) El documento debe seguir en el expediente tras reabrir =="
exigir tap_text "Consultar expediente"; sleep 2
exigir tap_text "RUT Funcionario"; type_text "$RUT"; hide_keyboard
exigir tap_text "Buscar"
exigir wait_for_text "$ARCHIVO"
shot "$OUT/12_persistencia_tras_reinicio.png"

echo ""
echo "== OK: el documento '$ARCHIVO' persistio tras cerrar y reabrir la app =="
echo "== Evidencia en $OUT =="
