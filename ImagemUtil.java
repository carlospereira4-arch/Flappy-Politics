import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public final class ImagemUtil {
    private ImagemUtil() {
        // Esta classe só oferece métodos utilitários.
    }

    public static BufferedImage carregar(String caminho) {
        try {
            return ImageIO.read(new File(caminho));
        } catch (IOException e) {
            System.err.println("Não foi possível carregar " + caminho + ": " + e.getMessage());
            return null;
        }
    }

    public static BufferedImage redimensionar(BufferedImage original, int largura, int altura) {
        if (original == null || largura <= 0 || altura <= 0) {
            return null;
        }
        BufferedImage resultado = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resultado.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, largura, altura, null);
        g.dispose();
        return resultado;
    }

    public static BufferedImage redimensionarParaPreencher(
            BufferedImage original, int largura, int altura) {
        if (original == null || largura <= 0 || altura <= 0) {
            return null;
        }
        BufferedImage resultado = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        double escala = Math.max(
                (double) largura / original.getWidth(),
                (double) altura / original.getHeight());
        int larguraEscalada = (int) Math.ceil(original.getWidth() * escala);
        int alturaEscalada = (int) Math.ceil(original.getHeight() * escala);
        int x = (largura - larguraEscalada) / 2;
        int y = (altura - alturaEscalada) / 2;
        Graphics2D g = resultado.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, x, y, larguraEscalada, alturaEscalada, null);
        g.dispose();
        return resultado;
    }

    public static BufferedImage espelharHorizontalmente(BufferedImage original) {
        if (original == null) {
            return null;
        }
        BufferedImage resultado = new BufferedImage(
                original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resultado.createGraphics();
        g.drawImage(original, original.getWidth(), 0, -original.getWidth(), original.getHeight(), null);
        g.dispose();
        return resultado;
    }
}
