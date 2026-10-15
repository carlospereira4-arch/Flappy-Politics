package com.flappylula.desktop;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Frame;
import java.awt.Window;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Menu inicial com saldo, perfil, loja e seleção de personagem. */
public class MenuInicial extends JPanel {
    private final BufferedImage fundo = ImagemUtil.redimensionarParaPreencher(
            ImagemUtil.carregar("backgroundFullScreen.jpg"),
            ConfiguracaoJogo.LARGURA, ConfiguracaoJogo.ALTURA);
    private final ProgressoJogo progresso;
    private final Runnable aoIniciar;
    private final BufferedImage[] personagens = new BufferedImage[ProgressoJogo.TOTAL_PERSONAGENS];
    private final BufferedImage fotoPerfil;
    private final JButton botaoJogar = new JButton("JOGAR");
    private final JButton botaoLoja = new JButton("LOJA DE PERSONAGENS");
    private final JButton botaoPersonagem = new JButton("TROCAR PERSONAGEM");

    public MenuInicial(ProgressoJogo progresso, Runnable aoIniciar) {
        this.progresso = progresso;
        this.aoIniciar = aoIniciar;
        fotoPerfil = progresso.carregarFotoPerfil();
        setPreferredSize(new java.awt.Dimension(ConfiguracaoJogo.LARGURA, ConfiguracaoJogo.ALTURA));
        setLayout(null);
        for (int i = 0; i < personagens.length; i++) {
            String nome = i == 0 ? "personagem_transparente.png"
                    : "personagem" + (i + 1) + "_transparente.png";
            personagens[i] = ImagemUtil.redimensionar(ImagemUtil.carregar(nome), 106, 157);
        }
        configurarBotao(botaoJogar, new Color(255, 218, 75), new Color(48, 48, 48));
        configurarBotao(botaoLoja, new Color(29, 103, 130), Color.WHITE);
        configurarBotao(botaoPersonagem, new Color(34, 77, 94), Color.WHITE);
        botaoJogar.addActionListener(e -> this.aoIniciar.run());
        botaoLoja.addActionListener(e -> abrirLoja());
        botaoPersonagem.addActionListener(e -> abrirLoja());
        add(botaoJogar);
        add(botaoLoja);
        add(botaoPersonagem);
    }

    private void configurarBotao(JButton botao, Color fundo, Color texto) {
        botao.setForeground(texto);
        botao.setBackground(fundo);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setOpaque(true);
    }

    @Override
    public void doLayout() {
        double escala = JogoViewport.obterEscala(getWidth(), getHeight());
        double margemX = JogoViewport.obterMargemX(getWidth(), escala);
        double margemY = JogoViewport.obterMargemY(getHeight(), escala);
        int x = (int) Math.round(margemX + 65 * escala);
        int largura = (int) Math.round(270 * escala);
        int altura = (int) Math.round(51 * escala);
        botaoJogar.setBounds(x, (int) Math.round(margemY + 413 * escala), largura, altura);
        botaoLoja.setBounds(x, (int) Math.round(margemY + 474 * escala), largura, altura);
        botaoPersonagem.setBounds(x, (int) Math.round(margemY + 535 * escala), largura, altura);
        ajustarBotao(botaoJogar, escala);
        ajustarBotao(botaoLoja, escala);
        ajustarBotao(botaoPersonagem, escala);
    }

