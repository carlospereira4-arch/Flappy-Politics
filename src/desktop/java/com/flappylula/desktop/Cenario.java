package com.flappylula.desktop;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Cenario {
    private final BufferedImage imagem;
    private final BufferedImage imagemEspelhada;
    private final int largura;
    private final int altura;
    private double distanciaPercorrida;

    public Cenario(int largura, int altura) {
        this.largura = largura;
        this.altura = altura;
        imagem = ImagemUtil.redimensionarParaPreencher(
                ImagemUtil.carregar("background_jogo.jpg"), largura, altura);
        imagemEspelhada = ImagemUtil.espelharHorizontalmente(imagem);
    }

    public void atualizar(double deltaSegundos) {
        distanciaPercorrida += ConfiguracaoJogo.VELOCIDADE_CENARIO * deltaSegundos;
    }

    public double getDistanciaPercorrida() {
        return distanciaPercorrida;
    }

    public void reiniciar() {
        distanciaPercorrida = 0;
    }

    public void desenhar(Graphics2D g) {
        if (imagem == null || imagemEspelhada == null) {
            g.setColor(new java.awt.Color(112, 197, 206));
            g.fillRect(0, 0, largura, altura);
            return;
        }

        int deslocamento = (int) (distanciaPercorrida % (largura * 2.0));
        for (int i = -1; i < 3; i++) {
            int x = i * largura - deslocamento;
            g.drawImage((i & 1) == 0 ? imagem : imagemEspelhada, x, 0, null);
        }
    }
}

