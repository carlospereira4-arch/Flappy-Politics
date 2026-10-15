package com.flappylula.desktop;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/** Moeda que acompanha a rolagem do cenário e pode ser coletada uma única vez. */
public final class Moeda {
    private static final BufferedImage IMAGEM = ImagemUtil.carregar("moeda_transparente.png");
    private static final double LARGURA = 42;
    private static final double ALTURA = 21;
    private double centroX;
    private final double centroY;
    private boolean coletada;

    public Moeda(double centroX, double centroY) {
        this.centroX = centroX;
        this.centroY = centroY;
    }

    public void atualizar(double deltaSegundos) {
        centroX -= ConfiguracaoJogo.VELOCIDADE_CENARIO * deltaSegundos;
    }

    public double getX() {
        return centroX;
    }

    public boolean isColetada() {
        return coletada;
    }

    public boolean tentarColetar(Rectangle2D personagem) {
        double margemX = LARGURA * 0.2;
        double margemY = ALTURA * 0.2;
        Ellipse2D hitbox = new Ellipse2D.Double(centroX - LARGURA / 2 + margemX,
                centroY - ALTURA / 2 + margemY,
                LARGURA - margemX * 2, ALTURA - margemY * 2);
        if (!coletada && hitbox.intersects(personagem)) {
            coletada = true;
            return true;
        }
        return false;
    }

    public void desenhar(Graphics2D g) {
        if (!coletada && IMAGEM != null) {
            g.drawImage(IMAGEM, (int) Math.round(centroX - LARGURA / 2),
                    (int) Math.round(centroY - ALTURA / 2),
                    (int) LARGURA, (int) ALTURA, null);
        }
    }
}
