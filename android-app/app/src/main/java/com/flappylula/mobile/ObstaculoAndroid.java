package com.flappylula.mobile;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Random;

/** Par de sprites que forma a passagem e a caixa de colisão de um obstáculo. */
public final class ObstaculoAndroid {
    private static final float PROP_Y_INICIAL = 76f / 1264f;
    private static final float PROP_Y_FINAL = 1110f / 1264f;
    private static final float PROP_ALTURA_ARTE = PROP_Y_FINAL - PROP_Y_INICIAL;
    private static final float PROP_X_ARTE = 199f / 843f;
    private static final float PROP_LARGURA_ARTE = (642f - 199f) / 843f;

    private final int largura;
    private final float inicioPassagem;
    private final float alturaPassagem;
    private final float distanciaProximo;
    private final Bitmap parteSuperior;
    private final Bitmap parteInferior;
    private float x;

    public ObstaculoAndroid(float x, float alturaMundo, Random aleatorio,
                             Bitmap imagem, Paint tintaImagem) {
        this.x = x;
        largura = aleatorio.nextInt(41) + 135;
        alturaPassagem = aleatorio.nextInt(61) + 160;
        float margem = 70f;
        float maximoInicio = Math.max(margem, alturaMundo - alturaPassagem - margem);
        int maximo = Math.max(0, (int) (maximoInicio - margem));
        inicioPassagem = margem + (maximo == 0 ? 0 : aleatorio.nextInt(maximo + 1));
        distanciaProximo = aleatorio.nextInt(121) + 360;
        parteSuperior = criarParteSuperior(imagem, tintaImagem);
        parteInferior = criarParteInferior(imagem, alturaMundo, tintaImagem);
    }

    public void atualizar(float delta, float velocidade) {
        x -= velocidade * delta;
    }

    public float getX() {
        return x;
    }

    public float getDistanciaProximo() {
        return distanciaProximo;
    }

    public float getLargura() {
        return largura;
    }

    public float getCentroPassagem() {
        return inicioPassagem + alturaPassagem / 2f;
    }

    public void desenhar(Canvas canvas, Paint tintaImagem) {
        if (parteSuperior != null) {
            canvas.drawBitmap(parteSuperior, x, 0, tintaImagem);
        }
        if (parteInferior != null) {
            canvas.drawBitmap(parteInferior, x, inicioPassagem + alturaPassagem, tintaImagem);
        }
    }

    public boolean colide(RectF hitbox) {
        float xArte = x + largura * PROP_X_ARTE;
        float larguraArte = largura * PROP_LARGURA_ARTE;
        float fimPassagem = inicioPassagem + alturaPassagem;
        return hitbox.left < xArte + larguraArte && hitbox.right > xArte
                && (hitbox.top < inicioPassagem || hitbox.bottom > fimPassagem);
    }

    public void liberar() {
        if (parteSuperior != null && !parteSuperior.isRecycled()) {
            parteSuperior.recycle();
        }
        if (parteInferior != null && !parteInferior.isRecycled()) {
            parteInferior.recycle();
        }
    }

    private Bitmap criarParteSuperior(Bitmap imagem, Paint tintaImagem) {
        if (imagem == null || inicioPassagem < 1) {
            return null;
        }
        Bitmap parte = Bitmap.createBitmap(largura, Math.max(1, (int) inicioPassagem),
                Bitmap.Config.ARGB_8888);
        float alturaArte = inicioPassagem / PROP_ALTURA_ARTE;
        Canvas canvas = new Canvas(parte);
        canvas.translate(0, PROP_Y_FINAL * alturaArte);
        canvas.scale(largura / (float) imagem.getWidth(),
                -alturaArte / imagem.getHeight());
        canvas.drawBitmap(imagem, 0, 0, tintaImagem);
        return parte;
    }

    private Bitmap criarParteInferior(Bitmap imagem, float alturaMundo, Paint tintaImagem) {
        if (imagem == null) {
            return null;
        }
        float fimPassagem = inicioPassagem + alturaPassagem;
        int alturaParte = Math.max(1, (int) (alturaMundo - fimPassagem));
        Bitmap parte = Bitmap.createBitmap(largura, alturaParte, Bitmap.Config.ARGB_8888);
        float alturaArte = alturaParte / PROP_ALTURA_ARTE;
        Canvas canvas = new Canvas(parte);
        canvas.translate(0, -PROP_Y_INICIAL * alturaArte);
        canvas.scale(largura / (float) imagem.getWidth(), alturaArte / imagem.getHeight());
        canvas.drawBitmap(imagem, 0, 0, tintaImagem);
        return parte;
    }
}
