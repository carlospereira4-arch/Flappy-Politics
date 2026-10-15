package com.flappylula.desktop;


import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class FlappyBird {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DadosPerfil dadosPerfil = solicitarPerfil(ProgressoJogo.carregarPerfilRecente());
            if (dadosPerfil == null) {
                return;
            }
            ProgressoJogo progresso;
            try {
                progresso = ProgressoJogo.entrar(dadosPerfil.nome(), dadosPerfil.idade(),
                        dadosPerfil.foto());
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(),
                        "Perfil inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFrame janela = new JFrame("Flappy Lula");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    AudioJogo.pararMusica();
                }
            });

            CardLayout layoutTelas = new CardLayout();
            JPanel telas = new JPanel(layoutTelas);
            PainelDoJogo[] jogoRef = new PainelDoJogo[1];
            MenuInicial menu = new MenuInicial(progresso, () -> {
                layoutTelas.show(telas, "jogo");
                jogoRef[0].iniciarNovaPartida();
                jogoRef[0].requestFocusInWindow();
            });
            PainelDoJogo jogo = new PainelDoJogo(progresso, () -> {
                layoutTelas.show(telas, "menu");
                menu.repaint();
            });
            jogoRef[0] = jogo;

            telas.add(menu, "menu");
            telas.add(jogo, "jogo");
            janela.setContentPane(telas);
            janela.pack();
            janela.setLocationRelativeTo(null);
            janela.setExtendedState(JFrame.MAXIMIZED_BOTH);
            janela.setVisible(true);
            AudioJogo.tocar("menu_sound.mp3");
            AudioJogo.iniciarMusica();
        });
    }

    private static DadosPerfil solicitarPerfil(ProgressoJogo.PerfilRecente perfilRecente) {
        JTextField nome = new JTextField(20);
        JTextField idade = new JTextField(5);
        JLabel arquivoFoto = new JLabel("Nenhuma imagem selecionada");
        String nomeAnterior = perfilRecente == null ? "" : perfilRecente.nome();
        nome.setText(nomeAnterior);
        if (perfilRecente != null) {
            idade.setText(Integer.toString(perfilRecente.idade()));
        }
        Path[] foto = {perfilRecente == null ? null : perfilRecente.foto()};
        if (foto[0] != null) {
            arquivoFoto.setText(foto[0].getFileName().toString());
        }
        nome.getDocument().addDocumentListener(new DocumentListener() {
            private void atualizarFotoAoMudarNome() {
                if (perfilRecente == null) {
                    return;
                }
                if (nome.getText().trim().equalsIgnoreCase(nomeAnterior.trim())) {
                    foto[0] = perfilRecente.foto();
                    arquivoFoto.setText(foto[0].getFileName().toString());
                } else {
                    foto[0] = null;
                    arquivoFoto.setText("Escolha uma foto para este perfil");
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { atualizarFotoAoMudarNome(); }
            @Override public void removeUpdate(DocumentEvent e) { atualizarFotoAoMudarNome(); }
            @Override public void changedUpdate(DocumentEvent e) { atualizarFotoAoMudarNome(); }
        });
        JButton escolherFoto = new JButton("Escolher foto…");
        escolherFoto.addActionListener(evento -> {
            JFileChooser seletor = new JFileChooser(new File(System.getProperty("user.home")));
            seletor.setDialogTitle("Escolha uma foto para o perfil");
            seletor.setFileFilter(new FileNameExtensionFilter(
                    "Imagens (PNG, JPG)", "png", "jpg", "jpeg"));
            if (seletor.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                foto[0] = seletor.getSelectedFile().toPath();
                arquivoFoto.setText(seletor.getSelectedFile().getName());
            }
        });

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 8, 7, 8);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        formulario.add(new JLabel("Nome de usuário (até 20 caracteres)"), c);
        c.gridx = 1;
        formulario.add(nome, c);
        c.gridx = 0;
        c.gridy++;
        formulario.add(new JLabel("Idade (1 a 120)"), c);
        c.gridx = 1;
        formulario.add(idade, c);
        c.gridx = 0;
        c.gridy++;
        formulario.add(escolherFoto, c);
        c.gridx = 1;
        formulario.add(arquivoFoto, c);

        while (true) {
            int resultado = JOptionPane.showConfirmDialog(null, formulario,
                    perfilRecente == null ? "Criar perfil local do Flappy Lula"
                            : "Entrar no perfil local do Flappy Lula",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (resultado != JOptionPane.OK_OPTION) {
                return null;
            }
            String nomeInformado = nome.getText().trim();
            int idadeInformada;
            try {
                idadeInformada = Integer.parseInt(idade.getText().trim());
            } catch (NumberFormatException e) {
                idade.requestFocusInWindow();
                JOptionPane.showMessageDialog(null, "Informe sua idade em números.",
                        "Perfil incompleto", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            if (nomeInformado.isEmpty() || nomeInformado.length() > 20) {
                JOptionPane.showMessageDialog(null, "Informe um nome de 1 a 20 caracteres.",
                        "Perfil incompleto", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            if (idadeInformada < 1 || idadeInformada > 120) {
                JOptionPane.showMessageDialog(null, "Use uma idade entre 1 e 120.",
                        "Perfil incompleto", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            if (foto[0] == null) {
                JOptionPane.showMessageDialog(null, "Escolha uma foto para continuar.",
                        "Perfil incompleto", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            return new DadosPerfil(nomeInformado, idadeInformada, foto[0]);
        }
    }

    private record DadosPerfil(String nome, int idade, Path foto) { }
}
