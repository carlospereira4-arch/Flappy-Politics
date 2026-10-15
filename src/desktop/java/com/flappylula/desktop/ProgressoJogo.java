package com.flappylula.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Locale;
import java.util.Properties;

/** Perfil local e progresso salvo separadamente para cada nome de jogador. */
public final class ProgressoJogo {
    public static final int TOTAL_PERSONAGENS = 4;
    private static final int[] PRECOS = {0, 10, 25, 50};
    private static final String[] NOMES = {"Lula Clássica", "Lula 2", "Lula 3", "Lula 4"};
    private static final String NOME_ARQUIVO_RECENTE = "ultimo-perfil.properties";
    private final Path arquivo;
    private final Properties dados = new Properties();
    private final String nome;
    private final int idade;
    private final Path arquivoFoto;

    private ProgressoJogo(String nome, int idade, Path arquivo, Path arquivoFoto) {
        this.nome = nome;
        this.idade = idade;
        this.arquivo = arquivo;
        this.arquivoFoto = arquivoFoto;
        try (InputStream entrada = Files.newInputStream(arquivo)) {
            dados.load(entrada);
        } catch (IOException ignored) {
            // Um arquivo novo começa com progresso zerado.
        }
        dados.putIfAbsent("unlocked.0", "true");
        dados.setProperty("nome", nome);
        dados.setProperty("idade", Integer.toString(idade));
        salvar();
    }

