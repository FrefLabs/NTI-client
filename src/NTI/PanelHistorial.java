package NTI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

/**
 * (NUEVA CLASE)
 * Panel para la pantalla "Historial", muestra detalles del modelo seleccionado.
 */
public class PanelHistorial extends JPanel {

    // --- Referencia a NTI y sus componentes ---
    private NTI nti; 

    // --- Formateadores ---
    private final DecimalFormat priceFormat = new DecimalFormat("'$'0.00");
    private final DecimalFormat diffFormat = new DecimalFormat("'$'0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    // --- Componentes de UI ---
    private JLabel lblTituloHistorial;
    private JPanel panelListaHistorial;
    private JScrollPane scrollPane;
    private JLabel lblFallback;

    /**
     * Constructor de PanelHistorial.
     */
    public PanelHistorial(NTI nti) {
        this.nti = nti;
        
        this.setBackground(nti.fondo);
        this.setBorder(new EmptyBorder(30, 20, 30, 20));
        this.setLayout(new BorderLayout(0, 15));

        initUI();
    }

    private void initUI() {
        lblTituloHistorial = new JLabel("Historial de...");
        lblTituloHistorial.setFont(Fuentes.getBlack(24f));
        lblTituloHistorial.setForeground(nti.letra);
        lblTituloHistorial.setBorder(new EmptyBorder(0, 5, 15, 0));
        this.add(lblTituloHistorial, BorderLayout.NORTH);

        panelListaHistorial = new JPanel();
        panelListaHistorial.setLayout(new BoxLayout(panelListaHistorial, BoxLayout.Y_AXIS));
        panelListaHistorial.setBackground(nti.fondo);
        
        lblFallback = new JLabel("No hay datos de historial para este modelo.");
        lblFallback.setFont(Fuentes.getRegular(16f));
        lblFallback.setForeground(nti.letra.darker());
        lblFallback.setVisible(false);
        panelListaHistorial.add(lblFallback);

        scrollPane = new JScrollPane(panelListaHistorial);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(nti.fondo);
        scrollPane.setBackground(nti.fondo);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        this.add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Carga/Recarga los datos del modelo seleccionado.
     */
    public void cargarDatos() {
        panelListaHistorial.removeAll();
        lblFallback.setVisible(false);
        
        new Thread(() -> {
            Map<String, Object> info = nti.modelo.obtenerInfoSeleccionado();
            Vector<Map<String, Object>> historial = nti.modelo.obtenerHistorialModelo(); // (Usando el SP que creamos)

            SwingUtilities.invokeLater(() -> {
                if (info != null) {
                    String simbolo = (String) info.get("Simbolo");
                    int id = nti.modelo.getIDModeloSeleccionado();
                    lblTituloHistorial.setText(String.format(
                        "<html><span style='color:%s;'>Historial de %s - </span><span style='color:%s;'>Modelo #%d</span></html>",
                        nti.letraStr, simbolo, nti.bordedoradoStr, id
                    ));
                }

                if (historial == null || historial.isEmpty()) {
                    lblFallback.setVisible(true);
                    panelListaHistorial.add(lblFallback);
                } else {
                    for (Map<String, Object> fila : historial) {
                        JPanel panelFila = crearPanelFilaHistorial(fila);
                        panelListaHistorial.add(panelFila);
                        panelListaHistorial.add(Box.createVerticalStrut(15));
                    }
                }
                
                panelListaHistorial.add(Box.createVerticalGlue());
                panelListaHistorial.revalidate();
                panelListaHistorial.repaint();
                scrollPane.getVerticalScrollBar().setValue(0);
            });
        }).start();
    }

    /**
     * Helper para crear una sola fila del historial
     */
    private JPanel crearPanelFilaHistorial(Map<String, Object> fila) {
        Date fecha = (Date) fila.get("H_Fecha");
        double prediccion = (double) fila.get("Prediccion");
        double abierto = (double) fila.get("ValorAbierto");
        double cerrado = (double) fila.get("ValorCerrado");
        double alto = (double) fila.get("ValorAlto");
        double bajo = (double) fila.get("ValorBajo");
        double diferencia = Math.abs(cerrado - prediccion);
        
        Color colorDiferencia = Color.decode("#22B14C");
        if (diferencia > 0.5) colorDiferencia = Color.ORANGE;
        if (diferencia > 1.0) colorDiferencia = Color.RED;

        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setBackground(nti.fondoPanel);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(20, nti.bordeDorado, 2),
            new EmptyBorder(15, 20, 15, 20)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblFecha = new JLabel(dateFormat.format(fecha));
        lblFecha.setFont(Fuentes.getBlack(22f));
        lblFecha.setForeground(nti.letra);
        panel.add(lblFecha, BorderLayout.WEST);

        JPanel panelDatos = new JPanel(new GridLayout(2, 3, 20, 5));
        panelDatos.setOpaque(false);

        panelDatos.add(crearCeldaDato("Valor de apertura", abierto, nti.letra, priceFormat));
        panelDatos.add(crearCeldaDato("Valor máximo", alto, nti.letra, priceFormat));
        panelDatos.add(crearCeldaDato("Valor mínimo", bajo, nti.letra, priceFormat));
        panelDatos.add(crearCeldaDato("Valor de cierre", cerrado, nti.letra, priceFormat));
        panelDatos.add(crearCeldaDato("Cierre predicho", prediccion, nti.bordeDorado, priceFormat));
        panelDatos.add(crearCeldaDato("Diferencia", diferencia, colorDiferencia, diffFormat));

        panel.add(panelDatos, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Helper para crear una celda de dato
     */
    private JPanel crearCeldaDato(String titulo, double valor, Color colorValor, DecimalFormat formato) {
        JPanel celda = new JPanel();
        celda.setLayout(new BoxLayout(celda, BoxLayout.Y_AXIS));
        celda.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Fuentes.getRegular(12f));
        lblTitulo.setForeground(nti.letra.darker());
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblValor = new JLabel(formato.format(valor));
        lblValor.setFont(Fuentes.getBold(16f));
        lblValor.setForeground(colorValor);
        lblValor.setAlignmentX(Component.LEFT_ALIGNMENT);

        celda.add(lblTitulo);
        celda.add(lblValor);
        return celda;
    }
}