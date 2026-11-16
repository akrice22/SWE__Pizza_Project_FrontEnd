#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

mkdir -p build/classes build/jar

echo "[1/3] Compiling Java Swing sources..."
javac -Xlint:all -d build/classes $(find src -name "*.java")

echo "[2/3] Packaging MomAndPopPizza.jar..."
jar --create --file build/jar/MomAndPopPizza.jar \
    --main-class com.momandpop.pizza.MomAndPopPizzaApp \
    -C build/classes .

echo "[3/3] Launching Mom & Pop Pizzeria..."
java -jar build/jar/MomAndPopPizza.jar "$@"
