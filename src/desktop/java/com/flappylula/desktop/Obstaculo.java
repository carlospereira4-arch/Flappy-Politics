package com.flappylula.desktop;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Obstaculo {
    private static final BufferedImage IMAGEM = ImagemUtil.carregar("obstaculos_transparente.png");
    private static final double PROPORCAO_Y_INICIAL = 76.0 / 1264.0;
    private static final double PROPORCAO_Y_FINAL = 1110.0 / 1264.0;
    private static final double PROPORCAO_TOPO_APOS_INVERTER = 1.0 - PROPORCAO_Y_FINAL;
    private static final double PROPORCAO_LARGURA_ARTE = (642.0 - 199.0) / 843.0;
    private static final double PROPORCAO_X_ARTE = 199.0 / 843.0;
    private static final double PROPORCAO_ALTURA_ARTE = PROPORCAO_Y_FINAL - PROPORCAO_Y_INICIAL;

    private final int largura;
    private final int alturaDaPassagem;
    private final int inicioDaPassagem;
    private final double distanciaAteProximo;
    private final BufferedImage parteSuperior;
    private final BufferedImage parteInferior;
    private double x;

    public Obstaculo(double x, Random aleatorio) {
        this.x = x;
        largura = aleatorio.nextInt(41) + 135;
        alturaDaPassagem = aleatorio.nextInt(61) + 160;
        int margem = 70;
        int maximoInicio = ConfiguracaoJogo.ALTURA - alturaDaPassagem - margem;
        inicioDaPassagem = aleatorio.nextInt(maximoInicio - margem + 1) + margem;
        distanciaAteProximo = aleatorio.nextInt(121) + 360;
        parteSuperior = criarParteSuperior();
        parteInferior = criarParteInferior();
    }

    public void atualizar(double deltaSegundos) {
        x -= ConfiguracaoJogo.VELOCIDADE_CENARIO * deltaSegundos;
    }

    public double getX() {
        return x;
    }

    public double getDistanciaAteProximo() {
        return distanciaAteProximo;
    }

    public void desenhar(Graphics2D g) {
        int fimDaPassagem = inicioDaPassagem + alturaDaPassagem;
        if (parteSuperior != null) {
            g.drawImage(parteSuperior, (int) x, 0, null);
        }
        if (parteInferior != null) {
            g.drawImage(parteInferior, (int) x, fimDaPassagem, null);
        }
    }

    private BufferedImage criarParteSuperior() {
        if (IMAGEM == null) {
            return null;
        }
        int alturaImagem = Math.max(1, inicioDaPassagem);
        BufferedImage resultado = new BufferedImage(largura, alturaImagem, BufferedImage.TYPE_INT_ARGB);
        double alturaArte = inicioDaPassagem / PROPORCAO_ALTURA_ARTE;
        Graphics2D g = resultado.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        AffineTransform transformacao = new AffineTransform();
        transformacao.translate(0, -PROPORCAO_TOPO_APOS_INVERTER * alturaArte + alturaArte);
        transformacao.scale((double) largura / IMAGEM.getWidth(),
                -alturaArte / IMAGEM.getHeight());
        g.drawImage(IMAGEM, transformacao, null);
        g.dispose();
        return resultado;
    }

    private BufferedImage criarParteInferior() {
        if (IMAGEM == null) {
            return null;
        }
        int fimDaPassagem = inicioDaPassagem + alturaDaPassagem;
        int alturaImagem = Math.max(1, ConfiguracaoJogo.ALTURA - fimDaPassagem);
        BufferedImage resultado = new BufferedImage(largura, alturaImagem, BufferedImage.TYPE_INT_ARGB);
        double alturaArte = alturaImagem / PROPORCAO_ALTURA_ARTE;
        Graphics2D g = resultado.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        AffineTransform transformacao = new AffineTransform();
        transformacao.translate(0, -PROPORCAO_Y_INICIAL * alturaArte);
        transformacao.scale((double) largura / IMAGEM.getWidth(), alturaArte / IMAGEM.getHeight());
        g.drawImage(IMAGEM, transformacao, null);
        g.dispose();
        return resultado;
    }

    public boolean colideCom(Rectangle2D hitboxPersonagem) {
        double xArte = x + largura * PROPORCAO_X_ARTE;
        double larguraArte = largura * PROPORCAO_LARGURA_ARTE;
        int fimDaPassagem = inicioDaPassagem + alturaDaPassagem;

        Rectangle2D parteSuperior = new Rectangle2D.Double(
                xArte, 0, larguraArte, inicioDaPassagem);
        Rectangle2D parteInferior = new Rectangle2D.Double(
                xArte, fimDaPassagem, larguraArte, ConfiguracaoJogo.ALTURA - fimDaPassagem);
        return parteSuperior.intersects(hitboxPersonagem)
                || parteInferior.intersects(hitboxPersonagem);
    }
}

