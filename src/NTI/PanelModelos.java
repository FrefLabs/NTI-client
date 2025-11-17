package NTI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.util.Date; // (Importar java.util.Date)
import java.util.Map;
import java.util.Vector;

/**
 * (CLASE MODIFICADA)
 * CORRECCIÓN: Alineada la columna derecha (Parámetros) al NORTE
 * y reducido el padding.
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

    // --- Componentes de Detalle (MODIFICADO) ---
    private JLabel lblDetalle_Titulo;
    private JTextArea txtDetalle_Features;
    private JButton btnDetalle_Seleccionado;
    
    // (Labels de datos históricos)
    private JLabel lblDetalle_MSE, lblDetalle_RMSE, lblDetalle_MAE, lblDetalle_R2, lblDetalle_MaxError, lblDetalle_MinError, lblDetalle_Percentil90, lblDetalle_Precision;
    private JLabel lblDetalle_FechaIni, lblDetalle_FechaFin, lblDetalle_Arquitectura, lblDetalle_Funciones, lblDetalle_Tasa, lblDetalle_NMaxError, lblDetalle_Epocas;

    // (Labels de datos actuales)
    private JLabel lblDetalle_Historico_Titulo; // (NUEVO)
    private JLabel lblDetalle_Actual_Titulo;
    private JLabel lblDetalle_Actual_MAE;
    private JLabel lblDetalle_Actual_Precision;
    private JLabel lblDetalle_Actual_Tendencia;


    public PanelModelos(NTI nti) {
        this.nti = nti;
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(nti.fondo);
        // (MODIFICADO) Padding inferior reducido
        this.setBorder(new EmptyBorder(30, 50, 10, 50)); 
        initUI();
    }

    private void initUI() {
        
        //--------------------------------
        // PANEL SUPERIOR (BARRA DE BUSQUEDA Y BOTON DE CREAR MODELO)
        //--------------------------------
        JPanel barraSuperior = new JPanel(new GridBagLayout());
        // ... (Este código no cambia) ...
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
        JLabel lblIconLupa = new JLabel("Q"); // (Placeholder)
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
        // ... (Este código no cambia) ...
        panelListaModelos.setLayout(new BoxLayout(panelListaModelos, BoxLayout.Y_AXIS));
        panelListaModelos.setBackground(nti.fondo);
        panelListaModelos.setBorder(new EmptyBorder(10, 0, 10, 0));
        lblFallbackRecomendados = new JLabel("No hay modelos recomendados disponibles.");
        lblFallbackRecomendados.setFont(Fuentes.getRegular(16f));
        lblFallbackRecomendados.setForeground(nti.letra.darker());
        lblFallbackRecomendados.setVisible(false);
        JScrollPane scrollListaModelos = new JScrollPane(panelListaModelos);
        scrollListaModelos.setBorder(null);
        scrollListaModelos.getViewport().setBackground(nti.fondo);
        scrollListaModelos.setBackground(nti.fondo);
        scrollListaModelos.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollListaModelos.getVerticalScrollBar().setUnitIncrement(16);

        // --- Tarjeta 2: Crear Nuevo Modelo ---
        JPanel panelCrearModelo = crearPanelCrearModelo(cardLayoutModelos, panelModelosContenido);
        
        // --- Tarjeta 3: Detalle del Modelo ---
        panelDetalleModelo = crearPanelDetalleModelo(); // (Llama al método modificado)

        // --- Añadir tarjetas al CardLayout ---
        panelModelosContenido.add(scrollListaModelos, "LISTA");
        panelModelosContenido.add(panelCrearModelo, "CREAR");
        panelModelosContenido.add(panelDetalleModelo, "DETALLE");
        this.add(panelModelosContenido, BorderLayout.CENTER);

        // --- Acción del botón ---
        btnCrearModelo.addActionListener(e -> cardLayoutModelos.show(panelModelosContenido, "CREAR"));
    }

    public void mostrarListaYRecargar() {
        cardLayoutModelos.show(panelModelosContenido, "LISTA");
        cargarPanelModelos();
    }

    public void cargarDatosIniciales() {
        cargarPanelModelos();
    }

    private void realizarBusqueda(String textoBusqueda) {
        // ... (Tu código para este helper no cambia) ...
        cardLayoutModelos.show(panelModelosContenido, "LISTA");
        panelListaModelos.removeAll();
        JLabel lblBuscando = new JLabel("Buscando modelos para '" + textoBusqueda + "'...");
        lblBuscando.setFont(Fuentes.getRegular(16f));
        lblBuscando.setForeground(nti.letra.darker());
        lblBuscando.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblBuscando.setBorder(new EmptyBorder(0, 5, 15, 0));
        panelListaModelos.add(lblBuscando);
        panelListaModelos.revalidate();
        panelListaModelos.repaint();
        new Thread(() -> {
            Vector<Map<String, Object>> resultados = nti.modelo.buscarModelos(textoBusqueda);
            SwingUtilities.invokeLater(() -> {
                panelListaModelos.removeAll();
                JLabel lblTituloBusqueda = new JLabel("<html>Resultados para: <span style='color:" + nti.bordedoradoStr + ";'>" + textoBusqueda + "</span></html>");
                lblTituloBusqueda.setFont(Fuentes.getBlack(24f));
                lblTituloBusqueda.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblTituloBusqueda.setBorder(new EmptyBorder(0, 5, 15, 0));
                panelListaModelos.add(lblTituloBusqueda);
                if (resultados == null || resultados.isEmpty()) {
                    lblFallbackRecomendados.setText("No se encontraron modelos para '" + textoBusqueda + "'.");
                    lblFallbackRecomendados.setVisible(true);
                    panelListaModelos.add(lblFallbackRecomendados);
                } else {
                    lblFallbackRecomendados.setVisible(false);
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
        // ... (Tu código para este helper no cambia) ...
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

    private JPanel crearPanelModeloUnificado(Map<String, Object> datos) {
        // ... (Tu código para este helper no cambia) ...
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

    private JPanel crearPanelCrearModelo(CardLayout cardLayout, JPanel cardPanel) {
        // ... (Tu código para este helper no cambia) ...
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
     * (MODIFICADO)
     * Muestra la tarjeta de "Detalle" y carga los datos de ambos SPs.
     */
    private void mostrarDetalleModelo(int idModelo) {
        cardLayoutModelos.show(panelModelosContenido, "DETALLE");
        
        // --- Resetear campos a "cargando" ---
        lblDetalle_Titulo.setText("Cargando Modelo #" + idModelo + "...");
        txtDetalle_Features.setText("Obteniendo datos de la base de datos...");
        
        // (Resetear labels históricos)
        lblDetalle_MSE.setText("...");
        lblDetalle_RMSE.setText("...");
        lblDetalle_MAE.setText("...");
        lblDetalle_R2.setText("...");
        lblDetalle_MaxError.setText("...");
        lblDetalle_MinError.setText("...");
        lblDetalle_Percentil90.setText("...");
        lblDetalle_Precision.setText("...");
        lblDetalle_FechaIni.setText("...");
        lblDetalle_FechaFin.setText("...");
        lblDetalle_Arquitectura.setText("...");
        lblDetalle_Funciones.setText("...");
        lblDetalle_Tasa.setText("...");
        lblDetalle_NMaxError.setText("...");
        lblDetalle_Epocas.setText("...");
        
        // (Resetear labels actuales)
        lblDetalle_Historico_Titulo.setText("Resultados desde ... a ...");
        lblDetalle_Actual_Titulo.setText("Resultados desde ... a hoy");
        lblDetalle_Actual_MAE.setText("...");
        lblDetalle_Actual_Precision.setText("...");
        lblDetalle_Actual_Tendencia.setText("...");

        // --- Hilo para cargar datos ---
        new Thread(() -> {
            // (Llamada 1: Datos Históricos y Features)
            Map<String, Object> d = nti.modelo.obtenerDetalles(idModelo);
            
            // (Llamada 2: Datos Actuales)
            Map<String, Object> dActual = nti.lectura.obtenerDatosActualesModelo(idModelo, new Date());

            SwingUtilities.invokeLater(() -> {
                // --- Cargar Datos Históricos (Panel Superior y Central) ---
                if (d == null) {
                    lblDetalle_Titulo.setText("Error al cargar Modelo #" + idModelo);
                    txtDetalle_Features.setText("El modelo no se encontró en la base de datos.");
                    return;
                }
                
                // (Panel Superior)
                // (MODIFICADO) Aplicar formato de color al título
                lblDetalle_Titulo.setText("<html><span style='color:white;'>Modelo #" + d.get("IDModelo") + " - </span><span style='color:" + nti.bordedoradoStr + ";'>" + d.get("Simbolo") + "</span></html>");
                txtDetalle_Features.setText((String) d.get("Features"));
                
                if ((int)d.get("IDModelo") == nti.modelo.getIDModeloSeleccionado()) {
                    btnDetalle_Seleccionado.setText("Seleccionado");
                    btnDetalle_Seleccionado.setEnabled(false);
                } else {
                    btnDetalle_Seleccionado.setText("Seleccionar");
                    btnDetalle_Seleccionado.setEnabled(true);
                }
                btnDetalle_Seleccionado.putClientProperty("IDModelo", d.get("IDModelo"));
                
                // (Setear labels históricos - Columna Izquierda)
                lblDetalle_MSE.setText(nti.df8.format(d.get("MSE")));
                lblDetalle_RMSE.setText(nti.df8.format(d.get("RMSE")));
                lblDetalle_MAE.setText(nti.df8.format(d.get("MAE")));
                lblDetalle_R2.setText(nti.df8.format(d.get("R2")));
                lblDetalle_MaxError.setText(nti.df8.format(d.get("MaxError")));
                lblDetalle_MinError.setText(nti.df8.format(d.get("MinError")));
                lblDetalle_Percentil90.setText(nti.df8.format(d.get("Percentil90")));
                lblDetalle_Precision.setText(nti.dfPercent.format(d.get("Precision")));
                
                // (Setear labels de parámetros - Columna Derecha)
                String fechaFin = d.get("FechaFin").toString();
                String fechaIni = d.get("FechaIni").toString();
                lblDetalle_FechaIni.setText(fechaIni);
                lblDetalle_FechaFin.setText(fechaFin);
                lblDetalle_Arquitectura.setText(d.get("Arquitectura").toString());
                lblDetalle_Funciones.setText(d.get("Funciones").toString());
                lblDetalle_Tasa.setText(d.get("TasaAprendizaje").toString());
                lblDetalle_NMaxError.setText(d.get("NMaxError").toString());
                lblDetalle_Epocas.setText(d.get("Epocas").toString());

                // (MODIFICADO) Titulo de resultados históricos AHORA TIENE LAS FECHAS
                lblDetalle_Historico_Titulo.setText("Resultados desde " + fechaIni + " a " + fechaFin);

                // --- Cargar Datos Actuales (Columna Izquierda, abajo) ---
                // (MODIFICADO) Formato de título
                lblDetalle_Actual_Titulo.setText("Resultados desde " + fechaFin + " a hoy");
                
                if (dActual != null) {
                    lblDetalle_Actual_MAE.setText(nti.maeFormat.format(dActual.get("MAE_A")));
                    lblDetalle_Actual_Precision.setText(nti.dfPercent.format(dActual.get("Precision")));
                    lblDetalle_Actual_Tendencia.setText(nti.dfPercent.format(dActual.get("TENDENCIA")));
                } else {
                    lblDetalle_Actual_MAE.setText("N/A");
                    lblDetalle_Actual_Precision.setText("N/A");
                    lblDetalle_Actual_Tendencia.setText("N/A");
                }
            });
        }).start();
    }

    /**
     * (MODIFICADO) Helper para crear las filas de datos con el formato
     * [ETIQUETA EN BLANCO (Oeste)] [DATO EN DORADO (Este)]
     * (Como en la imagen image_1b2c42.png)
     */
    private JPanel crearPanelDato(String etiqueta, JLabel labelValor) {
        JPanel panel = new JPanel(new BorderLayout(10, 0)); // (10px gap)
        panel.setOpaque(false);
        // (MODIFICADO) Aumentar la altura máxima y restaurar fuente
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25)); 
        
        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setFont(Fuentes.getRegular(14f)); // (Restaurar fuente)
        lblEtiqueta.setForeground(nti.letra); // Etiqueta en BLANCO
        panel.add(lblEtiqueta, BorderLayout.WEST); // (Alineado a la izquierda)
        
        // El labelValor (lblDetalle_MSE, etc.) se pasa como argumento
        labelValor.setFont(Fuentes.getBold(14f)); // (Restaurar fuente)
        labelValor.setForeground(nti.bordeDorado); // Datos en DORADO
        labelValor.setHorizontalAlignment(SwingConstants.RIGHT); // (Alineado a la derecha)
        panel.add(labelValor, BorderLayout.EAST); // (Alineado a la derecha)
        
        return panel;
    }


    /**
     * (RE-ESCRITO)
     * Crea la estructura del panel de "Detalle" con el nuevo diseño
     * usando BorderLayout y GridBagLayout para asegurar la alineación.
     */
    private JPanel crearPanelDetalleModelo() {
        // (Panel principal de la tarjeta "DETALLE")
        // (MODIFICADO) Gap vertical reducido a 10
        JPanel panel = new JPanel(new BorderLayout(0, 10)); 
        panel.setOpaque(false);
        // (MODIFICADO) Padding inferior del panel principal reducido a 0
        panel.setBorder(new EmptyBorder(10, 0, 0, 0)); 

        // --- 1. PANEL SUPERIOR (Título, Features, Botón) ---
        // (CORREGIDO) Usando BorderLayout como se solicitó
        JPanel panelSuperior = new JPanel(new BorderLayout(25, 10)); // (Gap horizontal 25px)
        panelSuperior.setBackground(nti.fondoPanel);
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(15, 20, 15, 20) // (padding interno)
        ));
        
        // (A) Contenedor para el título y features (CENTER)
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setOpaque(false);
        
        lblDetalle_Titulo = new JLabel("Cargando...");
        lblDetalle_Titulo.setFont(Fuentes.getBlack(26f));
        lblDetalle_Titulo.setForeground(Color.WHITE); 
        lblDetalle_Titulo.setAlignmentX(Component.LEFT_ALIGNMENT); // (Alinear a la izquierda)
        panelInfo.add(lblDetalle_Titulo);

        // (Línea dorada)
        panelInfo.add(Box.createVerticalStrut(5));
        JPanel linea = new JPanel();
        linea.setBackground(nti.bordeDorado);
        linea.setPreferredSize(new Dimension(100, 2));
        linea.setMaximumSize(new Dimension(100, 2));
        linea.setAlignmentX(Component.LEFT_ALIGNMENT); // (Alinear a la izquierda)
        panelInfo.add(linea);

        panelInfo.add(Box.createVerticalStrut(10)); // (Espacio)

        // (Título Features)
        JLabel lblTituloFeatures = new JLabel("FEATURES INCLUIDAS");
        lblTituloFeatures.setFont(Fuentes.getBlack(18f));
        lblTituloFeatures.setForeground(nti.letra); // (Color blanco)
        lblTituloFeatures.setAlignmentX(Component.LEFT_ALIGNMENT); // (Alinear a la izquierda)
        panelInfo.add(lblTituloFeatures);
        
        panelInfo.add(Box.createVerticalStrut(5)); // (Espacio)

        // (Features JTextArea)
        txtDetalle_Features = new JTextArea("Cargando features...");
        txtDetalle_Features.setFont(Fuentes.getRegular(11f));
        txtDetalle_Features.setForeground(Color.LIGHT_GRAY);
        txtDetalle_Features.setOpaque(false);
        txtDetalle_Features.setEditable(false);
        txtDetalle_Features.setWrapStyleWord(true);
        txtDetalle_Features.setLineWrap(true);
        txtDetalle_Features.setAlignmentX(Component.LEFT_ALIGNMENT); // (Alinear a la izquierda)
        
        // (Scroll para Features)
        JScrollPane scrollFeatures = new JScrollPane(txtDetalle_Features);
        scrollFeatures.setOpaque(false);
        scrollFeatures.getViewport().setOpaque(false);
        scrollFeatures.setBorder(null);
        scrollFeatures.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panelInfo.add(scrollFeatures);
        
        panelSuperior.add(panelInfo, BorderLayout.CENTER);
        
        // (B) Contenedor para Botón Seleccionar (EAST)
        JPanel panelBotonSel = new JPanel(new GridBagLayout());
        panelBotonSel.setOpaque(false);
        btnDetalle_Seleccionado = new JButton("Seleccionar");
        btnDetalle_Seleccionado.setBackground(nti.fondoPanel);
        btnDetalle_Seleccionado.setForeground(nti.bordeDorado);
        btnDetalle_Seleccionado.setFont(Fuentes.getBold(14f));
        btnDetalle_Seleccionado.setBorder(new RoundedBorder(15, nti.bordeDorado, 2));
        btnDetalle_Seleccionado.setFocusPainted(false);
        btnDetalle_Seleccionado.setPreferredSize(new Dimension(140, 40)); 
        panelBotonSel.add(btnDetalle_Seleccionado); // (Centrado por defecto)

        btnDetalle_Seleccionado.addActionListener(e -> {
            // (Esta lógica de guardado ya es correcta)
            int id = (int) btnDetalle_Seleccionado.getClientProperty("IDModelo");
            String nuevoSimbolo = nti.lectura.obtenerSimboloPorIDModelo(id);
            nti.modelo.setIDModeloSeleccionado(id);
            nti.accion.simbolo = nuevoSimbolo;
            String[] config = nti.ent.conseguirConfig();
            String moneda = config[0];
            boolean sfx = Boolean.parseBoolean(config[1]);
            boolean rdr = Boolean.parseBoolean(config[2]);
            String estilo = config[4];
            nti.ent.enviarNConfig(moneda, sfx, rdr, id, estilo); 
            JOptionPane.showMessageDialog(this,
                    "Modelo #" + id + " (" + nuevoSimbolo + ") seleccionado.",
                    "Modelo Actualizado",
                    JOptionPane.INFORMATION_MESSAGE);
            cardLayoutModelos.show(panelModelosContenido, "LISTA");
            cargarPanelModelos(); 
            nti.panelInicio.cargarDatosIniciales();
        });
        
        panelSuperior.add(panelBotonSel, BorderLayout.EAST);
        panel.add(panelSuperior, BorderLayout.NORTH); // (Panel superior listo)

        // --- 2. PANEL CENTRAL (Resultados Históricos y Parámetros) ---
        // (RE-ESCRITO CON GridBagLayout para 2 columnas + separador)
        JPanel panelCentral = new JPanel(new GridBagLayout()); 
        panelCentral.setBackground(nti.fondoPanel);
        panelCentral.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(15, 25, 15, 25) // (Padding reducido)
        ));
        GridBagConstraints gbcC = new GridBagConstraints();
        
        // --- Columna Izquierda (Resultados Históricos + Actuales) ---
        JPanel colResultados = new JPanel();
        colResultados.setLayout(new BoxLayout(colResultados, BoxLayout.Y_AXIS));
        colResultados.setOpaque(false);
        
        // (MODIFICADO) Título en su propio panel WEST para forzar alineación
        JLabel lblTituloRes = new JLabel("Resultados");
        lblTituloRes.setFont(Fuentes.getBlack(22f));
        lblTituloRes.setForeground(nti.letra); // (Blanco)
        JPanel panelTituloRes = new JPanel(new BorderLayout());
        panelTituloRes.setOpaque(false);
        panelTituloRes.add(lblTituloRes, BorderLayout.WEST);
        colResultados.add(panelTituloRes);
        
        colResultados.add(Box.createVerticalStrut(8)); // (Padding reducido)

        // (Subtítulo de datos históricos)
        JLabel lblTituloResHistorico = new JLabel("Resultados desde ... a ...");
        lblDetalle_Historico_Titulo = lblTituloResHistorico; // (Guardamos referencia)
        lblTituloResHistorico.setFont(Fuentes.getBold(18f));
        lblTituloResHistorico.setForeground(nti.bordeDorado);
        // (MODIFICADO) Título en su propio panel WEST
        JPanel panelTituloHist = new JPanel(new BorderLayout());
        panelTituloHist.setOpaque(false);
        panelTituloHist.add(lblTituloResHistorico, BorderLayout.WEST);
        colResultados.add(panelTituloHist);
        
        colResultados.add(Box.createVerticalStrut(10)); // (Padding reducido)
        
        // (Crear labels de valor)
        lblDetalle_MSE = new JLabel("...");
        lblDetalle_RMSE = new JLabel("...");
        lblDetalle_MAE = new JLabel("...");
        lblDetalle_R2 = new JLabel("...");
        lblDetalle_MaxError = new JLabel("...");
        lblDetalle_MinError = new JLabel("...");
        lblDetalle_Percentil90 = new JLabel("...");
        lblDetalle_Precision = new JLabel("...");
        
        // (Añadir usando el helper)
        colResultados.add(crearPanelDato("MSE", lblDetalle_MSE));
        colResultados.add(crearPanelDato("RMSE", lblDetalle_RMSE));
        colResultados.add(crearPanelDato("MAE", lblDetalle_MAE));
        colResultados.add(crearPanelDato("R2", lblDetalle_R2));
        colResultados.add(crearPanelDato("MAXERROR", lblDetalle_MaxError));
        colResultados.add(crearPanelDato("MINERROR", lblDetalle_MinError));
        colResultados.add(crearPanelDato("PERCENTIL 90", lblDetalle_Percentil90));
        colResultados.add(crearPanelDato("PRECISIÓN", lblDetalle_Precision));
        
        colResultados.add(Box.createVerticalStrut(10)); // (Padding reducido)
        
        // (Subtítulo de datos actuales)
        JLabel lblTituloResActual = new JLabel("Resultados desde ... a hoy");
        lblDetalle_Actual_Titulo = lblTituloResActual; // (Actualizamos referencia)
        lblTituloResActual.setFont(Fuentes.getBold(18f));
        lblTituloResActual.setForeground(nti.bordeDorado);
        // (MODIFICADO) Título en su propio panel WEST
        JPanel panelTituloAct = new JPanel(new BorderLayout());
        panelTituloAct.setOpaque(false);
        panelTituloAct.add(lblTituloResActual, BorderLayout.WEST);
        colResultados.add(panelTituloAct);
        
        colResultados.add(Box.createVerticalStrut(10)); // (Padding reducido)
        
        // (Crear labels de valor actual)
        lblDetalle_Actual_MAE = new JLabel("...");
        lblDetalle_Actual_Precision = new JLabel("...");
        lblDetalle_Actual_Tendencia = new JLabel("...");
        
        // (Añadir usando el helper)
        colResultados.add(crearPanelDato("ERROR PROMEDIO", lblDetalle_Actual_MAE));
        colResultados.add(crearPanelDato("PRECISIÓN", lblDetalle_Actual_Precision));
        colResultados.add(crearPanelDato("PRECISIÓN DE TENDENCIA", lblDetalle_Actual_Tendencia));

        colResultados.add(Box.createVerticalGlue()); // (Empuja todo hacia arriba)
        
        // AÑADIR COLUMNA IZQUIERDA AL GridBagLayout
        gbcC.gridx = 0;
        gbcC.gridy = 0;
        gbcC.weightx = 0.5; // (50% de ancho)
        gbcC.weighty = 1.0; // (Permitir que crezca verticalmente)
        gbcC.fill = GridBagConstraints.BOTH;
        gbcC.anchor = GridBagConstraints.NORTHWEST; // (Alinear arriba a la izquierda)
        panelCentral.add(colResultados, gbcC);
        
        // (Separador vertical BLANCO)
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setForeground(nti.letra); // (Color Blanco)
        separator.setBackground(nti.fondoPanel);
        
        gbcC.gridx = 1;
        gbcC.gridy = 0;
        gbcC.weightx = 0; // (Sin peso)
        gbcC.weighty = 1.0;
        gbcC.fill = GridBagConstraints.VERTICAL; // (Solo rellenar vertical)
        gbcC.insets = new Insets(0, 20, 0, 20); // (Espacio a los lados)
        panelCentral.add(separator, gbcC);

        // --- Columna Derecha (Parámetros) ---
        JPanel colParams = new JPanel();
        colParams.setLayout(new BoxLayout(colParams, BoxLayout.Y_AXIS));
        colParams.setOpaque(false);
        
        JLabel lblTituloParams = new JLabel("Parámetros");
        lblTituloParams.setFont(Fuentes.getBlack(22f));
        lblTituloParams.setForeground(nti.letra); // (Blanco)
        // (MODIFICADO) Título en su propio panel WEST
        JPanel panelTituloParams = new JPanel(new BorderLayout());
        panelTituloParams.setOpaque(false);
        panelTituloParams.add(lblTituloParams, BorderLayout.WEST);
        colParams.add(panelTituloParams);

        colParams.add(Box.createVerticalStrut(10)); // (Padding reducido)
        
        // (Crear labels de valor)
        lblDetalle_FechaIni = new JLabel("...");
        lblDetalle_FechaFin = new JLabel("...");
        lblDetalle_Arquitectura = new JLabel("...");
        lblDetalle_Funciones = new JLabel("...");
        lblDetalle_Tasa = new JLabel("...");
        lblDetalle_NMaxError = new JLabel("...");
        lblDetalle_Epocas = new JLabel("...");
        
        // (Añadir usando el helper)
        colParams.add(crearPanelDato("FECHA INICIO", lblDetalle_FechaIni));
        colParams.add(crearPanelDato("FECHA FIN", lblDetalle_FechaFin));
        colParams.add(crearPanelDato("ARQUITECTURA", lblDetalle_Arquitectura));
        colParams.add(crearPanelDato("FUNCIONES", lblDetalle_Funciones));
        colParams.add(crearPanelDato("TASA DE APRENDIZAJE", lblDetalle_Tasa));
        colParams.add(crearPanelDato("N° MAX. DE ERROR", lblDetalle_NMaxError));
        colParams.add(crearPanelDato("N° MAX. DE ÉPOCAS", lblDetalle_Epocas));
        
        colParams.add(Box.createVerticalGlue()); // (Empuja todo hacia arriba)
        
        // AÑADIR COLUMNA DERECHA AL GridBagLayout
        gbcC.gridx = 2;
        gbcC.gridy = 0;
        gbcC.weightx = 0.5; // (50% de ancho)
        gbcC.weighty = 1.0;
        gbcC.fill = GridBagConstraints.BOTH;
        gbcC.anchor = GridBagConstraints.NORTHWEST; // (Alinear arriba a la izquierda)
        gbcC.insets = new Insets(0, 0, 0, 0); // (Reset insets)
        panelCentral.add(colParams, gbcC);
        
        panel.add(panelCentral, BorderLayout.CENTER); // (Panel central listo)

        // --- 3. (NUEVO) PANEL INFERIOR (Botón Volver) ---
        JPanel panelBotonVolver = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); // (Sin gaps)
        panelBotonVolver.setOpaque(false);
        // (MODIFICADO) Padding superior e inferior eliminados
        panelBotonVolver.setBorder(new EmptyBorder(0, 0, 0, 0)); 

        JButton btnVolverAtras = new JButton("Volver Atras");
        btnVolverAtras.setBackground(nti.fondoPanel);
        btnVolverAtras.setForeground(nti.bordeDorado);
        btnVolverAtras.setFont(Fuentes.getBold(16f));
        btnVolverAtras.setFocusPainted(false);
        btnVolverAtras.setPreferredSize(new Dimension(200, 45));
        btnVolverAtras.setBorder(new RoundedBorder(15, nti.bordeDorado, 2));
        btnVolverAtras.addActionListener(e -> {
            cardLayoutModelos.show(panelModelosContenido, "LISTA");
        });
        
        panelBotonVolver.add(btnVolverAtras);
        
        panel.add(panelBotonVolver, BorderLayout.SOUTH); // (Panel inferior listo)
        
        return panel;
    }
    
    private void addPlaceholderFocusListener(JTextField textField, String placeholder) {
        // ... (Tu código para este helper no cambia) ...
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