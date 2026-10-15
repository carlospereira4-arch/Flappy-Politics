import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class PainelDoJogo extends JPanel {
    private final Cenario cenario = new Cenario(ConfiguracaoJogo.LARGURA, ConfiguracaoJogo.ALTURA);
    private final Personagem personagem = new Personagem();
    private final GerenciadorObstaculos gerenciadorObstaculos = new GerenciadorObstaculos();
    private final Timer temporizador;
    private long instanteUltimoQuadro = System.nanoTime();
    private boolean jogoEncerrado;
    private boolean espacoPressionado;

    public PainelDoJogo() {
        setPreferredSize(new Dimension(ConfiguracaoJogo.LARGURA, ConfiguracaoJogo.ALTURA));
        configurarTeclas();

        temporizador = new Timer(ConfiguracaoJogo.INTERVALO_QUADRO_MS, e -> atualizarJogo());
        temporizador.setCoalesce(true);
        temporizador.setInitialDelay(0);
    }

    public void iniciarNovaPartida() {
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
                    reiniciarJogo();
                    temporizador.start();
                }
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
                jogoEncerrado = true;
                temporizador.stop();
                repaint();
                return;
            }
        }
        repaint();
    }

    private void reiniciarJogo() {
        cenario.reiniciar();
        gerenciadorObstaculos.reiniciar();
        personagem.reiniciar();
        espacoPressionado = false;
        instanteUltimoQuadro = System.nanoTime();
        jogoEncerrado = false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        JogoViewport.preparar(g2, getWidth(), getHeight());

        cenario.desenhar(g2);
        gerenciadorObstaculos.desenhar(g2);
        personagem.desenhar(g2);
        if (jogoEncerrado) {
            desenharMensagemFimDeJogo(g2);
        }
        g2.dispose();
    }

    private void desenharMensagemFimDeJogo(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 165));
        int xCartao = 35;
        int yCartao = 235;
        g.fillRoundRect(xCartao, yCartao, ConfiguracaoJogo.LARGURA - 70, 125, 18, 18);

        g.setColor(Color.WHITE);
        g.setFont(FonteJogo.destaque(23));
        FontMetrics titulo = g.getFontMetrics();
        String textoTitulo = "FIM DE JOGO";
        g.drawString(textoTitulo,
                (ConfiguracaoJogo.LARGURA - titulo.stringWidth(textoTitulo)) / 2, 285);

        g.setFont(FonteJogo.texto(13));
        FontMetrics instrucao = g.getFontMetrics();
        String textoInstrucao = "Pressione R para reiniciar";
        g.drawString(textoInstrucao,
                (ConfiguracaoJogo.LARGURA - instrucao.stringWidth(textoInstrucao)) / 2, 325);
    }
}
