import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Personagem {
    // Aproximadamente 65% do tamanho anterior: redução de 35%.
    private static final int LARGURA = 64;
    private static final int ALTURA = 95;
    private static final double GRAVIDADE = 900.0;
    private static final double IMPULSO_PULO = -340.0;
    private static final double PROPORCAO_PERSONAGEM_X = 82.0 / 683.0;
    private static final double PROPORCAO_PERSONAGEM_LARGURA = (602.0 - 82.0) / 683.0;
    private static final double HITBOX_LARGURA = PROPORCAO_PERSONAGEM_LARGURA * 0.65;
    private static final double HITBOX_X = PROPORCAO_PERSONAGEM_X
            + (PROPORCAO_PERSONAGEM_LARGURA - HITBOX_LARGURA) / 2.0;
    private static final double HITBOX_Y = 217.0 / 1024.0;
    private static final double HITBOX_ALTURA = (891.0 - 217.0) / 1024.0;

    private final BufferedImage imagem;
    private double x;
    private double y;
    private double velocidadeVertical;

    public Personagem() {
        imagem = ImagemUtil.redimensionar(
                ImagemUtil.carregar("personagem_transparente.png"), LARGURA, ALTURA);
        reiniciar();
    }

    public boolean atualizar(double deltaSegundos, int alturaJogo) {
        velocidadeVertical += GRAVIDADE * deltaSegundos;
        y += velocidadeVertical * deltaSegundos;

        if (y < 0) {
            y = 0;
            velocidadeVertical = 0;
            return true;
        }
        if (y + ALTURA >= alturaJogo) {
            y = alturaJogo - ALTURA;
            return true;
        }
        return false;
    }

    public void pular() {
        velocidadeVertical = IMPULSO_PULO;
    }

    public void desenhar(Graphics2D g) {
        if (imagem != null) {
            g.drawImage(imagem, (int) x, (int) y, null);
        }
    }

    public Rectangle2D getHitbox() {
        return new Rectangle2D.Double(
                x + LARGURA * HITBOX_X,
                y + ALTURA * HITBOX_Y,
                LARGURA * HITBOX_LARGURA,
                ALTURA * HITBOX_ALTURA);
    }

    public void reiniciar() {
        x = 65;
        y = 210;
        velocidadeVertical = 0;
    }
}
