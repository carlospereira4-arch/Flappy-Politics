package com.flappylula.desktop;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class GerenciadorMoedas {
    private final List<Moeda> moedas = new ArrayList<>();

    public void adicionarPara(Obstaculo obstaculo) {
        moedas.add(new Moeda(obstaculo.getX() + obstaculo.getLargura() / 2.0,
                obstaculo.getCentroPassagem()));
    }

    public int atualizar(double deltaSegundos, Rectangle2D hitboxPersonagem) {
        int coletadas = 0;
        Iterator<Moeda> iterator = moedas.iterator();
        while (iterator.hasNext()) {
            Moeda moeda = iterator.next();
            moeda.atualizar(deltaSegundos);
            if (moeda.tentarColetar(hitboxPersonagem)) {
                coletadas++;
            }
            if (moeda.isColetada() || moeda.getX() < -60) {
                iterator.remove();
            }
        }
        return coletadas;
    }

    public void desenhar(Graphics2D g) {
        for (Moeda moeda : moedas) {
            moeda.desenhar(g);
        }
    }

    public void reiniciar() {
        moedas.clear();
    }
}
