#!/bin/bash
# ============================================================================
# Helpers de automatizacion UI por adb (uiautomator + input), reutilizables
# por los scripts de evidencia (Meta 8, Semana 9, etc.).
#
# Uso:  source "$(dirname "$0")/adb_ui.sh"
# ============================================================================

ADB="${ADB:-$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe}"
PKG="${PKG:-cl.cmq.salud}"
UIXML="${UIXML:-./ui_dump.xml}"

# Dumpea la jerarquia de UI. Borra el archivo remoto ANTES de dumpear (si el
# dump falla no se reutiliza evidencia vieja) y reintenta: el primer dump
# justo despues de lanzar la app puede fallar porque la UI aun no esta lista.
dump() {
  local intento
  for intento in 1 2 3; do
    "$ADB" shell rm //sdcard/ui.xml >/dev/null 2>&1
    "$ADB" shell uiautomator dump //sdcard/ui.xml >/dev/null 2>&1
    "$ADB" pull //sdcard/ui.xml "$UIXML" >/dev/null 2>&1
    if [ -s "$UIXML" ] && grep -q "<hierarchy" "$UIXML"; then
      return 0
    fi
    sleep 2
  done
  echo "AVISO: dump de UI fallido tras 3 intentos" >&2
  return 1
}

bounds_of_text() {
  dump || return 1
  local pat; pat=$(printf '%s' "$1" | sed 's/[.[\*^$()+?{|]/\\&/g')
  grep -o "text=\"$pat\"[^>]*bounds=\"\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]\"" "$UIXML" \
    | grep -o '\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]' | head -1
}

bounds_of_class() {  # $1=clase Android, $2=indice (0-based, orden del dump)
  dump || return 1
  grep -o "class=\"$1\"[^>]*bounds=\"[^\"]*\"" "$UIXML" \
    | grep -o 'bounds="[^"]*"' | sed 's/bounds="//;s/"$//' | sed -n "$(( $2 + 1 ))p"
}

center_of() {  # "[x1,y1][x2,y2]" -> "cx cy"
  local b=${1//'['/}; b=${b//']'/';'}
  local x1 y1 x2 y2
  IFS=',;' read -r x1 y1 x2 y2 <<< "$b"
  echo "$(( (x1 + x2) / 2 )) $(( (y1 + y2) / 2 ))"
}

tap_bounds() {
  local b="$1"
  read -r cx cy <<< "$(center_of "$b")"
  "$ADB" shell input tap "$cx" "$cy"
  echo "tap ($cx,$cy)"
  sleep 1
}

tap_text() {
  local b; b=$(bounds_of_text "$1") || return 1
  [ -z "$b" ] && { echo "NO_ENCONTRADO(texto): $1"; return 1; }
  echo -n "tap '$1' -> "; tap_bounds "$b"
}

tap_edittext() {
  local b; b=$(bounds_of_class "android.widget.EditText" "$1") || return 1
  [ -z "$b" ] && { echo "NO_ENCONTRADO(edittext #$1)"; return 1; }
  echo -n "tap edittext #$1 -> "; tap_bounds "$b"
}

tap_checkbox() {
  local b; b=$(bounds_of_class "android.widget.CheckBox" "${1:-0}") || return 1
  [ -z "$b" ] && { echo "NO_ENCONTRADO(checkbox #${1:-0})"; return 1; }
  echo -n "tap checkbox -> "; tap_bounds "$b"
}

# adb input text usa %s para el espacio (no se pueden teclear acentos)
type_text() { "$ADB" shell input text "$1"; sleep 1; }

# Cierra el teclado solo si esta visible (si no, BACK navegaria hacia atras)
hide_keyboard() {
  if "$ADB" shell dumpsys input_method 2>/dev/null | grep -q "mInputShown=true"; then
    "$ADB" shell input keyevent 4; sleep 1
  fi
}

swipe_up()   { "$ADB" shell input swipe 540 1800 540 500 300; sleep 1; }
swipe_down() { "$ADB" shell input swipe 540 500 540 1800 300; sleep 1; }

# Espera a que un texto aparezca en pantalla (con reintentos)
wait_for_text() {
  local texto="$1" intentos="${2:-10}" i
  for i in $(seq 1 "$intentos"); do
    if [ -n "$(bounds_of_text "$texto")" ]; then
      echo "aparecio: '$texto'"
      return 0
    fi
    sleep 2
  done
  echo "NO_APARECIO(texto): $texto" >&2
  return 1
}

# Estado del CheckBox (0 = indice) -> "checked=\"true\""
checkbox_estado() {
  dump || return 1
  grep -o 'class="android.widget.CheckBox"[^>]*checked="[^"]*"' "$UIXML" \
    | grep -o 'checked="[^"]*"' | head -1
}

shot() {  # $1 = ruta destino completa
  "$ADB" exec-out screencap -p > "$1"; echo "captura -> $1"
}

# Lanza la app SIN borrar datos (usar reset_app para partir de cero)
lanzar_app() {
  "$ADB" shell am force-stop "$PKG" >/dev/null 2>&1
  "$ADB" shell am start -n "$PKG/.MainActivity" >/dev/null 2>&1
  sleep 5
}

login() {
  echo "== Login (datos ficticios) =="
  tap_edittext 0; type_text "12.345.678-9"; hide_keyboard
  tap_edittext 1; type_text "secreta123"; hide_keyboard
  tap_text "INICIAR SESIÓN"; sleep 3
}
