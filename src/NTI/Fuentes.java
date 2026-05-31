package NTI;

import java.awt.Font;
import java.io.InputStream;

public class Fuentes {

    private static final String BASE_PATH = "/fonts/static/";

    private static Font interRegular;
    private static Font interBold;
    private static Font interBlack;

    static {
        try {
            interRegular = cargarFuente("Inter_24pt-Regular.ttf");
            interBold = cargarFuente("Inter_24pt-Bold.ttf");
            interBlack = cargarFuente("Inter_24pt-Black.ttf");
        } catch (Exception e) {
            e.printStackTrace();
            interRegular = new Font("SansSerif", Font.PLAIN, 14);
            interBold = new Font("SansSerif", Font.BOLD, 14);
            interBlack = new Font("SansSerif", Font.BOLD, 16);
        }
    }

    private static Font cargarFuente(String archivo) throws Exception {
        InputStream is = Fuentes.class.getResourceAsStream(BASE_PATH + archivo);
        if (is == null) {
            throw new RuntimeException("No se encontró la fuente: " + archivo);
        }
        return Font.createFont(Font.TRUETYPE_FONT, is);
    }

    public static Font getRegular(float size) {
        return interRegular.deriveFont(size);
    }

    public static Font getBold(float size) {
        return interBold.deriveFont(size);
    }

    public static Font getBlack(float size) {
        return interBlack.deriveFont(size);
    }
}