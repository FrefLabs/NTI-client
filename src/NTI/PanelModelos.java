package NTI;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class PanelModelos extends JPanel {

    // --- Referencia a NTI y sus componentes ---
    private NTI nti;
    // --- Componentes de la UI (Tus variables) ---
    private JPanel panelListaModelos;
    private JPanel panelDetalleModelo;
    private CardLayout cardLayoutModelos;
    private JPanel panelModelosContenido;
    private JLabel lblFallbackRecomendados;

    // --- Componentes de Detalle (MODIFICADO) ---
    private JLabel lblDetalle_Titulo;
    private JTextArea txtDetalle_Features;
    private JButton btnDetalle_Seleccionado;

    // (Labels de datos históricos)
    private JLabel lblDetalle_MSE, lblDetalle_RMSE, lblDetalle_MAE, lblDetalle_R2, lblDetalle_MaxError, lblDetalle_MinError, lblDetalle_Percentil90, lblDetalle_Precision;
    private JLabel lblDetalle_FechaIni, lblDetalle_FechaFin, lblDetalle_Arquitectura, lblDetalle_Funciones, lblDetalle_Tasa, lblDetalle_NMaxError, lblDetalle_Epocas;

    // (Labels de datos actuales)
    private JLabel lblDetalle_Historico_Titulo;
    private JLabel lblDetalle_Actual_Titulo;
    private JLabel lblDetalle_Actual_MAE;
    private JLabel lblDetalle_Actual_Precision;
    private JLabel lblDetalle_Actual_Tendencia;

    // --- Componentes de Features (Crear Modelo) ---
    private JPanel panelFeatures;
    private java.util.List<SliderToggleButton> featureToggles;
    private JTextField txtAccion, txtFechaInicio, txtFechaFin, txtArquitectura, txtFunciones, txtLearningRate, txtMaxError, txtMaxIter;

    public PanelModelos(NTI nti) {
        this.nti = nti;
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(nti.fondo);
        this.setBorder(new EmptyBorder(30, 50, 10, 50));
        initUI();
    }

    private void initUI() {

        //--------------------------------
        // PANEL SUPERIOR (BARRA DE BUSQUEDA Y BOTON DE CREAR MODELO)
        //--------------------------------
        JPanel barraSuperior = new JPanel(new GridBagLayout());
        barraSuperior.setOpaque(false);
        GridBagConstraints gbcBarra = new GridBagConstraints();
        gbcBarra.fill = GridBagConstraints.BOTH;
        gbcBarra.ipady = 10;
        JPanel panelBusqueda = new JPanel(new BorderLayout(10, 0));
        panelBusqueda.setBackground(nti.fondoPanel);
        panelBusqueda.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(5, 0, 0, 5)
        ));

        JLabel lblIconLupa = new JLabel();
        try {
            ImageIcon lupaIcon = new ImageIcon(getClass().getResource("/img/Lupa.png"));
            Image scaledImage = lupaIcon.getImage().getScaledInstance(21, 21, Image.SCALE_SMOOTH);
            lblIconLupa.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            System.err.println("Error al cargar imagen Lupa.png: " + e.getMessage());
            lblIconLupa.setText("Q"); // Fallback
            lblIconLupa.setFont(Fuentes.getBold(18f));
            lblIconLupa.setForeground(nti.letra);
        }
        lblIconLupa.setBorder(new EmptyBorder(5, 15, 5, 0));

        panelBusqueda.add(lblIconLupa, BorderLayout.WEST);
        JTextField txtBuscarModelo = new JTextField();
        txtBuscarModelo.setBackground(nti.fondoPanel);
        txtBuscarModelo.setCaretColor(Color.WHITE);
        txtBuscarModelo.setBorder(new EmptyBorder(5, 0, 5, 5));
        addPlaceholderFocusListener(txtBuscarModelo, "Buscar modelos");
        txtBuscarModelo.addActionListener(e -> {
            AudioManager.getInstance().playBotonSound();
            String textoBusqueda = txtBuscarModelo.getText().trim();
            if (textoBusqueda.isEmpty() || textoBusqueda.equals("Buscar modelos")) {
                return;
            }
            realizarBusqueda(textoBusqueda);
        });
        panelBusqueda.add(txtBuscarModelo, BorderLayout.CENTER);
        gbcBarra.gridx = 0;
        gbcBarra.weightx = 0.8;
        gbcBarra.insets = new Insets(0, 0, 0, 20);
        barraSuperior.add(panelBusqueda, gbcBarra);

        JButton btnCrearModelo = new JButton("+ Crear Modelo");
        btnCrearModelo.setBackground(nti.fondoPanel);
        btnCrearModelo.setForeground(nti.bordeDorado);
        btnCrearModelo.setFont(Fuentes.getBold(14f));
        btnCrearModelo.setBorder(new RoundedBorder(15, nti.bordeDorado, 3));
        btnCrearModelo.setFocusPainted(false);
        btnCrearModelo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCrearModelo.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                AudioManager.getInstance().playBotonSound();
            }
        });

        gbcBarra.gridx = 1;
        gbcBarra.weightx = 0.2;
        gbcBarra.insets = new Insets(0, 0, 0, 0);
        barraSuperior.add(btnCrearModelo, gbcBarra);
        this.add(barraSuperior, BorderLayout.NORTH);


        //--------------------------------
        // PANEL CENTRAL CON CARDLAYOUT
        //--------------------------------
        cardLayoutModelos = new CardLayout();
        panelModelosContenido = new JPanel(cardLayoutModelos);
        panelModelosContenido.setOpaque(false);

        // --- Tarjeta 1: Lista de Modelos ---
        panelListaModelos = new JPanel();
        panelListaModelos.setLayout(new BoxLayout(panelListaModelos, BoxLayout.Y_AXIS));
        panelListaModelos.setBackground(nti.fondo);
        panelListaModelos.setBorder(new EmptyBorder(10, 0, 10, 0));

        lblFallbackRecomendados = new JLabel("No hay modelos recomendados disponibles.");
        lblFallbackRecomendados.setFont(Fuentes.getRegular(16f));
        lblFallbackRecomendados.setForeground(nti.letra.darker());
        lblFallbackRecomendados.setHorizontalAlignment(SwingConstants.CENTER);
        lblFallbackRecomendados.setVisible(false);

        JPanel panelListaWrapper = new JPanel(new BorderLayout());
        panelListaWrapper.setOpaque(false);
        panelListaWrapper.add(panelListaModelos, BorderLayout.NORTH);
        panelListaWrapper.add(lblFallbackRecomendados, BorderLayout.CENTER);

        JScrollPane scrollListaModelos = new JScrollPane(panelListaWrapper);
        scrollListaModelos.setBorder(null);
        scrollListaModelos.getViewport().setBackground(nti.fondo);
        scrollListaModelos.setBackground(nti.fondo);
        scrollListaModelos.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollListaModelos.getVerticalScrollBar().setUnitIncrement(16);

        // --- Tarjeta 2: Crear Nuevo Modelo ---
        featureToggles = new ArrayList<>(); // (Inicializar lista)
        JPanel panelCrearModelo = crearPanelCrearModelo(cardLayoutModelos, panelModelosContenido);

        // --- Tarjeta 3: Detalle del Modelo ---
        panelDetalleModelo = crearPanelDetalleModelo();

        // --- (NUEVO) Tarjeta 4: Personalizar Features ---
        panelFeatures = crearPanelFeatures();


        // --- Añadir tarjetas al CardLayout ---
        panelModelosContenido.add(scrollListaModelos, "LISTA");
        panelModelosContenido.add(panelCrearModelo, "CREAR");
        panelModelosContenido.add(panelDetalleModelo, "DETALLE");
        panelModelosContenido.add(panelFeatures, "FEATURES");
        this.add(panelModelosContenido, BorderLayout.CENTER);

        // --- Acción del botón ---
        btnCrearModelo.addActionListener(e -> cardLayoutModelos.show(panelModelosContenido, "CREAR"));
    }

    public void mostrarListaYRecargar() {
        cardLayoutModelos.show(panelModelosContenido, "LISTA");
        cargarPanelModelos();
    }

    public boolean estaEnDetalle() {
        // Verificar si la tarjeta "DETALLE" está visible
        Component currentComponent = null;
        for (Component comp : panelModelosContenido.getComponents()) {
            if (comp.isVisible()) {
                currentComponent = comp;
                break;
            }
        }
        return currentComponent == panelDetalleModelo;
    }

    public boolean estaEnLista() {
        // Verificar si la tarjeta "LISTA" está visible
        Component currentComponent = null;
        for (Component comp : panelModelosContenido.getComponents()) {
            if (comp.isVisible()) {
                currentComponent = comp;
                break;
            }
        }
        return currentComponent instanceof JScrollPane; // LISTA es un JScrollPane
    }


    public void cargarDatosIniciales() {
        cargarPanelModelos();
    }

    private void realizarBusqueda(String textoBusqueda) {
        cardLayoutModelos.show(panelModelosContenido, "LISTA");
        panelListaModelos.removeAll();

        JLabel lblBuscando = new JLabel("Buscando modelos para '" + textoBusqueda + "'...");
        lblBuscando.setFont(Fuentes.getRegular(16f));
        lblBuscando.setForeground(nti.letra.darker());
        lblBuscando.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblBuscando.setBorder(new EmptyBorder(0, 0, 15, 0)); // Alineado
        panelListaModelos.add(lblBuscando);

        lblFallbackRecomendados.setVisible(false);

        panelListaModelos.revalidate();
        panelListaModelos.repaint();

        new Thread(() -> {
            Vector<Map<String, Object>> resultados = nti.modelo.buscarModelos(textoBusqueda);
            SwingUtilities.invokeLater(() -> {
                panelListaModelos.removeAll();

                JLabel lblTituloBusqueda = new JLabel("<html>Resultados para: <span style='color:" + nti.bordedoradoStr + ";'>" + textoBusqueda + "</span></html>");
                lblTituloBusqueda.setFont(Fuentes.getBlack(24f));
                lblTituloBusqueda.setForeground(Color.WHITE);
                lblTituloBusqueda.setBorder(new EmptyBorder(0, 0, 15, 0));
                lblTituloBusqueda.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelListaModelos.add(lblTituloBusqueda);

                if (resultados == null) {
                    lblFallbackRecomendados.setText("Error al buscar modelos: Servidor falló.");
                    lblFallbackRecomendados.setVisible(true);
                } else if (resultados.isEmpty()) {
                    lblFallbackRecomendados.setText("No se encontraron modelos para '" + textoBusqueda + "'.");
                    lblFallbackRecomendados.setVisible(true);
                } else {
                    lblFallbackRecomendados.setVisible(false);
                    for (Map<String, Object> m : resultados) {
                        panelListaModelos.add(crearPanelModeloUnificado(m));
                        panelListaModelos.add(Box.createVerticalStrut(15));
                    }
                }
                panelListaModelos.add(Box.createVerticalGlue());
                panelListaModelos.revalidate();
                panelListaModelos.repaint();
            });
        }).start();
    }

    private void cargarPanelModelos() {
        panelListaModelos.removeAll();
        lblFallbackRecomendados.setVisible(false);

        new Thread(() -> {
            Map<String, Object> sel = nti.modelo.obtenerInfoSeleccionado();
            Vector<Map<String, Object>> rec = nti.modelo.obtenerRecomendados();
            SwingUtilities.invokeLater(() -> {
                JLabel lblTituloSel = new JLabel("<html>Modelo <span style='color:" + nti.bordedoradoStr + ";'>seleccionado</span></html>");
                lblTituloSel.setFont(Fuentes.getBlack(24f));
                lblTituloSel.setForeground(nti.letra);
                lblTituloSel.setBorder(new EmptyBorder(0, 0, 15, 0));
                lblTituloSel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelListaModelos.add(lblTituloSel);

                if (sel != null) {
                    panelListaModelos.add(crearPanelModeloUnificado(sel));
                } else {
                    JLabel lblErrorSel = new JLabel("No se pudo cargar el modelo seleccionado (ID: " + nti.modelo.getIDModeloSeleccionado() + ")");
                    lblErrorSel.setForeground(Color.RED);
                    panelListaModelos.add(lblErrorSel);
                }

                panelListaModelos.add(Box.createVerticalStrut(25));
                JLabel lblTituloRec = new JLabel("<html>Modelos <span style='color:" + nti.bordedoradoStr + ";'>recomendados</span></html>");
                lblTituloRec.setFont(Fuentes.getBlack(24f));
                lblTituloRec.setForeground(nti.letra);
                lblTituloRec.setBorder(new EmptyBorder(0, 0, 15, 0));
                lblTituloRec.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelListaModelos.add(lblTituloRec);

                if (rec == null) {
                    lblFallbackRecomendados.setText("Error al cargar modelos recomendados: Servidor falló.");
                    lblFallbackRecomendados.setVisible(true);
                } else if (rec.isEmpty()) {
                    lblFallbackRecomendados.setText("No hay modelos recomendados disponibles.");
                    lblFallbackRecomendados.setVisible(true);
                } else {
                    for (Map<String, Object> m : rec) {
                        panelListaModelos.add(crearPanelModeloUnificado(m));
                        panelListaModelos.add(Box.createVerticalStrut(15));
                    }
                }
                panelListaModelos.add(Box.createVerticalGlue());
                panelListaModelos.revalidate();
                panelListaModelos.repaint();
            });
        }).start();
    }

    private JPanel crearPanelModeloUnificado(Map<String, Object> datos) {
        int idModelo = (datos.get("IDModelo") != null) ? (int) datos.get("IDModelo") : 0;
        String simbolo = (String) datos.getOrDefault("Simbolo", "N/A");
        double precision = (Double) datos.getOrDefault("Precision", 0.0);
        String fechaIni = datos.getOrDefault("FechaIni", "N/A").toString();
        String fechaFin = datos.getOrDefault("FechaFin", "N/A").toString();
        double mae = (Double) datos.getOrDefault("MAE", datos.getOrDefault("PromedioError", 0.0));

        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(nti.fondoPanel);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3), new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setOpaque(false);
        JLabel lblNombreModelo = new JLabel(simbolo);
        lblNombreModelo.setFont(Fuentes.getBlack(32f));
        lblNombreModelo.setForeground(Color.WHITE);
        panelIzquierdo.add(lblNombreModelo);
        JLabel lblIdModelo = new JLabel(String.format("<html>Modelo <span style='color:%s;'>#%d</span></html>", nti.bordedoradoStr, idModelo));
        lblIdModelo.setForeground(nti.letra);
        lblIdModelo.setFont(Fuentes.getBold(12f));
        panelIzquierdo.add(lblIdModelo);
        panel.add(panelIzquierdo, BorderLayout.WEST);

        JPanel panelDerecho = new JPanel(new GridLayout(3, 1, 0, 0));
        panelDerecho.setOpaque(false);
        JLabel lblPrecision = new JLabel(String.format("<html>Precision: <span style='color:#54DC81;'>%s</span></html>", nti.dfPercent.format(precision)));
        lblPrecision.setForeground(nti.letra);
        lblPrecision.setFont(Fuentes.getRegular(12f));
        lblPrecision.setHorizontalAlignment(SwingConstants.RIGHT);
        panelDerecho.add(lblPrecision);
        JLabel lblPromError = new JLabel(String.format("Promedio de error: %s", nti.maeFormat.format(mae)));
        lblPromError.setForeground(nti.letra);
        lblPromError.setFont(Fuentes.getRegular(12f));
        lblPromError.setHorizontalAlignment(SwingConstants.RIGHT);
        panelDerecho.add(lblPromError);
        JLabel lblRango = new JLabel(String.format("<html>Rango: <span style='color:%s;'>%s</span> - <span style='color:%s;'>%s</span></html>", nti.bordedoradoStr, fechaIni, nti.bordedoradoStr, fechaFin));
        lblRango.setForeground(nti.letra);
        lblRango.setFont(Fuentes.getRegular(12f));
        lblRango.setHorizontalAlignment(SwingConstants.RIGHT);
        panelDerecho.add(lblRango);
        panel.add(panelDerecho, BorderLayout.EAST);

        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AudioManager.getInstance().playBotonSound();
                mostrarDetalleDeModelo(idModelo);
            }
        });
        return panel;
    }

    private JPanel crearPanelCrearModelo(CardLayout cardLayout, JPanel cardPanel) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(nti.fondoPanel);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, nti.bordeDorado, 3), new EmptyBorder(20, 10, 20, 10)
        ));

        JLabel lblTituloCrear = new JLabel("<html>Crear nuevo <span style='color:" + nti.bordedoradoStr + ";'>modelo</span></html>");
        lblTituloCrear.setFont(Fuentes.getBlack(24f));
        lblTituloCrear.setForeground(nti.letra);
        lblTituloCrear.setBorder(new EmptyBorder(0, 5, 0, 0));
        panel.add(lblTituloCrear, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridLayout(0, 2, 15, 8));
        panelForm.setOpaque(false);
        panelForm.setBorder(new EmptyBorder(0, 15, 0, 15));

        String[] labels = {"Acción", "Fecha inicio", "Fecha fin", "Arquitectura", "Funciones", "Learning Rate", "Max Error", "Max Iter", "Features"};
        String[] placeholders = {
            "Ej: AAPL, MSFT, GOOGL", "YYYY-MM-DD", "YYYY-MM-DD", 
            "Ej: 19,10,5,1", "Ej: TANH,SIGMOID,RAMP,LINEAR", "Ej: 0.01", 
            "Ej: 0.001", "Ej: 10000"
        };
        
        // Asignar los JTextField a las variables de instancia
        txtAccion = new JTextField();
        txtFechaInicio = new JTextField();
        txtFechaFin = new JTextField();
        txtArquitectura = new JTextField();
        txtFunciones = new JTextField();
        txtLearningRate = new JTextField();
        txtMaxError = new JTextField();
        txtMaxIter = new JTextField();

        JTextField[] textFields = {
            txtAccion, txtFechaInicio, txtFechaFin, txtArquitectura, 
            txtFunciones, txtLearningRate, txtMaxError, txtMaxIter
        };
        
        Dimension btnSize = new Dimension(180, 45);

        for (int i = 0; i < labels.length; i++) {
            String s = labels[i];
            
            JLabel label = new JLabel(s);
            label.setFont(Fuentes.getBold(16f)); 
            label.setForeground(nti.letra);
            panelForm.add(label);
            
            if (!s.equals("Features")) {
                // Es un JTextField
                JTextField textField = textFields[i]; // Asignar la instancia
                
                textField.setBackground(nti.fondoPanel);
                textField.setCaretColor(nti.letra);
                textField.setForeground(Color.LIGHT_GRAY); 
                textField.setFont(Fuentes.getRegular(14f));
                textField.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(10, nti.bordeDorado, 2), 
                    new EmptyBorder(5, 10, 5, 10) 
                ));
                
                addPlaceholderFocusListener(textField, placeholders[i]);
                
                panelForm.add(textField);
                
            } else {
                // Es el botón "Personalizar"
                JButton btnPersonalizarFeatures = new JButton("Personalizar");
                
                btnPersonalizarFeatures.setBackground(nti.fondoPanel);
                btnPersonalizarFeatures.setForeground(nti.bordeDorado);
                btnPersonalizarFeatures.setFont(Fuentes.getBold(14f));
                btnPersonalizarFeatures.setBorder(new RoundedBorder(15, nti.bordeDorado, 3));
                btnPersonalizarFeatures.setFocusPainted(false);
                btnPersonalizarFeatures.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btnPersonalizarFeatures.setPreferredSize(btnSize);
                
                btnPersonalizarFeatures.addActionListener(e -> cardLayout.show(cardPanel, "FEATURES"));
                btnPersonalizarFeatures.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        AudioManager.getInstance().playBotonSound();
                    }
                });
                
                JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                btnWrapper.setOpaque(false);
                btnWrapper.add(btnPersonalizarFeatures);
                panelForm.add(btnWrapper);
            }
        }
        panel.add(panelForm, BorderLayout.CENTER);

        JPanel panelBotonesInferiores = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        panelBotonesInferiores.setOpaque(false);
        
        JButton btnVolverAtras = new JButton("Volver Atras");
        btnVolverAtras.setBackground(nti.fondoPanel);
        btnVolverAtras.setForeground(nti.bordeDorado);
        btnVolverAtras.setFont(Fuentes.getBold(14f));
        btnVolverAtras.setBorder(new RoundedBorder(15, nti.bordeDorado, 3));
        btnVolverAtras.setFocusPainted(false);
        btnVolverAtras.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolverAtras.setPreferredSize(btnSize);
        btnVolverAtras.addActionListener(e -> cardLayout.show(cardPanel, "LISTA"));
        btnVolverAtras.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                AudioManager.getInstance().playBotonSound();
            }
        });

        JButton btnSolicitar = new JButton("Solicitar");
        btnSolicitar.setBackground(nti.fondoPanel);
        btnSolicitar.setForeground(nti.bordeDorado);
        btnSolicitar.setFont(Fuentes.getBold(14f));
        btnSolicitar.setBorder(new RoundedBorder(15, nti.bordeDorado, 3));
        btnSolicitar.setFocusPainted(false);
        btnSolicitar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSolicitar.setPreferredSize(btnSize);
        
        btnSolicitar.addActionListener(e -> {
            // Lógica para el botón "Solicitar"
            Map<String, Object> parametros = new LinkedHashMap<>();
            try {
                // Validación de Ticker y fechas
                String ticker = txtAccion.getText().trim();
                if (ticker.isEmpty() || ticker.equals(placeholders[0])) {
                    JOptionPane.showMessageDialog(this, "Debe ingresar un Ticker.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                parametros.put("Ticker", ticker);
                
                String fechaInicio = txtFechaInicio.getText().trim();
                if (fechaInicio.isEmpty() || fechaInicio.equals(placeholders[1])) {
                    JOptionPane.showMessageDialog(this, "Debe ingresar una Fecha de Inicio.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                parametros.put("FechaInicio", fechaInicio);

                String fechaFin = txtFechaFin.getText().trim();
                if (fechaFin.isEmpty() || fechaFin.equals(placeholders[2])) {
                    JOptionPane.showMessageDialog(this, "Debe ingresar una Fecha de Fin.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                parametros.put("FechaFin", fechaFin);

                // Recolectar otros parámetros (asumiendo validación simple o valores por defecto)
                parametros.put("Arquitectura", txtArquitectura.getText().trim().isEmpty() ? "N/A" : txtArquitectura.getText().trim());
                parametros.put("Funciones", txtFunciones.getText().trim().isEmpty() ? "N/A" : txtFunciones.getText().trim());
                
                try {
                    parametros.put("LearningRate", Double.parseDouble(txtLearningRate.getText().trim()));
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Learning Rate inválido.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    parametros.put("MaxError", Double.parseDouble(txtMaxError.getText().trim()));
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Max Error inválido.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    parametros.put("MaxIter", Integer.parseInt(txtMaxIter.getText().trim()));
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Max Iter inválido.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Construir la cadena de Features
                List<String> selectedFeatures = new ArrayList<>();
                String[] featureNames = {
                    "fecha", "open_hoy", "high_ayer", "low_ayer", "close_ayer", "volume_ayer",
                    "gap_apertura", "rango_ayer", "close_position_ayer", "volumen_ratio",
                    "ATR_14", "RSI_14", "SMA_5", "SMA_20", "SMA_50", "EMA_12", "EMA_26",
                    "dia_semana"
                };
                
                for (int i = 0; i < featureNames.length; i++) {
                    if (featureToggles.get(i).isSelected()) {
                        selectedFeatures.add(featureNames[i]);
                    }
                }
                // Siempre añadir close_hoy al final
                selectedFeatures.add("close_hoy"); 
                
                parametros.put("Features", String.join(",", selectedFeatures));

                // Llamar a la API
                boolean exito = nti.registro.solicitarEntrenamientoModelo(parametros);

                if (exito) {
                    JOptionPane.showMessageDialog(this, "Solicitud de entrenamiento enviada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cardLayoutModelos.show(cardPanel, "LISTA"); // Volver a la lista
                    cargarPanelModelos(); // Recargar modelos si es necesario
                }
                // El bloque 'else' se elimina, los fallos ahora se manejan como excepciones.
                
            } catch (Exception ex) {
                // El mensaje de la excepción ahora contiene el error específico de la API.
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error en la Solicitud", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnSolicitar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                AudioManager.getInstance().playBotonSound();
            }
        });
        
        panelBotonesInferiores.add(btnVolverAtras);
        panelBotonesInferiores.add(btnSolicitar);
        panel.add(panelBotonesInferiores, BorderLayout.SOUTH);

        return panel;
    }

    public void mostrarDetalleDeModelo(int idModelo) {
        cardLayoutModelos.show(panelModelosContenido, "DETALLE");

        lblDetalle_Titulo.setText("Cargando Modelo #" + idModelo + "...");
        txtDetalle_Features.setText("Obteniendo datos de la base de datos...");

        new Thread(() -> {
            Map<String, Object> d = nti.modelo.obtenerDetalles(idModelo);
            Map<String, Object> dActual = nti.lectura.obtenerDatosActualesModelo(idModelo, new Date());

            SwingUtilities.invokeLater(() -> {
                if (d == null) {
                    lblDetalle_Titulo.setText("Error al cargar Modelo #" + idModelo);
                    txtDetalle_Features.setText("El modelo no se encontró en la base de datos.");
                    return;
                }

                lblDetalle_Titulo.setText("<html><span style='color:white;'>Modelo #" + d.get("IDModelo") + " - </span><span style='color:" + nti.bordedoradoStr + ";'>" + d.get("Simbolo") + "</span></html>");
                txtDetalle_Features.setText((String) d.get("Features"));

                boolean isSelected = (int)d.get("IDModelo") == nti.modelo.getIDModeloSeleccionado();
                btnDetalle_Seleccionado.setText(isSelected ? "Seleccionado" : "Seleccionar");
                btnDetalle_Seleccionado.setEnabled(!isSelected);
                btnDetalle_Seleccionado.putClientProperty("IDModelo", d.get("IDModelo"));
                btnDetalle_Seleccionado.putClientProperty("Simbolo", d.get("Simbolo"));

                lblDetalle_MSE.setText(nti.df8.format(d.get("MSE")));
                lblDetalle_RMSE.setText(nti.df8.format(d.get("RMSE")));
                lblDetalle_MAE.setText(nti.df8.format(d.get("MAE")));
                lblDetalle_R2.setText(nti.df8.format(d.get("R2")));
                lblDetalle_MaxError.setText(nti.df8.format(d.get("MaxError")));
                lblDetalle_MinError.setText(nti.df8.format(d.get("MinError")));
                lblDetalle_Percentil90.setText(nti.df8.format(d.get("Percentil90")));
                lblDetalle_Precision.setText(nti.dfPercent.format(d.get("Precision")));

                String fechaFin = d.get("FechaFin").toString();
                String fechaIni = d.get("FechaIni").toString();
                lblDetalle_FechaIni.setText(fechaIni);
                lblDetalle_FechaFin.setText(fechaFin);
                lblDetalle_Arquitectura.setText(d.get("Arquitectura").toString());
                lblDetalle_Funciones.setText(d.get("Funciones").toString());
                lblDetalle_Tasa.setText(d.get("TasaAprendizaje").toString());
                lblDetalle_NMaxError.setText(d.get("NMaxError").toString());
                lblDetalle_Epocas.setText(d.get("Epocas").toString());
                lblDetalle_Historico_Titulo.setText("Resultados desde " + fechaIni + " a " + fechaFin);
                lblDetalle_Actual_Titulo.setText("Resultados desde " + fechaFin + " a hoy");

                if (dActual != null) {
                    lblDetalle_Actual_MAE.setText(nti.maeFormat.format(dActual.get("MAE_A")));
                    lblDetalle_Actual_Precision.setText(nti.dfPercent.format(dActual.get("Precision")));
                    lblDetalle_Actual_Tendencia.setText(dActual.get("TENDENCIA").toString()); // Assuming TENDENCIA is String
                } else {
                    lblDetalle_Actual_MAE.setText("Error");
                    lblDetalle_Actual_Precision.setText("Error");
                    lblDetalle_Actual_Tendencia.setText("Servidor falló");
                }
            });
        }).start();
    }

    private JPanel crearPanelDato(String etiqueta, JLabel labelValor) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setFont(Fuentes.getRegular(14f));
        lblEtiqueta.setForeground(nti.letra);
        panel.add(lblEtiqueta, BorderLayout.WEST);
        labelValor.setFont(Fuentes.getBold(14f));
        labelValor.setForeground(nti.bordeDorado);
        labelValor.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(labelValor, BorderLayout.EAST);
        return panel;
    }

    private JPanel crearPanelDetalleModelo() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel panelSuperior = new JPanel(new BorderLayout(25, 10));
        panelSuperior.setBackground(nti.fondoPanel);
        
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3), new EmptyBorder(0, 0, 15, 25)
        ));

        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setOpaque(false);
        panelInfo.setBorder(new EmptyBorder(0, 25, 0, 0)); 

        lblDetalle_Titulo = new JLabel("Cargando...");
        lblDetalle_Titulo.setFont(Fuentes.getBlack(26f));
        lblDetalle_Titulo.setForeground(Color.WHITE);
        
        JPanel wrapTitulo = new JPanel(new BorderLayout());
        wrapTitulo.setOpaque(false);
        wrapTitulo.add(lblDetalle_Titulo, BorderLayout.WEST);
        panelInfo.add(wrapTitulo);


        JLabel lblTituloFeatures = new JLabel("FEATURES INCLUIDAS");
        lblTituloFeatures.setFont(Fuentes.getBlack(18f));
        lblTituloFeatures.setForeground(nti.letra);
        
        JPanel wrapFeatures = new JPanel(new BorderLayout());
        wrapFeatures.setOpaque(false);
        wrapFeatures.add(lblTituloFeatures, BorderLayout.WEST);
        panelInfo.add(Box.createVerticalStrut(10));
        panelInfo.add(wrapFeatures);


        txtDetalle_Features = new JTextArea("Cargando features...");
        txtDetalle_Features.setFont(Fuentes.getRegular(11f));
        txtDetalle_Features.setForeground(Color.LIGHT_GRAY);
        txtDetalle_Features.setOpaque(false);
        txtDetalle_Features.setEditable(false);
        txtDetalle_Features.setWrapStyleWord(true);
        txtDetalle_Features.setLineWrap(true);
        JScrollPane scrollFeatures = new JScrollPane(txtDetalle_Features);
        scrollFeatures.setOpaque(false);
        scrollFeatures.getViewport().setOpaque(false);
        scrollFeatures.setBorder(null);
        panelInfo.add(Box.createVerticalStrut(5));
        panelInfo.add(scrollFeatures);

        panelSuperior.add(panelInfo, BorderLayout.CENTER);

        JPanel panelBotonSel = new JPanel(new GridBagLayout());
        panelBotonSel.setOpaque(false);
        
        btnDetalle_Seleccionado = new JButton("Seleccionar");
        btnDetalle_Seleccionado.setFont(Fuentes.getBold(14f));
        btnDetalle_Seleccionado.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDetalle_Seleccionado.setBackground(nti.fondoPanel);
        btnDetalle_Seleccionado.setForeground(nti.bordeDorado);
        btnDetalle_Seleccionado.setBorder(new RoundedBorder(15, nti.bordeDorado, 3));
        btnDetalle_Seleccionado.setFocusPainted(false);
        btnDetalle_Seleccionado.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                AudioManager.getInstance().playBotonSound();
            }
        });
        
        Dimension btnSize = new Dimension(180, 45);
        btnDetalle_Seleccionado.setPreferredSize(btnSize);
        btnDetalle_Seleccionado.setMaximumSize(btnSize);
        btnDetalle_Seleccionado.setMinimumSize(btnSize);
        
        panelBotonSel.add(btnDetalle_Seleccionado);

        btnDetalle_Seleccionado.addActionListener(e -> {
            int id = (int) btnDetalle_Seleccionado.getClientProperty("IDModelo");
            String nuevoSimbolo = (String) btnDetalle_Seleccionado.getClientProperty("Simbolo");
            nti.modelo.setIDModeloSeleccionado(id);
            nti.accion.simbolo = nuevoSimbolo;
            nti.ent.enviarNConfig(nti.ent.moneda, nti.ent.sfx, nti.ent.rdr, id, nti.ent.estiloGrafica, nti.ent.volumenMusica);
            JOptionPane.showMessageDialog(this, "Modelo #" + id + " (" + nuevoSimbolo + ") seleccionado.", "Modelo Actualizado", JOptionPane.INFORMATION_MESSAGE);
            cardLayoutModelos.show(panelModelosContenido, "LISTA"); // Volver a la lista
            cargarPanelModelos(); // Recargar modelos si es necesario
            nti.panelInicio.cargarDatosIniciales();
        });

        panelSuperior.add(panelBotonSel, BorderLayout.EAST);
        panel.add(panelSuperior, BorderLayout.NORTH);

        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBackground(nti.fondoPanel);
        panelCentral.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3), new EmptyBorder(15, 0, 15, 25)
        ));
        GridBagConstraints gbcC = new GridBagConstraints();

        JPanel colResultados = new JPanel();
        colResultados.setLayout(new BoxLayout(colResultados, BoxLayout.Y_AXIS));
        colResultados.setOpaque(false);
        colResultados.setBorder(new EmptyBorder(0, 25, 0, 0));

        JLabel lblTituloResultados = new JLabel("Resultados");
        lblTituloResultados.setFont(Fuentes.getBlack(22f));
        lblTituloResultados.setForeground(nti.letra);
        
        JPanel wrapResultados = new JPanel(new BorderLayout());
        wrapResultados.setOpaque(false);
        wrapResultados.add(lblTituloResultados, BorderLayout.WEST);
        colResultados.add(wrapResultados);
        
        colResultados.add(Box.createVerticalStrut(8));
        lblDetalle_Historico_Titulo = new JLabel("Resultados desde ... a ...");
        lblDetalle_Historico_Titulo.setFont(Fuentes.getBold(18f));
        lblDetalle_Historico_Titulo.setForeground(nti.bordeDorado);

        JPanel wrapHist = new JPanel(new BorderLayout());
        wrapHist.setOpaque(false);
        wrapHist.add(lblDetalle_Historico_Titulo, BorderLayout.WEST);
        colResultados.add(wrapHist);
        
        colResultados.add(Box.createVerticalStrut(10));

        lblDetalle_MSE = new JLabel("...");
        lblDetalle_RMSE = new JLabel("...");
        lblDetalle_MAE = new JLabel("...");
        lblDetalle_R2 = new JLabel("...");
        lblDetalle_MaxError = new JLabel("...");
        lblDetalle_MinError = new JLabel("...");
        lblDetalle_Percentil90 = new JLabel("...");
        lblDetalle_Precision = new JLabel("...");

        colResultados.add(crearPanelDato("MSE", lblDetalle_MSE));
        colResultados.add(crearPanelDato("RMSE", lblDetalle_RMSE));
        colResultados.add(crearPanelDato("MAE", lblDetalle_MAE));
        colResultados.add(crearPanelDato("R2", lblDetalle_R2));
        colResultados.add(crearPanelDato("MAXERROR", lblDetalle_MaxError));
        colResultados.add(crearPanelDato("MINERROR", lblDetalle_MinError));
        colResultados.add(crearPanelDato("PERCENTIL 90", lblDetalle_Percentil90));
        colResultados.add(crearPanelDato("PRECISIÓN", lblDetalle_Precision));

        colResultados.add(Box.createVerticalStrut(10));
        lblDetalle_Actual_Titulo = new JLabel("Resultados desde ... a hoy");
        lblDetalle_Actual_Titulo.setFont(Fuentes.getBold(18f));
        lblDetalle_Actual_Titulo.setForeground(nti.bordeDorado);

        JPanel wrapActual = new JPanel(new BorderLayout());
        wrapActual.setOpaque(false);
        wrapActual.add(lblDetalle_Actual_Titulo, BorderLayout.WEST);
        colResultados.add(wrapActual);
        
        colResultados.add(Box.createVerticalStrut(10));

        lblDetalle_Actual_MAE = new JLabel("...");
        lblDetalle_Actual_Precision = new JLabel("...");
        lblDetalle_Actual_Tendencia = new JLabel("...");

        colResultados.add(crearPanelDato("ERROR PROMEDIO", lblDetalle_Actual_MAE));
        colResultados.add(crearPanelDato("PRECISIÓN", lblDetalle_Actual_Precision));
        colResultados.add(crearPanelDato("PRECISIÓN DE TENDENCIA", lblDetalle_Actual_Tendencia));
        colResultados.add(Box.createVerticalGlue());

        gbcC.gridx = 0;
        gbcC.gridy = 0;
        gbcC.weightx = 0.5;
        gbcC.weighty = 1.0; 
        gbcC.fill = GridBagConstraints.NONE; 
        gbcC.anchor = GridBagConstraints.NORTHWEST; 
        gbcC.insets = new Insets(0, 0, 0, 0); 
        panelCentral.add(colResultados, gbcC);

        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setForeground(Color.WHITE);
        separator.setBackground(nti.fondoPanel);
        gbcC.gridx = 1;
        gbcC.gridy = 0;
        gbcC.weightx = 0;
        gbcC.weighty = 1.0;
        gbcC.fill = GridBagConstraints.VERTICAL; 
        gbcC.anchor = GridBagConstraints.CENTER; 
        gbcC.insets = new Insets(0, 20, 0, 20);
        panelCentral.add(separator, gbcC);

        JPanel colParams = new JPanel();
        colParams.setLayout(new BoxLayout(colParams, BoxLayout.Y_AXIS));
        colParams.setOpaque(false);
        
        JLabel lblTituloParams = new JLabel("Parámetros");
        lblTituloParams.setFont(Fuentes.getBlack(22f));
        lblTituloParams.setForeground(nti.letra);

        JPanel wrapParams = new JPanel(new BorderLayout());
        wrapParams.setOpaque(false);
        wrapParams.add(lblTituloParams, BorderLayout.WEST);
        colParams.add(wrapParams);
        
        colParams.add(Box.createVerticalStrut(10));

        lblDetalle_FechaIni = new JLabel("...");
        lblDetalle_FechaFin = new JLabel("...");
        lblDetalle_Arquitectura = new JLabel("...");
        lblDetalle_Funciones = new JLabel("...");
        lblDetalle_Tasa = new JLabel("...");
        lblDetalle_NMaxError = new JLabel("...");
        lblDetalle_Epocas = new JLabel("...");

        colParams.add(crearPanelDato("FECHA INICIO", lblDetalle_FechaIni));
        colParams.add(Box.createVerticalStrut(10));
        colParams.add(crearPanelDato("FECHA FIN", lblDetalle_FechaFin));
        colParams.add(Box.createVerticalStrut(10));
        colParams.add(crearPanelDato("ARQUITECTURA", lblDetalle_Arquitectura));
        colParams.add(Box.createVerticalStrut(10));
        colParams.add(crearPanelDato("FUNCIONES", lblDetalle_Funciones));
        colParams.add(Box.createVerticalStrut(10));
        colParams.add(crearPanelDato("TASA DE APRENDIZAJE", lblDetalle_Tasa));
        colParams.add(Box.createVerticalStrut(10));
        colParams.add(crearPanelDato("N° MAX. DE ERROR", lblDetalle_NMaxError));
        colParams.add(Box.createVerticalStrut(10));
        colParams.add(crearPanelDato("N° MAX. DE ÉPOCAS", lblDetalle_Epocas));
        colParams.add(Box.createVerticalGlue());

        gbcC.gridx = 2;
        gbcC.gridy = 0;
        gbcC.weightx = 0.5;
        gbcC.weighty = 1.0; 
        gbcC.fill = GridBagConstraints.NONE; 
        gbcC.anchor = GridBagConstraints.NORTHWEST; 
        gbcC.insets = new Insets(0, 0, 0, 0); 
        panelCentral.add(colParams, gbcC);
        
        panel.add(panelCentral, BorderLayout.CENTER);

        JPanel panelBotonVolver = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panelBotonVolver.setOpaque(false);
        JButton btnVolverAtras = new JButton("Volver Atras");
        btnVolverAtras.setFont(Fuentes.getBold(14f));
        btnVolverAtras.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btnVolverAtras.setBackground(nti.fondoPanel);
        btnVolverAtras.setForeground(nti.bordeDorado);
        btnVolverAtras.setBorder(new RoundedBorder(15, nti.bordeDorado, 3));
        btnVolverAtras.setFocusPainted(false);
        btnVolverAtras.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                AudioManager.getInstance().playBotonSound();
            }
        });

        btnVolverAtras.setPreferredSize(btnSize);
        
        btnVolverAtras.addActionListener(e -> cardLayoutModelos.show(panelModelosContenido, "LISTA"));
        panelBotonVolver.add(btnVolverAtras);
        panel.add(panelBotonVolver, BorderLayout.SOUTH);

        return panel;
    }
    
    private JPanel crearPanelFeatures() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(nti.fondoPanel);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, nti.bordeDorado, 3), new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTituloFeatures = new JLabel("<html>Features <span style='color:" + nti.bordedoradoStr + ";'>Incluidas</span></html>");
        lblTituloFeatures.setFont(Fuentes.getBlack(24f));
        lblTituloFeatures.setForeground(nti.letra);
        lblTituloFeatures.setBorder(new EmptyBorder(0, 5, 5, 0)); // Reduced bottom padding
        panel.add(lblTituloFeatures, BorderLayout.NORTH); // Re-added this line

        JPanel panelContenido = new JPanel(new GridBagLayout());
        panelContenido.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        String[] featureNames = {
            "fecha", "open_hoy", "high_ayer", "low_ayer", "close_ayer", "volume_ayer",
            "gap_apertura", "rango_ayer", "close_position_ayer", "volumen_ratio",
            "ATR_14", "RSI_14", "SMA_5", "SMA_20", "SMA_50", "EMA_12", "EMA_26",
            "dia_semana"
        };

        featureToggles.clear();
        int midPoint = (featureNames.length + 1) / 2;

        // --- Separador Vertical ---
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        GridBagConstraints sepGbc = new GridBagConstraints();
        sepGbc.gridx = 2;
        sepGbc.gridy = 0;
        sepGbc.gridheight = midPoint;
        sepGbc.fill = GridBagConstraints.VERTICAL;
        sepGbc.insets = new Insets(0, 20, 0, 20);
        panelContenido.add(separator, sepGbc);

        for (int i = 0; i < midPoint; i++) {
            // --- Columna Izquierda ---
            gbc.gridy = i;
            gbc.insets = new Insets(0, 10, 8, 10); // Reduced bottom inset

            JLabel labelIzq = new JLabel(formatFeatureName(featureNames[i]));
            labelIzq.setFont(Fuentes.getBold(18f));
            labelIzq.setForeground(nti.letra);
            
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.fill = GridBagConstraints.NONE;
            panelContenido.add(labelIzq, gbc);

            SliderToggleButton toggleIzq = new SliderToggleButton();
            toggleIzq.setSelected(true); // Todos inician en true
            featureToggles.add(toggleIzq);

            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            panelContenido.add(toggleIzq, gbc);

            // --- Columna Derecha ---
            int rightIndex = i + midPoint;
            if (rightIndex < featureNames.length) {
                JLabel labelDer = new JLabel(formatFeatureName(featureNames[rightIndex]));
                labelDer.setFont(Fuentes.getBold(18f));
                labelDer.setForeground(nti.letra);

                gbc.gridx = 3;
                gbc.anchor = GridBagConstraints.EAST;
                panelContenido.add(labelDer, gbc);

                SliderToggleButton toggleDer = new SliderToggleButton();
                toggleDer.setSelected(true); // Todos inician en true
                featureToggles.add(toggleDer);
                
                gbc.gridx = 4;
                gbc.anchor = GridBagConstraints.WEST;
                panelContenido.add(toggleDer, gbc);
            }
        }

        // --- Lógica de Validación (Mínimo 2 seleccionados) ---
        for (JToggleButton toggle : featureToggles) {
            toggle.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    AudioManager.getInstance().playBotonSound();
                }
            });
            toggle.addActionListener(e -> {
                long selectedCount = featureToggles.stream().filter(JToggleButton::isSelected).count();
                if (selectedCount < 2) {
                    toggle.setSelected(true); // Revertir la deselección
                    JOptionPane.showMessageDialog(
                        PanelModelos.this, 
                        "Debe seleccionar al menos dos features.", 
                        "Selección Mínima Requerida", 
                        JOptionPane.WARNING_MESSAGE
                    );
                }
            });
        }

        panel.add(panelContenido, BorderLayout.CENTER);
        // --- FIN DE NUEVA LÓGICA DE LAYOUT ---

        JPanel panelBotonVolver = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panelBotonVolver.setOpaque(false);
        
        JButton btnVolver = new JButton("Volver Atras");
        Dimension btnSize = new Dimension(180, 45);
        btnVolver.setBackground(nti.fondoPanel);
        btnVolver.setForeground(nti.bordeDorado);
        btnVolver.setFont(Fuentes.getBold(14f));
        btnVolver.setBorder(new RoundedBorder(15, nti.bordeDorado, 3));
        btnVolver.setFocusPainted(false);
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.setPreferredSize(btnSize);
        btnVolver.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                AudioManager.getInstance().playBotonSound();
            }
        });
        
                    btnVolver.addActionListener(e -> cardLayoutModelos.show(panelModelosContenido, "CREAR"));        panelBotonVolver.add(btnVolver);
        panel.add(panelBotonVolver, BorderLayout.SOUTH);

        return panel;
    }
    
    private String formatFeatureName(String rawName) {
        String[] parts = rawName.split("_");
        StringBuilder formatted = new StringBuilder();
        for (String part : parts) {
            if (part.matches("ATR|RSI|SMA|EMA")) {
                formatted.append(part.toUpperCase());
            } else {
                formatted.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
            }
            formatted.append(" ");
        }
        return formatted.toString().trim();
    }

    private void addPlaceholderFocusListener(JTextField textField, String placeholder) {
        textField.setText(placeholder);
        textField.setForeground(Color.LIGHT_GRAY);
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(nti.letra);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(Color.LIGHT_GRAY);
                }
            }
        });
    }

    private class SliderToggleButton extends JToggleButton {
        private final Color colorOn = new Color(170, 120, 40);
        private final Color colorOff = new Color(80, 80, 80);
        private final Color colorThumb = nti.bordeDorado;
        private final int arc = 40;       // Ajustado a la nueva altura
        private final int thumbSize = 32; // Aumentado de 24
        private final int margin = 4;     // Aumentado de 3

        public SliderToggleButton() {
            super();
            Dimension size = new Dimension(90, 40);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isSelected() ? colorOn : colorOff);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc); // Usa las nuevas variables
            g2.setColor(colorThumb);
            int thumbX = isSelected() ? getWidth() - thumbSize - margin : margin;
            int thumbY = (getHeight() - thumbSize) / 2;
            g2.fillOval(thumbX, thumbY, thumbSize, thumbSize); // Usa las nuevas variables
            g2.dispose();
        }
    }
}