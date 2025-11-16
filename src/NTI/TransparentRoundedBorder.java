package NTI;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.border.Border;

/**
 * (CLASE DE UTILIDAD - ARCHIVO SEPARADO)
 * Dibuja un borde redondeado transparente para los botones de imagen.
 */
public class TransparentRoundedBorder implements Border {
    private int radius;

    public TransparentRoundedBorder(int radius) {
        this.radius = radius;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(0, 0, 0, 0); // Sin márgenes internos
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Dibujar un borde redondeado sin color visible
        g2.setColor(c.getBackground()); // Usar el color de fondo del botón
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }
}