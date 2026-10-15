# Flappy Lula

O repositório contém duas versões do jogo, separadas por plataforma:

- `src/desktop`: versão para computador, em Java Swing.
- `android-app`: app Android nativo, também em Java.

## Versão Android

Abra `android-app` no Android Studio para sincronizar e executar no emulador ou aparelho. Consulte [android-app/README.md](android-app/README.md) para gerar o APK.

## Versão desktop (Swing)

Requer Java 17. Os fontes ficam em `src/desktop/java/com/flappylula/desktop`, e os recursos usados pela versão desktop ficam em `src/desktop/resources`.

No PowerShell, a partir da pasta principal:

```powershell
$sources = Get-ChildItem .\src\desktop\java -Recurse -Filter *.java | ForEach-Object FullName
javac -encoding UTF-8 -d .\out $sources
Copy-Item .\src\desktop\resources\* .\out -Recurse -Force
java -cp .\out com.flappylula.desktop.FlappyBird
```

## Recursos

- `src/desktop/resources`: imagens usadas pelo jogo Swing.
- `fonts`: fontes e licenças compartilhadas pelas versões desktop e Android; a versão desktop as encontra por `src/desktop/resources/fonts`.
- `android-app/app/src/main/assets`: imagens e fontes empacotadas no app Android.

Arquivos `.class`, caches, saídas de compilação e arquivos locais do Android Studio são ignorados pelo Git.
