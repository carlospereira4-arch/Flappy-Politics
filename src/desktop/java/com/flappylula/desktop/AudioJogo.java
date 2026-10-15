package com.flappylula.desktop;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

/** Reprodução dos MP3 empacotados com a versão desktop. */
public final class AudioJogo {
    private static final AtomicBoolean MUSICA_ATIVA = new AtomicBoolean();
    private static volatile Object playerMusica;

    private AudioJogo() { }

    public static void iniciarMusica() {
        if (!MUSICA_ATIVA.compareAndSet(false, true)) {
            return;
        }
        Thread thread = new Thread(() -> {
            while (MUSICA_ATIVA.get()) {
                try (InputStream entrada = abrir("background_music.mp3")) {
                    if (entrada == null) {
                        MUSICA_ATIVA.set(false);
                        return;
                    }
                    Object player = criarPlayer(entrada);
                    playerMusica = player;
                    executar(player, "play");
                    playerMusica = null;
                } catch (IOException | ReflectiveOperationException e) {
                    if (MUSICA_ATIVA.get()) {
                        System.err.println("Não foi possível reproduzir a música: " + e.getMessage());
                    }
                    MUSICA_ATIVA.set(false);
                }
            }
        }, "flappy-lula-musica");
        thread.setDaemon(true);
        thread.start();
    }

    public static void pararMusica() {
        MUSICA_ATIVA.set(false);
        Object atual = playerMusica;
        if (atual != null) {
            try {
                executar(atual, "close");
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // A música será encerrada quando o fluxo MP3 terminar.
            }
        }
    }

    public static void tocar(String arquivo) {
        Thread thread = new Thread(() -> {
            try (InputStream entrada = abrir(arquivo)) {
                if (entrada != null) {
                    executar(criarPlayer(entrada), "play");
                }
            } catch (IOException | ReflectiveOperationException e) {
                System.err.println("Não foi possível reproduzir " + arquivo + ": " + e.getMessage());
            }
        }, "flappy-lula-audio-efeito");
        thread.setDaemon(true);
        thread.start();
    }

    private static InputStream abrir(String arquivo) throws IOException {
        InputStream recurso = AudioJogo.class.getClassLoader()
                .getResourceAsStream("sounds/" + arquivo);
        if (recurso != null) {
            return recurso;
        }
        File original = new File("assets/audio/original", arquivo);
        if (original.isFile()) {
            return original.toURI().toURL().openStream();
        }
        File recursoLocal = new File("src/desktop/resources/sounds", arquivo);
        return recursoLocal.isFile() ? recursoLocal.toURI().toURL().openStream() : null;
    }

    private static Object criarPlayer(InputStream entrada) throws ReflectiveOperationException {
        Class<?> tipoPlayer = Class.forName("javazoom.jl.player.Player");
        return tipoPlayer.getConstructor(InputStream.class)
                .newInstance(new BufferedInputStream(entrada));
    }

    private static void executar(Object player, String metodo)
            throws ReflectiveOperationException {
        try {
            player.getClass().getMethod(metodo).invoke(player);
        } catch (InvocationTargetException e) {
            Throwable causa = e.getCause();
            if (causa instanceof ReflectiveOperationException reflexiva) {
                throw reflexiva;
            }
            if (causa instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new ReflectiveOperationException("Falha ao executar o áudio MP3", causa);
        }
    }
}
