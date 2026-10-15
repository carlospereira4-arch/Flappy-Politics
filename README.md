# Flappy Lula

<p align="center">
  <img src="assets/images/branding/app_logo.png" alt="Logo do Flappy Lula" width="220" />
</p>

<p align="center">
  <a href="https://github.com/carlospereira4-arch/Flappy-Politics/releases/latest">
    <img src="https://img.shields.io/badge/Android-baixar%20APK-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Baixar o APK Android" />
  </a>
</p>

> **Quer jogar no celular?** [Baixe o APK mais recente na página de Releases](https://github.com/carlospereira4-arch/Flappy-Politics/releases/latest) e escolha `app-debug.apk` em **Assets**. Se ainda não houver uma publicação, consulte [como gerar uma versão](#compilar-para-android).

Um jogo de voo inspirado em *Flappy Bird*, desenvolvido em Java como projeto de aprendizado de programação orientada a objetos. O jogador guia o personagem por um percurso com obstáculos, tenta sobreviver o máximo possível e coleta moedas pelo caminho.

O projeto tem duas versões:

- **Android:** aplicativo nativo em Java, pensado para jogar com o celular na vertical e controles por toque.
- **Desktop:** versão para computador feita com Java Swing e controles de teclado.

## O jogo

- O personagem cai continuamente; cada toque ou comando de voo dá um impulso para cima.
- Desvie dos obstáculos e colete moedas durante a partida.
- O placar de tempo conta os segundos sobrevividos. As moedas coletadas aparecem em um contador separado.
- Uma colisão encerra a partida.
- Na versão Android, é possível criar perfis locais, acompanhar recordes e partidas, comprar personagens na loja e escolher qual usar.
- Na versão desktop, o saldo, os personagens desbloqueados e os recordes também são salvos por perfil neste computador.

### Controles

| Versão | Comandos |
| --- | --- |
| Android | Toque em **JOGAR** para iniciar e toque na tela durante a partida para impulsionar o personagem. |
| Desktop | Crie/recupere o perfil local com nome, idade e foto; use **Espaço** para voar, **R** para reiniciar e **Esc** para voltar ao menu. |

## Requisitos

- **Desktop:** Java Development Kit (JDK) 17 ou superior.
- **Android:** Android Studio e Android SDK Platform 37. O script de compilação baixa o Gradle 9.6.0 quando necessário.

## Executar no computador

No Bash (por exemplo, Git Bash), a partir da pasta principal, execute:

```bash
bash run-desktop.sh
```

O script compila os fontes, copia imagens, fontes e sons e abre o jogo. No Windows, você também pode abrir o PowerShell na pasta do projeto e executar:

```powershell
.\run-desktop.ps1
```

Ou dê duplo clique em `run-desktop.bat` no Explorador de Arquivos.

Use um dos iniciadores acima no terminal correspondente. Os comandos de PowerShell abaixo não devem ser colados no Git Bash. Também é possível compilar manualmente no PowerShell:

```powershell
New-Item -ItemType Directory -Force .\out | Out-Null
$sources = Get-ChildItem .\src\desktop\java -Recurse -Filter *.java | ForEach-Object FullName
Copy-Item .\libs\jlayer-1.0.1\javazoom .\out -Recurse -Force
javac -encoding UTF-8 -cp .\out -d .\out $sources
Copy-Item .\src\desktop\resources\* .\out -Recurse -Force
Copy-Item .\fonts .\out -Recurse -Force
java -cp .\out com.flappylula.desktop.FlappyBird
```

Na primeira inicialização, informe nome e idade e escolha uma foto para criar o perfil local. Depois, os dados do último perfil são preenchidos automaticamente; usar o mesmo nome recupera as moedas, compras e recordes. Durante a partida, **Espaço** dá impulso, **R** recomeça após uma colisão e **Esc** volta ao menu. No menu, abra a loja para comprar ou equipar personagens com as moedas coletadas.

Os arquivos compilados são colocados em `out/`.

## Compilar para Android

Abra a pasta `android-app` no Android Studio, aguarde a sincronização e execute o app em um emulador ou aparelho conectado. Para gerar um APK de depuração pelo PowerShell, entre em `android-app` e execute:

```powershell
powershell -ExecutionPolicy Bypass -File .\build-apk.ps1
```

O APK gerado ficará em `android-app/app/build/outputs/apk/debug/app-debug.apk`. Para instalar manualmente, transfira esse arquivo para o celular e abra-o; talvez seja necessário permitir a instalação de aplicativos pela origem usada.

Mais detalhes sobre perfil, progresso e loja estão em [android-app/README.md](android-app/README.md).

## Estrutura do projeto

```text
Flappy Lula/
├── android-app/                 # Aplicativo Android nativo em Java
│   └── app/src/main/             # Código, recursos e configuração do app
├── assets/
│   ├── audio/original/           # Arquivos de som originais
│   └── images/
│       ├── branding/             # Logo do projeto
│       ├── source/               # Imagens originais usadas para conversões
│       ├── processed/            # Versões convertidas para o jogo
│       └── archive/              # Cópias antigas de imagens
├── src/desktop/
│   ├── java/                     # Código da versão Java Swing
│   └── resources/                # Imagens, fontes e sons da versão desktop
├── fonts/                        # Fontes e arquivos de licença
├── libs/                         # Decodificador MP3 usado pela versão desktop
├── run-desktop.bat               # Iniciador por duplo clique no Windows
├── run-desktop.ps1               # Iniciador PowerShell
├── run-desktop.sh                # Iniciador Git Bash
└── README.md
```

`assets/` guarda os arquivos fonte e as versões processadas; as cópias usadas durante o jogo ficam nas pastas de recursos de cada versão. As fontes incluídas no projeto acompanham seus arquivos de licença.

## Dados e privacidade

Na versão Android, o perfil e o progresso são armazenados localmente no aparelho. A criação do perfil usa nome, idade e uma foto escolhida pelo jogador; o jogo não solicita e-mail nem requer uma conta on-line. O progresso de um perfil pode ser retomado usando o mesmo nome no mesmo aparelho. Na versão desktop, nome, idade, foto, saldo, recordes e compras são salvos localmente em `~/.flappy-lula/perfis` (na pasta do usuário do computador).

## Desenvolvimento

O código das duas versões está em Java. A versão desktop usa Swing; a versão Android usa a interface nativa da plataforma. Os arquivos gerados por compilação e os caches locais do Android Studio/Gradle não fazem parte dos fontes do projeto.
