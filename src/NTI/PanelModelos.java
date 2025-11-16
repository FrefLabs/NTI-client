package NTI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Vector;

/**
 * (NUEVA CLASE - BASADA EN TU CÓDIGO)
 * Encapsula todo el contenido de la pantalla "Modelos".
 */
public class PanelModelos extends JPanel {

    // --- Referencia a NTI y sus componentes ---
    private NTI nti;

    // --- Componentes de la UI (Tus variables) ---
    private JPanel panelListaModelos;
    private JPanel panelDetalleModelo;
    private CardLayout cardLayoutModelos;
    private JPanel panelModelosContenido;
    private JLabel lblFallbackRecomendados;

    private JLabel lblDetalle_Titulo;
    private JLabel lblDetalle_Descripcion;
    private JButton btnDetalle_Seleccionado;
    private JLabel lblDetalle_MSE, lblDetalle_RMSE, lblDetalle_MAE, lblDetalle_R2, lblDetalle_MaxError, lblDetalle_MinError, lblDetalle_Percentil90, lblDetalle_Precision;
    private JLabel lblDetalle_FechaIni, lblDetalle_FechaFin, lblDetalle_Arquitectura, lblDetalle_Funciones, lblDetalle_Tasa, lblDetalle_NMaxError, lblDetalle_Epocas;

    /**
     * Constructor de PanelModelos.
     */
    public PanelModelos(NTI nti) {
        // 1. Guardar Referencia
        this.nti = nti;

        // 2. Configurar este Panel (Tu layout original)
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(nti.fondo);
        this.setBorder(new EmptyBorder(30, 50, 30, 50));

        // 3. Construir la UI
        initUI();
    }

    /**
     * (TU CÓDIGO ORIGINAL) Construye la UI del panel de modelos.
     */
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
        JLabel lblIconLupa = new JLabel("Q");
        lblIconLupa.setFont(Fuentes.getBold(18f));
        lblIconLupa.setForeground(nti.letra);
        lblIconLupa.setBorder(new EmptyBorder(5, 15, 5, 0));
        panelBusqueda.add(lblIconLupa, BorderLayout.WEST);

