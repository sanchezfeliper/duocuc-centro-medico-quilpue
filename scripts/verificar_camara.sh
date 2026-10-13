#!/bin/bash
# ============================================================================
# Semana 9 - Verificacion del RECURSO NATIVO: camara del dispositivo (9.3, RF-09)
# ----------------------------------------------------------------------------
# Prueba que la opcion "Captura fisica con camara" de P05 deja de ser inerte y
# digitaliza un documento fisico real con CameraX:
#
#   1. App desde cero (pm clear) + login
#   2. P05: origen = "Captura fisica con camara" -> aparece la zona de captura
#   3. Permiso de camara (dialogo del sistema) -> se concede
#   4. Camara activa: se toma la fotografia (obturador)
#   5. La captura queda asociada al formulario (miniatura + ruta + huella)
#   6. Se registra el documento (sin escribir el nombre: lo toma de la captura)
#   7. SQLite guarda ruta_local + hash SHA-256 y el archivo existe en disco
#   8. P04 Detalle muestra la fotografia y declara el origen (RF-09)
#
# Todo paso falla de forma ruidosa (exigir) para no dejar evidencia fantasma.
#
# Requisitos: emulador arriba + app instalada (gradlew :app:installDebug)
# Uso:  ./scripts/verificar_camara.sh
# ============================================================================
set -u

DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/adb_ui.sh"

OUT="docs/evidencia_semana9"
RUT="18.765.432-7"
mkdir -p "$OUT"

exigir() { "$@" || { echo "FALLO: $*"; exit 1; }; }

# ---------------------------------------------------------------------------
echo "== 1) App desde cero y login =="
"$ADB" shell pm clear "$PKG" >/dev/null 2>&1
lanzar_app
exigir login

# ---------------------------------------------------------------------------
echo ""
echo "== 2) P05: origen = Captura fisica con camara =="
exigir tap_text "Cargar documento"; sleep 2
exigir tap_text "RUT Funcionario *"; type_text "$RUT"; hide_keyboard
exigir tap_text_scroll "Descripción / Observaciones *"
# Nota: adb "input text" pasa por el sh del dispositivo, asi que NO se pueden
# usar parentesis (los interpreta el shell). Se usa "RF-09" sin parentesis.
type_text "Documento%sfisico%sdigitalizado%scon%scamara%sRF-09"; hide_keyboard
exigir tap_text_scroll "Captura física con cámara"; sleep 1
exigir wait_for_text "Fotografía del documento físico (RF-09)"
shot "$OUT/30_p05_zona_captura.png"

exigir tap_text_scroll "Tomar fotografía"; sleep 3

# ---------------------------------------------------------------------------
echo ""
echo "== 3) Permiso de camara (dialogo del sistema) =="
sleep 2
shot "$OUT/31_permiso_camara.png"
exigir tap_primero "While using the app" "Mientras se usa la app" "Allow" "Permitir"
sleep 4

# ---------------------------------------------------------------------------
echo ""
echo "== 4) Camara activa: captura de la fotografia =="
exigir wait_for_desc "Botón capturar fotografía" 15
sleep 3
shot "$OUT/32_camara_preview.png"
exigir tap_desc "Botón capturar fotografía"
exigir wait_for_text_contains_scroll "Captura guardada:" 10
shot "$OUT/33_captura_asociada.png"

# ---------------------------------------------------------------------------
echo ""
echo "== 5) Registro del documento (el nombre sale de la captura) =="
exigir tap_checkbox_scroll
[ "$(checkbox_estado)" = 'checked="true"' ] || { echo "FALLO: la declaracion no quedo marcada"; exit 1; }
exigir tap_text_scroll "Guardar y Registrar Documento"
exigir wait_for_text "¡Documento Registrado!"
exigir tap_text "Ver en Inicio"; sleep 3

# ---------------------------------------------------------------------------
echo ""
echo "== 6) SQLite guarda ruta_local + hash SHA-256 (RF-09/RF-11) =="
"$ADB" exec-out run-as "$PKG" cat databases/cmq_salud.db > "$OUT/cmq_salud.db" 2>/dev/null
NOMBRE_TMP=$(mktemp)
python - "$OUT/cmq_salud.db" "$NOMBRE_TMP" <<'PY' || { echo "FALLO: verificacion de SQLite"; exit 1; }
import sqlite3, sys
con = sqlite3.connect(sys.argv[1])
fila = con.execute(
    "SELECT id_documento, nombre_archivo, ruta_local, hash_archivo "
    "FROM documentos WHERE ruta_local IS NOT NULL ORDER BY id_documento DESC LIMIT 1"
).fetchone()
assert fila, "no se encontro ningun documento con captura de camara"
print(f"  folio #{fila[0]}  archivo={fila[1]}")
print(f"  ruta_local={fila[2]}")
print(f"  hash_sha256={fila[3]}")
assert fila[2].startswith("fotos/"), f"ruta_local inesperada: {fila[2]}"
assert fila[3] and len(fila[3]) == 64, f"hash SHA-256 invalido: {fila[3]}"
print("OK: ruta_local y hash SHA-256 persistidos en la base local")
with open(sys.argv[2], "w", encoding="utf-8") as fh:
    fh.write(fila[1])
PY
NOMBRE=$(cat "$NOMBRE_TMP")
rm -f "$NOMBRE_TMP"
echo "  (nombre capturado: $NOMBRE)"
echo "  archivos en files/fotos del dispositivo:"
"$ADB" shell run-as "$PKG" ls -l files/fotos
"$ADB" shell run-as "$PKG" ls files/fotos | grep -q "\.jpg" || { echo "FALLO: no hay archivo .jpg en files/fotos"; exit 1; }

# ---------------------------------------------------------------------------
echo ""
echo "== 7) P04 Detalle muestra la captura y declara el origen (RF-09) =="
exigir tap_text "Consultar expediente"; sleep 2
exigir tap_text "RUT Funcionario"; type_text "$RUT"; hide_keyboard
exigir tap_text "Buscar"
exigir wait_for_text "$NOMBRE"
exigir tap_text "$NOMBRE"; sleep 2
exigir wait_for_text_scroll "ORIGEN DE CAPTURA"
exigir wait_for_text_scroll "Cámara del dispositivo (RF-09)"
exigir wait_for_text_scroll "HASH SHA-256 (RF-11)"
shot "$OUT/34_detalle_captura_camara.png"

echo ""
echo "== OK: la camara del dispositivo captura, guarda y persiste el documento (RF-09) =="
echo "== Evidencia en $OUT =="
