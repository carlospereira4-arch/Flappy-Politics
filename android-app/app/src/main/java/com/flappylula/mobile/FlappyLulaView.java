package com.flappylula.mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
        JOGANDO,
        FIM_DE_JOGO
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

    private final Bitmap fundoOriginal;
    private final Bitmap imagemPersonagem;
    private final Bitmap imagemObstaculo;
    private final Typeface fonteTitulo;
    private final Typeface fonteDestaque;
    private final Typeface fonteTexto;

    private Bitmap fundoTela;
    private float alturaMundo = 600f;
    private float escalaTela = 1f;
    private float personagemY;
    private float velocidadeVertical;
    private float distanciaCenario;
    private long instanteAnterior;
    private Estado estado = Estado.MENU;
    private boolean atividadeAtiva;
    private boolean callbackAgendado;

    public FlappyLulaView(Context context) {
        super(context);
        setFocusable(true);
        setContentDescription("Flappy Lula. Toque na tela para jogar e voar.");
        fundoOriginal = carregarBitmap("backgroundFullScreen.jpg");
        imagemPersonagem = carregarBitmap("personagem_transparente.png");
        imagemObstaculo = carregarBitmap("obstaculos_transparente.png");
        fonteTitulo = carregarFonte("fonts/Honk.ttf");
        fonteDestaque = carregarFonte("fonts/Oi-Regular.ttf");
        fonteTexto = carregarFonte("fonts/ShortStack-Regular.ttf");
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
        if (fundoOriginal == null) {
            fundoTela = null;
            return;
        }
        if (fundoTela != null && !fundoTela.isRecycled()) {
            fundoTela.recycle();
        }
        fundoTela = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888);
        Canvas canvasFundo = new Canvas(fundoTela);
        float proporcaoAlvo = (float) largura / altura;
        float proporcaoImagem = (float) fundoOriginal.getWidth() / fundoOriginal.getHeight();
        if (proporcaoImagem > proporcaoAlvo) {
            int larguraCortada = Math.round(fundoOriginal.getHeight() * proporcaoAlvo);
            int esquerda = (fundoOriginal.getWidth() - larguraCortada) / 2;
            corteFundo.set(esquerda, 0, esquerda + larguraCortada, fundoOriginal.getHeight());
        } else {
            int alturaCortada = Math.round(fundoOriginal.getWidth() / proporcaoAlvo);
            int topo = (fundoOriginal.getHeight() - alturaCortada) / 2;
            corteFundo.set(0, topo, fundoOriginal.getWidth(), topo + alturaCortada);
        }
        destinoFundo.set(0, 0, largura, altura);
        canvasFundo.drawBitmap(fundoOriginal, corteFundo, destinoFundo, tintaImagem);
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
            return;
        }

        for (ObstaculoAndroid obstaculo : obstaculos) {
            obstaculo.atualizar(delta, VELOCIDADE);
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
                ultimo = new ObstaculoAndroid(ultimo.getX() + ultimo.getDistanciaProximo(),
                        alturaMundo, aleatorio, imagemObstaculo, tintaImagem);
                obstaculos.add(ultimo);
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
        float x = LARGURA_MUNDO + 35f;
        while (x < LARGURA_MUNDO * 3f) {
            ObstaculoAndroid obstaculo = new ObstaculoAndroid(
                    x, alturaMundo, aleatorio, imagemObstaculo, tintaImagem);
            obstaculos.add(obstaculo);
            x += obstaculo.getDistanciaProximo();
        }
        agendarProximoQuadro();
        invalidate();
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
        } else {
            desenharFundo(canvas, true);
            desenharObstaculos(canvas);
            desenharPersonagem(canvas, personagemY);
            if (estado == Estado.FIM_DE_JOGO) {
                desenharFimDeJogo(canvas);
            }
        }
        canvas.restore();
    }

    private void desenharFundo(Canvas canvas, boolean rolar) {
        if (fundoTela == null) {
            tintaForma.setColor(Color.rgb(112, 197, 206));
            canvas.drawRect(0, 0, LARGURA_MUNDO, alturaMundo, tintaForma);
            return;
        }
        if (!rolar) {
            destino.set(0, 0, LARGURA_MUNDO, alturaMundo);
            canvas.drawBitmap(fundoTela, null, destino, tintaImagem);
            return;
        }
        float deslocamento = distanciaCenario % (LARGURA_MUNDO * 2f);
        for (int i = -1; i < 3; i++) {
            float x = i * LARGURA_MUNDO - deslocamento;
            destino.set(x, 0, x + LARGURA_MUNDO, alturaMundo);
            if ((i & 1) == 0) {
                canvas.drawBitmap(fundoTela, null, destino, tintaImagem);
            } else {
                canvas.save();
                canvas.scale(-1, 1, x + LARGURA_MUNDO / 2f, 0);
                canvas.drawBitmap(fundoTela, null, destino, tintaImagem);
                canvas.restore();
            }
        }
    }

    private void desenharMenu(Canvas canvas) {
        tintaForma.setColor(Color.argb(82, 10, 28, 45));
        canvas.drawRect(0, 0, LARGURA_MUNDO, alturaMundo, tintaForma);
        tintaForma.setColor(Color.argb(130, 0, 0, 0));
        canvas.drawRoundRect(28, 42, 372, 132, 20, 20, tintaForma);
        desenharTextoCentral(canvas, "FLAPPY LULA", 101, 46, fonteTitulo,
                Color.rgb(255, 203, 65), true);
        desenharPersonagemMenu(canvas, alturaMundo * 0.36f - 89f);
        desenharTextoCentral(canvas, "TOQUE PARA VOAR", alturaMundo * 0.60f,
                22, fonteDestaque, Color.WHITE, false);

        float topoBotao = alturaMundo * 0.66f;
        tintaForma.setColor(Color.rgb(255, 218, 75));
        canvas.drawRoundRect(75, topoBotao, 325, topoBotao + 68, 20, 20, tintaForma);
        desenharTextoCentral(canvas, "JOGAR", topoBotao + 44, 25,
                fonteDestaque, Color.rgb(48, 48, 48), false);
        desenharTextoCentral(canvas, "TOQUE NA TELA PARA COMEÇAR", alturaMundo * 0.82f,
                13, fonteTexto, Color.WHITE, false);
    }

    private void desenharPersonagem(Canvas canvas, float y) {
        if (imagemPersonagem != null) {
            destino.set(65, y, 65 + LARGURA_PERSONAGEM, y + ALTURA_PERSONAGEM);
            canvas.drawBitmap(imagemPersonagem, null, destino, tintaImagem);
        }
    }

    private void desenharPersonagemMenu(Canvas canvas, float y) {
        if (imagemPersonagem != null) {
            destino.set((LARGURA_MUNDO - 120f) / 2f, y,
                    (LARGURA_MUNDO + 120f) / 2f, y + 178f);
            canvas.drawBitmap(imagemPersonagem, null, destino, tintaImagem);
        }
    }

    private void desenharObstaculos(Canvas canvas) {
        for (ObstaculoAndroid obstaculo : obstaculos) {
            obstaculo.desenhar(canvas, tintaImagem);
        }
    }

    private void desenharFimDeJogo(Canvas canvas) {
        tintaForma.setColor(Color.argb(105, 0, 0, 0));
        canvas.drawRect(0, 0, LARGURA_MUNDO, alturaMundo, tintaForma);
        float topo = alturaMundo * 0.43f;
        tintaForma.setColor(Color.argb(225, 13, 32, 48));
        canvas.drawRoundRect(24, topo, 376, topo + 150, 20, 20, tintaForma);
        desenharTextoCentral(canvas, "FIM DE JOGO", topo + 61, 27,
                fonteDestaque, Color.WHITE, false);
        desenharTextoCentral(canvas, "TOQUE PARA JOGAR NOVAMENTE", topo + 111, 13,
                fonteTexto, Color.WHITE, false);
    }

    private void desenharTextoCentral(Canvas canvas, String texto, float y, float tamanho,
                                       Typeface fonte, int cor, boolean contorno) {
        tintaTexto.setTypeface(fonte);
        tintaTexto.setTextSize(tamanho);
        tintaTexto.setTextAlign(Paint.Align.CENTER);
        tintaTexto.setColor(cor);
        if (contorno) {
            tintaTexto.setStyle(Paint.Style.STROKE);
            tintaTexto.setStrokeWidth(2.5f);
            tintaTexto.setColor(Color.argb(180, 0, 0, 0));
            canvas.drawText(texto, LARGURA_MUNDO / 2f + 2, y + 2, tintaTexto);
            tintaTexto.setStyle(Paint.Style.FILL);
            tintaTexto.setColor(cor);
        }
        canvas.drawText(texto, LARGURA_MUNDO / 2f, y, tintaTexto);
        tintaTexto.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (estado == Estado.MENU) {
            iniciarPartida();
        } else if (estado == Estado.FIM_DE_JOGO) {
            iniciarPartida();
        } else {
            velocidadeVertical = IMPULSO;
        }
        performClick();
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

}
