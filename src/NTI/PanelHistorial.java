package NTI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Vector;
import NTI.PanelModelos; 

public class PanelHistorial extends JPanel {

    private NTI nti; 

    private final DecimalFormat priceFormat = new DecimalFormat("'$'0.00");
    private final DecimalFormat diffFormat = new DecimalFormat("'$'0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    // --- Componentes de UI ---
    private JLabel lblTituloHistorial;
    private JPanel panelListaHistorial;
    private JScrollPane scrollPane;
    private JLabel lblFallback;
    
    // --- Componentes para el layout ---
    private CardLayout cardLayoutCentral;
    private JPanel panelContenedorCentral;
    private JButton btnVerDetalles;

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
        
        // --- Panel Superior para Título y Botón ---
        JPanel panelTitulo = new JPanel(new BorderLayout(20, 0));
        panelTitulo.setOpaque(false);
        
        lblTituloHistorial = new JLabel("Historial de...");
        lblTituloHistorial.setFont(Fuentes.getBlack(24f));
        lblTituloHistorial.setForeground(nti.letra);
        lblTituloHistorial.setBorder(new EmptyBorder(0, 5, 15, 0));
        panelTitulo.add(lblTituloHistorial, BorderLayout.CENTER);

        // --- Botón Ver Detalles ---
        btnVerDetalles = new JButton("Ver Detalles");
        btnVerDetalles.setPreferredSize(new Dimension(190, 50));
        btnVerDetalles.setBackground(nti.fondoPanel);
        btnVerDetalles.setForeground(nti.bordeDorado);
        btnVerDetalles.setFont(Fuentes.getBold(14f));
        btnVerDetalles.setFocusPainted(false);
        btnVerDetalles.setBorder(new RoundedBorder(15, nti.bordeDorado, 2));
        // Añadir cursor de mano
        btnVerDetalles.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Acción del botón:
        btnVerDetalles.addActionListener(e -> {
            int idModelo = nti.modelo.getIDModeloSeleccionado();
            
            if (nti.panelModelos != null) {
                nti.panelModelos.mostrarDetalleDeModelo(idModelo);
            }
            
            nti.panelModelos.mostrarDetalleDeModelo(idModelo);
            nti.cambiarPanel("modelos");
            
            
        });
        btnVerDetalles.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                AudioManager.getInstance().playBotonSound();
            }
        });
        
        JPanel panelBotonContenedor = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBotonContenedor.setOpaque(false);
        panelBotonContenedor.add(btnVerDetalles);
        
        panelTitulo.add(panelBotonContenedor, BorderLayout.EAST);
        
        this.add(panelTitulo, BorderLayout.NORTH);


        // --- Panel Central con CardLayout ---
        cardLayoutCentral = new CardLayout();
        panelContenedorCentral = new JPanel(cardLayoutCentral);
        panelContenedorCentral.setOpaque(false);

        // --- Panel Fallback ---
        lblFallback = new JLabel("No hay datos de historial para este modelo.");
        lblFallback.setFont(Fuentes.getRegular(16f));
        lblFallback.setForeground(nti.letra.darker());
        
        JPanel panelFallbackContenedor = new JPanel(new GridBagLayout());
        panelFallbackContenedor.setOpaque(false);
        panelFallbackContenedor.add(lblFallback); // GridBagLayout por defecto centra

        // --- Panel de Lista ---
        panelListaHistorial = new JPanel();
        panelListaHistorial.setLayout(new BoxLayout(panelListaHistorial, BoxLayout.Y_AXIS));
        panelListaHistorial.setBackground(nti.fondo);
        
        scrollPane = new JScrollPane(panelListaHistorial);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(nti.fondo);
        scrollPane.setBackground(nti.fondo);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // --- Añadir las "cartas" ---
        panelContenedorCentral.add(scrollPane, "LISTA");
        panelContenedorCentral.add(panelFallbackContenedor, "FALLBACK");
        
        // Añade el contenedor central al CENTRO del panel principal
        this.add(panelContenedorCentral, BorderLayout.CENTER);
    }

    /**
     * Carga/Recarga los datos del modelo seleccionado.
     */
    public void cargarDatos() {
        panelListaHistorial.removeAll();
        
        new Thread(() -> {
            Map<String, Object> info = nti.modelo.obtenerInfoSeleccionado();
            Vector<Map<String, Object>> historial = nti.modelo.obtenerHistorialModelo(); 

            // Lógica de conversión de moneda
            String monedaSeleccionada = nti.ent.moneda;
            double tasaDeCambio = 1.0;
            boolean errorTasaCambio = false;

            if (!monedaSeleccionada.equalsIgnoreCase("USD")) {
                tasaDeCambio = nti.lectura.obtenerValorMoneda(monedaSeleccionada);
                if (Double.isNaN(tasaDeCambio)) {
                    errorTasaCambio = true;
                    tasaDeCambio = 1.0;
                }
            }
            final double finalTasaDeCambio = tasaDeCambio;
            final boolean finalErrorTasaCambio = errorTasaCambio;

            SwingUtilities.invokeLater(() -> {
                String simboloMoneda = finalErrorTasaCambio ? "$" : getCurrencySymbol(monedaSeleccionada);

                if (info != null) {
                    String simbolo = (String) info.get("Simbolo");
                    int id = nti.modelo.getIDModeloSeleccionado();
                    lblTituloHistorial.setText(String.format(
                        "<html>Historial de Modelo #%d <span style='color:%s;'>%s</span></html>",
                        id, nti.bordedoradoStr, simbolo
                    ));
                }

                if (historial == null || historial.isEmpty()) {
                    cardLayoutCentral.show(panelContenedorCentral, "FALLBACK");
                } else {
                    for (Map<String, Object> fila : historial) {
                        JPanel panelFila = crearPanelFilaHistorial(fila, finalTasaDeCambio, simboloMoneda);
                        panelListaHistorial.add(panelFila);
                        panelListaHistorial.add(Box.createVerticalStrut(15));
                    }
                    panelListaHistorial.add(Box.createVerticalGlue());
                    cardLayoutCentral.show(panelContenedorCentral, "LISTA");
                }
                
                panelListaHistorial.revalidate();
                panelListaHistorial.repaint();
                scrollPane.getVerticalScrollBar().setValue(0);
            });
        }).start();
    }

    /**
     * Helper para crear una sola fila del historial
     */
    private String getCurrencySymbol(String currencyName) {
        if (currencyName == null) return "$";
        if (currencyName.contains("EUR")) return "€";
        if (currencyName.contains("ARS")) return "ARS$";
        return "$";
    }

    /**
     * Helper para crear una sola fila del historial
     */
    private JPanel crearPanelFilaHistorial(Map<String, Object> fila, double tasaDeCambio, String simboloMoneda) {
        Date fecha = (Date) fila.get("H_Fecha");
        double prediccion = (double) fila.get("Prediccion") * tasaDeCambio;
        double abierto = (double) fila.get("ValorAbierto") * tasaDeCambio;
        double cerrado = (double) fila.get("ValorCerrado") * tasaDeCambio;
        double alto = (double) fila.get("ValorAlto") * tasaDeCambio;
        double bajo = (double) fila.get("ValorBajo") * tasaDeCambio;
        double diferencia = Math.abs(cerrado - prediccion);
        
        Color colorDiferencia = Color.decode("#22B14C");
        if (diferencia > 0.5 * tasaDeCambio) colorDiferencia = Color.ORANGE;
        if (diferencia > 1.0 * tasaDeCambio) colorDiferencia = Color.RED;

        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setBackground(nti.fondoPanel);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(20, nti.bordeDorado, 2),
            new EmptyBorder(15, 20, 15, 20)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Añadir cursor de mano a la fila
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                AudioManager.getInstance().playBotonSound();
            }
        });

        JLabel lblFecha = new JLabel(dateFormat.format(fecha));
        lblFecha.setFont(Fuentes.getBlack(22f));
        lblFecha.setForeground(nti.letra);
        panel.add(lblFecha, BorderLayout.WEST);

        JPanel panelDatos = new JPanel(new GridLayout(2, 3, 20, 5));
        panelDatos.setOpaque(false);

        DecimalFormat customPriceFormat = new DecimalFormat(simboloMoneda + "0.00");
        DecimalFormat customDiffFormat = new DecimalFormat(simboloMoneda + "0.00");

        panelDatos.add(crearCeldaDato("Valor de apertura", abierto, nti.letra, customPriceFormat));
        panelDatos.add(crearCeldaDato("Valor máximo", alto, nti.letra, customPriceFormat));
        panelDatos.add(crearCeldaDato("Valor mínimo", bajo, nti.letra, customPriceFormat));
        panelDatos.add(crearCeldaDato("Valor de cierre", cerrado, nti.letra, customPriceFormat));
        panelDatos.add(crearCeldaDato("Cierre predicho", prediccion, nti.bordeDorado, customPriceFormat));
        panelDatos.add(crearCeldaDato("Diferencia", diferencia, colorDiferencia, customDiffFormat));

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
