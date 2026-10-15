package com.flappylula.desktop;


import java.awt.CardLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class FlappyBird {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("Flappy Lula");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            CardLayout layoutTelas = new CardLayout();
            JPanel telas = new JPanel(layoutTelas);
            PainelDoJogo jogo = new PainelDoJogo();
            MenuInicial menu = new MenuInicial(() -> {
                layoutTelas.show(telas, "jogo");
                jogo.iniciarNovaPartida();
                jogo.requestFocusInWindow();
            });

            telas.add(menu, "menu");
            telas.add(jogo, "jogo");
            janela.setContentPane(telas);
            janela.pack();
            janela.setLocationRelativeTo(null);
            janela.setExtendedState(JFrame.MAXIMIZED_BOTH);
            janela.setVisible(true);
        });
    }
}
