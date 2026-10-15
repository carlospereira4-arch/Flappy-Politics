package com.flappylula.desktop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/** Tela da partida: física, colisões, coleta, placares e fim de jogo. */
public class PainelDoJogo extends JPanel {
    private static final BufferedImage IMAGEM_MOEDA = ImagemUtil.carregar("moeda_transparente.png");
    private final Cenario cenario = new Cenario(ConfiguracaoJogo.LARGURA, ConfiguracaoJogo.ALTURA);
    private final ProgressoJogo progresso;
    private final GerenciadorMoedas gerenciadorMoedas = new GerenciadorMoedas();
    private final GerenciadorObstaculos gerenciadorObstaculos =
            new GerenciadorObstaculos(gerenciadorMoedas::adicionarPara);
    private final Personagem personagem;
    private final Runnable aoVoltarMenu;
    private final Timer temporizador;
    private long instanteUltimoQuadro = System.nanoTime();
    private double fracaoSegundo;
    private int pontuacaoTempo;
    private int pontuacaoMoedas;
    private boolean novoRecorde;
    private boolean jogoEncerrado;
    private boolean espacoPressionado;

    public PainelDoJogo(ProgressoJogo progresso, Runnable aoVoltarMenu) {
        this.progresso = progresso;
        this.aoVoltarMenu = aoVoltarMenu;
        personagem = new Personagem(progresso.getPersonagemSelecionado());
        setPreferredSize(new Dimension(ConfiguracaoJogo.LARGURA, ConfiguracaoJogo.ALTURA));
        configurarTeclas();

        temporizador = new Timer(ConfiguracaoJogo.INTERVALO_QUADRO_MS, e -> atualizarJogo());
        temporizador.setCoalesce(true);
        temporizador.setInitialDelay(0);
    }

    public void iniciarNovaPartida() {
        personagem.selecionar(progresso.getPersonagemSelecionado());
        reiniciarJogo();
        temporizador.start();
    }