        JTextField txtBuscarModelo = new JTextField();
        txtBuscarModelo.setBackground(nti.fondoPanel);
        txtBuscarModelo.setCaretColor(Color.WHITE);
        txtBuscarModelo.setBorder(new EmptyBorder(5, 0, 5, 5));
        addPlaceholderFocusListener(txtBuscarModelo, "Buscar modelos"); // (Llama al helper)
        txtBuscarModelo.addActionListener(e -> {
            String textoBusqueda = txtBuscarModelo.getText().trim();
            
            // No buscar si está vacío o sigue siendo el placeholder
            if (textoBusqueda.isEmpty() || textoBusqueda.equals("Buscar modelos")) {
                return;
            }
            
            // Ejecutar la búsqueda en un nuevo hilo
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
        lblFallbackRecomendados.setVisible(false);
        // (Se añade en cargarPanelModelos)

        JScrollPane scrollListaModelos = new JScrollPane(panelListaModelos);
        scrollListaModelos.setBorder(null);
        scrollListaModelos.getViewport().setBackground(nti.fondo);
        scrollListaModelos.setBackground(nti.fondo);
        scrollListaModelos.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollListaModelos.getVerticalScrollBar().setUnitIncrement(16);

        // --- Tarjeta 2: Crear Nuevo Modelo ---
        JPanel panelCrearModelo = crearPanelCrearModelo(cardLayoutModelos, panelModelosContenido);
        
        // --- Tarjeta 3: Detalle del Modelo ---
        panelDetalleModelo = crearPanelDetalleModelo();

        // --- Añadir tarjetas al CardLayout ---
        panelModelosContenido.add(scrollListaModelos, "LISTA");
        panelModelosContenido.add(panelCrearModelo, "CREAR");
        panelModelosContenido.add(panelDetalleModelo, "DETALLE");
        this.add(panelModelosContenido, BorderLayout.CENTER);

        // --- Acción del botón ---
        btnCrearModelo.addActionListener(e -> cardLayoutModelos.show(panelModelosContenido, "CREAR"));
    }

    /**
     * (NUEVO) Método público para ser llamado desde NTI.
     */
    public void mostrarListaYRecargar() {
        cardLayoutModelos.show(panelModelosContenido, "LISTA");
        cargarPanelModelos();
    }
    
    /**
     * (TU MÉTODO ORIGINAL) Carga los datos al iniciar.
     */
    public void cargarDatosIniciales() {
        // Este método carga los datos en el hilo principal, como en tu original
        cargarPanelModelos();
    }

    private void realizarBusqueda(String textoBusqueda) {
        // Mostrar la lista (por si acaso estábamos en "Crear" o "Detalle")
        cardLayoutModelos.show(panelModelosContenido, "LISTA");

        // Limpiar el panel y mostrar "Buscando..."
        panelListaModelos.removeAll();
        JLabel lblBuscando = new JLabel("Buscando modelos para '" + textoBusqueda + "'...");
        lblBuscando.setFont(Fuentes.getRegular(16f));
        lblBuscando.setForeground(nti.letra.darker());
        lblBuscando.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblBuscando.setBorder(new EmptyBorder(0, 5, 15, 0));
        panelListaModelos.add(lblBuscando);
        panelListaModelos.revalidate();
        panelListaModelos.repaint();

        // Hilo para la consulta a la BD
        new Thread(() -> {
            Vector<Map<String, Object>> resultados = nti.modelo.buscarModelos(textoBusqueda);

            // Actualizar la UI en el hilo de Swing
            SwingUtilities.invokeLater(() -> {
                panelListaModelos.removeAll(); // Limpiar el "Buscando..."

                // Título de Resultados
                JLabel lblTituloBusqueda = new JLabel("<html>Resultados para: <span style='color:" + nti.bordedoradoStr + ";'>" + textoBusqueda + "</span></html>");
                lblTituloBusqueda.setFont(Fuentes.getBlack(24f));
                lblTituloBusqueda.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblTituloBusqueda.setBorder(new EmptyBorder(0, 5, 15, 0));
                panelListaModelos.add(lblTituloBusqueda);

                if (resultados == null || resultados.isEmpty()) {
                    // Usar el Fallback con un mensaje nuevo
                    lblFallbackRecomendados.setText("No se encontraron modelos para '" + textoBusqueda + "'.");
                    lblFallbackRecomendados.setVisible(true);
                    panelListaModelos.add(lblFallbackRecomendados);
                } else {
                    lblFallbackRecomendados.setVisible(false);
                    // Re-utilizamos el helper que ya tenías
                    for (Map<String, Object> m : resultados) {
                        JPanel panelResultado = crearPanelModeloUnificado(m);
                        panelListaModelos.add(panelResultado);
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
                JLabel lblTituloSel = new JLabel("<html><span style='color:" + nti.letraStr + ";'>Modelo</span> <span style='color:" + nti.bordedoradoStr + ";'>seleccionado</span></html>");
                lblTituloSel.setFont(Fuentes.getBlack(24f));
                lblTituloSel.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblTituloSel.setBorder(new EmptyBorder(0, 5, 15, 0));
                panelListaModelos.add(lblTituloSel);

                if (sel != null) {
                    JPanel panelSel = crearPanelModeloUnificado(sel);
                    panelListaModelos.add(panelSel);
                } else {
                    JLabel lblErrorSel = new JLabel("No se pudo cargar el modelo seleccionado (ID: " + nti.modelo.getIDModeloSeleccionado() + ")");
                    lblErrorSel.setForeground(Color.RED);
                    panelListaModelos.add(lblErrorSel);
                }

                panelListaModelos.add(Box.createVerticalStrut(25));

                JLabel lblTituloRec = new JLabel("<html><span style='color:" + nti.letraStr + ";'>Modelos</span> <span style='color:" + nti.bordedoradoStr + ";'>recomendados</span></html>");
                lblTituloRec.setFont(Fuentes.getBlack(24f));
                lblTituloRec.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblTituloRec.setBorder(new EmptyBorder(0, 5, 15, 0));
                panelListaModelos.add(lblTituloRec);

                if (rec == null || rec.isEmpty()) {
                    lblFallbackRecomendados.setVisible(true);
                    panelListaModelos.add(lblFallbackRecomendados);
                } else {
                    for (Map<String, Object> m : rec) {
                        JPanel panelRec = crearPanelModeloUnificado(m);
                        panelListaModelos.add(panelRec);
                        panelListaModelos.add(Box.createVerticalStrut(15));
                    }
                }

                panelListaModelos.add(Box.createVerticalGlue());
                panelListaModelos.revalidate();
                panelListaModelos.repaint();
            });
        }).start();
    }

    /**
     * (TU MÉTODO ORIGINAL) Crea el panel de un solo modelo.
     */
    private JPanel crearPanelModeloUnificado(Map<String, Object> datos) {
        int idModelo = (datos.get("IDModelo") != null) ? (int) datos.get("IDModelo") : 0;
        String simbolo = (datos.get("Simbolo") != null) ? (String) datos.get("Simbolo") : "N/A";
        double precision = (datos.get("Precision") != null) ? (double) datos.get("Precision") : 0.0;
        String fechaIni = (datos.get("FechaIni") != null) ? datos.get("FechaIni").toString() : "N/A";
        String fechaFin = (datos.get("FechaFin") != null) ? datos.get("FechaFin").toString() : "N/A";
        double mae = 0.0;
        if (datos.get("PromedioError") != null) {
            mae = (double) datos.get("PromedioError");
        } else if (datos.get("MAE") != null) {
            mae = (double) datos.get("MAE");
        }
        String colorPrecisionVerde = "#54DC81";
        String grisStr = "#999999";

        Font fuenteBold14 = Fuentes.getBold(12f);
        String cssBold = String.format("font-family:'%s'; font-size:12px; font-weight:bold;",
                                        fuenteBold14.getFamily());
        Font fuenteReg16 = Fuentes.getRegular(12f);
        String cssRegular16 = String.format("font-family:'%s'; font-size:12px; font-weight:300;",
                                        fuenteReg16.getFamily());
        Font fuenteReg14 = Fuentes.getRegular(12f);
        String cssRegular14 = String.format("font-family:'%s'; font-size:12px; font-weight:300;",
                                        fuenteReg14.getFamily());

        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(nti.fondoPanel);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setOpaque(false);
        panelIzquierdo.setAlignmentY(Component.TOP_ALIGNMENT);

        JLabel lblNombreModelo = new JLabel(simbolo);
        lblNombreModelo.setFont(Fuentes.getBlack(32f));
        lblNombreModelo.setForeground(Color.WHITE);
        lblNombreModelo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelIzquierdo.add(lblNombreModelo);

        JLabel lblIdModelo = new JLabel(String.format(
            "<html><span style=\"%s color:%s;\">Modelo </span><span style=\"%s color:%s;\">#%d</span></html>",
            cssBold, nti.letraStr, cssBold, nti.bordedoradoStr, idModelo
        ));
        lblIdModelo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelIzquierdo.add(lblIdModelo);
        panel.add(panelIzquierdo, BorderLayout.WEST);

        JPanel panelDerecho = new JPanel(new GridLayout(3, 1, 0, 0));
        panelDerecho.setOpaque(false);

        JLabel lblPrecision = new JLabel(String.format(
            "<html><span style=\"%s color:%s;\">Precision: </span><span style=\"%s color:%s;\">%s</span></html>",
            cssRegular16, nti.letraStr, cssRegular16, colorPrecisionVerde, nti.dfPercent.format(precision)
        ));
        lblPrecision.setHorizontalAlignment(SwingConstants.RIGHT);
        panelDerecho.add(lblPrecision);

        JLabel lblPromError = new JLabel(String.format(
            "<html><span style=\"%s color:%s;\">Promedio de error: %s</span></html>",
            cssRegular14, nti.letraStr, nti.maeFormat.format(mae)
        ));
        lblPromError.setHorizontalAlignment(SwingConstants.RIGHT);
        panelDerecho.add(lblPromError);

        JLabel lblRango = new JLabel(String.format(
            "<html><span style=\"%s color:%s;\">Rango de entrenamiento: </span>" +
            "<span style=\"%s color:%s;\">%s</span>" +
            "<span style=\"%s color:%s;\"> - </span>" +
            "<span style=\"%s color:%s;\">%s</span></html>",
            cssRegular14, nti.letraStr,
            cssRegular14, nti.bordedoradoStr, fechaIni,
            cssRegular14, grisStr,
            cssRegular14, nti.bordedoradoStr, fechaFin
        ));
        lblRango.setHorizontalAlignment(SwingConstants.RIGHT);
        panelDerecho.add(lblRango);
        panel.add(panelDerecho, BorderLayout.EAST);

        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mostrarDetalleModelo(idModelo);
            }
        });
        return panel;
    }

