package com.flappylula.desktop;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

public final class FonteJogo {
    private static final Font HONK = carregar("fonts/Honk.ttf");
    private static final Font OI = carregar("fonts/Oi-Regular.ttf");
    private static final Font SHORT_STACK = carregar("fonts/ShortStack-Regular.ttf");

    private FonteJogo() {
    }

    public static Font titulo(float tamanho) {
        return HONK.deriveFont(Font.PLAIN, tamanho);
    }

    public static Font destaque(float tamanho) {
        return OI.deriveFont(Font.PLAIN, tamanho);
    }

    public static Font texto(float tamanho) {
        return SHORT_STACK.deriveFont(Font.PLAIN, tamanho);
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

