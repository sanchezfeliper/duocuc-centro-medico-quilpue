#!/bin/bash
# ============================================================================
# Semana 9 - Verificacion de VISUALIZACION de los datos guardados (9.2)
# ----------------------------------------------------------------------------
# Prueba que lo guardado en el dispositivo se VEA en la interfaz y sobreviva:
#
#   1. App desde cero (pm clear): Home muestra el total de la semilla (4)
#   2. Se carga un documento nuevo en P05
#   3. Home vuelve a mostrar el total, ahora 5  -> la vista lee de SQLite
#   4. Se cierra la app por completo y se reabre: Home sigue mostrando 5
#   5. P03 Consulta rotula el origen local y P04 Detalle lo declara
#
# Todo paso falla de forma ruidosa (exigir) para no dejar evidencia fantasma.
#
# Requisitos: emulador arriba + app instalada (gradlew :app:installDebug)
# Uso:  ./scripts/verificar_visualizacion.sh
# ============================================================================
set -u

DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/adb_ui.sh"

OUT="docs/evidencia_semana9"
RUT="18.765.432-7"
ARCHIVO="viz_semana9.pdf"
SEED=4          # documentos ficticios de la semilla (RN-03)
DESPUES=$((SEED + 1))
mkdir -p "$OUT"

exigir() { "$@" || { echo "FALLO: $*"; exit 1; }; }

# ---------------------------------------------------------------------------
echo "== 1) App desde cero: Home debe mostrar los $SEED documentos de la semilla =="
"$ADB" shell pm clear "$PKG" >/dev/null 2>&1
lanzar_app
exigir login
exigir wait_for_text "$SEED documentos en almacenamiento local"
shot "$OUT/21_home_almacenamiento_inicial.png"

# ---------------------------------------------------------------------------
echo ""
echo "== 2) Carga de un documento nuevo en P05 =="
exigir tap_text "Cargar documento"; sleep 2
exigir tap_text "RUT Funcionario *"; type_text "$RUT"; hide_keyboard
exigir tap_text "Nombre del archivo con extensión *"; type_text "$ARCHIVO"; hide_keyboard
exigir tap_text "Descripción / Observaciones *"; type_text "Documento%sde%sevidencia%s9.2"; hide_keyboard

exigir tap_checkbox
[ "$(checkbox_estado)" = 'checked="true"' ] || { echo "FALLO: la declaracion no quedo marcada"; exit 1; }

swipe_up; swipe_up
exigir tap_text "Guardar y Registrar Documento"
exigir wait_for_text "¡Documento Registrado!"
exigir tap_text "Ver en Inicio"; sleep 3

# ---------------------------------------------------------------------------
echo ""
echo "== 3) Home debe reflejar el nuevo total ($DESPUES): la vista lee de SQLite =="
exigir wait_for_text "$DESPUES documentos en almacenamiento local"
shot "$OUT/22_home_tras_cargar.png"

# ---------------------------------------------------------------------------
echo ""
echo "== 4) Cierre total de la app y reapertura =="
"$ADB" shell am force-stop "$PKG"
sleep 2
echo "(app cerrada por completo)"
lanzar_app
exigir login
exigir wait_for_text "$DESPUES documentos en almacenamiento local"
shot "$OUT/23_home_persistencia_tras_reinicio.png"

# ---------------------------------------------------------------------------
echo ""
echo "== 5) La base local conserva las filas (conteo directo en cmq_salud.db) =="
"$ADB" exec-out run-as "$PKG" cat databases/cmq_salud.db > "$OUT/cmq_salud.db" 2>/dev/null
python - "$OUT/cmq_salud.db" "$DESPUES" <<'PY'
import sqlite3, sys
con = sqlite3.connect(sys.argv[1])
esperado = int(sys.argv[2])
filas = list(con.execute("SELECT id_documento, estado, nombre_archivo FROM documentos ORDER BY id_documento"))
for f in filas: print("  ", f)
total = con.execute("SELECT COUNT(*) FROM documentos").fetchone()[0]
assert total == esperado, f"se esperaban {esperado} filas y hay {total}"
print(f"OK: {total} documentos en la base local")
PY

# ---------------------------------------------------------------------------
echo ""
echo "== 6) P03 Consulta rotula el origen local =="
exigir tap_text "Consultar expediente"; sleep 2
exigir tap_text "RUT Funcionario"; type_text "$RUT"; hide_keyboard
exigir tap_text "Buscar"
exigir wait_for_text "$ARCHIVO"
exigir wait_for_text "$DESPUES documentos del expediente · leídos del almacenamiento local ($DESPUES en el dispositivo)"
shot "$OUT/24_consulta_origen_local.png"

# ---------------------------------------------------------------------------
echo ""
echo "== 7) P04 Detalle declara que el documento viene del dispositivo =="
exigir tap_text "$ARCHIVO"; sleep 2
exigir wait_for_text "ORIGEN"
exigir wait_for_text "Almacenamiento local del dispositivo (SQLite)"
shot "$OUT/25_detalle_origen_local.png"

echo ""
echo "== OK: los datos guardados se ven en la interfaz y persisten tras reiniciar =="
echo "== Evidencia en $OUT =="
