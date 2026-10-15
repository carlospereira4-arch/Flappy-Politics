# Flappy Lula para Android

![Logo do Flappy Lula](../assets/images/branding/app_logo.png)

Esta pasta contém a versão Android nativa do jogo, escrita em Java. A versão Swing fica em `../src/desktop`.

**Para instalar no celular:** baixe `app-debug.apk` em [Releases no GitHub](https://github.com/carlospereira4-arch/Flappy-Politics/releases/latest). A cada nova publicação do projeto, o GitHub Actions compila e anexa um APK à versão.

## Como abrir e gerar o APK

1. Instale o Android Studio e o Android SDK Platform 37.
2. No Android Studio, abra esta pasta (`android-app`) e aguarde a sincronização do Gradle.
3. Para gerar o APK pelo terminal, execute `powershell -ExecutionPolicy Bypass -File .\build-apk.ps1` nesta pasta.
   A primeira execução baixa o Gradle 9.6.0 e as dependências do projeto.

O APK de depuração fica em `app/build/outputs/apk/debug/app-debug.apk`. Copie-o para o celular e abra o arquivo para instalar. O Android pode pedir que você permita a instalação de apps dessa origem. Saídas de build e caches do Gradle são gerados automaticamente e ignorados pelo Git.

Para publicar uma versão para download, crie e envie uma tag começando com `v` (por exemplo, `v1.1`). O workflow `.github/workflows/android-apk.yml` compila o app e anexa o APK automaticamente à Release correspondente. Também é possível iniciar a compilação manualmente pela aba **Actions** do GitHub; nesse caso, o resultado aparece como uma pré-lançamento chamada **Latest Android build**.

## Controles

- Toque em **JOGAR** no menu para iniciar.
- Toque na tela durante a partida para impulsionar o personagem.
- Depois de uma colisão, escolha **JOGAR NOVAMENTE** ou volte ao menu.
- O placar central conta os segundos sobrevividos; o contador à direita mostra as cédulas coletadas nas passagens.

## Perfil local, progresso e loja

- No primeiro uso, crie um perfil com nome, idade e uma foto escolhida da galeria.
- O perfil, a foto, o saldo, os recordes, as partidas, os personagens comprados e o personagem equipado ficam salvos apenas no aparelho. O jogo não pede e-mail nem acessa um serviço on-line.
- Cada nome de usuário mantém seu próprio progresso local. Reutilize o nome para voltar ao perfil; use outro nome para iniciar outro perfil neste aparelho.
- As cédulas coletadas durante a partida entram no saldo. Os outros três personagens custam 10, 25 e 50 moedas; a compra desbloqueia e equipa o personagem.

O app trava a orientação em retrato, ocupa a tela inteira e ajusta a área de jogo à proporção disponível no aparelho. Os sprites dos obstáculos são preparados uma vez por partida e reutilizados durante a animação.
