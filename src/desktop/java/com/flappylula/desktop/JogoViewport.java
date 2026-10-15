package com.flappylula.desktop;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class JogoViewport {
    private static final Color COR_FUNDO = new Color(9, 18, 31);
    private JogoViewport() {
    }

    public static double preparar(Graphics2D g, int larguraPainel, int alturaPainel) {
        double escala = Math.min(
                (double) larguraPainel / ConfiguracaoJogo.LARGURA,
                (double) alturaPainel / ConfiguracaoJogo.ALTURA);
        double larguraRenderizada = ConfiguracaoJogo.LARGURA * escala;
        double alturaRenderizada = ConfiguracaoJogo.ALTURA * escala;
        double margemX = (larguraPainel - larguraRenderizada) / 2.0;
        double margemY = (alturaPainel - alturaRenderizada) / 2.0;

        g.setColor(COR_FUNDO);
        g.fillRect(0, 0, larguraPainel, alturaPainel);
        g.translate(margemX, margemY);
        g.scale(escala, escala);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return escala;
    }

    public static double obterEscala(int larguraPainel, int alturaPainel) {
        return Math.min(
                (double) larguraPainel / ConfiguracaoJogo.LARGURA,
                (double) alturaPainel / ConfiguracaoJogo.ALTURA);
    }

    public static double obterMargemX(int larguraPainel, double escala) {
        return (larguraPainel - ConfiguracaoJogo.LARGURA * escala) / 2.0;
    }

    public static double obterMargemY(int alturaPainel, double escala) {
        return (alturaPainel - ConfiguracaoJogo.ALTURA * escala) / 2.0;
    }
}

