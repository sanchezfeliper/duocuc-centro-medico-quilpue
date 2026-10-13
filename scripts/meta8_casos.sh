#!/bin/bash
# ============================================================================
# Meta 8 - Automatizacion de casos VALIDOS e INVALIDOS en P05 y P06
# ----------------------------------------------------------------------------
# Cubre el ciclo incremental de la pauta sobre un emulador Android:
#   Build -> Run -> Probar (valido / invalido) -> Capturar -> Commit
#
# Casos automatizados:
#   P05 Carga de documento   INVALIDO: guardar con formulario vacio
#                            VALIDO:   formulario completo -> dialogo de exito
#   P06 Validacion           INVALIDO: rechazar sin comentario (obligatorio)
#                            VALIDO:   rechazar con comentario -> snackbar exito
#
# Requisitos:
#   1) Emulador arriba:  emulator -avd Pixel_7 -no-snapshot -no-audio -no-boot-anim -no-window &
#      (o cualquier AVD con USB debug);  adb wait-for-device
#   2) App instalada:    gradlew :app:installDebug
#
# Uso:  ./scripts/meta8_casos.sh
# ============================================================================
set -u

ADB="${ADB:-$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe}"
PKG="cl.cmq.salud"
OUT="docs/evidencia_meta8"
UIXML="./ui_dump.xml"

# ----------------------------------------------------------------------------
# Helpers de UI (uiautomator + adb input)
# ----------------------------------------------------------------------------

# Dumpea la jerarquia de UI. Borra el archivo remoto ANTES de dumpear: si el
# dump falla, no se reutiliza evidencia vieja (bug detectado en la primera
# version del helper: se estaba leyendo un ui.xml de una sesion anterior).
dump() {
  "$ADB" shell rm //sdcard/ui.xml >/dev/null 2>&1
  "$ADB" shell uiautomator dump //sdcard/ui.xml >/dev/null 2>&1
  "$ADB" pull //sdcard/ui.xml "$UIXML" >/dev/null 2>&1
  [ -s "$UIXML" ]
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

tap_edittext() {  # $1 = indice del EditText visible (0-based, arriba a abajo)
  local b; b=$(bounds_of_class "android.widget.EditText" "$1") || return 1
  [ -z "$b" ] && { echo "NO_ENCONTRADO(edittext #$1)"; return 1; }
  echo -n "tap edittext #$1 -> "; tap_bounds "$b"
}

tap_checkbox() {  # $1 = indice del CheckBox (0-based)
  local b; b=$(bounds_of_class "android.widget.CheckBox" "${1:-0}") || return 1
  [ -z "$b" ] && { echo "NO_ENCONTRADO(checkbox #${1:-0})"; return 1; }
  echo -n "tap checkbox -> "; tap_bounds "$b"
}

# adb input text usa %s para el espacio (los acentos no se pueden teclear aqui)
type_text() { "$ADB" shell input text "$1"; sleep 1; }

# Cierra el teclado solo si esta visible (si no, BACK navegaria hacia atras)
hide_keyboard() {
  if "$ADB" shell dumpsys input_method 2>/dev/null | grep -q "mInputShown=true"; then
    "$ADB" shell input keyevent 4; sleep 1
  fi
}

swipe_up()   { "$ADB" shell input swipe 540 1800 540 500 300; sleep 1; }
swipe_down() { "$ADB" shell input swipe 540 500 540 1800 300; sleep 1; }

shot() { "$ADB" exec-out screencap -p > "$OUT/$1"; echo "captura -> $OUT/$1"; }

# Verifica el estado del CheckBox (0 = indice) e informa si quedo marcado
checkbox_estado() {
  dump || return 1
  grep -o 'class="android.widget.CheckBox"[^>]*checked="[^"]*"' "$UIXML" | grep -o 'checked="[^"]*"' | head -1
}

# ----------------------------------------------------------------------------
# Preparacion: app desde cero en Login
# ----------------------------------------------------------------------------
reset_app() {
  echo "== Preparando app desde cero =="
  "$ADB" shell pm clear "$PKG" >/dev/null 2>&1
  "$ADB" shell am start -n "$PKG/.MainActivity" >/dev/null 2>&1
  sleep 6
}

login() {
  echo "== Login (datos ficticios) =="
  tap_edittext 0; type_text "12.345.678-9"; hide_keyboard
  tap_edittext 1; type_text "secreta123"; hide_keyboard
  tap_text "INICIAR SESIÓN"; sleep 3
}

# ----------------------------------------------------------------------------
# Casos de prueba
# ----------------------------------------------------------------------------
p05_invalido() {
  echo ""
  echo "== P05 CASO INVALIDO: guardar formulario vacio =="
  tap_text "Cargar documento"; sleep 2
  swipe_up; swipe_up
  tap_text "Guardar y Registrar Documento"; sleep 2
  shot "08_carga_invalido_vacio.png"
}

p05_valido() {
  echo ""
  echo "== P05 CASO VALIDO: formulario completo =="
  swipe_down; swipe_down
  tap_edittext 0; type_text "18.765.432-7"; hide_keyboard     # RUT con DV correcto (Modulo 11)
  tap_edittext 2; type_text "constancia_meta8_ok.pdf"; hide_keyboard
  tap_edittext 3; type_text "Constancia%sde%sprueba%sMeta%s8"; hide_keyboard
  tap_checkbox
  echo "estado checkbox: $(checkbox_estado)"
  swipe_up; swipe_up
  tap_text "Guardar y Registrar Documento"; sleep 3
  shot "09_carga_valido_exito.png"
}

p06_invalido() {
  echo ""
  echo "== P06 CASO INVALIDO: rechazar sin comentario =="
  tap_text "Ver en Inicio"; sleep 3                # cierra el exito de P05 -> Home
  tap_text "Validaciones pendientes"; sleep 3
  shot "10a_validacion_bandeja.png"
  tap_text "Certificado capacitacion"; sleep 2     # selecciona un pendiente de la bandeja
  swipe_up
  tap_text "RECHAZAR"; sleep 2
  shot "10_validacion_invalido_sin_comentario.png"
}

p06_valido() {
  echo ""
  echo "== P06 CASO VALIDO: rechazar con comentario =="
  tap_edittext 0; type_text "Documento%sno%slegible"    # unico EditText: comentario del revisor
  hide_keyboard
  swipe_up
  tap_text "RECHAZAR"; sleep 2
  shot "11_validacion_valido_rechazo.png"
}

# ----------------------------------------------------------------------------
# Ejecucion
# ----------------------------------------------------------------------------
mkdir -p "$OUT"
reset_app
login
p05_invalido
p05_valido
p06_invalido
p06_valido

echo ""
echo "== Evidencia generada =="
ls -1 "$OUT"
