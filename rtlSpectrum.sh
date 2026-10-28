#!/bin/sh
# Launch rtlSpectrum with native access enabled (silences JavaFX warnings on JDK 17+).
# Usage: ./rtlSpectrum.sh [--jar <jarfile>] [app args...]
JAR="target/rtlSpectrum_linux.jar"
if [ "${1:-}" = "--jar" ]; then
  if [ $# -lt 2 ]; then
    echo "ERROR: --jar requires a file argument" >&2
    exit 1
  fi
  JAR="$2"
  shift 2
fi
exec java --enable-native-access=ALL-UNNAMED -jar "$JAR" "$@"
