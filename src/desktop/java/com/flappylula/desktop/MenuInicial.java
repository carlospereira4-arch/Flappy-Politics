package com.flappylula.desktop;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JPanel;

public class MenuInicial extends JPanel {
    private final BufferedImage fundo = ImagemUtil.redimensionarParaPreencher(
            ImagemUtil.carregar("backgroundFullScreen.jpg"),
            ConfiguracaoJogo.LARGURA, ConfiguracaoJogo.ALTURA);
    private final BufferedImage personagem = ImagemUtil.redimensionar(
            ImagemUtil.carregar("personagem_transparente.png"), 120, 178);
    private final JButton botaoJogar;

    public MenuInicial(Runnable aoIniciar) {
        setPreferredSize(new java.awt.Dimension(ConfiguracaoJogo.LARGURA, ConfiguracaoJogo.ALTURA));
        setLayout(null);

        botaoJogar = new JButton("JOGAR");
        botaoJogar.setFont(FonteJogo.destaque(20));
        botaoJogar.setForeground(new Color(48, 48, 48));
        botaoJogar.setBackground(new Color(255, 218, 75));
        botaoJogar.setFocusPainted(false);
        botaoJogar.setBorderPainted(false);
        botaoJogar.setOpaque(true);
        botaoJogar.addActionListener(e -> aoIniciar.run());
        add(botaoJogar);
    }

    @Override
    public void doLayout() {
        double escala = JogoViewport.obterEscala(getWidth(), getHeight());
        double margemX = JogoViewport.obterMargemX(getWidth(), escala);
        double margemY = JogoViewport.obterMargemY(getHeight(), escala);
        botaoJogar.setBounds(
                (int) Math.round(margemX + 115 * escala),
                (int) Math.round(margemY + 375 * escala),
                (int) Math.round(170 * escala),
                (int) Math.round(56 * escala));
        botaoJogar.setFont(FonteJogo.destaque((float) (20 * escala)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        JogoViewport.preparar(g2, getWidth(), getHeight());
        if (fundo != null) {
            g2.drawImage(fundo, 0, 0, ConfiguracaoJogo.LARGURA,
                    ConfiguracaoJogo.ALTURA, null);
        } else {
            g2.setColor(new Color(112, 197, 206));
            g2.fillRect(0, 0, ConfiguracaoJogo.LARGURA, ConfiguracaoJogo.ALTURA);
        }

        g2.setColor(new Color(10, 28, 45, 75));
        g2.fillRect(0, 0, ConfiguracaoJogo.LARGURA, ConfiguracaoJogo.ALTURA);

        g2.setColor(new Color(0, 0, 0, 95));
        g2.fillRoundRect(28, 42, ConfiguracaoJogo.LARGURA - 56, 90, 20, 20);
        g2.setFont(FonteJogo.titulo(46));
        g2.setColor(Color.WHITE);
        String titulo = "FLAPPY LULA";
        FontMetrics metricasTitulo = g2.getFontMetrics();
        int xTitulo = (ConfiguracaoJogo.LARGURA - metricasTitulo.stringWidth(titulo)) / 2;
        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawString(titulo, xTitulo + 2, 101 + 2);
        g2.setPaint(new GradientPaint(xTitulo, 78, new Color(255, 111, 48),
                xTitulo + metricasTitulo.stringWidth(titulo), 106, new Color(255, 224, 77)));
        g2.drawString(titulo, xTitulo, 101);

        if (personagem != null) {
            g2.drawImage(personagem, (ConfiguracaoJogo.LARGURA - 100) / 2,
                    160, 100, 148, null);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(FonteJogo.destaque(17));
        desenharCentralizado(g2, "DESVIE DOS OBSTACULOS", 335);
        g2.setFont(FonteJogo.texto(14));
        desenharCentralizado(g2, "Pressione ESPACO para dar um impulso", 465);
        desenharCentralizado(g2, "Pressione R para reiniciar apos colisao", 493);
        g2.dispose();
    }

    private void desenharCentralizado(Graphics2D g, String texto, int y) {
        FontMetrics metricas = g.getFontMetrics();
        g.drawString(texto,
                (ConfiguracaoJogo.LARGURA - metricas.stringWidth(texto)) / 2, y);
    }
}