    public static ProgressoJogo entrar(String nome, int idade, Path fotoPerfil) {
        String nomeLimpo = nome == null ? "" : nome.trim().replaceAll("\\s+", " ");
        if (nomeLimpo.isEmpty() || nomeLimpo.length() > 20) {
            throw new IllegalArgumentException("O nome deve ter entre 1 e 20 caracteres.");
        }
        String chave = Base64.getUrlEncoder().withoutPadding().encodeToString(
                nomeLimpo.toLowerCase(Locale.ROOT).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Path arquivo = Path.of(System.getProperty("user.home"), ".flappy-lula", "perfis",
                chave + ".properties");
        Path arquivoFoto = arquivo.resolveSibling(chave + ".png");
        if (idade < 1 || idade > 120) {
            throw new IllegalArgumentException("A idade deve estar entre 1 e 120 anos.");
        }
        if (fotoPerfil == null || !Files.isRegularFile(fotoPerfil)) {
            throw new IllegalArgumentException("Escolha uma imagem para o perfil.");
        }
        try {
            BufferedImage imagem = ImageIO.read(fotoPerfil.toFile());
            if (imagem == null) {
                throw new IllegalArgumentException("O arquivo escolhido não é uma imagem válida.");
            }
            Files.createDirectories(arquivoFoto.getParent());
            BufferedImage reduzida = imagem;
            int maiorLado = Math.max(imagem.getWidth(), imagem.getHeight());
            if (maiorLado > 512) {
                double escala = 512.0 / maiorLado;
                reduzida = ImagemUtil.redimensionar(imagem,
                        Math.max(1, (int) Math.round(imagem.getWidth() * escala)),
                        Math.max(1, (int) Math.round(imagem.getHeight() * escala)));
            }
            ImageIO.write(reduzida, "png", arquivoFoto.toFile());
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível salvar a imagem do perfil.", e);
        }
        return new ProgressoJogo(nomeLimpo, idade, arquivo, arquivoFoto);
    }

    public static PerfilRecente carregarPerfilRecente() {
        Path diretorio = Path.of(System.getProperty("user.home"), ".flappy-lula", "perfis");
        Path arquivoRecente = diretorio.resolve(NOME_ARQUIVO_RECENTE);
        Properties recente = new Properties();
        try (InputStream entrada = Files.newInputStream(arquivoRecente)) {
            recente.load(entrada);
            String chave = recente.getProperty("perfil", "");
            if (!chave.matches("[A-Za-z0-9_-]+")) {
                return null;
            }
            Properties dadosPerfil = new Properties();
            try (InputStream perfil = Files.newInputStream(diretorio.resolve(chave + ".properties"))) {
                dadosPerfil.load(perfil);
            }
            int idade = Math.max(1, Math.min(120,
                    Integer.parseInt(dadosPerfil.getProperty("idade", "1"))));
            Path foto = diretorio.resolve(chave + ".png");
            return Files.isRegularFile(foto)
                    ? new PerfilRecente(dadosPerfil.getProperty("nome", ""), idade, foto) : null;
        } catch (IOException | NumberFormatException e) {
            return null;
        }
    }

    public String getNome() { return nome; }
    public int getIdade() { return idade; }
    public BufferedImage carregarFotoPerfil() {
        try {
            return Files.isRegularFile(arquivoFoto) ? ImageIO.read(arquivoFoto.toFile()) : null;
        } catch (IOException e) {
            return null;
        }
    }
    public synchronized int getMoedas() { return numero("moedas", 0); }
    public synchronized int getRecordeTempo() { return numero("recordeTempo", 0); }
    public synchronized int getRecordeMoedas() { return numero("recordeMoedas", 0); }
    public synchronized int getPartidas() { return numero("partidas", 0); }
    public synchronized int getPersonagemSelecionado() {
        int selecionado = numero("personagem", 0);
        return estaDesbloqueado(selecionado) ? selecionado : 0;
    }

    public synchronized void coletarMoeda() {
        dados.setProperty("moedas", Integer.toString(getMoedas() + 1));
        dados.setProperty("moedasTotais", Integer.toString(numero("moedasTotais", 0) + 1));
        salvar();
    }

    public synchronized boolean estaDesbloqueado(int indice) {
        validarIndice(indice);
        return Boolean.parseBoolean(dados.getProperty("unlocked." + indice,
                indice == 0 ? "true" : "false"));
    }

    public synchronized boolean selecionarOuComprar(int indice) {
        validarIndice(indice);
        if (estaDesbloqueado(indice)) {
            dados.setProperty("personagem", Integer.toString(indice));
            salvar();
            return true;
        }
        if (getMoedas() < PRECOS[indice]) {
            return false;
        }
        dados.setProperty("moedas", Integer.toString(getMoedas() - PRECOS[indice]));
        dados.setProperty("unlocked." + indice, "true");
        dados.setProperty("personagem", Integer.toString(indice));
        salvar();
        return true;
    }

    public synchronized boolean registrarPartida(int tempo, int moedasNaPartida) {
        boolean recorde = tempo > getRecordeTempo();
        dados.setProperty("partidas", Integer.toString(getPartidas() + 1));
        dados.setProperty("recordeMoedas", Integer.toString(
                Math.max(getRecordeMoedas(), moedasNaPartida)));
        if (recorde) {
            dados.setProperty("recordeTempo", Integer.toString(tempo));
        }
        salvar();
        return recorde;
    }

    public static int getPreco(int indice) {
        validarIndice(indice);
        return PRECOS[indice];
    }

    public static String getNomePersonagem(int indice) {
        validarIndice(indice);
        return NOMES[indice];
    }

    private int numero(String chave, int padrao) {
        try {
            return Math.max(0, Integer.parseInt(dados.getProperty(chave, Integer.toString(padrao))));
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    private void salvar() {
        try {
            Files.createDirectories(arquivo.getParent());
            Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
            try (OutputStream saida = Files.newOutputStream(temporario)) {
                dados.store(saida, "Progresso local do Flappy Lula");
            }
            try {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
            Properties recente = new Properties();
            recente.setProperty("perfil", arquivo.getFileName().toString().replace(".properties", ""));
            try (OutputStream saida = Files.newOutputStream(arquivo.getParent()
                    .resolve(NOME_ARQUIVO_RECENTE))) {
                recente.store(saida, "Último perfil local do Flappy Lula");
            }
        } catch (IOException e) {
            System.err.println("Não foi possível salvar o perfil: " + e.getMessage());
        }
    }

    private static void validarIndice(int indice) {
        if (indice < 0 || indice >= TOTAL_PERSONAGENS) {
            throw new IllegalArgumentException("Personagem inexistente: " + indice);
        }
    }

    public record PerfilRecente(String nome, int idade, Path foto) { }
}
