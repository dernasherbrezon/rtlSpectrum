#!/usr/bin/env bats
# Tests for rtlSpectrum.sh argument routing.
# Uses a fake `java` on PATH that echoes its arguments, so no GUI is launched.

setup() {
  FAKEBIN="$(mktemp -d)"
  cat > "$FAKEBIN/java" <<'EOF'
#!/bin/sh
echo "JAVAARGS:$*"
EOF
  chmod +x "$FAKEBIN/java"
  export PATH="$FAKEBIN:$PATH"
  SCRIPT="$BATS_TEST_DIRNAME/../rtlSpectrum.sh"
}

teardown() {
  rm -rf "$FAKEBIN"
}

@test "naked file passes through with default jar" {
  run "$SCRIPT" base.csv
  [ "$status" -eq 0 ]
  [[ "$output" == *"-jar target/rtlSpectrum_linux.jar base.csv"* ]]
}

@test "base plus add and subtract keep CLI order" {
  run "$SCRIPT" base.csv --add a.csv --subtract s.csv --add b.csv
  [ "$status" -eq 0 ]
  [[ "$output" == *"base.csv --add a.csv --subtract s.csv --add b.csv"* ]]
}

@test "--jar selects jarfile and passes remaining args" {
  run "$SCRIPT" --jar /tmp/x.jar base.csv
  [ "$status" -eq 0 ]
  [[ "$output" == *"-jar /tmp/x.jar base.csv"* ]]
}

@test "bare --jar errors with exit 1" {
  run "$SCRIPT" --jar
  [ "$status" -eq 1 ]
  [[ "$output" == *"--jar requires a file argument"* ]]
}

@test "--help passes through to the app" {
  run "$SCRIPT" --help
  [ "$status" -eq 0 ]
  [[ "$output" == *"-jar target/rtlSpectrum_linux.jar --help"* ]]
}

@test "multiple naked files and subtract-equals pass through in order" {
  run "$SCRIPT" base.csv s1.csv --subtract=x.csv s2.csv
  [ "$status" -eq 0 ]
  [[ "$output" == *"base.csv s1.csv --subtract=x.csv s2.csv"* ]]
}
