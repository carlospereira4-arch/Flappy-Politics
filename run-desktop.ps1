$ErrorActionPreference = 'Stop'

$projectRoot = $PSScriptRoot
$saida = Join-Path $projectRoot 'out'
$fontes = Join-Path $projectRoot 'src\desktop\java'
$recursos = Join-Path $projectRoot 'src\desktop\resources'
$biblioteca = Join-Path $projectRoot 'libs\jlayer-1.0.1\javazoom'

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    throw 'O compilador javac não foi encontrado. Instale um JDK 17 e configure o PATH.'
}
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw 'O Java não foi encontrado. Instale um JDK 17 e configure o PATH.'
}
if (-not (Test-Path -LiteralPath $biblioteca)) {
    throw 'A biblioteca de áudio do jogo não foi encontrada em libs/jlayer-1.0.1.'
}

New-Item -ItemType Directory -Force -Path $saida | Out-Null
Copy-Item -LiteralPath $biblioteca -Destination $saida -Recurse -Force
$fontesJava = Get-ChildItem -LiteralPath $fontes -Recurse -Filter '*.java' |
    Select-Object -ExpandProperty FullName
if ($fontesJava.Count -eq 0) {
    throw 'Nenhum arquivo Java encontrado em src/desktop/java.'
}

& javac -encoding UTF-8 -cp $saida -d $saida $fontesJava
if ($LASTEXITCODE -ne 0) {
    throw "A compilação desktop falhou (código $LASTEXITCODE)."
}
Copy-Item -Path (Join-Path $recursos '*') -Destination $saida -Recurse -Force
Copy-Item -LiteralPath (Join-Path $projectRoot 'fonts') -Destination $saida -Recurse -Force
& java -cp $saida 'com.flappylula.desktop.FlappyBird'
if ($LASTEXITCODE -ne 0) {
    throw "O jogo foi encerrado com erro (código $LASTEXITCODE)."
}
