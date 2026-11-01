#!/bin/bash
# ============================================================================
# Semana 10 - Flujo CRUD completo con persistencia local
# ----------------------------------------------------------------------------
# Demuestra el flujo que pide la pauta:
#
#   formulario -> validacion -> guardar en Room* -> listar -> editar ->
#   eliminar -> cerrar app -> reabrir -> los datos siguen disponibles
#
#   (*) persistencia local con SQLite del SDK (ver doc de equivalencia).
#
# Pasos:
#   1. App desde cero (pm clear): Home muestra los 4 documentos de la semilla
#   2. CREATE: cargar un documento nuevo en P05 -> Home pasa a 5
#   3. READ:   P03 Consulta lo lista y P04 Detalle lo abre
#   4. UPDATE: P04 -> Editar informacion -> cambiar la descripcion -> guardar
#   5. Persistencia: cerrar la app por completo y reabrir -> sigue el cambio
#   6. DELETE: P04 -> Eliminar documento -> confirmar -> vuelve a 4
#   7. La base local confirma el conteo (SQLite en el dispositivo)
#
# Cada paso falla de forma ruidosa (exigir) para no dejar evidencia fantasma.
#
# Requisitos: emulador arriba + app instalada (gradlew :app:installDebug)
# Uso:  ./scripts/verificar_crud.sh
# ============================================================================
set -u

DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/adb_ui.sh"

OUT="docs/evidencia_semana10"
RUT="18.765.432-7"
ARCHIVO="crud_evidencia.pdf"
DESC="Documento CRUD de evidencia semana 10"
DESC_EDIT="Descripcion editada y guardada en el dispositivo"
SEED=4
DESPUES=$((SEED + 1))
mkdir -p "$OUT"

exigir() { "$@" || { echo "FALLO: $*"; exit 1; }; }

contar_en_bd() {  # imprime el COUNT(*) de la tabla documentos
  "$ADB" exec-out run-as "$PKG" cat databases/cmq_salud.db > "$OUT/cmq_salud.db" 2>/dev/null
  python - "$OUT/cmq_salud.db" <<'PY'
import sqlite3, sys
con = sqlite3.connect(sys.argv[1])
print(con.execute("SELECT COUNT(*) FROM documentos").fetchone()[0])
PY
}

# ---------------------------------------------------------------------------
echo "== 1) App desde cero: Home debe mostrar los $SEED documentos de la semilla =="
"$ADB" shell pm clear "$PKG" >/dev/null 2>&1
lanzar_app
exigir login
exigir wait_for_text "$SEED documentos en almacenamiento local"
shot "$OUT/40_crud_home_inicial.png"

# ---------------------------------------------------------------------------
echo ""
echo "== 2) CREATE: cargar un documento nuevo en P05 =="
exigir tap_text "Cargar documento"; sleep 2
exigir tap_text "RUT Funcionario *"; type_text "$RUT"; hide_keyboard
exigir tap_text "Nombre del archivo con extensión *"; type_text "$ARCHIVO"; hide_keyboard
exigir tap_text "Descripción / Observaciones *"; type_text "$DESC"; hide_keyboard

exigir tap_checkbox
[ "$(checkbox_estado)" = 'checked="true"' ] || { echo "FALLO: la declaracion no quedo marcada"; exit 1; }

swipe_up; swipe_up
exigir tap_text "Guardar y Registrar Documento"
exigir wait_for_text "¡Documento Registrado!"
shot "$OUT/41_crud_creado.png"
exigir tap_text "Ver en Inicio"; sleep 3

exigir wait_for_text "$DESPUES documentos en almacenamiento local"

# ---------------------------------------------------------------------------
echo ""
echo "== 3) READ: P03 Consulta lo lista y P04 Detalle lo abre =="
exigir tap_text "Consultar expediente"; sleep 2
exigir tap_text "RUT Funcionario"; type_text "$RUT"; hide_keyboard
exigir tap_text "Buscar"
exigir wait_for_text "$ARCHIVO"
shot "$OUT/42_crud_listado.png"

exigir tap_text "$ARCHIVO"; sleep 2
exigir wait_for_text "Detalle del documento"
exigir wait_for_text "ORIGEN"
shot "$OUT/43_crud_detalle_antes.png"

# ---------------------------------------------------------------------------
echo ""
echo "== 4) UPDATE: editar la descripcion del documento =="
exigir tap_text "Editar información"; sleep 2
exigir wait_for_text "Editar documento"
# La descripcion se reemplaza: limpiar el campo y escribir el nuevo texto.
exigir tap_text "Descripción / Observaciones *"
"$ADB" shell input keyevent KEYCODE_MOVE_END
for _ in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40; do
  "$ADB" shell input keyevent KEYCODE_DEL
done
hide_keyboard
exigir tap_text "Descripción / Observaciones *"; type_text "$DESC_EDIT"; hide_keyboard

swipe_up; swipe_up
exigir tap_text "Guardar cambios"
exigir wait_for_text "¡Documento Actualizado!"
shot "$OUT/44_crud_actualizado.png"
exigir tap_text "Ver en Inicio"; sleep 3

# ---------------------------------------------------------------------------
echo ""
echo "== 5) Persistencia: cerrar la app por completo y reabrir =="
"$ADB" shell am force-stop "$PKG"; sleep 2
echo "(app cerrada por completo)"
lanzar_app
exigir login
exigir wait_for_text "$DESPUES documentos en almacenamiento local"
shot "$OUT/45_crud_persistencia.png"

# El cambio debe seguir visible tras el reinicio
exigir tap_text "Consultar expediente"; sleep 2
exigir tap_text "RUT Funcionario"; type_text "$RUT"; hide_keyboard
exigir tap_text "Buscar"
exigir tap_text "$ARCHIVO"; sleep 2
exigir wait_for_text_scroll "$DESC_EDIT"
shot "$OUT/46_crud_detalle_editado_persistido.png"

# ---------------------------------------------------------------------------
echo ""
echo "== 6) DELETE: eliminar el documento (con confirmacion) =="
exigir tap_text_scroll "Eliminar documento"; sleep 1
exigir wait_for_text "Eliminar documento"      # titulo del dialogo
exigir tap_text "Eliminar"                     # boton de confirmacion
sleep 3
exigir wait_for_text "Consulta de expediente"  # vuelve al expediente
exigir wait_for_text "Buscar"
shot "$OUT/47_crud_eliminado_listado.png"

# ---------------------------------------------------------------------------
echo ""
echo "== 7) La base local confirma el conteo tras eliminar =="
TOTAL=$(contar_en_bd)
echo "  documentos en la base local: $TOTAL"
[ "$TOTAL" = "$SEED" ] || { echo "FALLO: se esperaban $SEED documentos y hay $TOTAL"; exit 1; }
python - "$OUT/cmq_salud.db" <<'PY'
import sqlite3, sys
con = sqlite3.connect(sys.argv[1])
for f in con.execute("SELECT id_documento, nombre_archivo, descripcion FROM documentos ORDER BY id_documento"):
    print("  ", f)
PY

echo ""
echo "== OK: flujo CRUD completo con persistencia real verificada =="
echo "== Evidencia en $OUT =="