    /**
     * (TU MÉTODO ORIGINAL) Crea el panel para "Crear Modelo".
     */
    private JPanel crearPanelCrearModelo(CardLayout cardLayout, JPanel cardPanel) {
        JPanel panel = new JPanel(new BorderLayout(0, 30));
        panel.setBackground(nti.fondoPanel);
        panel.setBorder(BorderFactory.createCompoundBorder(
                     new RoundedBorder(10, nti.bordeDorado, 3),
                     new EmptyBorder(20, 10, 20, 10)
                 ));

        JLabel lblTituloCrear = new JLabel("<html>Crear nuevo <span style='color:" + nti.bordedoradoStr + ";'>modelo</span></html>");
        lblTituloCrear.setFont(Fuentes.getBlack(24f));
        lblTituloCrear.setForeground(nti.letra);
        lblTituloCrear.setBorder(new EmptyBorder(0, 5, 15, 0));
        panel.add(lblTituloCrear, BorderLayout.NORTH);

        JPanel panelForm = new JPanel();
        panelForm.setLayout(new BoxLayout(panelForm, BoxLayout.X_AXIS));
        panelForm.setOpaque(false);
        panelForm.setBorder(new EmptyBorder(0, 15, 0, 15));

        JPanel panelLabels = new JPanel();
        panelLabels.setLayout(new BoxLayout(panelLabels, BoxLayout.Y_AXIS));
        panelLabels.setOpaque(false);
        panelLabels.setAlignmentY(Component.TOP_ALIGNMENT);

        JPanel panelTextFields = new JPanel();
        panelTextFields.setLayout(new BoxLayout(panelTextFields, BoxLayout.Y_AXIS));
        panelTextFields.setOpaque(false);
        panelTextFields.setAlignmentY(Component.TOP_ALIGNMENT);
        
        String[] labels = {"Acción", "Fecha inicio", "Fecha fin", "Arquitectura", "Funciones", "Learning Rate", "Max Error", "Max Iter"};
        String[] placeholders = {"Símbolo de la acción", "Fecha de inicio del dataset", "Fecha de finalización del dataset",
            "Capas y neuronas de la red neuronal", "Funciones de las distintas capas",
            "Tasa de aprendizaje de la red neuronal", "Máximo número de error permitido",
            "Máximo número de épocas permitidas"};
        
        int filaAltura = 45;
        Dimension labelSize = new Dimension(200, filaAltura);
        Dimension textFieldSize = new Dimension(300, filaAltura);

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setFont(Fuentes.getBold(18f));
            label.setForeground(nti.letra);
            
            JPanel labelWrapper = new JPanel(new BorderLayout());
            labelWrapper.setOpaque(false);
            labelWrapper.setPreferredSize(labelSize);
            labelWrapper.setMinimumSize(labelSize);
            labelWrapper.setMaximumSize(labelSize);
            labelWrapper.setAlignmentY(Component.TOP_ALIGNMENT);
            labelWrapper.add(label, BorderLayout.CENTER);
            panelLabels.add(labelWrapper);

            JTextField textField = new JTextField();
            textField.setBackground(nti.fondoPanel);
            textField.setCaretColor(nti.letra);
            textField.setFont(Fuentes.getRegular(14f));
            textField.setPreferredSize(textFieldSize);
            textField.setMinimumSize(textFieldSize);
            textField.setMaximumSize(textFieldSize);
            textField.setAlignmentY(Component.TOP_ALIGNMENT);
            textField.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(10, nti.bordeDorado, 2),
                    new EmptyBorder(0, 10, 0, 10)
            ));
            
            addPlaceholderFocusListener(textField, placeholders[i]); // (Llama al helper)
            panelTextFields.add(textField);
            
            if (i < labels.length - 1) {
                panelLabels.add(Box.createVerticalStrut(5));
                panelTextFields.add(Box.createVerticalStrut(5));
            }
        }
        
        panelForm.add(panelLabels);
        panelForm.add(Box.createHorizontalStrut(20));
        panelForm.add(panelTextFields);
        panelForm.add(Box.createHorizontalGlue());
        panel.add(panelForm, BorderLayout.CENTER);

        JPanel panelBotonesInferiores = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        panelBotonesInferiores.setOpaque(false);

        JButton btnVolverAtras = new JButton("Volver Atras");
        btnVolverAtras.setBackground(nti.fondoPanel);
        btnVolverAtras.setForeground(nti.bordeDorado);
        btnVolverAtras.setFont(Fuentes.getBold(16f));
        btnVolverAtras.setFocusPainted(false);
        btnVolverAtras.setPreferredSize(new Dimension(200, 45));
        btnVolverAtras.setBorder(new RoundedBorder(15, nti.bordeDorado, 2));

        JButton btnSolicitar = new JButton("Solicitar");
        btnSolicitar.setBackground(nti.fondoPanel);
        btnSolicitar.setForeground(nti.bordeDorado);
        btnSolicitar.setFont(Fuentes.getBold(16f));
        btnSolicitar.setFocusPainted(false);
        btnSolicitar.setPreferredSize(new Dimension(200, 45));
        btnSolicitar.setBorder(new RoundedBorder(15, nti.bordeDorado, 2));

        btnVolverAtras.addActionListener(e -> {
            cardLayout.show(cardPanel, "LISTA");
        });

        btnSolicitar.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Funcionalidad 'Solicitar' no implementada.");
        });

        panelBotonesInferiores.add(btnVolverAtras);
        panelBotonesInferiores.add(btnSolicitar);
        panel.add(panelBotonesInferiores, BorderLayout.SOUTH);

        return panel;
    }
    
    /**
     * (TU MÉTODO ORIGINAL) Muestra la tarjeta de "Detalle".
     */
    private void mostrarDetalleModelo(int idModelo) {
        cardLayoutModelos.show(panelModelosContenido, "DETALLE");
        lblDetalle_Titulo.setText("Cargando Modelo #" + idModelo + "...");
        lblDetalle_Descripcion.setText("Obteniendo datos de la base de datos...");
        
        new Thread(() -> {
            Map<String, Object> d = nti.modelo.obtenerDetalles(idModelo);

            SwingUtilities.invokeLater(() -> {
                if (d == null) {
                    lblDetalle_Titulo.setText("Error al cargar Modelo #" + idModelo);
                    lblDetalle_Descripcion.setText("El modelo no se encontró en la base de datos.");
                    return;
                }
                
                lblDetalle_Titulo.setText("Modelo #" + d.get("IDModelo") + " - " + d.get("Simbolo"));
                lblDetalle_Descripcion.setText("<html>" + d.get("E_Descripcion") + "</html>");
                
                if ((int)d.get("IDModelo") == nti.modelo.getIDModeloSeleccionado()) {
                    btnDetalle_Seleccionado.setText("Seleccionado");
                    btnDetalle_Seleccionado.setEnabled(false);
                } else {
                    btnDetalle_Seleccionado.setText("Seleccionar");
                    btnDetalle_Seleccionado.setEnabled(true);
                }
                btnDetalle_Seleccionado.putClientProperty("IDModelo", d.get("IDModelo"));
                
                lblDetalle_MSE.setText("MSE: " + nti.df8.format(d.get("MSE")));
                lblDetalle_RMSE.setText("RMSE: " + nti.df8.format(d.get("RMSE")));
                lblDetalle_MAE.setText("MAE: " + nti.df8.format(d.get("MAE")));
                lblDetalle_R2.setText("R2: " + nti.df8.format(d.get("R2")));
                lblDetalle_MaxError.setText("MaxError: " + nti.df8.format(d.get("MaxError")));
                lblDetalle_MinError.setText("MinError: " + nti.df8.format(d.get("MinError")));
                lblDetalle_Percentil90.setText("Percentil 90: " + nti.df8.format(d.get("Percentil90")));
                lblDetalle_Precision.setText("Precisión: " + nti.dfPercent.format(d.get("Precision")));
                
                lblDetalle_FechaIni.setText("Fecha inicio: " + d.get("FechaIni").toString());
                lblDetalle_FechaFin.setText("Fecha fin: " + d.get("FechaFin").toString());
                lblDetalle_Arquitectura.setText("Arquitectura: " + d.get("Arquitectura").toString());
                lblDetalle_Funciones.setText("Funciones: " + d.get("Funciones").toString());
                lblDetalle_Tasa.setText("Tasa de aprendizaje: " + d.get("TasaAprendizaje").toString());
                lblDetalle_NMaxError.setText("N° max. de error: " + d.get("NMaxError").toString());
                lblDetalle_Epocas.setText("N° max. de épocas: " + d.get("Epocas").toString());
            });
        }).start();
    }
    
    /**
     * (TU MÉTODO ORIGINAL) Crea la estructura del panel de "Detalle".
     */
    private JPanel crearPanelDetalleModelo() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JPanel panelSuperior = new JPanel(new BorderLayout(15, 10));
        panelSuperior.setBackground(nti.fondoPanel);
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(15, 15, 15, 15)
        ));
        
        JPanel panelTituloDesc = new JPanel();
        panelTituloDesc.setLayout(new BoxLayout(panelTituloDesc, BoxLayout.Y_AXIS));
        panelTituloDesc.setOpaque(false);
        
        lblDetalle_Titulo = new JLabel("Cargando...");
        lblDetalle_Titulo.setFont(Fuentes.getBlack(26f));
        lblDetalle_Titulo.setForeground(Color.WHITE);
        panelTituloDesc.add(lblDetalle_Titulo);
        
        lblDetalle_Descripcion = new JLabel("Cargando descripción...");
        lblDetalle_Descripcion.setFont(Fuentes.getRegular(14f));
        lblDetalle_Descripcion.setForeground(Color.LIGHT_GRAY);
        panelTituloDesc.add(lblDetalle_Descripcion);
        
        panelSuperior.add(panelTituloDesc, BorderLayout.CENTER);
        
        btnDetalle_Seleccionado = new JButton("Seleccionar");
        btnDetalle_Seleccionado.setBackground(nti.fondoPanel);
        btnDetalle_Seleccionado.setForeground(nti.bordeDorado);
        btnDetalle_Seleccionado.setFont(Fuentes.getBold(14f));
        btnDetalle_Seleccionado.setBorder(new RoundedBorder(15, nti.bordeDorado, 2));
        btnDetalle_Seleccionado.setFocusPainted(false);
        
        btnDetalle_Seleccionado.addActionListener(e -> {
            int id = (int) btnDetalle_Seleccionado.getClientProperty("IDModelo");
            nti.modelo.setIDModeloSeleccionado(id);
            cardLayoutModelos.show(panelModelosContenido, "LISTA");
            cargarPanelModelos();
        });
        
        panelSuperior.add(btnDetalle_Seleccionado, BorderLayout.EAST);
        panel.add(panelSuperior, BorderLayout.NORTH);

        JPanel panelInferior = new JPanel(new GridLayout(1, 2, 20, 0)); // (Corregido)
        panelInferior.setBackground(nti.fondoPanel);
        panelInferior.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(20, 25, 20, 25)
        ));
        
        JPanel colResultados = new JPanel();
        colResultados.setLayout(new BoxLayout(colResultados, BoxLayout.Y_AXIS));
        colResultados.setOpaque(false);
        
        JLabel lblTituloRes = new JLabel("Resultados del Modelo");
        lblTituloRes.setFont(Fuentes.getBlack(22f));
        lblTituloRes.setForeground(nti.bordeDorado);
        colResultados.add(lblTituloRes);
        colResultados.add(Box.createVerticalStrut(20));
        
        lblDetalle_MSE = new JLabel("MSE: ...");
        lblDetalle_RMSE = new JLabel("RMSE: ...");
        lblDetalle_MAE = new JLabel("MAE: ...");
        lblDetalle_R2 = new JLabel("R2: ...");
        lblDetalle_MaxError = new JLabel("MaxError: ...");
        lblDetalle_MinError = new JLabel("MinError: ...");
        lblDetalle_Percentil90 = new JLabel("Percentil 90: ...");
        lblDetalle_Precision = new JLabel("Precisión: ...");
        lblDetalle_Precision.setFont(Fuentes.getBold(18f));
        
        for (JLabel l : new JLabel[]{lblDetalle_MSE, lblDetalle_RMSE, lblDetalle_MAE, lblDetalle_R2, lblDetalle_MaxError, lblDetalle_MinError, lblDetalle_Percentil90, lblDetalle_Precision}) {
            l.setFont(l.getFont().deriveFont(16f));
            l.setForeground(nti.letra);
            l.setBorder(new EmptyBorder(0, 0, 10, 0));
            colResultados.add(l);
        }
        colResultados.add(Box.createVerticalGlue());
        panelInferior.add(colResultados);

        JPanel colParams = new JPanel();
        colParams.setLayout(new BoxLayout(colParams, BoxLayout.Y_AXIS));
        colParams.setOpaque(false);
        
        JLabel lblTituloParams = new JLabel("Parámetros del Modelo");
        lblTituloParams.setFont(Fuentes.getBlack(22f));
        lblTituloParams.setForeground(nti.bordeDorado);
        colParams.add(lblTituloParams);
        colParams.add(Box.createVerticalStrut(20));
        
        lblDetalle_FechaIni = new JLabel("Fecha inicio: ...");
        lblDetalle_FechaFin = new JLabel("Fecha fin: ...");
        lblDetalle_Arquitectura = new JLabel("Arquitectura: ...");
        lblDetalle_Funciones = new JLabel("Funciones: ...");
        lblDetalle_Tasa = new JLabel("Tasa de aprendizaje: ...");
        lblDetalle_NMaxError = new JLabel("N° max. de error: ...");
        lblDetalle_Epocas = new JLabel("N° max. de épocas: ...");
        
        for (JLabel l : new JLabel[]{lblDetalle_FechaIni, lblDetalle_FechaFin, lblDetalle_Arquitectura, lblDetalle_Funciones, lblDetalle_Tasa, lblDetalle_NMaxError, lblDetalle_Epocas}) {
            l.setFont(l.getFont().deriveFont(16f));
            l.setForeground(nti.letra);
            l.setBorder(new EmptyBorder(0, 0, 10, 0));
            colParams.add(l);
        }
        colParams.add(Box.createVerticalGlue());
        panelInferior.add(colParams);
        
        panel.add(panelInferior, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * (TU MÉTODO ORIGINAL) Helper para placeholders.
     */
    private void addPlaceholderFocusListener(JTextField textField, String placeholder) {
        Color placeholderColor = Color.LIGHT_GRAY;
        Color textColor = nti.letra;

        textField.setText(placeholder);
        textField.setForeground(placeholderColor);

        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(textColor);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(placeholderColor);
                }
            }
        });
    }
}