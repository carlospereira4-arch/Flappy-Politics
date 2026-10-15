# Flappy Lula

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

### Controles

| Versão | Comandos |
| --- | --- |
| Android | Toque em **JOGAR** para iniciar e toque na tela durante a partida para impulsionar o personagem. |
| Desktop | Use **Espaço** para voar. Use **R** para reiniciar a partida. |

## Requisitos

- **Desktop:** Java Development Kit (JDK) 17 ou superior.
- **Android:** Android Studio e Android SDK Platform 37. O script de compilação baixa o Gradle 9.6.0 quando necessário.

## Executar no computador

No PowerShell, a partir da pasta principal do projeto:

```powershell
$sources = Get-ChildItem .\src\desktop\java -Recurse -Filter *.java | ForEach-Object FullName
javac -encoding UTF-8 -d .\out $sources
Copy-Item .\src\desktop\resources\* .\out -Recurse -Force
java -cp .\out com.flappylula.desktop.FlappyBird
```

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
├── src/desktop/
│   ├── java/                     # Código da versão Java Swing
│   └── resources/                # Imagens e fontes da versão desktop
├── fonts/                        # Fontes e arquivos de licença
└── README.md
```

As imagens e fontes são organizadas como recursos de cada versão. As fontes incluídas no projeto acompanham seus arquivos de licença.

## Dados e privacidade

Na versão Android, o perfil e o progresso são armazenados localmente no aparelho. A criação do perfil usa nome, idade e uma foto escolhida pelo jogador; o jogo não solicita e-mail nem requer uma conta on-line. O progresso de um perfil pode ser retomado usando o mesmo nome no mesmo aparelho.

## Desenvolvimento

O código das duas versões está em Java. A versão desktop usa Swing; a versão Android usa a interface nativa da plataforma. Os arquivos gerados por compilação e os caches locais do Android Studio/Gradle não fazem parte dos fontes do projeto.
