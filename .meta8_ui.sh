#!/bin/bash
# Helper de automatizacion UI para la evidencia de la Meta 8
# (//sdcard evita que Git Bash convierta la ruta a C:\Program Files\Git\sdcard)
ADB="$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe"
UIXML="./ui_dump.xml"

dump() {
  "$ADB" shell uiautomator dump //sdcard/ui.xml >/dev/null 2>&1
  "$ADB" pull //sdcard/ui.xml "$UIXML" >/dev/null 2>&1
  [ -s "$UIXML" ]
}

texts() { dump || return 1; grep -o 'text="[^"]*"' "$UIXML" | sed 's/text="//;s/"$//' | grep -v '^$' | sort -u; }

bounds_of() {
  # Escapa caracteres especiales de regex para que el texto se busque literal
  local pat; pat=$(printf '%s' "$1" | sed 's/[.[\*^$()+?{|]/\\&/g')
  grep -o "text=\"$pat\"[^>]*bounds=\"\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]\"" "$UIXML" | grep -o '\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]' | head -1
}

tap() {
  dump || { echo "dump fallido"; return 1; }
  local b; b=$(bounds_of "$1")
  if [ -z "$b" ]; then echo "NO_ENCONTRADO: $1"; return 1; fi
  # bounds = [x1,y1][x2,y2] -> "x1,y1;x2,y2"
  b=${b//'['/}; b=${b//']'/';'}
  local x1 y1 x2 y2 cx cy
  IFS=',;' read -r x1 y1 x2 y2 <<< "$b"
  cx=$(( (x1 + x2) / 2 )); cy=$(( (y1 + y2) / 2 ))
  echo "tap($cx,$cy) -> $1"
  "$ADB" shell input tap "$cx" "$cy"
}

type_text() { "$ADB" shell input text "$1"; }
swipe_up() { "$ADB" shell input swipe 540 1800 540 500 300; }
shot() { "$ADB" exec-out screencap -p > "docs/evidencia_meta8/$1"; echo "captura: $1"; }
