#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

mkdir -p out
cp -R libs/jlayer-1.0.1/. out/
mapfile -d '' sources < <(find src/desktop/java -type f -name '*.java' -print0)
if ((${#sources[@]} == 0)); then
    echo "Nenhum arquivo Java encontrado em src/desktop/java." >&2
    exit 1
fi

javac -encoding UTF-8 -cp out -d out "${sources[@]}"
cp -R src/desktop/resources/. out/
cp -R fonts out/
exec java -cp out com.flappylula.desktop.FlappyBird