    private void configurarTeclas() {
        InputMap entrada = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap acoes = getActionMap();
        entrada.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), "espaco_pressionado");
        entrada.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), "espaco_solto");
        acoes.put("espaco_pressionado", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!espacoPressionado && !jogoEncerrado) {
                    personagem.pular();
                }
                espacoPressionado = true;
            }
        });
        acoes.put("espaco_solto", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                espacoPressionado = false;
            }
        });
        entrada.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0, false), "reiniciar_jogo");
        acoes.put("reiniciar_jogo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jogoEncerrado) {
                    iniciarNovaPartida();
                }
            }
        });
        entrada.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "voltar_menu");
        acoes.put("voltar_menu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                temporizador.stop();
                aoVoltarMenu.run();
            }
        });
    }

    private void atualizarJogo() {
        long agora = System.nanoTime();
        double deltaSegundos = Math.min((agora - instanteUltimoQuadro) / 1_000_000_000.0, 0.05);
        instanteUltimoQuadro = agora;

        if (!jogoEncerrado) {
            cenario.atualizar(deltaSegundos);
            gerenciadorObstaculos.atualizar(deltaSegundos);
            boolean bateuNaBorda = personagem.atualizar(deltaSegundos, ConfiguracaoJogo.ALTURA);
            if (bateuNaBorda || gerenciadorObstaculos.colidiuCom(personagem.getHitbox())) {
                encerrarPartida();
                repaint();
                return;
            }

            int moedasEncontradas = gerenciadorMoedas.atualizar(
                    deltaSegundos, personagem.getHitbox());
            for (int i = 0; i < moedasEncontradas; i++) {
                pontuacaoMoedas++;
                progresso.coletarMoeda();
                AudioJogo.tocar("money_sound.mp3");
            }
            fracaoSegundo += deltaSegundos;
            while (fracaoSegundo >= 1) {
                pontuacaoTempo++;
                fracaoSegundo -= 1;
            }
        }
        repaint();
    }

    private void encerrarPartida() {
        jogoEncerrado = true;
        temporizador.stop();
        novoRecorde = progresso.registrarPartida(pontuacaoTempo, pontuacaoMoedas);
        AudioJogo.tocar("death_sound.mp3");
    }

    private void reiniciarJogo() {
        cenario.reiniciar();
        gerenciadorMoedas.reiniciar();
        gerenciadorObstaculos.reiniciar();
        personagem.reiniciar();
        espacoPressionado = false;
        instanteUltimoQuadro = System.nanoTime();
        fracaoSegundo = 0;
        pontuacaoTempo = 0;
        pontuacaoMoedas = 0;
        novoRecorde = false;
        jogoEncerrado = false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        JogoViewport.preparar(g2, getWidth(), getHeight());
        cenario.desenhar(g2);
        gerenciadorObstaculos.desenhar(g2);
        gerenciadorMoedas.desenhar(g2);
        personagem.desenhar(g2);
        desenharPlacar(g2);
        if (jogoEncerrado) {
            desenharMensagemFimDeJogo(g2);
        }
        g2.dispose();
    }

    private void desenharPlacar(Graphics2D g) {
        g.setColor(new Color(13, 32, 48, 205));
        g.fillRoundRect(27, 18, ConfiguracaoJogo.LARGURA - 54, 50, 15, 15);
        desenharCentralizado(g, "TEMPO: " + pontuacaoTempo + "s", 50,
                FonteJogo.destaque(17), Color.WHITE, 220);
        if (IMAGEM_MOEDA != null) {
            g.drawImage(IMAGEM_MOEDA, 292, 29, 38, 22, null);
        } else {
            g.setColor(new Color(255, 226, 92));
            g.fillOval(300, 31, 19, 19);
        }
        desenharTexto(g, Integer.toString(pontuacaoMoedas), 352, 50,
                FonteJogo.texto(19), Color.WHITE, 54);
    }

    private void desenharMensagemFimDeJogo(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, ConfiguracaoJogo.LARGURA, ConfiguracaoJogo.ALTURA);
        int xCartao = 24;
        int yCartao = 198;
        int largura = ConfiguracaoJogo.LARGURA - 48;
        g.setColor(new Color(13, 32, 48, 238));
        g.fillRoundRect(xCartao, yCartao, largura, 225, 20, 20);
        desenharCentralizado(g, "FIM DE JOGO", 244, FonteJogo.destaque(24), Color.WHITE, largura - 30);
        desenharCentralizado(g, "TEMPO " + pontuacaoTempo + "s  |  MOEDAS " + pontuacaoMoedas,
                279, FonteJogo.texto(14), new Color(255, 226, 92), largura - 28);
        desenharCentralizado(g, "RECORDE: " + progresso.getRecordeTempo() + "s  |  SALDO: "
                        + progresso.getMoedas(), 308, FonteJogo.texto(12), Color.WHITE, largura - 28);
        if (novoRecorde) {
            desenharCentralizado(g, "NOVO RECORDE!", 337, FonteJogo.destaque(15),
                    new Color(255, 218, 75), largura - 28);
        }
        desenharCentralizado(g, "R  JOGAR NOVAMENTE", 375, FonteJogo.destaque(12),
                new Color(255, 218, 75), largura - 28);
        desenharCentralizado(g, "ESC  VOLTAR AO MENU", 403, FonteJogo.texto(13),
                Color.WHITE, largura - 28);
    }

    private void desenharCentralizado(Graphics2D g, String texto, int y,
                                      java.awt.Font fonte, Color cor, int largura) {
        g.setFont(FonteJogo.ajustar(fonte, texto, largura, 26));
        g.setColor(cor);
        FontMetrics metricas = g.getFontMetrics();
        g.drawString(texto, (ConfiguracaoJogo.LARGURA - metricas.stringWidth(texto)) / 2, y);
    }

    private void desenharTexto(Graphics2D g, String texto, int x, int y,
                                java.awt.Font fonte, Color cor, int largura) {
        g.setFont(FonteJogo.ajustar(fonte, texto, largura, 26));
        g.setColor(cor);
        g.drawString(texto, x - g.getFontMetrics().stringWidth(texto) / 2, y);
    }
}
