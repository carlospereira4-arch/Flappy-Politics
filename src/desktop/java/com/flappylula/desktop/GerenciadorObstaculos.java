package com.flappylula.desktop;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class GerenciadorObstaculos {
    private final List<Obstaculo> obstaculos = new ArrayList<>();
    private final Random aleatorio = new Random();
    private final Consumer<Obstaculo> aoCriarObstaculo;

    public GerenciadorObstaculos() {
        this(null);
    }

    public GerenciadorObstaculos(Consumer<Obstaculo> aoCriarObstaculo) {
        this.aoCriarObstaculo = aoCriarObstaculo;
        reiniciar();
    }

    public void atualizar(double deltaSegundos) {
        for (Obstaculo obstaculo : obstaculos) {
            obstaculo.atualizar(deltaSegundos);
        }

        Iterator<Obstaculo> iterator = obstaculos.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getX() < -ConfiguracaoJogo.LARGURA) {
                iterator.remove();
            }
        }

        while (!obstaculos.isEmpty()
                && obstaculos.get(obstaculos.size() - 1).getX() < ConfiguracaoJogo.LARGURA * 2) {
            Obstaculo ultimo = obstaculos.get(obstaculos.size() - 1);
            adicionar(new Obstaculo(ultimo.getX() + ultimo.getDistanciaAteProximo(), aleatorio));
        }
    }

    public boolean colidiuCom(Rectangle2D hitboxPersonagem) {
        for (Obstaculo obstaculo : obstaculos) {
            if (obstaculo.colideCom(hitboxPersonagem)) {
                return true;
            }
        }
        return false;
    }

    public void desenhar(Graphics2D g) {
        for (Obstaculo obstaculo : obstaculos) {
            obstaculo.desenhar(g);
        }
    }

    public void reiniciar() {
        obstaculos.clear();
        double x = ConfiguracaoJogo.LARGURA + 35;
        while (x < ConfiguracaoJogo.LARGURA * 3) {
            Obstaculo obstaculo = new Obstaculo(x, aleatorio);
            adicionar(obstaculo);
            x += obstaculo.getDistanciaAteProximo();
        }
    }

    private void adicionar(Obstaculo obstaculo) {
        obstaculos.add(obstaculo);
        if (aoCriarObstaculo != null) {
            aoCriarObstaculo.accept(obstaculo);
        }
    }
}

