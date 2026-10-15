package com.flappylula.mobile;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/** Nota coletável posicionada dentro da abertura de um obstáculo. */
public final class MoedaAndroid {
    private static final float LARGURA = 42f;
    private static final float ALTURA = 21f;

    private final Bitmap imagem;
    private final RectF destino = new RectF();
    private final RectF hitbox = new RectF();
    private float centroX;
    private final float centroY;

    public MoedaAndroid(float centroX, float centroY, Bitmap imagem) {
        this.centroX = centroX;
        this.centroY = centroY;
        this.imagem = imagem;
    }

    public void atualizar(float delta, float velocidade) {
        centroX -= velocidade * delta;
    }

    public float getX() {
        return centroX;
    }

    public void desenhar(Canvas canvas, Paint tinta) {
        if (imagem == null) {
            return;
        }
        destino.set(centroX - LARGURA / 2f, centroY - ALTURA / 2f,
                centroX + LARGURA / 2f, centroY + ALTURA / 2f);
        canvas.drawBitmap(imagem, null, destino, tinta);
    }

    public boolean colide(RectF personagem) {
        float margemX = LARGURA * 0.22f;
        float margemY = ALTURA * 0.20f;
        hitbox.set(centroX - LARGURA / 2f + margemX,
                centroY - ALTURA / 2f + margemY,
                centroX + LARGURA / 2f - margemX,
                centroY + ALTURA / 2f - margemY);
        return RectF.intersects(personagem, hitbox);
    }
}
