# Flappy Lula para Android

Esta pasta contém a versão Android nativa do jogo, escrita em Java. A versão Swing continua na pasta principal.

## Como abrir e gerar o APK

1. Instale o Android Studio e o Android SDK Platform 37.
2. No Android Studio, abra esta pasta (`android-app`) e aguarde a sincronização do Gradle.
3. Para gerar o APK pelo terminal, execute `powershell -ExecutionPolicy Bypass -File .\build-apk.ps1` nesta pasta.
   A primeira execução baixa o Gradle 9.6.0 e as dependências do projeto.

O APK de depuração fica em `app/build/outputs/apk/debug/app-debug.apk`. Copie-o para o celular e abra o arquivo para instalar. O Android pode pedir que você permita a instalação de apps dessa origem.

## Controles

- Toque em qualquer lugar no menu para iniciar.
- Toque na tela durante a partida para impulsionar o personagem.
- Toque na tela depois de uma colisão para jogar novamente.

O app trava a orientação em retrato, ocupa a tela inteira e ajusta a área de jogo à proporção disponível no aparelho. Os sprites dos obstáculos são preparados uma vez por partida e reutilizados durante a animação.
