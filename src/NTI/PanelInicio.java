package NTI;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;
import org.jfree.ui.RectangleInsets;
import org.jfree.chart.axis.NumberAxis;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class PanelInicio extends JPanel {

    private NTI nti;
    private JPanel listaValores;
    private JLabel lblFallbackModelos;
    private JPanel panelDescripcion;
    private JPanel panelNoticias;
    private ChartPanel chartPanel;
    private JPanel panelCentro;
    private JPanel panelDatosInferior;

    private JLabel lblNombreEmpresa;
    private JComboBox<String> cbEstiloGrafica;
    private final String[] ESTILOS_GRAFICA = {"Línea", "Línea y Puntos", "Solo Puntos"};

    private boolean inicializando = true;

    private JLabel lblRecSimbolo;
    private JLabel lblRecValorActual;
    private JLabel lblRecRecomendacion;
    private JLabel lblRecValorPredecido;
    private Timer valorActualTimer;

    public PanelInicio(NTI nti) {
        this.nti = nti;
        this.setLayout(new BorderLayout());
        this.setBackground(nti.fondo);
        initUI();
        valorActualTimer = new Timer(60000, e -> actualizarValorActual());
    }

    private void actualizarValorActual() {
        // Usa el símbolo actual, o "KO" por defecto si no está disponible.
        String simbolo = (nti.accion.simbolo == null || nti.accion.simbolo.isEmpty()) ? "KO" : nti.accion.simbolo;
        
        new Thread(() -> {
            Double valorActual = nti.lectura.obtenerValorActual(simbolo);
            if (valorActual == null) {
                // El error ya se loguea en la consola desde Lectura.java
                return;
            }

            // Reutilizar la lógica de conversión de moneda
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
                String simboloMoneda;
                if (finalErrorTasaCambio) {
                    simboloMoneda = "$";
                } else {
                    simboloMoneda = getCurrencySymbol(monedaSeleccionada);
                }
                DecimalFormat customMaeFormat = new DecimalFormat(simboloMoneda + "0.00");

                Object openHoyValue = lblRecValorActual.getClientProperty("openHoy");
                if (openHoyValue instanceof Double) {
                    double openHoy = (Double) openHoyValue;
                    lblRecValorActual.setForeground(valorActual >= openHoy ? Color.decode("#54DC81") : Color.RED);
                } else {
                    lblRecValorActual.setForeground(Color.WHITE); // Default color if open price isn't set
                }

                lblRecValorActual.setText(customMaeFormat.format(valorActual * finalTasaDeCambio));
            });
        }).start();
    }

    private void initUI() {
        panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.setBackground(nti.fondo);
        panelCentro.setBorder(new EmptyBorder(30, 20, 25, 0));

        JPanel panelGraficoContenedor = new JPanel(new BorderLayout(0, 10));
        panelGraficoContenedor.setBackground(nti.fondoPanel);
        panelGraficoContenedor.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(0, 0, 10, 0)
        ));

        JPanel panelTituloGrafico = crearPanelTituloGrafico();

        chartPanel = crearGraficoPanel();
        chartPanel.setBackground(nti.fondoPanel);

        panelGraficoContenedor.add(panelTituloGrafico, BorderLayout.NORTH);
        panelGraficoContenedor.add(chartPanel, BorderLayout.CENTER);

        panelCentro.add(panelGraficoContenedor, BorderLayout.CENTER);

        panelDatosInferior = crearPanelDatos();
        panelDatosInferior.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(12, 15, 10, 15)
        ));
        panelCentro.add(panelDatosInferior, BorderLayout.SOUTH);
        this.add(panelCentro, BorderLayout.CENTER);

        JPanel derecha = new JPanel();
        derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS));
        derecha.setPreferredSize(new Dimension(450, nti.getHeight()));
        derecha.setBackground(nti.fondo);
        derecha.setBorder(new EmptyBorder(30, 15, 25, 15));

        JPanel recomendacion = crearPanelRecomendacion();
        recomendacion.setBackground(nti.fondoPanel);
        recomendacion.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(10, 10, 10, 10) // Padding externo
        ));
        recomendacion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        recomendacion.setAlignmentX(Component.LEFT_ALIGNMENT);
        derecha.add(recomendacion);
        derecha.add(Box.createVerticalStrut(15));

        // --- (Panel Descripción) ---
        panelDescripcion = new JPanel();
        panelDescripcion.setBackground(nti.fondoPanel);
        panelDescripcion.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(12, 10, 10, 10)
        ));
        panelDescripcion.setLayout(new BorderLayout());
        panelDescripcion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        panelDescripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
        derecha.add(panelDescripcion);
        derecha.add(Box.createVerticalStrut(15));

        // --- (Panel Lista Modelos) ---
        listaValores = new JPanel();
        listaValores.setLayout(new BoxLayout(listaValores, BoxLayout.Y_AXIS));
        listaValores.setMaximumSize(new Dimension(200, 180));
        listaValores.setBackground(nti.fondoPanel);

        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setOpaque(false);
        panelTitulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lblTituloModelos = new JLabel("<html>Modelos más <span style='color:" + nti.bordedoradoStr + ";'>precisos</span></html>");
        lblTituloModelos.setForeground(nti.letra);
        lblTituloModelos.setFont(Fuentes.getRegular(12f));
        lblTituloModelos.setBorder(new EmptyBorder(0, 5, 10, 5));
        panelTitulo.add(lblTituloModelos, BorderLayout.WEST);
        listaValores.add(panelTitulo);

        lblFallbackModelos = new JLabel("Aún no hay modelos disponibles", SwingConstants.CENTER);
        lblFallbackModelos.setForeground(nti.letra.darker());
        lblFallbackModelos.setFont(Fuentes.getRegular(16f));
        lblFallbackModelos.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblFallbackModelos.setVisible(false);
        listaValores.add(lblFallbackModelos);

        JPanel contenedorLista = new JPanel(new BorderLayout());
        contenedorLista.setBackground(nti.fondoPanel);
        contenedorLista.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(10, 10, 10, 10) // (Padding ya estaba reducido)
        ));
        contenedorLista.setMaximumSize(new Dimension(Integer.MAX_VALUE, 245));
        contenedorLista.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenedorLista.add(listaValores, BorderLayout.CENTER);
        derecha.add(contenedorLista);
        derecha.add(Box.createVerticalStrut(15));

        // --- (Panel Noticias) ---
        panelNoticias = new JPanel();
        panelNoticias.setLayout(new BoxLayout(panelNoticias, BoxLayout.Y_AXIS));
        panelNoticias.setBackground(nti.fondoPanel);
        panelNoticias.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(20, 10, 10, 10)
        ));
        panelNoticias.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Altura fija para el panel de noticias
        Dimension fixedNoticiasHeight = new Dimension(400, 170);
        panelNoticias.setMinimumSize(fixedNoticiasHeight);
        panelNoticias.setPreferredSize(fixedNoticiasHeight);
        panelNoticias.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));

        derecha.add(panelNoticias);

        this.add(derecha, BorderLayout.EAST);
    }

    private JPanel crearPanelRecomendacion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true); // Hacer el panel GBL transparente
        GridBagConstraints gbc = new GridBagConstraints();

        // Padding horizontal entre las dos columnas
        gbc.insets = new Insets(2, 10, 2, 10);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.NONE;

        // Añadir pegamento superior
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalGlue(), gbc);
        gbc.gridx = 1; // Añadir pegamento a ambas columnas
        panel.add(Box.createVerticalGlue(), gbc);
        gbc.weighty = 0; // Resetear weighty para los componentes

        // --- Símbolo y Título ---
        lblRecSimbolo = new JLabel("...");
        lblRecSimbolo.setFont(Fuentes.getBold(32f));
        lblRecSimbolo.setForeground(nti.bordeDorado);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER; 
        panel.add(lblRecSimbolo, gbc);

        JLabel lblTituloRec = new JLabel("RECOMENDACION DIARIA");
        lblTituloRec.setFont(Fuentes.getRegular(12f));
        lblTituloRec.setForeground(nti.letra);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER; 
        panel.add(lblTituloRec, gbc);

        // --- Título y Valor---
        JLabel lblTituloActual = new JLabel("VALOR ACTUAL");
        lblTituloActual.setFont(Fuentes.getRegular(12f));
        lblTituloActual.setForeground(nti.letra);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(lblTituloActual, gbc);

        lblRecRecomendacion = new JLabel("...");
        lblRecRecomendacion.setFont(Fuentes.getBold(20f));
        lblRecRecomendacion.setForeground(nti.bordeDorado);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(lblRecRecomendacion, gbc);

        // --- Valor Título ---
        lblRecValorActual = new JLabel("N/A");
        lblRecValorActual.setFont(Fuentes.getBold(24f));
        lblRecValorActual.setForeground(Color.GRAY);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(lblRecValorActual, gbc);

        JLabel lblTituloPredecido = new JLabel("VALOR PREDECIDO DE CIERRE");
        lblTituloPredecido.setFont(Fuentes.getRegular(12f));
        lblTituloPredecido.setForeground(nti.letra);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(lblTituloPredecido, gbc);

        lblRecValorPredecido = new JLabel("N/A");
        lblRecValorPredecido.setFont(Fuentes.getBold(24f));
        lblRecValorPredecido.setForeground(Color.GRAY);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(lblRecValorPredecido, gbc);

        // Añadir pegamento inferior
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalGlue(), gbc);
        gbc.gridx = 1; // Añadir pegamento a ambas columnas
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private JPanel crearPanelTituloGrafico() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 15, 5, 15));

        lblNombreEmpresa = new JLabel("Cargando Empresa...");
        lblNombreEmpresa.setForeground(nti.bordeDorado);
        lblNombreEmpresa.setFont(Fuentes.getBold(16f));
        panel.add(lblNombreEmpresa, BorderLayout.WEST);

        cbEstiloGrafica = new JComboBox<>(ESTILOS_GRAFICA);
        personalizarComboBox(cbEstiloGrafica);

        cbEstiloGrafica.addActionListener(e -> {
            if (inicializando) return;
            String estiloSeleccionado = (String) cbEstiloGrafica.getSelectedItem();
            if (estiloSeleccionado != null) {
                if (this.isShowing()) {
                    aplicarEstiloGrafica(estiloSeleccionado, true);
                }
            }
        });

        cbEstiloGrafica.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                AudioManager.getInstance().playComboInSound();
            }
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                AudioManager.getInstance().playComboOutSound();
            }
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                AudioManager.getInstance().playComboOutSound();
            }
        });

        panel.add(cbEstiloGrafica, BorderLayout.EAST);
        return panel;
    }

    public void cargarDatosIniciales() {
        new Thread(() -> {
            int idModeloActual = nti.ent.idModelo;
            Map<String, Object> predictionData = nti.lectura.getPredictionData(idModeloActual);

            if (predictionData == null) {
                SwingUtilities.invokeLater(() -> {
                    lblNombreEmpresa.setText("Error al cargar datos");
                    lblNombreEmpresa.setForeground(Color.RED);
                    panelDescripcion.removeAll();
                    panelDescripcion.revalidate();
                    panelDescripcion.repaint();
                    panelNoticias.removeAll();
                    panelNoticias.revalidate();
                    panelNoticias.repaint();
                    chartPanel.getChart().getXYPlot().setDataset(null);
                    panelDatosInferior.removeAll();
                    panelDatosInferior.revalidate();
                    panelDatosInferior.repaint();
                    poblarPanelRecomendacion(null);
                });
                return;
            }

            String nuevoSimbolo = (String) predictionData.get("ticker");
            if (nuevoSimbolo != null && !nuevoSimbolo.isEmpty()) {
                nti.accion.simbolo = nuevoSimbolo;
            } else {
                nti.accion.simbolo = "KO";
            }

            SwingUtilities.invokeLater(() -> {
                String nombreEmpresa = (String) predictionData.get("nombreEmpresa");
                String descripcionEmpresa = (String) predictionData.get("descripcionEmpresa");

                lblNombreEmpresa.setText(nombreEmpresa);
                panelDescripcion.removeAll();

                if (nombreEmpresa != null && descripcionEmpresa != null) {

                    // Reemplazar JLabel por JTextPane)
                    JTextPane textPane = new JTextPane();
                    textPane.setContentType("text/html");
                    textPane.setOpaque(false);
                    textPane.setEditable(false);
                    textPane.setFocusable(false);
                    
                    // Tamaño de fuente 12pt)
                    String html = "<html><body style='color:" + nti.letraStr + "; font-family: Arial; font-size: 13pt;'>"
                            + "<p style='margin:0; padding:0;'><span style='color:" + nti.bordedoradoStr + "; font-weight:bold;'>"
                            + nombreEmpresa + "</span><br>"
                            + descripcionEmpresa + "</p></body></html>";
                    textPane.setText(html);
                    
                    panelDescripcion.add(textPane, BorderLayout.CENTER);
                }
                panelDescripcion.revalidate();
                panelDescripcion.repaint();

                poblarPanelRecomendacion(predictionData);
                cargarGrafico(nti.accion.simbolo);
                cargarPanelDatosInferior(nti.accion.simbolo);
                cargarNoticias(nti.accion.simbolo);
                cargarTopModelos();

                String estiloGuardado = nti.ent.estiloGrafica;
                if (estiloGuardado.equalsIgnoreCase("lineal")) {
                    estiloGuardado = "Línea";
                }
                cbEstiloGrafica.setSelectedItem(estiloGuardado);
                aplicarEstiloGrafica(estiloGuardado, false);

                inicializando = false;
            });
        }).start();
    }

    private void cargarNoticias(String simbolo) {
        Vector<Tupla> datnot = nti.not.getDatos(simbolo);
        panelNoticias.removeAll();
        // Asegurar que el layout sea BoxLayout para alinear verticalmente
        panelNoticias.setLayout(new BoxLayout(panelNoticias, BoxLayout.Y_AXIS));

        if (datnot != null && !datnot.isEmpty()) {
            for (Tupla noticia : datnot) {
                panelNoticias.add(crearPanelNoticia(noticia));
                panelNoticias.add(Box.createVerticalStrut(10));
            }
        } else {
            // Centrar el mensaje de error si no hay noticias
            JLabel errorLabel = new JLabel("Servidor falló o no hay noticias para " + simbolo + ".");
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            panelNoticias.add(Box.createVerticalGlue());
            panelNoticias.add(errorLabel);
            panelNoticias.add(Box.createVerticalGlue());
        }
        panelNoticias.revalidate();
        panelNoticias.repaint();
    }

    private String getCurrencySymbol(String currencyName) {
        if (currencyName == null) return "$";
        if (currencyName.contains("EUR")) return "€";
        if (currencyName.contains("ARS")) return "ARS$";
        return "$";
    }

    private void poblarPanelRecomendacion(Map<String, Object> datos) {
        if (datos == null || datos.isEmpty()) {
            lblRecSimbolo.setText("N/A");
            lblRecValorActual.setText("N/A");
            lblRecRecomendacion.setText("N/A");
            lblRecValorPredecido.setText("N/A");
            return;
        }

        String ticker = (String) datos.get("ticker");
        Double openHoy = (Double) datos.get("openHoy");
        String senal = (String) datos.get("senalCompraVenta");
        Double valorPredicho = (Double) datos.get("valorPredicho");
        Double predichoRefinado = (Double) datos.get("predichoRefinado");

        lblRecSimbolo.setText(ticker != null ? ticker : "N/A");
        lblRecRecomendacion.setText(senal != null ? senal : "N/A");

        Double valorFinalPredecido = nti.ent.rdr ? predichoRefinado : valorPredicho;

        new Thread(() -> {
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
                String simboloMoneda;
                if (finalErrorTasaCambio) {
                    simboloMoneda = "$";
                } else {
                    simboloMoneda = getCurrencySymbol(monedaSeleccionada);
                }
                DecimalFormat customMaeFormat = new DecimalFormat(simboloMoneda + "0.00");

                    if (openHoy != null) {
                        lblRecValorActual.putClientProperty("openHoy", openHoy);
                        lblRecValorActual.setText(customMaeFormat.format(openHoy * finalTasaDeCambio));
                        lblRecValorActual.setForeground(Color.WHITE); // Color neutro para 'open'
                    } else {
                    lblRecValorActual.setText("N/A");
                    lblRecValorActual.setForeground(Color.GRAY);
                }

                if (valorFinalPredecido != null && openHoy != null) {
                    lblRecValorPredecido.setText(customMaeFormat.format(valorFinalPredecido * finalTasaDeCambio));
                    lblRecValorPredecido.setForeground(valorFinalPredecido >= openHoy ? Color.decode("#54DC81") : Color.RED);
                } else {
                    lblRecValorPredecido.setText("N/A");
                    lblRecValorPredecido.setForeground(Color.GRAY);
                }
            });
        }).start();
    }

    private void aplicarEstiloGrafica(String estilo, boolean guardar) {
        XYPlot plot = chartPanel.getChart().getXYPlot();
        try {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
            String estiloParaGuardar = estilo;
            switch (estilo) {
                case "Línea y Puntos":
                    renderer.setBaseLinesVisible(true);
                    renderer.setBaseShapesVisible(true);
                    break;
                case "Solo Puntos":
                    renderer.setBaseLinesVisible(false);
                    renderer.setBaseShapesVisible(true);
                    break;
                case "Línea":
                default:
                    renderer.setBaseLinesVisible(true);
                    renderer.setBaseShapesVisible(false);
                    estiloParaGuardar = "lineal";
                    break;
            }

            if (guardar) {
                nti.estiloGraficaActual = estiloParaGuardar;
                nti.ent.enviarNConfig(nti.ent.moneda, nti.ent.sfx, nti.ent.rdr, nti.ent.idModelo, estiloParaGuardar, nti.ent.volumenMusica);
            }
        } catch (Exception e) {
            System.err.println("Error al aplicar estilo: " + e.getMessage());
        }
    }

    private void cargarPanelDatosInferior(String simbolo) {
        new Thread(() -> {
            Map<String, Double> datos = nti.lectura.obtenerDatosEmpresa(simbolo);
            SwingUtilities.invokeLater(() -> poblarPanelDatos(datos));
        }).start();
    }

    private void poblarPanelDatos(Map<String, Double> datos) {
        panelDatosInferior.removeAll();

        if (datos == null || datos.isEmpty()) {
            JLabel lblError = new JLabel("No hay datos de empresa para mostrar.");
            lblError.setForeground(nti.letra.darker());
            panelDatosInferior.setLayout(new BorderLayout());
            panelDatosInferior.add(lblError, BorderLayout.CENTER);
        } else {
            panelDatosInferior.setLayout(new GridLayout(6, 2, 15, 5));
            for (Map.Entry<String, Double> entry : datos.entrySet()) {
                JPanel panelDato = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                panelDato.setBackground(nti.fondoPanel);

                JLabel labelClave = new JLabel(entry.getKey() + ":");
                labelClave.setForeground(Color.WHITE);
                labelClave.setFont(new Font("Arial", Font.PLAIN, 12));

                String valorFormateado;
                String clave = entry.getKey();

                if (clave.equals("ROE") || clave.equals("ROA") || clave.equals("Profit Margin")) {
                    valorFormateado = nti.dfPercent.format(entry.getValue() / 100.0);
                } else {
                    valorFormateado = nti.maeFormat.format(entry.getValue());
                }

                JLabel labelValor = new JLabel(valorFormateado);
                labelValor.setForeground(nti.bordeDorado);
                labelValor.setFont(new Font("Arial", Font.BOLD, 12));

                panelDato.add(labelClave);
                panelDato.add(Box.createRigidArea(new Dimension(5, 0)));
                panelDato.add(labelValor);
                panelDatosInferior.add(panelDato);
            }
        }
        panelDatosInferior.revalidate();
        panelDatosInferior.repaint();
    }

    private void cargarTopModelos() {
        new Thread(() -> {
            Vector<Map<String, Object>> modelos = nti.modelo.obtenerTop3ParaVista();
            SwingUtilities.invokeLater(() -> {
                Component titulo = listaValores.getComponent(0);
                listaValores.removeAll();
                listaValores.add(titulo);

                if (modelos == null) {
                    lblFallbackModelos.setText("Error al cargar modelos: Servidor falló.");
                    lblFallbackModelos.setVisible(true);
                } else if (modelos.isEmpty()) {
                    lblFallbackModelos.setText("Aún no hay modelos disponibles.");
                    lblFallbackModelos.setVisible(true);
                } else {
                    lblFallbackModelos.setVisible(false);
                    int rank = 1;
                    for (Map<String, Object> m : modelos) {
                        JPanel panelModelo = crearPanelUnModelo(rank++, (String) m.get("Simbolo"), (String) m.get("E_Nombre"), (Double) m.get("MAE"), (Double) m.get("Precision"));
                        listaValores.add(panelModelo);
                    }
                    listaValores.add(Box.createVerticalGlue());
                }
                listaValores.revalidate();
                listaValores.repaint();
            });
        }).start();
    }

    private JPanel crearPanelUnModelo(int rank, String simbolo, String nombreEmpresa, double mae, double precision) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 5, 0, 5));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblRank = new JLabel(rank + ". " + simbolo);
        lblRank.setForeground(nti.letra);
        lblRank.setFont(Fuentes.getBlack(14f));
        panel.add(lblRank);

        panel.add(new JLabel(""));

        JLabel lblNombre = new JLabel(nombreEmpresa);
        lblNombre.setForeground(nti.letra.darker());
        lblNombre.setFont(Fuentes.getRegular(12f));
        panel.add(lblNombre);

        String maeTexto = (mae > 0) ? "± " + nti.maeFormat.format(mae) : nti.maeFormat.format(mae);
        String precisionTexto = nti.percentFormat.format(precision);
        JLabel lblStats = new JLabel(maeTexto + " - " + precisionTexto);
        lblStats.setForeground(nti.bordeDorado);
        lblStats.setFont(Fuentes.getBold(12f));
        lblStats.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lblStats);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                AudioManager.getInstance().playBotonSound();
                nti.cambiarPanel("modelos");
                nti.panelModelos.mostrarListaYRecargar();
            }
        });

        return panel;
    }

    private JPanel crearPanelNoticia(final Tupla noticia) {
        JPanel panelNoticia = new JPanel(new BorderLayout());
        panelNoticia.setBackground(nti.fondoPanel);

        JTextArea title = new JTextArea(noticia.getTitulo());
        title.setWrapStyleWord(true);
        title.setLineWrap(true);
        title.setOpaque(false);
        title.setEditable(false);
        title.setFocusable(false);
        title.setForeground(Color.WHITE);
        title.setFont(Fuentes.getBlack(14f));

        JPanel fuentePanel = new JPanel(new BorderLayout());
        fuentePanel.setBackground(nti.fondoPanel);
        final JLabel fuenteLabel = new JLabel(noticia.getFuente());
        fuenteLabel.setForeground(nti.bordeDorado);
        fuenteLabel.setFont(Fuentes.getBlack(12f));
        fuenteLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        fuenteLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                AudioManager.getInstance().playBotonSound();
                try {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(noticia.getUrl()));
                    }
                } catch (Exception ex) {
                    System.err.println("No se pudo abrir el enlace: " + ex.getMessage());
                }
            }
        });
        fuentePanel.add(fuenteLabel, BorderLayout.EAST);

        JPanel textoPanel = new JPanel();
        textoPanel.setLayout(new BoxLayout(textoPanel, BoxLayout.Y_AXIS));
        textoPanel.setBackground(nti.fondoPanel);
        textoPanel.add(title);
        textoPanel.add(Box.createVerticalStrut(3));
        textoPanel.add(fuentePanel);
        panelNoticia.add(textoPanel, BorderLayout.CENTER);

        return panelNoticia;
    }

    private void personalizarComboBox(JComboBox<String> cb) {
        cb.setBackground(nti.fondoPanel);
        cb.setForeground(nti.letra);
        cb.setFont(Fuentes.getBold(12f));
        cb.setPreferredSize(new Dimension(180, 30));
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cb.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(10, nti.bordeDorado, 2), new EmptyBorder(5, 10, 5, 5)));
        cb.setUI(new CustomComboBoxUI());
        cb.setRenderer(new CustomListRenderer());
    }

    private class CustomComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton("\u25BC");
            button.setBackground(nti.fondoPanel);
            button.setForeground(nti.letra);
            button.setBorder(new EmptyBorder(0, 5, 0, 10));
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return button;
        }
        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            g.setColor(nti.fondoPanel);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    private class CustomListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            list.setBackground(nti.fondoPanel);
            setBackground(isSelected ? nti.bordeDorado : nti.fondoPanel);
            setForeground(isSelected ? nti.fondoPanel : nti.letra);
            setBorder(new EmptyBorder(5, 10, 5, 10));
            return this;
        }
    }

    private ChartPanel crearGraficoPanel() {
        JFreeChart chart = ChartFactory.createXYLineChart("", "Fecha", "Precio", null, PlotOrientation.VERTICAL, false, false, false);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(nti.fondoPanel);
        
        // Ocultar bandas grises y mostrar cuadrícula sutil
        plot.setDomainTickBandPaint(nti.fondoPanel); 
        plot.setRangeTickBandPaint(nti.fondoPanel);
        
        Color colorRejilla = new Color(60, 60, 60);
        plot.setDomainGridlinesVisible(true); 
        plot.setDomainGridlinePaint(colorRejilla);
        plot.setRangeGridlinesVisible(true); 
        plot.setRangeGridlinePaint(colorRejilla);
        
        plot.setRenderer(new XYLineAndShapeRenderer(true, false));
        chart.setBackgroundPaint(nti.fondoPanel);
        plot.getDomainAxis().setTickLabelPaint(nti.letra);
        plot.getRangeAxis().setTickLabelPaint(nti.letra);
        plot.getDomainAxis().setLabelPaint(nti.letra);
        plot.getRangeAxis().setLabelPaint(nti.letra);

        ChartPanel panel = new ChartPanel(chart);
        panel.setPopupMenu(null);
        return panel;
    }

    private void cargarGrafico(String simbolo) {
        new Thread(() -> {
            Map<String, Vector<?>> datosGrafico = nti.lectura.obtenerUltimos7Dias(simbolo);
            Vector<Double> precios = (Vector<Double>) datosGrafico.get("precios");
            Vector<String> fechas = (Vector<String>) datosGrafico.get("fechas");

            SwingUtilities.invokeLater(() -> {
                XYPlot plot = chartPanel.getChart().getXYPlot();
                if (precios == null || precios.isEmpty() || precios.size() < 2) {
                    plot.setDataset(new XYSeriesCollection());
                    return;
                }
                plot.setDataset(crearDataset(precios));
                actualizarColoresRenderer((XYLineAndShapeRenderer) plot.getRenderer(), precios);

                NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                rangeAxis.setRange(Collections.min(precios) - 3.0, Collections.max(precios) + 3.0);

                NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
                domainAxis.setRange(-0.5, precios.size() - 0.5);
                if (fechas != null && !fechas.isEmpty()) {
                    domainAxis.setNumberFormatOverride(new java.text.NumberFormat() {
                        public StringBuffer format(double number, StringBuffer toAppendTo, java.text.FieldPosition pos) {
                            int i = (int) Math.round(number);
                            return (i >= 0 && i < fechas.size()) ? toAppendTo.append(fechas.get(i)) : toAppendTo;
                        }
                        public StringBuffer format(long number, StringBuffer toAppendTo, java.text.FieldPosition pos) { return format((double) number, toAppendTo, pos); }
                        public Number parse(String source, java.text.ParsePosition parsePosition) { return null; }
                    });
                }
            });
        }).start();
    }

    private XYDataset crearDataset(Vector<Double> precios) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (int i = 0; i < precios.size() - 1; i++) {
            XYSeries segmento = new XYSeries("S" + i);
            segmento.add(i, precios.get(i));
            segmento.add(i + 1, precios.get(i + 1));
            dataset.addSeries(segmento);
        }
        return dataset;
    }

    private void actualizarColoresRenderer(XYLineAndShapeRenderer renderer, Vector<Double> precios) {
        renderer.setBaseLinesVisible(true); 

        for (int i = 0; i < precios.size() - 1; i++) {
            renderer.setSeriesPaint(i, (precios.get(i + 1) > precios.get(i)) ? Color.decode("#54DC81") : Color.RED);
            renderer.setSeriesStroke(i, new BasicStroke(2.0f));
        }
        
        String estiloGuardado = nti.ent.estiloGrafica;
        if (estiloGuardado.equalsIgnoreCase("lineal")) {
            estiloGuardado = "Línea";
        }
        aplicarEstiloGrafica(estiloGuardado, false);
    }

    private JPanel crearPanelDatos() {
        JPanel datosPanel = new JPanel();
        datosPanel.setBorder(new RoundedBorder(20, nti.bordeDorado, 3));
        datosPanel.setBackground(nti.fondoPanel);
        datosPanel.setLayout(new GridLayout(6, 2, 15, 5));
        
        // Altura fija para el panel de datos
        Dimension fixedDatosHeight = new Dimension(600, 170); 
        datosPanel.setMinimumSize(fixedDatosHeight);
        datosPanel.setPreferredSize(fixedDatosHeight);
        datosPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        
        return datosPanel;
    }
}