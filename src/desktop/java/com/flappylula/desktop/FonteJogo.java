package com.flappylula.desktop;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;

public final class FonteJogo {
    private static final Font BITCOUNT_INK = carregar("fonts/BitcountInk.ttf");
    private static final Font PRESS_START_2P = carregar("fonts/PressStart2P-Regular.ttf");
    private static final Font PIXELIFY_SANS = carregar("fonts/PixelifySans.ttf");

    private FonteJogo() {
    }

    public static Font titulo(float tamanho) {
        return BITCOUNT_INK.deriveFont(Font.PLAIN, tamanho);
    }

    public static Font destaque(float tamanho) {
        return PRESS_START_2P.deriveFont(Font.PLAIN, tamanho);
    }

    public static Font texto(float tamanho) {
        return PIXELIFY_SANS.deriveFont(Font.PLAIN, tamanho);
    }

    public static Font ajustar(Font fonte, String texto, float larguraMaxima, float alturaMaxima) {
        FontRenderContext contexto = new FontRenderContext(null, true, true);
        Rectangle2D limites = fonte.getStringBounds(texto, contexto);
        double escala = 1.0;
        if (limites.getWidth() > larguraMaxima && limites.getWidth() > 0) {
            escala = Math.min(escala, larguraMaxima / limites.getWidth());
        }
        if (limites.getHeight() > alturaMaxima && limites.getHeight() > 0) {
            escala = Math.min(escala, alturaMaxima / limites.getHeight());
        }
        return escala < 1.0 ? fonte.deriveFont((float) (fonte.getSize2D() * escala)) : fonte;
    }

    private static Font carregar(String caminho) {
        try (InputStream recurso = FonteJogo.class.getClassLoader().getResourceAsStream(caminho)) {
            if (recurso != null) {
                return Font.createFont(Font.TRUETYPE_FONT, recurso);
            }
            return Font.createFont(Font.TRUETYPE_FONT, new java.io.File(caminho));
        } catch (FontFormatException | IOException e) {
            System.err.println("Não foi possível carregar a fonte " + caminho + ": " + e.getMessage());
            return new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
    }
}