    private void ajustarBotao(JButton botao, double escala) {
        botao.setFont(FonteJogo.ajustar(FonteJogo.destaque((float) (17 * escala)),
                botao.getText(), (float) (250 * escala), (float) (32 * escala)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        JogoViewport.preparar(g2, getWidth(), getHeight());
        if (fundo != null) {
            g2.drawImage(fundo, 0, 0, ConfiguracaoJogo.LARGURA, ConfiguracaoJogo.ALTURA, null);
        } else {
            g2.setColor(new Color(50, 97, 121));
            g2.fillRect(0, 0, ConfiguracaoJogo.LARGURA, ConfiguracaoJogo.ALTURA);
        }
        g2.setColor(new Color(10, 28, 45, 78));
        g2.fillRect(0, 0, ConfiguracaoJogo.LARGURA, ConfiguracaoJogo.ALTURA);

        g2.setColor(new Color(0, 0, 0, 125));
        g2.fillRoundRect(20, 22, ConfiguracaoJogo.LARGURA - 40, 83, 20, 20);
        String titulo = "FLAPPY LULA";
        g2.setFont(FonteJogo.ajustar(FonteJogo.titulo(43), titulo,
                ConfiguracaoJogo.LARGURA - 60, 55));
        FontMetrics mt = g2.getFontMetrics();
        int xTitulo = (ConfiguracaoJogo.LARGURA - mt.stringWidth(titulo)) / 2;
        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawString(titulo, xTitulo + 2, 79 + 2);
        g2.setPaint(new GradientPaint(xTitulo, 51, new Color(255, 119, 43),
                xTitulo + mt.stringWidth(titulo), 82, new Color(255, 226, 92)));
        g2.drawString(titulo, xTitulo, 79);

        desenharCartaoPerfil(g2);
        BufferedImage personagem = personagens[progresso.getPersonagemSelecionado()];
        if (personagem != null) {
            g2.drawImage(personagem, (ConfiguracaoJogo.LARGURA - 98) / 2, 207, 98, 145, null);
        }
        desenharCentralizado(g2, "DESVIE DOS OBSTÁCULOS E PEGUE AS MOEDAS", 383,
                FonteJogo.texto(13), Color.WHITE, ConfiguracaoJogo.LARGURA - 30);
        g2.dispose();
    }

    private void desenharCartaoPerfil(Graphics2D g) {
        g.setColor(new Color(13, 32, 48, 210));
        g.fillRoundRect(28, 120, ConfiguracaoJogo.LARGURA - 56, 70, 17, 17);
        if (fotoPerfil != null) {
            Graphics2D avatar = (Graphics2D) g.create();
            avatar.setClip(new java.awt.geom.Ellipse2D.Double(40, 126, 58, 58));
            avatar.drawImage(fotoPerfil, 40, 126, 58, 58, null);
            avatar.dispose();
        }
        g.setColor(new Color(255, 218, 75));
        g.drawOval(40, 126, 58, 58);
        g.setColor(Color.WHITE);
        String saudacao = "OLÁ, " + progresso.getNome().toUpperCase();
        g.setFont(FonteJogo.ajustar(FonteJogo.destaque(13), saudacao, 265, 22));
        g.drawString(saudacao, 111, 148);
        String estatisticas = "IDADE " + progresso.getIdade() + "  •  RECORDE "
                + progresso.getRecordeTempo() + "s";
        g.setFont(FonteJogo.ajustar(FonteJogo.texto(11), estatisticas, 265, 18));
        g.setColor(new Color(207, 231, 237));
        g.drawString(estatisticas, 111, 165);
        String saldo = "MOEDAS " + progresso.getMoedas() + "  •  "
                + progresso.getPartidas() + " PARTIDAS";
        g.setFont(FonteJogo.ajustar(FonteJogo.texto(11), saldo, 265, 18));
        g.setColor(new Color(255, 226, 92));
        g.drawString(saldo, 111, 182);
    }

    private void desenharCentralizado(Graphics2D g, String texto, int y,
                                      java.awt.Font fonte, Color cor, int larguraMaxima) {
        g.setFont(FonteJogo.ajustar(fonte, texto, larguraMaxima, 26));
        g.setColor(cor);
        g.drawString(texto, (ConfiguracaoJogo.LARGURA - g.getFontMetrics().stringWidth(texto)) / 2, y);
    }

    private void abrirLoja() {
        Window janela = SwingUtilities.getWindowAncestor(this);
        Frame dono = janela instanceof Frame frame ? frame : null;
        new LojaPersonagens(dono, progresso, this::repaint).setVisible(true);
    }
}
