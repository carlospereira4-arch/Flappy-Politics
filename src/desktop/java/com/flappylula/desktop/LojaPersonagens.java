package com.flappylula.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Loja visual de personagens, com compra e equipamento no mesmo perfil local. */
public final class LojaPersonagens extends JDialog {
    private static final Color FUNDO = new Color(13, 32, 48);
    private static final Color CARTAO = new Color(25, 56, 74);
    private static final Color AMARELO = new Color(255, 218, 75);

    private final ProgressoJogo progresso;
    private final Runnable aoAtualizar;
    private final JLabel saldo = new JLabel();
    private final JLabel mensagem = new JLabel(" ", SwingConstants.CENTER);
    private final JButton[] botoes = new JButton[ProgressoJogo.TOTAL_PERSONAGENS];

    public LojaPersonagens(java.awt.Frame dono, ProgressoJogo progresso, Runnable aoAtualizar) {
        super(dono, "Loja de personagens", true);
        this.progresso = progresso;
        this.aoAtualizar = aoAtualizar;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(FUNDO);
        setLayout(new BorderLayout(12, 12));

        JLabel titulo = new JLabel("LOJA DE PERSONAGENS", SwingConstants.CENTER);
        titulo.setForeground(AMARELO);
        titulo.setFont(FonteJogo.ajustar(FonteJogo.destaque(23), titulo.getText(), 570, 42));
        saldo.setHorizontalAlignment(SwingConstants.CENTER);
        saldo.setForeground(Color.WHITE);
        saldo.setFont(FonteJogo.texto(15));
        JPanel cabecalho = new JPanel(new BorderLayout(4, 5));
        cabecalho.setOpaque(false);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(18, 12, 2, 12));
        cabecalho.add(titulo, BorderLayout.NORTH);
        cabecalho.add(saldo, BorderLayout.SOUTH);
        add(cabecalho, BorderLayout.NORTH);

        JPanel grade = new JPanel(new GridLayout(2, 2, 12, 12));
        grade.setOpaque(false);
        grade.setBorder(BorderFactory.createEmptyBorder(2, 18, 2, 18));
        for (int i = 0; i < botoes.length; i++) {
            final int indice = i;
            JPanel cartao = criarCartao(i);
            botoes[i] = new JButton();
            botoes[i].setFocusPainted(false);
            botoes[i].setBorderPainted(false);
            botoes[i].setOpaque(true);
            botoes[i].addActionListener(evento -> acionarPersonagem(indice));
            JPanel areaAcao = new JPanel(new BorderLayout());
            areaAcao.setOpaque(false);
            areaAcao.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 14));
            areaAcao.add(botoes[i], BorderLayout.CENTER);
            cartao.add(areaAcao, BorderLayout.SOUTH);
            grade.add(cartao);
        }
        add(grade, BorderLayout.CENTER);

        mensagem.setForeground(AMARELO);
        mensagem.setFont(FonteJogo.texto(12));
        JButton fechar = new JButton("VOLTAR AO MENU");
        fechar.setFont(FonteJogo.destaque(12));
        fechar.addActionListener(evento -> dispose());
        JPanel rodape = new JPanel(new BorderLayout(8, 8));
        rodape.setOpaque(false);
        rodape.setBorder(BorderFactory.createEmptyBorder(0, 18, 16, 18));
        rodape.add(mensagem, BorderLayout.NORTH);
        rodape.add(fechar, BorderLayout.SOUTH);
        add(rodape, BorderLayout.SOUTH);

        atualizarCartoes();
        setMinimumSize(new Dimension(620, 570));
        setSize(700, 650);
        setLocationRelativeTo(dono);
    }

    private JPanel criarCartao(int indice) {
        JPanel cartao = new JPanel(new BorderLayout(5, 5));
        cartao.setBackground(CARTAO);
        cartao.setBorder(BorderFactory.createLineBorder(new Color(72, 113, 128), 1, true));
        String arquivo = indice == 0 ? "personagem_transparente.png"
                : "personagem" + (indice + 1) + "_transparente.png";
        BufferedImage imagem = ImagemUtil.redimensionar(
                ImagemUtil.carregar(arquivo), 84, 112);
        JLabel retrato = new JLabel();
        if (imagem != null) {
            retrato.setIcon(new ImageIcon(imagem));
        }
        retrato.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel nome = new JLabel(ProgressoJogo.getNomePersonagem(indice), SwingConstants.CENTER);
        nome.setForeground(Color.WHITE);
        nome.setFont(FonteJogo.ajustar(FonteJogo.destaque(15), nome.getText(), 260, 28));
        JLabel preco = new JLabel(indice == 0 ? "GRÁTIS"
                : "CUSTO: " + ProgressoJogo.getPreco(indice) + " MOEDAS", SwingConstants.CENTER);
        preco.setForeground(new Color(202, 225, 231));
        preco.setFont(FonteJogo.texto(12));
        JPanel descricoes = new JPanel(new GridLayout(2, 1, 0, 3));
        descricoes.setOpaque(false);
        descricoes.add(nome);
        descricoes.add(preco);
        cartao.add(retrato, BorderLayout.CENTER);
        cartao.add(descricoes, BorderLayout.NORTH);
        return cartao;
    }

    private void acionarPersonagem(int indice) {
        boolean jaDesbloqueado = progresso.estaDesbloqueado(indice);
        if (progresso.selecionarOuComprar(indice)) {
            mensagem.setText(jaDesbloqueado ? "Personagem equipado." : "Personagem comprado e equipado!");
            if (!jaDesbloqueado) {
                AudioJogo.tocar("money_sound.mp3");
            }
            aoAtualizar.run();
        } else {
            int faltam = ProgressoJogo.getPreco(indice) - progresso.getMoedas();
            mensagem.setText("Faltam " + faltam + " moedas para essa compra.");
        }
        atualizarCartoes();
    }

    private void atualizarCartoes() {
        saldo.setText("SALDO: " + progresso.getMoedas() + " MOEDAS");
        for (int i = 0; i < botoes.length; i++) {
            boolean desbloqueado = progresso.estaDesbloqueado(i);
            boolean selecionado = progresso.getPersonagemSelecionado() == i;
            botoes[i].setText(desbloqueado ? (selecionado ? "EQUIPADO" : "USAR")
                    : "COMPRAR · " + ProgressoJogo.getPreco(i) + " MOEDAS");
            botoes[i].setEnabled(!selecionado);
            botoes[i].setBackground(desbloqueado && selecionado
                    ? new Color(118, 126, 87) : AMARELO);
            botoes[i].setForeground(new Color(34, 44, 49));
            botoes[i].setFont(FonteJogo.ajustar(FonteJogo.destaque(12), botoes[i].getText(),
                    260, 32));
        }
    }
}
