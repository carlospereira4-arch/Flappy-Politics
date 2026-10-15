package com.flappylula.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Guarda o perfil e o progresso local do jogador neste aparelho. */
public final class ProgressoJogo {
    public static final int TOTAL_PERSONAGENS = 4;
    private static final String PREFERENCIAS = "flappy_lula_local";
    private static final int[] PRECOS = {0, 10, 25, 50};
    private static final String[] NOMES_PERSONAGENS = {
            "Lula Clássica", "Lula 2", "Lula 3", "Lula 4"
    };

    private final SharedPreferences dados;
    private String idPerfil;

    public ProgressoJogo(Context contexto) {
        dados = contexto.getApplicationContext().getSharedPreferences(
                PREFERENCIAS, Context.MODE_PRIVATE);
        idPerfil = dados.getString("perfil_ativo", "");
        if (!dados.getBoolean(chave("personagem_0"), true)) {
            dados.edit().putBoolean(chave("personagem_0"), true).apply();
        }
    }

    public synchronized boolean temPerfil() {
        return !getNomeUsuario().isEmpty() && getIdade() > 0;
    }

    public synchronized void salvarPerfil(String nome, int idade) {
        String nomeLimpo = nome.trim();
        idPerfil = Base64.encodeToString(nomeLimpo.toLowerCase(Locale.ROOT)
                        .getBytes(StandardCharsets.UTF_8),
                Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        dados.edit().putString("perfil_ativo", idPerfil)
                .putString(chave("nome_usuario"), nomeLimpo)
                .putInt(chave("idade"), idade)
                .apply();
    }

    public synchronized String getIdPerfil() {
        return idPerfil;
    }

    public synchronized String getNomeUsuario() {
        return dados.getString(chave("nome_usuario"), "");
    }

    public synchronized int getIdade() {
        return dados.getInt(chave("idade"), 0);
    }

    public synchronized int getMoedas() {
        return dados.getInt(chave("saldo_moedas"), 0);
    }

    public synchronized int getTotalMoedasColetadas() {
        return dados.getInt(chave("total_moedas_coletadas"), 0);
    }

    public synchronized int getRecordeTempo() {
        return dados.getInt(chave("recorde_tempo"), 0);
    }

    public synchronized int getRecordeMoedasPartida() {
        return dados.getInt(chave("recorde_moedas_partida"), 0);
    }

    public synchronized int getPartidasJogadas() {
        return dados.getInt(chave("partidas_jogadas"), 0);
    }

    public synchronized void coletarMoeda() {
        dados.edit()
                .putInt(chave("saldo_moedas"), getMoedas() + 1)
                .putInt(chave("total_moedas_coletadas"), getTotalMoedasColetadas() + 1)
                .apply();
    }

    /** @return true quando a partida estabelece um novo recorde de tempo. */
    public synchronized boolean registrarPartida(int tempo, int moedasPartida) {
        boolean novoRecorde = tempo > getRecordeTempo();
        SharedPreferences.Editor editor = dados.edit()
                .putInt(chave("partidas_jogadas"), getPartidasJogadas() + 1)
                .putInt(chave("recorde_moedas_partida"),
                        Math.max(getRecordeMoedasPartida(), moedasPartida));
        if (novoRecorde) {
            editor.putInt(chave("recorde_tempo"), tempo);
        }
        editor.apply();
        return novoRecorde;
    }

    public synchronized boolean personagemDesbloqueado(int indice) {
        validarIndice(indice);
        return dados.getBoolean(chave(chavePersonagem(indice)), indice == 0);
    }

    public synchronized int getPersonagemSelecionado() {
        int indice = dados.getInt(chave("personagem_selecionado"), 0);
        return personagemDesbloqueado(indice) ? indice : 0;
    }

    public synchronized boolean selecionarPersonagem(int indice) {
        validarIndice(indice);
        if (!personagemDesbloqueado(indice)) {
            return false;
        }
        dados.edit().putInt(chave("personagem_selecionado"), indice).apply();
        return true;
    }

    /** Compra e desbloqueia uma aparência, gastando o saldo persistido. */
    public synchronized boolean comprarPersonagem(int indice) {
        validarIndice(indice);
        if (personagemDesbloqueado(indice)) {
            return false;
        }
        int preco = getPrecoPersonagem(indice);
        int saldo = getMoedas();
        if (saldo < preco) {
            return false;
        }
        dados.edit()
                .putInt(chave("saldo_moedas"), saldo - preco)
                .putBoolean(chave(chavePersonagem(indice)), true)
                .putInt(chave("personagem_selecionado"), indice)
                .apply();
        return true;
    }

    public static int getPrecoPersonagem(int indice) {
        validarIndice(indice);
        return PRECOS[indice];
    }

    public static String getNomePersonagem(int indice) {
        validarIndice(indice);
        return NOMES_PERSONAGENS[indice];
    }

    private static String chavePersonagem(int indice) {
        return "personagem_" + indice;
    }

    private String chave(String nome) {
        return (idPerfil.isEmpty() ? "perfil_novo" : idPerfil) + "_" + nome;
    }

    private static void validarIndice(int indice) {
        if (indice < 0 || indice >= TOTAL_PERSONAGENS) {
            throw new IllegalArgumentException("Personagem inexistente: " + indice);
        }
    }
}
