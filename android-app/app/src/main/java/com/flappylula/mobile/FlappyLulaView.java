package com.flappylula.mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class FlappyLulaView extends View implements Choreographer.FrameCallback {
    private static final float LARGURA_MUNDO = 400f;
    private static final float VELOCIDADE = 240f;
    private static final float GRAVIDADE = 900f;
    private static final float IMPULSO = -340f;
    private static final float LARGURA_PERSONAGEM = 64f;
    private static final float ALTURA_PERSONAGEM = 95f;
    private enum Estado {
        MENU,
        LOJA,
        JOGANDO,
        FIM_DE_JOGO
    }

    public interface MenuListener {
        void editarPerfil();
    }

    private final Paint tintaImagem = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint tintaForma = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tintaTexto = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Rect corteFundo = new Rect();
    private final Rect destinoFundo = new Rect();
    private final RectF destino = new RectF();
    private final RectF hitboxPersonagem = new RectF();
    private final Random aleatorio = new Random();
    private final List<ObstaculoAndroid> obstaculos = new ArrayList<>();
    private final List<MoedaAndroid> moedas = new ArrayList<>();
    private final ProgressoJogo progresso;
    private final MenuListener menuListener;

    private final Bitmap fundoOriginal;
    private final Bitmap fundoJogoOriginal;
    private final Bitmap[] personagens = new Bitmap[ProgressoJogo.TOTAL_PERSONAGENS];
    private Bitmap imagemPersonagem;
    private final Bitmap imagemObstaculo;
    private final Bitmap imagemMoeda;
    private final Typeface fonteTitulo;
    private final Typeface fonteDestaque;
    private final Typeface fonteTexto;

    private Bitmap fundoTela;
    private Bitmap fundoJogoTela;
    private Bitmap fotoPerfil;
    private float alturaMundo = 600f;
    private float escalaTela = 1f;
    private float personagemY;
    private float velocidadeVertical;
    private float distanciaCenario;
    private long instanteAnterior;
    private float fracaoSegundo;
    private int pontuacaoTempo;
    private int pontuacaoMoedas;
    private boolean novoRecorde;
    private String avisoLoja = "";
    private Estado estado = Estado.MENU;
    private boolean atividadeAtiva;
    private boolean callbackAgendado;

    public FlappyLulaView(Context context, ProgressoJogo progresso, MenuListener menuListener) {
        super(context);
        this.progresso = progresso;
        this.menuListener = menuListener;
        setFocusable(true);
        setContentDescription("Flappy Lula. Toque na tela para jogar e voar.");
        fundoOriginal = carregarBitmap("backgroundFullScreen.jpg");
        fundoJogoOriginal = carregarBitmap("background_jogo.jpg");
        personagens[0] = carregarBitmap("personagem_transparente.png");
        personagens[1] = carregarBitmap("personagem2_transparente.png");
        personagens[2] = carregarBitmap("personagem3_transparente.png");
        personagens[3] = carregarBitmap("personagem4_transparente.png");
        imagemPersonagem = personagens[progresso.getPersonagemSelecionado()];
        imagemObstaculo = carregarBitmap("novo_obstaculo.png");
        imagemMoeda = carregarBitmap("moeda_transparente.png");
        fonteTitulo = carregarFonte("fonts/BitcountInk.ttf");
        fonteDestaque = carregarFonte("fonts/PressStart2P-Regular.ttf");
        fonteTexto = carregarFonte("fonts/PixelifySans.ttf");
        tintaTexto.setTextAlign(Paint.Align.LEFT);
        tintaTexto.setColor(Color.WHITE);
        tintaForma.setStyle(Paint.Style.FILL);
    }

    private Bitmap carregarBitmap(String caminho) {
        try (InputStream entrada = getContext().getAssets().open(caminho)) {
            return BitmapFactory.decodeStream(entrada);
        } catch (IOException e) {
            return null;
        }
    }

    private Typeface carregarFonte(String caminho) {
        try {
            return Typeface.createFromAsset(getContext().getAssets(), caminho);
        } catch (RuntimeException e) {
            return Typeface.DEFAULT_BOLD;
        }
    }

    public void setFotoPerfil(Bitmap foto) {
        fotoPerfil = foto;
        invalidate();
    }

    public void atualizarDadosDoPerfil() {
        invalidate();
    }

    public boolean voltarAoMenu() {
        if (estado == Estado.LOJA || estado == Estado.FIM_DE_JOGO) {
            estado = Estado.MENU;
            invalidate();
            return true;
        }
        return false;
    }

    @Override
    protected void onSizeChanged(int largura, int altura, int larguraAntiga, int alturaAntiga) {
        super.onSizeChanged(largura, altura, larguraAntiga, alturaAntiga);
        if (largura <= 0 || altura <= 0) {
            return;
        }
        escalaTela = largura / LARGURA_MUNDO;
        alturaMundo = altura / escalaTela;
        prepararFundo(largura, altura);
        if (estado == Estado.JOGANDO || estado == Estado.FIM_DE_JOGO) {
            iniciarPartida();
        } else {
            personagemY = alturaMundo * 0.35f;
        }
    }

    private void prepararFundo(int largura, int altura) {
        if (fundoTela != null && !fundoTela.isRecycled()) {
            fundoTela.recycle();
        }
        if (fundoJogoTela != null && !fundoJogoTela.isRecycled()) {
            fundoJogoTela.recycle();
        }
        fundoTela = prepararFundoParaImagem(fundoOriginal, largura, altura);
        fundoJogoTela = prepararFundoParaImagem(fundoJogoOriginal, largura, altura);
    }

    private Bitmap prepararFundoParaImagem(Bitmap origem, int largura, int altura) {
        if (origem == null) {
            return null;
        }
        Bitmap resultado = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888);
        Canvas canvasFundo = new Canvas(resultado);
        float proporcaoAlvo = (float) largura / altura;
        float proporcaoImagem = (float) origem.getWidth() / origem.getHeight();
        if (proporcaoImagem > proporcaoAlvo) {
            int larguraCortada = Math.round(origem.getHeight() * proporcaoAlvo);
            int esquerda = (origem.getWidth() - larguraCortada) / 2;
            corteFundo.set(esquerda, 0, esquerda + larguraCortada, origem.getHeight());
        } else {
            int alturaCortada = Math.round(origem.getWidth() / proporcaoAlvo);
            int topo = (origem.getHeight() - alturaCortada) / 2;
            corteFundo.set(0, topo, origem.getWidth(), topo + alturaCortada);
        }
        destinoFundo.set(0, 0, largura, altura);
        canvasFundo.drawBitmap(origem, corteFundo, destinoFundo, tintaImagem);
        return resultado;
    }

    public void retomar() {
        atividadeAtiva = true;
        instanteAnterior = 0;
        agendarProximoQuadro();
        invalidate();
    }

    public void pausar() {
        atividadeAtiva = false;
        instanteAnterior = 0;
        if (callbackAgendado) {
            Choreographer.getInstance().removeFrameCallback(this);
            callbackAgendado = false;
        }
    }

    private void agendarProximoQuadro() {
        if (atividadeAtiva && estado == Estado.JOGANDO && !callbackAgendado) {
            callbackAgendado = true;
            Choreographer.getInstance().postFrameCallback(this);
        }
    }

    @Override
    public void doFrame(long instante) {
        callbackAgendado = false;
        if (!atividadeAtiva || estado != Estado.JOGANDO) {
            return;
        }
        if (instanteAnterior == 0) {
            instanteAnterior = instante;
        }
        float delta = Math.min((instante - instanteAnterior) / 1_000_000_000f, 0.05f);
        instanteAnterior = instante;
        atualizar(delta);
        invalidate();
        agendarProximoQuadro();
    }

    private void atualizar(float delta) {
        distanciaCenario += VELOCIDADE * delta;
        velocidadeVertical += GRAVIDADE * delta;
        personagemY += velocidadeVertical * delta;

        RectF hitbox = atualizarHitboxPersonagem();
        if (personagemY <= 0 || personagemY + ALTURA_PERSONAGEM >= alturaMundo
                || colidiu(hitbox)) {
            if (personagemY < 0) {
                personagemY = 0;
            } else if (personagemY + ALTURA_PERSONAGEM > alturaMundo) {
                personagemY = alturaMundo - ALTURA_PERSONAGEM;
            }
            estado = Estado.FIM_DE_JOGO;
            novoRecorde = progresso.registrarPartida(pontuacaoTempo, pontuacaoMoedas);
            return;
        }

        fracaoSegundo += delta;
        while (fracaoSegundo >= 1f) {
            pontuacaoTempo++;
            fracaoSegundo -= 1f;
        }

        for (ObstaculoAndroid obstaculo : obstaculos) {
            obstaculo.atualizar(delta, VELOCIDADE);
        }
        for (MoedaAndroid moeda : moedas) {
            moeda.atualizar(delta, VELOCIDADE);
        }
        Iterator<MoedaAndroid> moedasIterador = moedas.iterator();
        while (moedasIterador.hasNext()) {
            MoedaAndroid moeda = moedasIterador.next();
            if (moeda.colide(hitbox)) {
                pontuacaoMoedas++;
                progresso.coletarMoeda();
                moedasIterador.remove();
            } else if (moeda.getX() < -50f) {
                moedasIterador.remove();
            }
        }
        Iterator<ObstaculoAndroid> iterador = obstaculos.iterator();
        while (iterador.hasNext()) {
            ObstaculoAndroid obstaculo = iterador.next();
            if (obstaculo.getX() < -LARGURA_MUNDO) {
                obstaculo.liberar();
                iterador.remove();
            }
        }
        if (!obstaculos.isEmpty()) {
            ObstaculoAndroid ultimo = obstaculos.get(obstaculos.size() - 1);
            while (ultimo.getX() < LARGURA_MUNDO * 2f) {
                ultimo = adicionarObstaculo(ultimo.getX() + ultimo.getDistanciaProximo());
            }
        }
    }

    private RectF atualizarHitboxPersonagem() {
        float proporcaoXImagem = 82f / 683f;
        float proporcaoLarguraImagem = (602f - 82f) / 683f;
        float larguraHitbox = proporcaoLarguraImagem * 0.65f;
        float xHitbox = proporcaoXImagem + (proporcaoLarguraImagem - larguraHitbox) / 2f;
        hitboxPersonagem.set(
                65f + LARGURA_PERSONAGEM * xHitbox,
                personagemY + ALTURA_PERSONAGEM * (217f / 1024f),
                65f + LARGURA_PERSONAGEM * (xHitbox + larguraHitbox),
                personagemY + ALTURA_PERSONAGEM * (891f / 1024f));
        return hitboxPersonagem;
    }

    private boolean colidiu(RectF hitbox) {
        for (ObstaculoAndroid obstaculo : obstaculos) {
            if (obstaculo.colide(hitbox)) {
                return true;
            }
        }
        return false;
    }

    private void iniciarPartida() {
        estado = Estado.JOGANDO;
        distanciaCenario = 0;
        velocidadeVertical = 0;
        personagemY = alturaMundo * 0.35f;
        instanteAnterior = 0;
        for (ObstaculoAndroid obstaculo : obstaculos) {
            obstaculo.liberar();
        }
        obstaculos.clear();
        moedas.clear();
        pontuacaoTempo = 0;
        pontuacaoMoedas = 0;
        fracaoSegundo = 0;
        novoRecorde = false;
        float x = LARGURA_MUNDO + 35f;
        while (x < LARGURA_MUNDO * 3f) {
            ObstaculoAndroid obstaculo = adicionarObstaculo(x);
            x += obstaculo.getDistanciaProximo();
        }
        agendarProximoQuadro();
        invalidate();
    }

    private ObstaculoAndroid adicionarObstaculo(float x) {
        ObstaculoAndroid obstaculo = new ObstaculoAndroid(
                x, alturaMundo, aleatorio, imagemObstaculo, tintaImagem);
        obstaculos.add(obstaculo);
        moedas.add(new MoedaAndroid(x + obstaculo.getLargura() / 2f,
                obstaculo.getCentroPassagem(), imagemMoeda));
        return obstaculo;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        canvas.save();
        canvas.scale(escalaTela, escalaTela);
        if (estado == Estado.MENU) {
            desenharFundo(canvas, false);
            desenharMenu(canvas);
        } else if (estado == Estado.LOJA) {
            desenharFundo(canvas, false);
            desenharLoja(canvas);
        } else {
            desenharFundo(canvas, true);
            desenharObstaculos(canvas);
            desenharMoedas(canvas);
            desenharPersonagem(canvas, personagemY);
            desenharPlacar(canvas);
            if (estado == Estado.FIM_DE_JOGO) {
                desenharFimDeJogo(canvas);
            }
        }
        canvas.restore();
    }

    private void desenharFundo(Canvas canvas, boolean rolar) {
        Bitmap fundoExibido = rolar ? fundoJogoTela : fundoTela;
        if (fundoExibido == null) {
            tintaForma.setColor(Color.rgb(112, 197, 206));
            canvas.drawRect(0, 0, LARGURA_MUNDO, alturaMundo, tintaForma);
            return;
        }
        if (!rolar) {
            destino.set(0, 0, LARGURA_MUNDO, alturaMundo);
            canvas.drawBitmap(fundoExibido, null, destino, tintaImagem);
            return;
        }
        float deslocamento = distanciaCenario % (LARGURA_MUNDO * 2f);
        for (int i = -1; i < 3; i++) {
            float x = i * LARGURA_MUNDO - deslocamento;
            destino.set(x, 0, x + LARGURA_MUNDO, alturaMundo);
            if ((i & 1) == 0) {
                canvas.drawBitmap(fundoExibido, null, destino, tintaImagem);
            } else {
                canvas.save();
                canvas.scale(-1, 1, x + LARGURA_MUNDO / 2f, 0);
                canvas.drawBitmap(fundoExibido, null, destino, tintaImagem);
                canvas.restore();
            }
        }
    }

    private void desenharMenu(Canvas canvas) {
        tintaForma.setColor(Color.argb(82, 10, 28, 45));
        canvas.drawRect(0, 0, LARGURA_MUNDO, alturaMundo, tintaForma);
        tintaForma.setColor(Color.argb(130, 0, 0, 0));
        canvas.drawRoundRect(20, 24, 380, 103, 20, 20, tintaForma);
        desenharTextoCentral(canvas, "FLAPPY LULA", 78, 43, fonteTitulo,
                Color.rgb(255, 203, 65), true);
        desenharCartaoPerfil(canvas);
        float alturaPersonagem = Math.min(160f, alturaMundo * 0.25f);
        float larguraPersonagem = alturaPersonagem * 0.68f;
        float topoPersonagem = alturaMundo * 0.34f;
        if (imagemPersonagem != null) {
            destino.set((LARGURA_MUNDO - larguraPersonagem) / 2f, topoPersonagem,
                    (LARGURA_MUNDO + larguraPersonagem) / 2f, topoPersonagem + alturaPersonagem);
            canvas.drawBitmap(imagemPersonagem, null, destino, tintaImagem);
        }

        float topoJogar = alturaMundo - 171f;
        desenharBotao(canvas, 75, topoJogar, 325, topoJogar + 60,
                "JOGAR", Color.rgb(255, 218, 75), Color.rgb(48, 48, 48));
        float topoLoja = alturaMundo - 99f;
        desenharBotao(canvas, 75, topoLoja, 325, topoLoja + 54,
                "LOJA DE PERSONAGENS", Color.rgb(29, 103, 130), Color.WHITE);
        desenharTextoCentral(canvas, "TOQUE PARA VOAR", alturaMundo - 18f,
                12, fonteTexto, Color.WHITE, false);
    }

    private void desenharCartaoPerfil(Canvas canvas) {
        tintaForma.setColor(Color.argb(205, 13, 32, 48));
        canvas.drawRoundRect(24, 113, 376, 197, 17, 17, tintaForma);
        float centroX = 69, centroY = 155, raio = 28;
        if (fotoPerfil != null && !fotoPerfil.isRecycled()) {
            Path recorte = new Path();
            recorte.addCircle(centroX, centroY, raio, Path.Direction.CW);
            canvas.save();
            canvas.clipPath(recorte);
            destino.set(centroX - raio, centroY - raio, centroX + raio, centroY + raio);
            canvas.drawBitmap(fotoPerfil, null, destino, tintaImagem);
            canvas.restore();
        } else {
            tintaForma.setColor(Color.rgb(59, 147, 167));
            canvas.drawCircle(centroX, centroY, raio, tintaForma);
            tintaTexto.setTypeface(fonteDestaque);
            tintaTexto.setTextSize(27);
            tintaTexto.setTextAlign(Paint.Align.CENTER);
            tintaTexto.setColor(Color.WHITE);
            canvas.drawText("?", centroX, centroY + 9, tintaTexto);
        }
        tintaTexto.setTypeface(fonteDestaque);
        tintaTexto.setTextAlign(Paint.Align.LEFT);
        tintaTexto.setColor(Color.WHITE);
        tintaTexto.setTextSize(19);
        String nome = progresso.getNomeUsuario().isEmpty() ? "CRIAR PERFIL" : progresso.getNomeUsuario();
        float tamanhoNome = ajustarTamanhoTexto(nome, fonteDestaque, 19, 275, 27);
        tintaTexto.setTextSize(tamanhoNome);
        desenharTextoAjustado(canvas, nome, 111, 145, 275, 27);
        tintaTexto.setTypeface(fonteTexto);
        tintaTexto.setColor(Color.rgb(207, 231, 237));
        tintaTexto.setTextSize(12);
        desenharTextoAjustado(canvas, "IDADE " + progresso.getIdade() + "  •  RECORDE "
                + progresso.getRecordeTempo() + "s", 111, 164, 252, 17);
        tintaTexto.setColor(Color.rgb(255, 226, 92));
        tintaTexto.setTextSize(12);
        desenharTextoAjustado(canvas, "MOEDAS " + progresso.getMoedas() + "  •  "
                + progresso.getPartidasJogadas() + " PARTIDAS", 111, 184, 252, 17);
        tintaTexto.setTextAlign(Paint.Align.LEFT);
    }

    private void desenharBotao(Canvas canvas, float esquerda, float topo, float direita,
                                float base, String texto, int fundo, int corTexto) {
        tintaForma.setColor(fundo);
        canvas.drawRoundRect(esquerda, topo, direita, base, 17, 17, tintaForma);
        desenharTextoNoCentro(canvas, texto, (esquerda + direita) / 2f,
                topo + (base - topo) * 0.66f, 18, fonteDestaque, corTexto, false,
                (direita - esquerda) - 20, (base - topo) * 0.58f);
    }

    private void desenharPersonagem(Canvas canvas, float y) {
        if (imagemPersonagem != null) {
            destino.set(65, y, 65 + LARGURA_PERSONAGEM, y + ALTURA_PERSONAGEM);
            canvas.drawBitmap(imagemPersonagem, null, destino, tintaImagem);
        }
    }

    private void desenharLoja(Canvas canvas) {
        tintaForma.setColor(Color.argb(180, 10, 28, 45));
        canvas.drawRect(0, 0, LARGURA_MUNDO, alturaMundo, tintaForma);
        desenharTextoCentral(canvas, "LOJA DE PERSONAGENS", 61, 29,
                fonteDestaque, Color.rgb(255, 218, 75), false);
        desenharTextoCentral(canvas, "SALDO: " + progresso.getMoedas() + " MOEDAS",
                91, 15, fonteTexto, Color.WHITE, false);

        float topoInicial = 109f;
        float alturaCartao = Math.min(94f, (alturaMundo - 183f) / 4f);
        float espacamento = 8f;
        for (int indice = 0; indice < ProgressoJogo.TOTAL_PERSONAGENS; indice++) {
            float topo = topoInicial + indice * (alturaCartao + espacamento);
            desenharCartaoPersonagem(canvas, indice, topo, alturaCartao);
        }
        if (!avisoLoja.isEmpty()) {
            desenharTextoCentral(canvas, avisoLoja, alturaMundo - 67, 12,
                    fonteTexto, Color.rgb(255, 226, 92), false);
        }
        desenharBotao(canvas, 120, alturaMundo - 54, 280, alturaMundo - 12,
                "VOLTAR", Color.rgb(29, 103, 130), Color.WHITE);
    }

    private void desenharCartaoPersonagem(Canvas canvas, int indice, float topo, float altura) {
        boolean desbloqueado = progresso.personagemDesbloqueado(indice);
        boolean selecionado = progresso.getPersonagemSelecionado() == indice;
        tintaForma.setColor(selecionado ? Color.argb(230, 46, 116, 112)
                : Color.argb(215, 13, 32, 48));
        canvas.drawRoundRect(20, topo, 380, topo + altura, 15, 15, tintaForma);
        Bitmap retrato = personagens[indice];
        if (retrato != null) {
            float alturaRetrato = Math.min(altura - 10, 74);
            float larguraRetrato = alturaRetrato * 0.68f;
            destino.set(31, topo + (altura - alturaRetrato) / 2,
                    31 + larguraRetrato, topo + (altura + alturaRetrato) / 2);
            canvas.drawBitmap(retrato, null, destino, tintaImagem);
        }
        tintaTexto.setTypeface(fonteDestaque);
        tintaTexto.setTextAlign(Paint.Align.LEFT);
        tintaTexto.setColor(Color.WHITE);
        tintaTexto.setTextSize(16);
        desenharTextoAjustado(canvas, ProgressoJogo.getNomePersonagem(indice),
                87, topo + altura * 0.43f, 164, altura * 0.35f);
        tintaTexto.setTypeface(fonteTexto);
        tintaTexto.setTextSize(12);
        tintaTexto.setColor(Color.rgb(215, 231, 235));
        String detalhe = indice == 0 ? "GRÁTIS" : "CUSTO: "
                + ProgressoJogo.getPrecoPersonagem(indice) + " MOEDAS";
        desenharTextoAjustado(canvas, detalhe, 87, topo + altura * 0.72f, 164,
                altura * 0.32f);

        float esquerdaBotao = 266;
        float topoBotao = topo + altura * 0.25f;
        float baseBotao = topo + altura * 0.75f;
        if (desbloqueado) {
            String texto = selecionado ? "EQUIPADO" : "USAR";
            desenharBotao(canvas, esquerdaBotao, topoBotao, 371, baseBotao,
                    texto, selecionado ? Color.rgb(255, 218, 75) : Color.rgb(29, 103, 130),
                    selecionado ? Color.rgb(48, 48, 48) : Color.WHITE);
        } else {
            desenharBotao(canvas, esquerdaBotao, topoBotao, 371, baseBotao,
                    "COMPRAR", Color.rgb(255, 218, 75), Color.rgb(48, 48, 48));
        }
        tintaTexto.setTextAlign(Paint.Align.LEFT);
    }

    private void desenharObstaculos(Canvas canvas) {
        for (ObstaculoAndroid obstaculo : obstaculos) {
            obstaculo.desenhar(canvas, tintaImagem);
        }
    }

    private void desenharMoedas(Canvas canvas) {
        for (MoedaAndroid moeda : moedas) {
            moeda.desenhar(canvas, tintaImagem);
        }
    }

    private void desenharPlacar(Canvas canvas) {
        tintaForma.setColor(Color.argb(190, 13, 32, 48));
        canvas.drawRoundRect(24, 17, 376, 72, 17, 17, tintaForma);
        desenharTextoCentral(canvas, "TEMPO: " + pontuacaoTempo + "s", 54, 22,
                fonteDestaque, Color.WHITE, false);
        if (imagemMoeda != null) {
            destino.set(298, 34, 332, 51);
            canvas.drawBitmap(imagemMoeda, null, destino, tintaImagem);
        }
        desenharTextoNoCentro(canvas, String.valueOf(pontuacaoMoedas), 352, 52,
                20, fonteTexto, Color.rgb(255, 226, 92), false, 38, 25);
    }

    private void desenharFimDeJogo(Canvas canvas) {
        tintaForma.setColor(Color.argb(105, 0, 0, 0));
        canvas.drawRect(0, 0, LARGURA_MUNDO, alturaMundo, tintaForma);
        float topo = alturaMundo * 0.34f;
        tintaForma.setColor(Color.argb(225, 13, 32, 48));
        canvas.drawRoundRect(24, topo, 376, topo + 240, 20, 20, tintaForma);
        desenharTextoCentral(canvas, "FIM DE JOGO", topo + 45, 27,
                fonteDestaque, Color.WHITE, false);
        desenharTextoCentral(canvas, "TEMPO " + pontuacaoTempo + "s  •  MOEDAS " + pontuacaoMoedas,
                topo + 78, 15, fonteTexto, Color.rgb(255, 226, 92), false);
        if (novoRecorde) {
            desenharTextoCentral(canvas, "NOVO RECORDE!", topo + 105, 16,
                    fonteDestaque, Color.rgb(255, 218, 75), false);
        } else {
            desenharTextoCentral(canvas, "RECORDE: " + progresso.getRecordeTempo() + "s",
                    topo + 105, 14, fonteTexto, Color.WHITE, false);
        }
        desenharTextoCentral(canvas, "SALDO: " + progresso.getMoedas() + " MOEDAS",
                topo + 132, 14, fonteTexto, Color.WHITE, false);
        desenharBotao(canvas, 53, topo + 151, 347, topo + 190,
                "JOGAR NOVAMENTE", Color.rgb(255, 218, 75), Color.rgb(48, 48, 48));
        desenharBotao(canvas, 53, topo + 196, 347, topo + 232,
                "VOLTAR AO MENU", Color.rgb(29, 103, 130), Color.WHITE);
    }

    private void desenharTextoCentral(Canvas canvas, String texto, float y, float tamanho,
                                       Typeface fonte, int cor, boolean contorno) {
        desenharTextoNoCentro(canvas, texto, LARGURA_MUNDO / 2f, y,
                tamanho, fonte, cor, contorno);
    }

    private void desenharTextoNoCentro(Canvas canvas, String texto, float x, float y, float tamanho,
                                       Typeface fonte, int cor) {
        desenharTextoNoCentro(canvas, texto, x, y, tamanho, fonte, cor, false);
    }

    private void desenharTextoNoCentro(Canvas canvas, String texto, float x, float y, float tamanho,
                                       Typeface fonte, int cor, boolean contorno) {
        desenharTextoNoCentro(canvas, texto, x, y, tamanho, fonte, cor, contorno,
                LARGURA_MUNDO - 32, 48);
    }

    private void desenharTextoNoCentro(Canvas canvas, String texto, float x, float y, float tamanho,
                                       Typeface fonte, int cor, boolean contorno, float larguraMaxima) {
        desenharTextoNoCentro(canvas, texto, x, y, tamanho, fonte, cor, contorno,
                larguraMaxima, 48);
    }

    private void desenharTextoNoCentro(Canvas canvas, String texto, float x, float y, float tamanho,
                                       Typeface fonte, int cor, boolean contorno,
                                       float larguraMaxima, float alturaMaxima) {
        tintaTexto.setTypeface(fonte);
        tintaTexto.setTextSize(ajustarTamanhoTexto(texto, fonte, tamanho,
                larguraMaxima, alturaMaxima));
        tintaTexto.setTextAlign(Paint.Align.CENTER);
        tintaTexto.setColor(cor);
        if (contorno) {
            tintaTexto.setStyle(Paint.Style.STROKE);
            tintaTexto.setStrokeWidth(2.5f);
            tintaTexto.setColor(Color.argb(180, 0, 0, 0));
            canvas.drawText(texto, x + 2, y + 2, tintaTexto);
            tintaTexto.setStyle(Paint.Style.FILL);
            tintaTexto.setColor(cor);
        }
        canvas.drawText(texto, x, y, tintaTexto);
        tintaTexto.setTextAlign(Paint.Align.LEFT);
    }

    private float ajustarTamanhoTexto(String texto, Typeface fonte, float tamanho,
                                      float larguraMaxima) {
        return ajustarTamanhoTexto(texto, fonte, tamanho, larguraMaxima, 48);
    }

    private float ajustarTamanhoTexto(String texto, Typeface fonte, float tamanho,
                                      float larguraMaxima, float alturaMaxima) {
        tintaTexto.setTypeface(fonte);
        tintaTexto.setTextSize(tamanho);
        float largura = tintaTexto.measureText(texto);
        Paint.FontMetrics metricas = tintaTexto.getFontMetrics();
        float altura = metricas.descent - metricas.ascent;
        float escala = 1f;
        if (largura > larguraMaxima && largura > 0) {
            escala = Math.min(escala, larguraMaxima / largura);
        }
        if (altura > alturaMaxima && altura > 0) {
            escala = Math.min(escala, alturaMaxima / altura);
        }
        if (escala >= 1f) {
            return tamanho;
        }
        return Math.max(7f, tamanho * escala);
    }

    private void desenharTextoAjustado(Canvas canvas, String texto, float x, float y,
                                       float larguraMaxima, float alturaMaxima) {
        tintaTexto.setTextSize(ajustarTamanhoTexto(texto, tintaTexto.getTypeface(),
                tintaTexto.getTextSize(), larguraMaxima, alturaMaxima));
        canvas.drawText(texto, x, y, tintaTexto);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return true;
        }
        float x = event.getX() / escalaTela;
        float y = event.getY() / escalaTela;
        if (estado == Estado.MENU) {
            float topoJogar = alturaMundo - 171f;
            float topoLoja = alturaMundo - 99f;
            if (y >= 113 && y <= 197 && x >= 24 && x <= 376) {
                menuListener.editarPerfil();
            } else if (x >= 75 && x <= 325 && y >= topoJogar && y <= topoJogar + 60) {
                iniciarPartida();
            } else if (x >= 75 && x <= 325 && y >= topoLoja && y <= topoLoja + 54) {
                estado = Estado.LOJA;
                avisoLoja = "";
                invalidate();
            }
        } else if (estado == Estado.LOJA) {
            if (y >= alturaMundo - 58) {
                estado = Estado.MENU;
                avisoLoja = "";
                invalidate();
            } else {
                float topoInicial = 109f;
                float alturaCartao = Math.min(94f, (alturaMundo - 183f) / 4f);
                float espacamento = 8f;
                for (int indice = 0; indice < ProgressoJogo.TOTAL_PERSONAGENS; indice++) {
                    float topo = topoInicial + indice * (alturaCartao + espacamento);
                    if (y >= topo && y <= topo + alturaCartao && x >= 20 && x <= 380) {
                        selecionarOuComprarPersonagem(indice);
                        break;
                    }
                }
            }
        } else if (estado == Estado.FIM_DE_JOGO) {
            float topo = alturaMundo * 0.34f;
            if (y >= topo + 151 && y <= topo + 190 && x >= 53 && x <= 347) {
                iniciarPartida();
            } else if (y >= topo + 196 && y <= topo + 232 && x >= 53 && x <= 347) {
                estado = Estado.MENU;
                invalidate();
            }
        } else {
            velocidadeVertical = IMPULSO;
        }
        performClick();
        return true;
    }

    private void selecionarOuComprarPersonagem(int indice) {
        if (progresso.personagemDesbloqueado(indice)) {
            progresso.selecionarPersonagem(indice);
            imagemPersonagem = personagens[indice];
            estado = Estado.MENU;
            avisoLoja = "";
        } else if (progresso.comprarPersonagem(indice)) {
            imagemPersonagem = personagens[indice];
            avisoLoja = ProgressoJogo.getNomePersonagem(indice) + " COMPRADO!";
        } else {
            int faltam = ProgressoJogo.getPrecoPersonagem(indice) - progresso.getMoedas();
            avisoLoja = "FALTAM " + faltam + " MOEDAS";
        }
        invalidate();
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

}
